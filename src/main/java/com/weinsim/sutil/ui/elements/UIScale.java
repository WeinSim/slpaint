package com.weinsim.sutil.ui.elements;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

import com.weinsim.sutil.math.SVector;
import com.weinsim.sutil.ui.UIColors;
import com.weinsim.sutil.ui.UISizes;
import com.weinsim.sutil.ui.UIStyle;

public class UIScale extends UIDragContainer {

    private DoubleSupplier getter;
    private DoubleConsumer setter;

    private boolean narrow;

    public UIScale(int orientation, DoubleSupplier getter, DoubleConsumer setter) {
        this(orientation, getter, setter, true);
    }

    public UIScale(int orientation, DoubleSupplier getter, DoubleConsumer setter, boolean narrow) {
        this.orientation = orientation;

        this.getter = getter;
        this.setter = setter;
        this.narrow = narrow;

        setAlignment(CENTER);
        zeroPadding();

        if (orientation == VERTICAL) {
            setHMinimalSize();
            setVFillSize();
        } else {
            setHFillSize();
            setVMinimalSize();
        }

        add(new Filler());
        add(getVisuals(orientation));
        add(new Slider(orientation));

        handCursorAbove = true;
    }

    protected Visuals getVisuals(int orientation) {
        return new Visuals(orientation);
    }

    @Override
    public double getRelativeX() {
        return orientation == VERTICAL ? 0 : getter.getAsDouble();
    }

    @Override
    public double getRelativeY() {
        return orientation == VERTICAL ? getter.getAsDouble() : 0;
    }

    @Override
    public void setRelativeX(double x) {
        if (orientation == HORIZONTAL) {
            setter.accept(Math.clamp(x, 0, 1));
        }
    }

    @Override
    public void setRelativeY(double y) {
        if (orientation == VERTICAL) {
            setter.accept(Math.clamp(y, 0, 1));
        }
    }

    private double getVisualWidth() {
        return (narrow ? UISizes.SCALE_NARROW : UISizes.SCALE_WIDE).get1f();
    }

    /**
     * The bar in the the middle
     */
    protected class Visuals extends UIContainer {

        public Visuals(int orientation) {
            super(orientation, 0);
            style.setBackgroundColor(UIColors.OUTLINE);
            double s = getVisualWidth();
            if (orientation == VERTICAL) {
                setHFixedSize(s);
                setVFillSize();
            } else {
                setHFillSize();
                setVFixedSize(s);
            }
        }
    }

    /**
     * The white visual indicators
     */
    protected class Slider extends UIFloatContainer {

        public Slider(int orientation) {
            super(orientation, 0);
            setStyle(new UIStyle(UIColors.HIGHLIGHT, null, null));
            addAnchor(
                    orientation == VERTICAL ? Anchor.CENTER_LEFT : Anchor.TOP_CENTER,
                    () -> new SVector(getRelativeX(), getRelativeY()).mult(parent.getSize()));
        }

        @Override
        public void update() {
            super.update();
            double len = 2 * UISizes.SCALE_SLIDER_LENGTH.get1f() + getVisualWidth();
            double width = UISizes.SCALE_SLIDER_WIDTH.get1f();
            if (orientation == VERTICAL)
                setFixedSize(new SVector(len, width));
            else
                setFixedSize(new SVector(width, len));
        }
    }

    /**
     * Responsible for the gaps around the Visuals
     */
    private class Filler extends UIElement {

        Filler() {
        }

        @Override
        public void setPreferredSize() {
            double w = 2 * UISizes.SCALE_SLIDER_LENGTH.get1f() + getVisualWidth();

            if (orientation == VERTICAL) {
                size.set(w, 0);
            } else {
                size.set(0, w);
            }
        }
    }

}
