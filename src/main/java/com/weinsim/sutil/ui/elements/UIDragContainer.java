package com.weinsim.sutil.ui.elements;

import com.weinsim.sutil.math.SVector;
import com.weinsim.sutil.ui.UI;

public abstract class UIDragContainer extends UIContainer {

    protected boolean dragging = false;

    public UIDragContainer() {
        super(0, 0);

        addLeftClickAction(this::startDragging);
    }

    @Override
    public void update() {
        super.update();

        if (!UI.isLeftMousePressed())
            dragging = false;

        if (dragging) {
            drag();
        }
    }

    protected void startDragging() {
        dragging = true;
    }

    protected void drag() {
        UI.setDragging();

        SVector relativePos = new SVector(mousePosition).sub(position);
        relativePos.x /= size.x;
        relativePos.y /= size.y;

        setRelativeX(relativePos.x);
        setRelativeY(relativePos.y);
    }

    public boolean isDragging() {
        return dragging;
    }

    /**
     * @return The underlying value represented by the x-coordinate of this
     *         {@code UIDragContainer}, in the range from 0 (minimum value) to 1
     *         (maximum value).
     */
    public abstract double getRelativeX();

    /**
     * @return The underlying value represented by the y-coordinate of this
     *         {@code UIDragContainer}, in the range from 0 (minimum value) to 1
     *         (maximum value).
     */
    public abstract double getRelativeY();

    /**
     * Sets the underlying value represented by the x-coordinate of this
     * {@code UIDragContainer}.
     *
     * @param x The new value, which is <b>not</b> guaranteed to be in the range
     *          from 0 to 1.
     */
    public abstract void setRelativeX(double x);

    /**
     * Sets the underlying value represented by the y-coordinate of this
     * {@code UIDragContainer}.
     *
     * @param y The new value, which is <b>not</b> guaranteed to be in the range
     *          from 0 to 1.
     */
    public abstract void setRelativeY(double y);
}
