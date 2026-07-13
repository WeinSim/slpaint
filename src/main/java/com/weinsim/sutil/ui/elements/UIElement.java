package com.weinsim.sutil.ui.elements;

import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.lwjglx.util.vector.Vector4f;

import com.weinsim.sutil.SUtil;
import com.weinsim.sutil.math.SVector;
import com.weinsim.sutil.ui.UI;
import com.weinsim.sutil.ui.UICharInputAction;
import com.weinsim.sutil.ui.UIColors;
import com.weinsim.sutil.ui.UIKeyPressAction;
import com.weinsim.sutil.ui.UIMouseButtonAction;
import com.weinsim.sutil.ui.UIMouseWheelAction;
import com.weinsim.sutil.ui.UIShape;
import com.weinsim.sutil.ui.UISizes;
import com.weinsim.sutil.ui.UIStyle;

public abstract class UIElement {

    protected SVector position;
    protected SVector size;
    protected UIContainer parent;
    protected int relativeLayer = 0;

    protected SVector mousePosition;
    protected boolean mouseAbove = false;
    protected ArrayList<UIMouseButtonAction> mousePressActions, mouseReleaseActions;
    protected ArrayList<UIMouseWheelAction> mouseWheelActions;

    protected ArrayList<UIKeyPressAction> keyPressActions;
    protected ArrayList<UICharInputAction> charInputActions;

    /**
     * Setting this to {@code true} blocks user inputs from reaching this UI
     * element's non-solo siblings (and with that their children as well).
     */
    protected boolean soloInputs = false;
    protected boolean ignoreParentClipArea = false;
    protected boolean selectOnClick = false;
    protected boolean selectable = false;

    protected BooleanSupplier visibilitySupplier = this::isVisible;
    private boolean visible = true;

    protected boolean outlineNormal = false;
    protected boolean outlineHighlight = false;
    protected boolean backgroundNormal = false;
    protected boolean backgroundHighlight = false;
    protected UIStyle style;
    protected boolean handCursorAbove = false;
    protected Supplier<Integer> cursorShapeSupplier = () -> handCursorAbove && mouseAbove
            ? GLFW_POINTING_HAND_CURSOR
            : null;

    public UIElement() {
        position = new SVector();
        size = new SVector();
        mousePosition = new SVector();

        mousePressActions = new ArrayList<>();
        mouseReleaseActions = new ArrayList<>();
        mouseWheelActions = new ArrayList<>();
        keyPressActions = new ArrayList<>();
        charInputActions = new ArrayList<>();

        setDefaultStyle();

        addLeftClickAction(() -> {
            if (selectOnClick)
                UI.select(this);
        });

        // why do we not need to specify that this element needs to be selected??
        Runnable runLeftMouseActions = () -> {
            // We skip the click action that selects this element.
            // Doing it this way is kind of fragile because it assumes that this click
            // action is always at index zero.
            for (int i = 1; i < mousePressActions.size(); i++) {
                UIMouseButtonAction action = mousePressActions.get(i);
                if (action.button() == GLFW_MOUSE_BUTTON_LEFT)
                    action.action().run();
            }
        };
        addKeyPressAction(GLFW_KEY_ENTER, 0, runLeftMouseActions);
        addKeyPressAction(GLFW_KEY_SPACE, 0, runLeftMouseActions);
    }

    public void updateVisibility() {
        visible = visibilitySupplier.getAsBoolean();
    }

    public void updateMousePosition(SVector mouse) {
        mousePosition.set(mouse);
    }

    public boolean updateMouseAbove(boolean valid, boolean insideParent, int currentLayer, final int targetLayer) {
        currentLayer += relativeLayer;
        if (currentLayer != targetLayer)
            return false;

        if (!valid || (!insideParent && !ignoreParentClipArea))
            mouseAbove = false;
        else
            mouseAbove = calculateMouseAbove(mousePosition);

        return mouseAbove;
    }

    protected boolean calculateMouseAbove(SVector mouse) {
        return SUtil.pointInsideRect(mouse, position, size);
    }

    public void update() {
    }

    public final void mousePressed(int mouseButton, int mods) {
        for (UIMouseButtonAction action : mousePressActions)
            action.mouseAction(mouseButton, mods, mouseAbove);
    }

