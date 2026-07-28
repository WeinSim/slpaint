package com.weinsim.sutil.ui.elements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BooleanSupplier;

import com.weinsim.sutil.math.SVector;
import com.weinsim.sutil.ui.UI;
import com.weinsim.sutil.ui.UIColors;
import com.weinsim.sutil.ui.UISizes;
import com.weinsim.sutil.ui.UIStyle;

public class UIContainer extends UIElement {

    protected static final int LEFT = UI.LEFT,
            TOP = UI.TOP,
            CENTER = UI.CENTER,
            RIGHT = UI.RIGHT,
            BOTTOM = UI.BOTTOM;

    protected static final int VERTICAL = UI.VERTICAL,
            HORIZONTAL = UI.HORIZONTAL,
            NONE = UI.NONE,
            BOTH = UI.BOTH;

    protected final double EPSILON = 1e-6;

    /**
     * Every {@code UIContainer} has one of three size types in both directions
     * (vertical and horizontal):
     * <ul>
     * <li>{@code MINIMAL}: The width / height of the container is the smallest
     * width / height that fits all of its children, the padding between the
     * children and the margin around the border.</li>
     * <li>{@code FIXED}: A fixed width / height is specified, which the container
     * will always have.</li>
     * <li>{@code FILL}: The container expands horizontally / vertically as much as
     * possible, withouth affecting its parent container.</li>
     * </ul>
     */
    protected enum SizeType {
        MINIMAL,
        FIXED,
        FILL;
    }

    private ArrayList<UIElement> children;
    private Iterable<UIElement> visibleChildren, soloChildren;

    protected int orientation;
    protected int hAlignment, vAlignment;

    private final int scrollMode;
    private SVector scrollOffset;
    private SVector areaOvershoot;
    protected boolean clipChildren;

    protected SizeType hSizeType, vSizeType;

    protected SVector minSize;

    private double topMarginScale = 0,
            bottomMarginScale = 0,
            leftMarginScale = 0,
            rightMarginScale = 0;
    private double paddingScale = 1;

    protected boolean automaticSeparators = false;

    public UIContainer(int orientation, int alignment) {
        this(orientation, alignment, alignment, NONE);
    }

    public UIContainer(int orientation, int hAlignment, int vAlignment) {
        this(orientation, hAlignment, vAlignment, NONE);
    }

    public UIContainer(int orientation, int hAlignment, int vAlignment, int scrollMode) {
        super();

        this.scrollMode = scrollMode;

        setOrientation(orientation);
        setHAlignment(hAlignment);
        setVAlignment(vAlignment);

        hSizeType = SizeType.MINIMAL;
        vSizeType = SizeType.MINIMAL;

        if (isHScroll())
            setHFillSize();

        if (isVScroll())
            setVFillSize();

        scrollOffset = new SVector();
        minSize = new SVector();

        clipChildren = isHScroll() || isVScroll();

        children = new ArrayList<>();
        visibleChildren = new ChildIterable() {
            @Override
            boolean iterateOver(UIElement child) {
                return child.isVisible();
            }
        };
        soloChildren = new ChildIterable() {
            @Override
            boolean iterateOver(UIElement child) {
                return child.isVisible() && child.soloInputs();
            }
        };

        addMouseWheelAction(0, scroll -> {
            boolean doScroll = false;
            if (isHScroll()) {
                scrollOffset.x += scroll.x;
                doScroll = true;
            }
            if (isVScroll()) {
                scrollOffset.y += scroll.y;
                doScroll = true;
            }
            return doScroll;
        });
    }

    // Adding / removing children

    public void addSeparator() {
        add(new UISeparator());
    }

    public void addFill() {
        addFill(true);
    }

    public void addFill(boolean margin) {
        add(new UIFill(true));
    }

    public void add(UIElement child) {
        addActual(child, false);
    }

    public void addFirst(UIElement child) {
        addActual(child, true);
    }

    private final void addActual(UIElement child, boolean first) {
        addActual(first ? 0 : children.size(), child);
    }

