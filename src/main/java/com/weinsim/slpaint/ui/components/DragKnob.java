package com.weinsim.slpaint.ui.components;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import com.weinsim.slpaint.main.apps.MainApp;
import com.weinsim.slpaint.main.tools.Draggable;
import com.weinsim.sutil.math.SVector;
import com.weinsim.sutil.ui.UI;
import com.weinsim.sutil.ui.UIColors;
import com.weinsim.sutil.ui.UISizes;
import com.weinsim.sutil.ui.elements.UIFloatContainer;

public class DragKnob extends UIFloatContainer {

    protected MainApp app;

    protected final Draggable draggable;

    /**
     * Whether {@code this} DragKnob is currently being dragged.
     */
    protected boolean dragging;
    protected SVector dragStartPos;
    protected SVector dragStartMouse;

    public DragKnob(Draggable draggable, BooleanSupplier visibilitySupplier, int cursorShape, Anchor parentAnchor,
            MainApp app) {

        this(draggable, visibilitySupplier, cursorShape, app);

        addAnchor(Anchor.CENTER_CENTER, parentAnchor);
    }

    public DragKnob(Draggable draggable, BooleanSupplier visibilitySupplier, int cursorShape,
            Supplier<SVector> positionSupplier, MainApp app) {

        this(draggable, visibilitySupplier, cursorShape, app);

        addAnchor(Anchor.CENTER_CENTER, positionSupplier);
    }

    private DragKnob(Draggable draggable, BooleanSupplier visibilitySupplier, int cursorShape, MainApp app) {
        super(0, 0);
        this.app = app;
        this.draggable = draggable;

        style.setBackgroundColor(UIColors.SELECTION_BORDER_2);
        style.setStrokeColor(UIColors.SELECTION_BORDER_1);
        style.setStrokeWeight(() -> UISizes.STROKE_WEIGHT.get() * 2.0);

        setFixedSize(UISizes.SIZE_KNOB.getWidthHeight());

        setVisibilitySupplier(visibilitySupplier);
        addLeftClickAction(this::startDrag);
        setCursorShape(() -> mouseAbove || dragging ? cursorShape : null);

        relativeLayer = 1;

        dragging = false;
        dragStartPos = new SVector();
        dragStartMouse = new SVector();
    }

    protected void startDrag() {
        dragging = true;
        dragStartPos.set(draggable.getX(), draggable.getY());
        dragStartMouse.set(app.getMouseImagePosVec());

        draggable.startDragging();
    }

    @Override
    public void update() {
        super.update();

        if (dragging) {
            SVector mouseDelta = app.getMouseImagePosVec().sub(dragStartMouse);
            drag(mouseDelta);
            UI.setDragging();

            if (!UI.isLeftMousePressed()) {
                dragging = false;
                draggable.finishDragging();
            }
        }
    }

    @Override
    public void setPosition() {
        super.setPosition();
    }

    protected void drag(SVector mouseDelta) {
        SVector pos = new SVector(dragStartPos).add(mouseDelta);

        draggable.setX((int) Math.round(pos.x));
        draggable.setY((int) Math.round(pos.y));
    }

}