    public final void mouseReleased(int mouseButton, int mods) {
        for (UIMouseButtonAction action : mouseReleaseActions)
            action.mouseAction(mouseButton, mods, mouseAbove);
    }

    /**
     * @param scroll
     * @param mods   Only contains the {@code GLFW_MOD_CONTROL} and
     *               {@code GLFW_MOD_SHIFT} modifiers
     * @return Whether the mouse scroll action has been "used up" by this
     *         {@code UIElement}.
     */
    public final boolean mouseWheel(SVector scroll, int mods) {
        for (UIMouseWheelAction action : mouseWheelActions) {
            if (action.mouseWheel(scroll, mods, mouseAbove))
                return true;
        }
        return false;
    }

    public final void keyPressed(int key, int mods) {
        for (UIKeyPressAction action : keyPressActions)
            action.keyPressed(key, mods, isSelected());
    }

    public final void charInput(char c) {
        for (UICharInputAction action : charInputActions)
            action.charInput(c, isSelected());
    }

    public abstract void setPreferredSize();

    /**
     * The {@code select} and {@code unselect} are always called when an element
     * gets selected / unselected. They can <i>not</i> be used to actually select /
     * unselect an element. Use {@code UI.select(element)} to select an element
     * instead.
     * 
     * @see UI#select(UIElement)
     */
    public void select() {
    }

    /**
     * The {@code select} and {@code unselect} are always called when an element
     * gets selected / unselected. They can <i>not</i> be used to actually select /
     * unselect an element. Use {@code UI.select(null)} to unselect the currently
     * selected element.
     * 
     * @see UI#select(UIElement)
     */
    public void unselect() {
    }