    private final void addActual(int index, UIElement child) {
        children.add(index, child);
        child.parent = this;
    }

    public void remove(UIElement child) {
        children.remove(child);
    }

    /**
     * Removes and returns all children of the specified type from this container's
     * list of children.
     * 
     * @param <T>   the type of child element to remove
     * @param clazz the type's class
     * @return a list of all removed children
     */
    public <T extends UIElement> List<T> removeAll(Class<T> clazz) {
        ArrayList<T> removed = new ArrayList<>();
        for (int i = children.size() - 1; i >= 0; i--) {
            UIElement child = children.get(i);
            if (clazz.isInstance(child)) {
                children.remove(i);
                removed.add(clazz.cast(child));
            }
        }
        return removed;
    }

    // Recursive methods

    @Override
    public void handleEvents() {
        super.handleEvents();
        for (UIElement child : getChildren()) {
            child.handleEvents();
        }
    }

    @Override
    public void updateVisibility() {
        super.updateVisibility();
        if (!isVisible())
            return;

        createChildren();
        if (automaticSeparators)
            removeAll(UISeparator.class);
        boolean addSeparators = false;
        for (int i = 0; i < children.size(); i++) {
            UIElement child = children.get(i);
            child.updateVisibility();
            if (child.isVisible()
                    && !(child instanceof UIFloatContainer)
                    && !(child instanceof UISeparator)) {
                if (automaticSeparators && addSeparators)
                    // increment i by one because the element we just processed has moved one
                    // position to the right
                    addActual(i++, new UISeparator());
                addSeparators = true;
            }
        }
    }

    /**
     * This method can overridden by subclasses of {@code UIContainer} to add or
     * remove children on every UI update cycle, analogous to children having a
     * visibility supplier.
     */
    protected void createChildren() {
    }

    @Override
    public void updateMousePosition(SVector mouse) {
        super.updateMousePosition(mouse);

        SVector relativeMouse = new SVector(mouse).sub(position);
        for (UIElement child : getChildren()) {
            child.updateMousePosition(relativeMouse);
        }
    }

    @Override
    public boolean updateMouseAbove(boolean valid, boolean insideParent, int currentLayer, final int targetLayer) {
        boolean ret = super.updateMouseAbove(valid, insideParent, currentLayer, targetLayer);
        currentLayer += relativeLayer;

        // This is a bit ugly. But it is neccessary because a scroll container's
        // children should not have mouseAbove set if the mouse is not above the scroll
        // container, regardles of the layers.
        boolean ownMouseAbove = (insideParent || ignoreParentClipArea)
                ? (clipChildren ? calculateMouseAbove(mousePosition) : true)
                : false;

        boolean childMouseAbove = false;
        for (UIElement child : getChildren()) {
            boolean childValid = valid && !childMouseAbove;
            childMouseAbove |= child.updateMouseAbove(childValid, ownMouseAbove, currentLayer, targetLayer);
        }

        return ret || childMouseAbove;
    }

    @Override
    public void update() {
        super.update();
        for (UIElement child : getChildren()) {
            child.update();
        }
    }

    @Override
    public Integer getCursorShape() {
        Integer shape = super.getCursorShape();
        if (shape != null)
            return shape;

        for (UIElement child : getChildren()) {
            shape = child.getCursorShape();
            if (shape != null)
                return shape;
        }

        return null;
    }

    // Layout stuff

    protected void setMinSize() {
        for (UIElement child : getChildren()) {
            if (child instanceof UIContainer container) {
                container.setMinSize();
            } else {
                child.setPreferredSize();
            }
        }

        setSizeAccordingToBoundingBox();

        if (isHScroll() && hSizeType != SizeType.FIXED) {
            size.x = 4 * UISizes.MARGIN.get1f();
        }
        if (isVScroll() && vSizeType != SizeType.FIXED) {
            size.y = 4 * UISizes.MARGIN.get1f();
        }

        minSize.set(size);
    }

