package com.weinsim.slpaint.ui.components.toolContainers;

import static org.lwjgl.glfw.GLFW.*;

import java.awt.image.BufferedImage;
import java.util.function.BooleanSupplier;

import com.weinsim.slpaint.main.apps.MainApp;
import com.weinsim.slpaint.main.image.Image;
import com.weinsim.slpaint.main.tools.Draggable;
import com.weinsim.slpaint.main.tools.ImageTool;
import com.weinsim.slpaint.main.tools.LineTool;
import com.weinsim.slpaint.ui.components.DragKnob;
import com.weinsim.sutil.math.SVector;
import com.weinsim.sutil.ui.UI;
import com.weinsim.sutil.ui.elements.UIImage;

public final class LineToolContainer extends ToolContainer<LineTool> {

    private Image previewImage;

    public LineToolContainer(MainApp app) {
        super(ImageTool.LINE, app);

        addAnchor(Anchor.TOP_LEFT, () -> app.getCanvas().getImageTranslation());

        zeroMargin();
        relativeLayer = 2;

        add(new ImageDisplay());

        addMousePressAction(GLFW_MOUSE_BUTTON_RIGHT, false, () -> {
            if (tool.getState() == LineTool.INITIAL_DRAG)
                tool.cancel();
        });
        addMouseReleaseAction(GLFW_MOUSE_BUTTON_LEFT, false, () -> {
            switch (tool.getState()) {
                case LineTool.INITIAL_DRAG -> tool.setState(LineTool.IDLE);
            }
        });

        final int visibleStates = LineTool.IDLE;
        BooleanSupplier dragKnobVis = () -> (tool.getState() & visibleStates) != 0;
        int cursorShape = GLFW_RESIZE_ALL_CURSOR;
        Draggable[] draggables = { ImageTool.LINE.getDrag1(), ImageTool.LINE.getDrag2() };
        for (int i = 0; i < 2; i++) {
            final Draggable d = draggables[i];
            add(new DragKnob(d, dragKnobVis, cursorShape, () -> getDragKnobPos(d), app));
        }

        Image image = app.getImage();
        int width = image.getWidth(),
                height = image.getHeight();
        previewImage = new Image(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
    }

    @Override
    public void handleEvents() {
        super.handleEvents();

        Image image = app.getImage();
        int width = image.getWidth(),
                height = image.getHeight();
        // int[] pixels = new int[width * height];
        previewImage.setSizeAndClear(width, height, 0);

        switch (tool.getState()) {
            case LineTool.INITIAL_DRAG -> {
                int[] imageMousePos = app.getMouseImagePosition();
                int mouseX = imageMousePos[0],
                        mouseY = imageMousePos[1];
                // snap line direction to 45° angles when holding shift
                if ((UI.getModifiers() & GLFW_MOD_SHIFT) != 0) {
                    int startX = tool.getDrag1().getX(),
                            startY = tool.getDrag1().getY();
                    int dx = mouseX - startX,
                            dy = mouseY - startY;
                    double ratio = Math.abs(((double) dx) / dy);
                    if (ratio >= 0.5 && ratio < 2) {
                        // 45 degree angle
                        int mag = (int) Math.round(Math.sqrt(dx * dx + dy * dy) / Math.sqrt(2));
                        dx = Integer.signum(dx) * mag;
                        dy = Integer.signum(dy) * mag;
                    } else {
                        // 90 degree angle
                        if (Math.abs(dx) > Math.abs(dy))
                            dy = 0;
                        else
                            dx = 0;
                    }
                    mouseX = startX + dx;
                    mouseY = startY + dy;
                }
                tool.getDrag2().setX(mouseX);
                tool.getDrag2().setY(mouseY);

                drawPreviewLine();
            }
            case LineTool.IDLE -> {
                drawPreviewLine();
            }
        }

        previewImage.sync();
    }

    private void drawPreviewLine() {
        Draggable d1 = tool.getDrag1(),
                d2 = tool.getDrag2();
        previewImage.drawLine(d1.getX(), d1.getY(),
                d2.getX(), d2.getY(),
                tool.getSize(),
                app.getPrimaryColor(),
                true);
    }

    @Override
    protected int getVisibleStates() {
        return LineTool.INITIAL_DRAG | LineTool.IDLE;
    }

    @Override
    protected boolean calculateMouseAbove(SVector mouse) {
        return false;
    }

    private SVector getDragKnobPos(Draggable draggable) {
        SVector p = new SVector(draggable.getX(), draggable.getY());
        p.scale(app.getImageZoom());
        return p;
    }

    // copied from ImageCanvas
    private class ImageDisplay extends UIImage {

        ImageDisplay() {
            super(() -> previewImage.getTextureID(), new SVector());
        }

        @Override
        public void setPreferredSize() {
            Image image = app.getImage();

            size.set(image.getWidth(), image.getHeight());
            size.scale(app.getImageZoom());
        }

        @Override
        protected boolean calculateMouseAbove(SVector mouse) {
            return false;
        }
    }

}