    public boolean isSelected() {
        return UI.getSelectedElement() == this;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public SVector getPosition() {
        return position;
    }

    public SVector getSize() {
        return size;
    }

    public int getRelativeLayer() {
        return relativeLayer;
    }

    public UIElement setRelativeLayer(int relativeLayer) {
        this.relativeLayer = relativeLayer;
        return this;
    }

    public void setIgnoreParentClipArea(boolean ignoreParentClipArea) {
        this.ignoreParentClipArea = ignoreParentClipArea;
    }

    public boolean ignoreParentClipArea() {
        return ignoreParentClipArea;
    }

    public boolean soloInputs() {
        return soloInputs;
    }

    public void setSoloInputs(boolean soloInputs) {
        this.soloInputs = soloInputs;
    }

    public final boolean isVisible() {
        return visible;
    }

    public UIElement setVisibilitySupplier(BooleanSupplier visibilitySupplier) {
        this.visibilitySupplier = visibilitySupplier;
        return this;
    }

    public boolean mouseAbove() {
        return mouseAbove;
    }

    public void addLeftClickAction(Runnable leftClickAction) {
        addMousePressAction(GLFW_MOUSE_BUTTON_LEFT, leftClickAction);
    }

    public void addRightClickAction(Runnable rightClickAction) {
        addMousePressAction(GLFW_MOUSE_BUTTON_RIGHT, rightClickAction);
    }

    public void addMousePressAction(int button, Runnable action) {
        mousePressActions.add(new UIMouseButtonAction(button, action));
    }

    public void addMousePressAction(int button, boolean mouseAbove, Runnable action) {
        mousePressActions.add(new UIMouseButtonAction(button, mouseAbove, action));
    }

    public void addMouseReleaseAction(int button, Runnable action) {
        mouseReleaseActions.add(new UIMouseButtonAction(button, action));
    }

    public void addMouseReleaseAction(int button, boolean mouseAbove, Runnable action) {
        mouseReleaseActions.add(new UIMouseButtonAction(button, mouseAbove, action));
    }

    public void addMouseWheelAction(int mods, Predicate<SVector> action) {
        mouseWheelActions.add(new UIMouseWheelAction(mods, action));
    }

    public void addMouseWheelAction(int mods, boolean mouseAbove, Predicate<SVector> action) {
        mouseWheelActions.add(new UIMouseWheelAction(mods, mouseAbove, action));
    }

    public void addMouseWheelAction(int mods, boolean mouseAbove, BooleanSupplier possible, Predicate<SVector> action) {
        mouseWheelActions.add(new UIMouseWheelAction(mods, mouseAbove, possible, action));
    }

    public void addKeyPressAction(int key, int mods, Runnable action) {
        keyPressActions.add(new UIKeyPressAction(key, mods, action));
    }

    public void addKeyPressAction(int key, int mods, boolean selected, Runnable action) {
        keyPressActions.add(new UIKeyPressAction(key, mods, selected, action));
    }

    public void addCharInputAction(Consumer<Character> action) {
        charInputActions.add(new UICharInputAction(action));
    }

    public void addCharInputAction(boolean selected, Consumer<Character> action) {
        charInputActions.add(new UICharInputAction(selected, action));
    }

    public SVector getAbsolutePosition() {
        return parent.getAbsolutePosition().add(position);
    }

    public UIElement setHandCursor() {
        handCursorAbove = true;
        return this;
    }

    public void setCursorShape(Integer cursorShape) {
        setCursorShape(() -> cursorShape);
    }

    public void setCursorShape(Supplier<Integer> cursorShapeSupplier) {
        this.cursorShapeSupplier = cursorShapeSupplier;
    }

    public Integer getCursorShape() {
        return cursorShapeSupplier.get();
    }

    public Vector4f backgroundColor() {
        return style.backgroundColor();
    }

    public boolean doBackgroundCheckerboard() {
        return style.doBackgroundCheckerboard();
    }

    public Vector4f backgroundCheckerboardColor1() {
        return style.backgroundCheckerboardColor1();
    }

    public Vector4f backgroundCheckerboardColor2() {
        return style.backgroundCheckerboardColor2();
    }

    public double backgroundCheckerboardSize() {
        return style.backgroundCheckerboardSize();
    }

    public final Vector4f strokeColor() {
        Vector4f ol = style.strokeColor();
        if (ol == null && isSelected()) {
            ol = UIColors.OUTLINE.get();
        }
        return ol;
    }

    public final double strokeWeight() {
        double sw = style.strokeWeight();
        if (isSelected()) {
            if (style.strokeColor() == null) {
                sw = UISizes.STROKE_WEIGHT.get();
            } else {
                sw *= 2;
            }
        }
        return sw;
    }

    public boolean doStrokeCheckerboard() {
        return style.doStrokeCheckerboard();
    }

    public Vector4f strokeCheckerboardColor1() {
        return style.strokeCheckerboardColor1();
    }

    public Vector4f strokeCheckerboardColor2() {
        return style.strokeCheckerboardColor2();
    }

    public double strokeCheckerboardSize() {
        return style.strokeCheckerboardSize();
    }

    public UIShape shape() {
        return style.shape();
    }

    public void setOutlineNormal(boolean outlineNormal) {
        this.outlineNormal = outlineNormal;
    }

    public void setOutlineHighlight(boolean outlineHighlight) {
        this.outlineHighlight = outlineHighlight;
    }

    public void setBackgroundNormal(boolean backgroundNormal) {
        this.backgroundNormal = backgroundNormal;
    }

    public void setBackgroundHighlight(boolean backgroundHighlight) {
        this.backgroundHighlight = backgroundHighlight;
    }

    public UIElement noOutline() {
        setOutlineNormal(false);
        setOutlineHighlight(false);
        return this;
    }

    public UIElement noBackground() {
        setBackgroundNormal(false);
        setBackgroundHighlight(false);
        return this;
    }

    public UIElement withOutline() {
        setOutlineNormal(true);
        setOutlineHighlight(false);
        return this;
    }

    public UIElement withBackground() {
        setBackgroundNormal(true);
        setBackgroundHighlight(false);
        return this;
    }

    public void setStyle(UIStyle style) {
        this.style = style;
    }

    public void setDefaultStyle() {
        Supplier<Vector4f> backgroundColorSupplier = () -> {
            Vector4f bgColor = null;
            if (backgroundNormal)
                bgColor = UIColors.BACKGROUND.get();
            if (backgroundHighlight && mouseAbove)
                bgColor = UIColors.BACKGROUND_HIGHLIGHT.get();
            return bgColor;
        };
        Supplier<Vector4f> outlineColorSupplier = () -> {
            Vector4f outlineColor = null;
            if (outlineNormal || (outlineHighlight && mouseAbove))
                outlineColor = UIColors.OUTLINE.get();
            return outlineColor;
        };
        DoubleSupplier strokeWeightSupplier = UISizes.STROKE_WEIGHT;

        style = new UIStyle(backgroundColorSupplier, outlineColorSupplier, strokeWeightSupplier);
    }

}