    @Override
    public final void setPreferredSize() {
        for (UIElement child : getChildren()) {
            child.setPreferredSize();
        }

        setSizeAccordingToBoundingBox();
    }

    private void setSizeAccordingToBoundingBox() {
        if (hSizeType == SizeType.FIXED && vSizeType == SizeType.FIXED) {
            return;
        }

        SVector boundingBox = getChildrenBoundingBox();

        if (hSizeType != SizeType.FIXED) {
            size.x = boundingBox.x;
        }
        if (vSizeType != SizeType.FIXED) {
            size.y = boundingBox.y;
        }
    }

    public void expandAsNeccessary() {
        // the order of these two doesn't matter
        adjustAlongAxis();
        adjustAcrossAxis();

        for (UIElement child : getChildren()) {
            if (child instanceof UIContainer container) {
                container.expandAsNeccessary();
            }
        }
    }

    private void adjustAlongAxis() {
        SVector boundingBox = getChildrenBoundingBox();
        double remainingSize = orientation == VERTICAL ? size.y - boundingBox.y : size.x - boundingBox.x;

        ArrayList<UIContainer> hvChildren = new ArrayList<>();
        if (remainingSize > 0) {
            // determine all children that can expand
            for (UIElement child : getChildren()) {
                if (child instanceof UIContainer container) {
                    if ((orientation == VERTICAL ? container.vSizeType : container.hSizeType) == SizeType.FILL) {
                        hvChildren.add(container);
                    }
                }
            }
        } else {
            // all children can shrink, but only up to minimumSize
            for (UIElement child : getChildren()) {
                if (child instanceof UIContainer container) {
                    hvChildren.add(container);
                }
            }
        }

        if (!hvChildren.isEmpty()) {
            expandOrShrinkChildren(hvChildren, remainingSize);
        }
    }

    /**
     * 
     * @see https://github.com/nicbarker/clay/blob/main/clay.h#L2190
     */
    private void expandOrShrinkChildren(ArrayList<UIContainer> containers, double remainingSize) {
        int sign = remainingSize < 0 ? -1 : 1;
        remainingSize *= sign; // remaining size will now always be positive.

        while (remainingSize > EPSILON) {
            if (sign == -1) {
                // containers cannot shrink below their minimum size
                for (int i = containers.size() - 1; i >= 0; i--) {
                    UIContainer child = containers.get(i);
                    if (child.getSizeAlongAxis() - child.getMinSizeAlongAxis() < EPSILON) {
                        containers.remove(i);
                    }
                }
            }
            if (containers.isEmpty()) {
                return;
            }

            double smallest = containers.get(0).getSizeAlongAxis() * sign;
            double secondSmallest = Double.POSITIVE_INFINITY;
            double sizeToAdd = remainingSize; // or size to remove. always positive.

            for (UIContainer child : containers) {
                double component = child.getSizeAlongAxis() * sign;
                if (lessThan(component, smallest, EPSILON)) {
                    secondSmallest = smallest;
                    smallest = component;
                }
                if (lessThan(smallest, component, EPSILON)) {
                    secondSmallest = Math.min(secondSmallest, component);
                    sizeToAdd = secondSmallest - smallest;
                }
            }

            sizeToAdd = Math.min(sizeToAdd, remainingSize / containers.size());

            for (UIContainer child : containers) {
                double component = child.getSizeAlongAxis() * sign;
                if (equalTo(component, smallest, EPSILON)) {
                    double adding; // or removing. always positive.
                    if (sign == 1) {
                        adding = sizeToAdd;
                    } else {
                        double maxRemoveAmount = child.getSizeAlongAxis() - child.getMinSizeAlongAxis();
                        adding = Math.min(maxRemoveAmount, sizeToAdd);
                    }

                    child.setSizeAlongAxis((component + adding) * sign);
                    remainingSize -= adding;
                }
            }
        }
    }

