package com.weinsim.slpaint.ui.components;

import static org.lwjgl.glfw.GLFW.*;

import java.util.function.BooleanSupplier;

import com.weinsim.slpaint.main.apps.MainApp;
import com.weinsim.slpaint.main.tools.Resizable;
import com.weinsim.sutil.math.SVector;

public class SizeKnob extends DragKnob {

    private final int dx, dy;

    private final Resizable resizable;

    private SVector dragStartSize;

    public SizeKnob(Resizable resizable, int dx, int dy, BooleanSupplier visibilitySupplier, MainApp app) {

        int cursorShape;
        if (dx == 1)
            cursorShape = GLFW_RESIZE_NS_CURSOR;
        else if (dy == 1)
            cursorShape = GLFW_RESIZE_EW_CURSOR;
        else
            cursorShape = GLFW_RESIZE_ALL_CURSOR;

        Anchor parentAnchor = Anchor.fromOffsets(dx, dy);

        super(resizable, visibilitySupplier, cursorShape, parentAnchor, app);

        this.dx = dx;
        this.dy = dy;
        this.resizable = resizable;

        dragStartSize = new SVector();
    }

    @Override
    protected void startDrag() {
        super.startDrag();

        dragStartSize.set(resizable.getWidth(), resizable.getHeight());
    }

    @Override
    protected void drag(SVector mouseDelta) {
        // size

        double oldWidth = dragStartSize.x,
                oldHeight = dragStartSize.y;
        double newWidth = oldWidth,
                newHeight = oldHeight;

        newWidth += mouseDelta.x * (dx - 1);
        newHeight += mouseDelta.y * (dy - 1);

        if (resizable.lockRatio()) {
            double ratio = oldWidth / oldHeight;
            if (dx == 1) {
                // change only height
                newWidth = newHeight * ratio;
            } else if (dy == 1) {
                // change only width
                newHeight = newWidth / ratio;
            } else {
                // change both

                // find projection of target size onto original size
                double scale = (newWidth * oldWidth + newHeight * oldHeight)
                        / (oldWidth * oldWidth + oldHeight * oldHeight);

                newWidth = scale * oldWidth;
                newHeight = scale * oldHeight;
            }
        }

        resizable.setWidth((int) Math.round(newWidth));
        resizable.setHeight((int) Math.round(newHeight));

        // position

        double x = dragStartPos.x,
                y = dragStartPos.y;

        if (dx == 0)
            x -= resizable.getWidth() - oldWidth;
        if (dy == 0)
            y -= resizable.getHeight() - oldHeight;

        resizable.setX((int) Math.round(x));
        resizable.setY((int) Math.round(y));
    }
}