    private void adjustAcrossAxis() {
        double availableSpace = getAvailableSpaceAcrossAxis();
        for (UIElement child : getChildren()) {
            if (child instanceof UIContainer container) {
                if (container.getSizeAcrossAxis() < availableSpace) {
                    // expand
                    SizeType sizeTypeAcrossAxis = orientation == VERTICAL
                            ? container.hSizeType
                            : container.vSizeType;
                    if (sizeTypeAcrossAxis == SizeType.FILL) {
                        container.setSizeAcrossAxis(availableSpace);
                    }
                } else {
                    // shrink
                    double newWH = Math.max(availableSpace, container.getMinSizeAcrossAxis());
                    container.setSizeAcrossAxis(newWH);
                }
            }
        }
    }

    protected double getAvailableSpaceAcrossAxis() {
        boolean isScroll = orientation == VERTICAL ? isVScroll() : isHScroll();
        if (isScroll) {
            SVector boundingBox = getChildrenBoundingBox();
            return orientation == VERTICAL
                    ? Math.max(size.x, boundingBox.x) - getHMargins()
                    : Math.max(size.y, boundingBox.y) - getVMargins();
        } else {
            return orientation == VERTICAL
                    ? size.x - getHMargins()
                    : size.y - getVMargins();
        }
    }

    private double getSizeAlongAxis() {
        return parent.orientation == VERTICAL ? size.y : size.x;
    }

    private double getSizeAcrossAxis() {
        return parent.orientation == VERTICAL ? size.x : size.y;
    }

    private double getMinSizeAlongAxis() {
        return parent.orientation == VERTICAL ? minSize.y : minSize.x;
    }

    private double getMinSizeAcrossAxis() {
        return parent.orientation == VERTICAL ? minSize.x : minSize.y;
    }

    private void setSizeAlongAxis(double wh) {
        if (parent.orientation == VERTICAL) {
            size.y = wh;
        } else {
            size.x = wh;
        }
    }

    private void setSizeAcrossAxis(double wh) {
        if (parent.orientation == VERTICAL) {
            size.x = wh;
        } else {
            size.y = wh;
        }
    }

    private boolean lessThan(double a, double b, double epsilon) {
        return a + epsilon < b;
    }

    private boolean equalTo(double a, double b, double epsilon) {
        double difference = a - b;
        return difference > -epsilon && difference < epsilon;
    }

    public void positionChildren() {
        double topMargin = getTopMargin(),
                bottomMargin = getBottomMargin(),
                leftMargin = getLeftMargin(),
                rightMargin = getRightMargin();
        double padding = getPadding();
        SVector boundingBox = getChildrenBoundingBox();

        areaOvershoot = new SVector(
                Math.max(0, boundingBox.x - size.x),
                Math.max(0, boundingBox.y - size.y));

        scrollOffset.x = Math.min(0, Math.max(scrollOffset.x, -areaOvershoot.x));
        scrollOffset.y = Math.min(0, Math.max(scrollOffset.y, -areaOvershoot.y));

        double runningTotal = 0;
        for (UIElement child : getChildren()) {
            if (child instanceof UIFloatContainer floatContainer) {
                floatContainer.setPosition();
            } else {
                SVector childPos = child.getPosition();
                SVector childSize = child.getSize();

                childPos.x = orientation == VERTICAL
                        ? leftMargin + (size.x - (leftMargin + rightMargin) - childSize.x) * hAlignment / 2.0
                        : leftMargin + runningTotal + (size.x - boundingBox.x) * hAlignment / 2.0;
                childPos.y = orientation == VERTICAL
                        ? topMargin + runningTotal + (size.y - boundingBox.y) * vAlignment / 2.0
                        : topMargin + (size.y - (topMargin + bottomMargin) - childSize.y) * vAlignment / 2.0;

                runningTotal += orientation == VERTICAL ? childSize.y : childSize.x;
                runningTotal += padding;
                child.getPosition().add(scrollOffset);
            }

            if (child instanceof UIContainer container)
                container.positionChildren();
        }
    }

    protected SVector getChildrenBoundingBox() {
        double topMargin = getTopMargin(),
                bottomMargin = getBottomMargin(),
                leftMargin = getLeftMargin(),
                rightMargin = getRightMargin();
        double padding = getPadding();
        double sum = 0;
        double max = 0;
        int numNonFloatChildren = 0;
        for (UIElement child : getChildren()) {
            if (child instanceof UIFloatContainer)
                continue;
            SVector childSize = child.getSize();
            if (orientation == VERTICAL) {
                max = Math.max(max, childSize.x);
                sum += childSize.y;
            } else {
                max = Math.max(max, childSize.y);
                sum += childSize.x;
            }
            numNonFloatChildren++;
        }
        sum += Math.max(0, (numNonFloatChildren - 1)) * padding;
        SVector ret = switch (orientation) {
            case VERTICAL -> new SVector(max, sum);
            case HORIZONTAL -> new SVector(sum, max);
            default -> null;
        };
        ret.x += leftMargin + rightMargin;
        ret.y += topMargin + bottomMargin;

        return ret;
    }

    // Getters / setters

    private double getHMargins() {
        return UISizes.MARGIN.get1f() * (leftMarginScale + rightMarginScale);
    }

    private double getVMargins() {
        return UISizes.MARGIN.get1f() * (topMarginScale + bottomMarginScale);
    }

    private double getTopMargin() {
        return UISizes.MARGIN.get1f() * topMarginScale;
    }

    private double getBottomMargin() {
        return UISizes.MARGIN.get1f() * bottomMarginScale;
    }

    private double getLeftMargin() {
        return UISizes.MARGIN.get1f() * leftMarginScale;
    }

    private double getRightMargin() {
        return UISizes.MARGIN.get1f() * rightMarginScale;
    }

    private double getPadding() {
        return UISizes.PADDING.get1f() * paddingScale;
    }

    public UIContainer setMinimalSize() {
        hSizeType = SizeType.MINIMAL;
        vSizeType = SizeType.MINIMAL;
        return this;
    }

    public UIContainer setHMinimalSize() {
        hSizeType = SizeType.MINIMAL;
        return this;
    }

    public UIContainer setVMinimalSize() {
        vSizeType = SizeType.MINIMAL;
        return this;
    }

    public UIContainer setFixedSize(SVector size) {
        this.size.set(size);
        hSizeType = SizeType.FIXED;
        vSizeType = SizeType.FIXED;
        return this;
    }

    public UIContainer setHFixedSize(double width) {
        size.x = width;
        hSizeType = SizeType.FIXED;
        return this;
    }

    public UIContainer setVFixedSize(double height) {
        size.y = height;
        vSizeType = SizeType.FIXED;
        return this;
    }

    public UIContainer setFillSize() {
        hSizeType = SizeType.FILL;
        vSizeType = SizeType.FILL;
        return this;
    }

    public UIContainer setHFillSize() {
        hSizeType = SizeType.FILL;
        return this;
    }

    public UIContainer setVFillSize() {
        vSizeType = SizeType.FILL;
        return this;
    }

    public int getOrientation() {
        return orientation;
    }

    /**
     * 
     * @return an iterator over this container's visible children.
     */
    public Iterable<UIElement> getChildren() {
        return visibleChildren;
    }

    /**
     * If any of this container's visible children have the {@code soloInputs} flag
     * set, this method returns an iterator over all children that have the
     * {@code soloInputs} flag set. Otherwise, it returns an iterator over all
     * visible children.
     */
    public Iterable<UIElement> getSoloChildren() {
        boolean hasSoloChildren = hasSoloChildren();
        return hasSoloChildren ? soloChildren : visibleChildren;
    }

    public boolean hasSoloChildren() {
        boolean hasSoloChildren = false;
        for (UIElement child : getChildren()) {
            if (child.soloInputs()) {
                hasSoloChildren = true;
                break;
            }
        }
        return hasSoloChildren;
    }

    public UIContainer setMarginScale(double marginScale) {
        topMarginScale = marginScale;
        bottomMarginScale = marginScale;
        leftMarginScale = marginScale;
        rightMarginScale = marginScale;
        return this;
    }

    public UIContainer setHMarginScale(double hMarginScale) {
        leftMarginScale = hMarginScale;
        rightMarginScale = hMarginScale;
        return this;
    }

    public UIContainer setVMarginScale(double vMarginScale) {
        topMarginScale = vMarginScale;
        bottomMarginScale = vMarginScale;
        return this;
    }

    public UIContainer setTopMarginScale(double topMarginScale) {
        this.topMarginScale = topMarginScale;
        return this;
    }

    public UIContainer setBottomMarginScale(double bottomMarginScale) {
        this.bottomMarginScale = bottomMarginScale;
        return this;
    }

    public UIContainer setLeftMarginScale(double leftMarginScale) {
        this.leftMarginScale = leftMarginScale;
        return this;
    }

    public UIContainer setRightMarginScale(double rightMarginScale) {
        this.rightMarginScale = rightMarginScale;
        return this;
    }

    public UIContainer setPaddingScale(double paddingScale) {
        this.paddingScale = paddingScale;
        return this;
    }

    public UIContainer withMargin() {
        setMarginScale(1.0);
        return this;
    }

    public UIContainer zeroMargin() {
        setHMarginScale(0);
        setVMarginScale(0);
        return this;
    }

    public UIContainer withPadding() {
        setPaddingScale(1.0);
        return this;
    }

    public UIContainer zeroPadding() {
        setPaddingScale(0);
        return this;
    }

    public UIContainer setOrientation(int orientation) {
        if (orientation < 0 || orientation >= 2) {
            throw new IllegalArgumentException(String.format("Invalid orientation (%d)", orientation));
        }
        this.orientation = orientation;

        return this;
    }

    public UIContainer setAlignment(int alignment) {
        return setAlignment(alignment, alignment);
    }

    public UIContainer setAlignment(int hAlignment, int vAlignment) {
        setHAlignment(hAlignment);
        setVAlignment(vAlignment);
        return this;
    }

    public UIContainer setHAlignment(int hAlignment) {
        if (hAlignment < 0 || hAlignment >= 3) {
            throw new IllegalArgumentException(String.format("Invalid horizontal alignment: %d", hAlignment));
        }
        this.hAlignment = hAlignment;

        return this;
    }

    public UIContainer setVAlignment(int vAlignment) {
        if (vAlignment < 0 || vAlignment >= 3) {
            throw new IllegalArgumentException(String.format("Invalid vertical alignment: %d", vAlignment));
        }
        this.vAlignment = vAlignment;

        return this;
    }

    /**
     * Automaticalls inserts separators between every element that is being added.
     * 
     * @param spaciousLayout {@code true}: sets margin and padding scale to 2.
     *                       {@code false}: sets margin and padding scale to 0.
     * 
     * @return {@code this}
     */
    public UIContainer withSeparators(boolean spaciousLayout) {
        automaticSeparators = true;

        if (spaciousLayout) {
            setMarginScale(2.0);
            setPaddingScale(2.0);
        } else {
            zeroMargin();
            zeroPadding();
        }

        return this;
    }

    private void setRelativeScrollX(double scrollX) {
        scrollOffset.x = -areaOvershoot.x * Math.clamp(scrollX, 0, 1);
    }

    private void setRelativeScrollY(double scrollY) {
        scrollOffset.y = -areaOvershoot.y * Math.clamp(scrollY, 0, 1);
    }

    private double getRelativeScrollX() {
        return areaOvershoot.x <= 0.0 ? 0 : -scrollOffset.x / areaOvershoot.x;
    }

    private double getRelativeScrollY() {
        return areaOvershoot.y <= 0.0 ? 0 : -scrollOffset.y / areaOvershoot.y;
    }

    private double getWidthFraction() {
        return size.x / (size.x + areaOvershoot.x);
    }

    private double getHeightFraction() {
        return size.y / (size.y + areaOvershoot.y);
    }

    public boolean isHScroll() {
        return switch (scrollMode) {
            case HORIZONTAL, BOTH -> true;
            default -> false;
        };
    }

    public boolean isVScroll() {
        return switch (scrollMode) {
            case VERTICAL, BOTH -> true;
            default -> false;
        };
    }

    public boolean clipChildren() {
        return clipChildren;
    }

    // Scrolling

    public UIContainer addScrollbars() {
        UIContainer ret = this;
        if (isHScroll()) {
            ret = wrapHScroll(this);
        }
        if (isVScroll()) {
            ret = wrapVScroll(ret);
        }
        return ret;
    }

    private UIContainer wrapHScroll(UIContainer container) {
        return new UIScrollbarContainerWrapper(HORIZONTAL, container, this);
    }

    private UIContainer wrapVScroll(UIContainer container) {
        return new UIScrollbarContainerWrapper(VERTICAL, container, this);
    }

    private boolean showScrollbar(int orientation) {
        if (areaOvershoot == null)
            return false;

        return orientation == VERTICAL
                ? isVScroll() && areaOvershoot.y > EPSILON
                : isHScroll() && areaOvershoot.x > EPSILON;
    }

    private static class UISeparator extends UIContainer {

        UISeparator() {
            super(VERTICAL, LEFT);
            style.setStrokeColor(UIColors.SEPARATOR);
            zeroPadding();
        }

        @Override
        public void update() {
            super.update();

            if (parent.getOrientation() == VERTICAL) {
                setHFillSize();
                setVMinimalSize();
            } else {
                setHMinimalSize();
                setVFillSize();
            }
        }

    }

    private static class UIFill extends UIContainer {

        private final boolean margin;

        UIFill(boolean margin) {
            super(0, 0);
            this.margin = margin;
        }

        @Override
        public void update() {
            super.update();

            if (parent.getOrientation() == VERTICAL) {
                setHMinimalSize();
                setVFillSize();
                setHMarginScale(0.0);
                setVMarginScale(margin ? 1.0 : 0.0);
            } else {
                setHFillSize();
                setVMinimalSize();
                setHMarginScale(margin ? 1.0 : 0.0);
                setVMarginScale(0.0);
            }
        }

    }

    private static class UIScrollbarContainerWrapper extends UIContainer {

        final UIContainer scrollArea;
        final UIScrollbarContainer scrollbarContainer;

        UIScrollbarContainerWrapper(int orientation, UIContainer container, UIContainer scrollArea) {
            super(1 - orientation, 0);
            this.scrollArea = scrollArea;

            noBackground();
            zeroPadding();

            add(container);
            scrollbarContainer = new UIScrollbarContainer(scrollArea, orientation);
            add(scrollbarContainer);
        }

        @Override
        public void update() {
            super.update();

            hSizeType = scrollArea.hSizeType;
            if (hSizeType == SizeType.FIXED) {
                hSizeType = SizeType.MINIMAL;
            }
            vSizeType = scrollArea.vSizeType;
            if (vSizeType == SizeType.FIXED) {
                vSizeType = SizeType.MINIMAL;
            }
            visibilitySupplier = scrollArea.visibilitySupplier;
        }

        @Override
        public UIElement setVisibilitySupplier(BooleanSupplier visibilitySupplier) {
            scrollArea.setVisibilitySupplier(visibilitySupplier);
            return this;
        }

    }

    private static class UIScrollbarContainer extends UIDragContainer {

        private UIContainer scrollArea;
        private UIScrollbar scrollbar;

        private SVector dragStartMouse;
        private SVector dragStartD;

        UIScrollbarContainer(UIContainer scrollArea, int orientation) {
            this.orientation = orientation;
            this.scrollArea = scrollArea;

            scrollbar = new UIScrollbar(scrollArea, this, orientation);
            add(scrollbar);

            style.setStrokeColor(scrollArea::strokeColor);
            style.setStrokeWeight(scrollArea::strokeWeight);
            withBackground();

            setVisibilitySupplier(() -> scrollArea.showScrollbar(orientation));

            double min = UISizes.SCROLLBAR.get1f();
            if (orientation == VERTICAL) {
                setHFixedSize(min);
                setVFillSize();
            } else {
                setHFillSize();
                setVFixedSize(min);
            }

            dragStartMouse = new SVector();
            dragStartD = new SVector();
        }

        @Override
        protected void startDragging() {
            super.startDragging();

            if (scrollbar.mouseAbove) {
                dragStartD.set(scrollbar.position);
            } else {
                dragStartD.set(scrollbar.size).scale(-0.5);
                dragStartD.add(mousePosition).sub(position);
            }
            dragStartMouse.set(mousePosition);
        }

        @Override
        protected void drag() {
            UI.setDragging();

            SVector newDragPos = new SVector(mousePosition).sub(dragStartMouse).add(dragStartD);

            SVector dragAreaSize = new SVector(size).sub(scrollbar.size);

            double relativeX = newDragPos.x / dragAreaSize.x;
            if (!Double.isFinite(relativeX))
                relativeX = 0;
            setRelativeX(relativeX);

            double relativeY = newDragPos.y / dragAreaSize.y;
            if (!Double.isFinite(relativeY))
                relativeY = 0;
            setRelativeY(relativeY);
        }

        @Override
        public void expandAsNeccessary() {
            super.expandAsNeccessary();

            scrollbar.expandAsNeccessary();
        }

        public double getRelativeX() {
            return orientation == VERTICAL ? 0 : scrollArea.getRelativeScrollX();
        }

        public double getRelativeY() {
            return orientation == VERTICAL ? scrollArea.getRelativeScrollY() : 0;
        }

        public void setRelativeX(double x) {
            if (orientation == HORIZONTAL) {
                scrollArea.setRelativeScrollX(x);
            }
        }

        public void setRelativeY(double y) {
            if (orientation == VERTICAL) {
                scrollArea.setRelativeScrollY(y);
            }
        }

    }

    private static class UIScrollbar extends UIFloatContainer {

        private UIContainer scrollArea;
        private UIScrollbarContainer scrollbarContainer;

        UIScrollbar(UIContainer scrollArea, UIScrollbarContainer scrollbarContainer, int orientation) {
            super(orientation, 0);
            this.scrollArea = scrollArea;
            this.scrollbarContainer = scrollbarContainer;

            withMargin();

            setStyle(new UIStyle(
                    () -> mouseAbove || scrollbarContainer.isDragging()
                            ? UIColors.OUTLINE.get()
                            : UIColors.SCROLLBAR_HIGHLIGHT.get(),
                    null, null));

            addAnchor(Anchor.TOP_LEFT, this::getPos);
        }

        private SVector getPos() {
            SVector siz = new SVector(parent.getSize()).sub(size);
            SVector pos = new SVector(scrollbarContainer.getRelativeX(), scrollbarContainer.getRelativeY()).mult(siz);
            return pos;
        }

        @Override
        public void setMinSize() {
            size.set(UISizes.SCROLLBAR.get2f());
        }

        public void expandAsNeccessary() {
            // this cannot be put in setPreferredSize() because we need the parent's size
            // (which is usually too small during setPreferredSize)
            if (orientation == VERTICAL) {
                size.y = Math.max(size.y, scrollArea.getHeightFraction() * parent.size.y);
            } else {
                size.x = Math.max(size.x, scrollArea.getWidthFraction() * parent.size.x);
            }
        }

    }

    // Child list

    private abstract class ChildIterable implements Iterable<UIElement> {

        ChildIterable() {
        }

        abstract boolean iterateOver(UIElement child);

        @Override
        public Iterator<UIElement> iterator() {
            return new Iterator<UIElement>() {

                private final Iterator<UIElement> inner = children.iterator();
                private UIElement next = null;

                @Override
                public boolean hasNext() {
                    while (inner.hasNext()) {
                        UIElement innerNext = inner.next();
                        if (iterateOver(innerNext)) {
                            next = innerNext;
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public UIElement next() {
                    if (next != null || hasNext()) {
                        UIElement result = next;
                        next = null;
                        return result;
                    }
                    throw new NoSuchElementException();
                }

            };
        }

    }

}
