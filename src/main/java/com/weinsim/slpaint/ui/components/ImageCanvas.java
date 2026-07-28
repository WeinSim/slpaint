package com.weinsim.slpaint.ui.components;

import static org.lwjgl.glfw.GLFW.*;

import com.weinsim.slpaint.main.apps.MainApp;
import com.weinsim.slpaint.main.image.Image;
import com.weinsim.slpaint.main.tools.ImageTool;
import com.weinsim.slpaint.main.tools.LineTool;
import com.weinsim.slpaint.main.tools.PencilTool;
import com.weinsim.slpaint.main.tools.Resizable;
import com.weinsim.slpaint.main.tools.SelectionTool;
import com.weinsim.slpaint.main.tools.TextTool;
import com.weinsim.slpaint.ui.components.toolContainers.LineToolContainer;
import com.weinsim.slpaint.ui.components.toolContainers.PencilToolContainer;
import com.weinsim.slpaint.ui.components.toolContainers.SelectionToolContainer;
import com.weinsim.slpaint.ui.components.toolContainers.TextToolContainer;
import com.weinsim.slpaint.ui.components.toolContainers.ToolContainer;
import com.weinsim.sutil.math.SVector;
import com.weinsim.sutil.ui.UI;
import com.weinsim.sutil.ui.UIColors;
import com.weinsim.sutil.ui.UISizes;
import com.weinsim.sutil.ui.elements.UIContainer;
import com.weinsim.sutil.ui.elements.UIElement;
import com.weinsim.sutil.ui.elements.UIFloatContainer;
import com.weinsim.sutil.ui.elements.UIImage;

public class ImageCanvas extends UIContainer {

    private static final int MIN_ZOOM_LEVEL = -6;
    private static final int MAX_ZOOM_LEVEL = 8;
    private static final double ZOOM_BASE = 1.6;

    private MainApp app;

    private SVector imageTranslation;
    private int imageZoomLevel;
    private boolean draggingImage;

    private int newX, newY, newWidth, newHeight;
    private boolean resizing;

    public ImageCanvas(int orientation, int hAlignment, int vAlignment, MainApp app) {
        super(orientation, hAlignment, vAlignment);

        this.app = app;
        app.setCanvas(this);

        setFillSize();

        setCursorShape(() -> draggingImage ? GLFW_POINTING_HAND_CURSOR : null);

        style.setBackgroundColor(UIColors.CANVAS);

        clipChildren = true;

        add(new ImageResize());
        add(new ImageDisplay());

        for (ImageTool tool : ImageTool.INSTANCES) {
            add(switch (tool) {
                case PencilTool _ -> new PencilToolContainer(app);
                case LineTool _ -> new LineToolContainer(app);
                case TextTool _ -> new TextToolContainer(app);
                case SelectionTool _ -> new SelectionToolContainer(app);
                default -> new ToolContainer<ImageTool>(tool, app);
            });
        }

        resetImageTransform();
        // imageTranslation = new SVector();
        // imageZoomLevel = 0;

        draggingImage = false;
        resizing = false;

        addMousePressAction(GLFW_MOUSE_BUTTON_LEFT, false, this::leftClick);
        addMousePressAction(GLFW_MOUSE_BUTTON_RIGHT, false, this::rightClick);

        // zoom
        addMouseWheelAction(GLFW_MOD_CONTROL, false, this::canDoScrollZoom,
                scroll -> {
                    zoom((int) Math.signum(scroll.y), new SVector(mousePosition).sub(position));
                    return true;
                });
        // scroll
        addMouseWheelAction(0, false, this::canDoScrollZoom,
                scroll -> {
                    imageTranslation.add(scroll);
                    return true;
                });
        addMouseWheelAction(GLFW_MOD_SHIFT, false, this::canDoScrollZoom,
                scroll -> {
                    imageTranslation.add(new SVector(scroll.y, scroll.x));
                    return true;
                });
    }

    @Override
    public void handleEvents() {
        super.handleEvents();

        // stop dragging image
        if (draggingImage) {
            int mods = UI.getModifiers();
            boolean control = (mods & GLFW_MOD_CONTROL) != 0;
            if (!UI.isRightMousePressed() || !control) {
                draggingImage = false;
            }
        }
        // dragging image
        if (draggingImage) {
            SVector mouseMovement = new SVector(app.getMousePosition()).sub(app.getPrevMousePosition());
            imageTranslation.add(mouseMovement);
        }
    }

    private void leftClick() {
        if (mouseAbove) {
            int mods = UI.getModifiers();
            toolClick(GLFW_MOUSE_BUTTON_LEFT, mods);
        }
    }

    private void rightClick() {
        int mods = UI.getModifiers();

        if (mouseAbove)
            toolClick(GLFW_MOUSE_BUTTON_RIGHT, mods);
        if (canDoScrollZoom()) {
            // dragging image
            if ((mods & GLFW_MOD_CONTROL) != 0) {
                draggingImage = true;
            }
        }
    }

    private void toolClick(int mouseButton, int mods) {
        if ((mods & GLFW_MOD_CONTROL) == 0) {
            int[] mousePosition = app.getMouseImagePosition();
            int mouseX = mousePosition[0],
                    mouseY = mousePosition[1];

            app.getActiveTool().click(mouseX, mouseY, mouseButton);
        }
    }

    private void zoom(int delta, SVector origin) {
        double prevZoom = getImageZoom();
        imageZoomLevel += delta;
        imageZoomLevel = Math.clamp(imageZoomLevel, MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL);
        double zoom = getImageZoom();
        imageTranslation.sub(origin).scale(zoom / prevZoom).add(origin);
    }

    public boolean canDoScrollZoom() {
        return mouseAboveChild(this);
    }

    private boolean mouseAboveChild(UIElement element) {
        if (element.mouseAbove())
            return true;
        if (element instanceof UIContainer container) {
            for (UIElement child : container.getChildren()) {
                if (mouseAboveChild(child))
                    return true;
            }
        }
        return false;
    }

    public void resetImageTransform() {
        // imageTranslation = new SVector(1, 1).scale(UISizes.MARGIN.get() *
        // UI.getUIScale());
        // imageZoomLevel = 0;

        // Math.pow(ZOOM_BASE, imageZoomLevel) * UI.getUIScale() * imageWidth = 1500 *
        // UI.getScale()
        // ZOOM_BASE^imageZoomLevel = 1500 / imageWidth

        SVector size = new SVector(this.size);
        if (size.magSq() < 1)
            size = new SVector(1576, 888); // canvas size when on 1920x1080 window with ui scale of 1

        imageZoomLevel = getDefaultZoomLevel();
        Image image = app.getImage();
        int imageWidth = image.getWidth(),
                imageHeight = image.getHeight();
        double zoom = getImageZoom();
        imageTranslation = new SVector(size).sub(new SVector(imageWidth, imageHeight).scale(zoom)).div(2);
    }

    public void zoomIn() {
        zoom(1, new SVector(size).div(2));
    }

    public boolean canZoomIn() {
        return imageZoomLevel < MAX_ZOOM_LEVEL;
    }

    public void zoomOut() {
        zoom(-1, new SVector(size).div(2));
    }

    public boolean canZoomOut() {
        return imageZoomLevel > MIN_ZOOM_LEVEL;
    }

    public void resetZoom() {
        int deltaZoom = getDefaultZoomLevel() - imageZoomLevel;
        zoom(deltaZoom, new SVector(size).div(2));
    }

    private int getDefaultZoomLevel() {
        double uiScale = UI.getUIScale();
        Image image = app.getImage();
        int imageWidth = image.getWidth(),
                imageHeight = image.getHeight();
        double widthRatio = size.x / (imageWidth * uiScale),
                heightRatio = size.y / (imageHeight * uiScale);
        int level = (int) Math.floor(Math.log(Math.min(widthRatio, heightRatio)) / Math.log(ZOOM_BASE));
        level = Math.clamp(level, MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL);
        return level;
    }

    public double getImageZoom() {
        return Math.pow(ZOOM_BASE, imageZoomLevel) * UI.getUIScale();
    }

    public void translateImage(SVector delta) {
        imageTranslation.add(delta.scale(getImageZoom()));
    }

    public SVector getImageTranslation() {
        return imageTranslation;
    }

    public SVector getImagePosition(SVector screenSpacePos) {
        return screenSpacePos.copy().sub(getAbsolutePosition()).sub(imageTranslation).div(getImageZoom());
    }

    public SVector getScreenPosition(SVector imagePos) {
        return imagePos.copy().scale(getImageZoom()).add(getAbsolutePosition().add(imageTranslation));
    }

    public boolean isImageResizing() {
        return resizing;
    }

    public int getNewImageWidth() {
        return newWidth;
    }

    public int getNewImageHeight() {
        return newHeight;
    }

    private class ImageDisplay extends UIFloatContainer {

        ImageDisplay() {
            super(0, 0);

            noBackground();
            addAnchor(Anchor.TOP_LEFT, ImageCanvas.this::getImageTranslation);
            add(new ImageContainerChild());
        }

        // Horrible name but whatever.
        // This needs to be its own class because it is a UIImage (and ImageContainer is
        // a subclass of UIFloatContainer).
        private class ImageContainerChild extends UIImage {

            ImageContainerChild() {
                super(() -> app.getImage().getPreviewTextureID(), new SVector());

                style.setBackgroundCheckerboard(UIColors.TRANSPARENCY_1, UIColors.TRANSPARENCY_2, UISizes.CHECKERBOARD);
            }

            @Override
            public void setPreferredSize() {
                Image image = app.getImage();

                size.set(image.getWidth(), image.getHeight());
                size.scale(app.getImageZoom());
            }
        }
    }

    private class ImageResize extends UIFloatContainer implements Resizable {

        ImageResize() {
            super(0, 0);

            noBackground();
            style.setStrokeCheckerboard(
                    () -> resizing,
                    UIColors.SELECTION_BORDER_1,
                    UIColors.SELECTION_BORDER_2,
                    UISizes.CHECKERBOARD);
            style.setStrokeWeight(() -> 2 * UISizes.STROKE_WEIGHT.get1f());

            addAnchor(Anchor.TOP_LEFT, this::getPos);

            for (int dy = 0; dy <= 2; dy++) {
                for (int dx = 0; dx <= 2; dx++) {
                    if (dx == 1 && dy == 1)
                        continue;

                    add(new SizeKnob(this, dx, dy, () -> true, app));
                }
            }
        }

        @Override
        public void update() {
            newX = 0;
            newY = 0;
            newWidth = app.getImage().getWidth();
            newHeight = app.getImage().getHeight();

            super.update();

            setFixedSize(new SVector(newWidth, newHeight).scale(getImageZoom()));
        }

        private SVector getPos() {
            SVector pos = getImageTranslation().copy();
            double zoom = getImageZoom();
            pos.x += newX * zoom;
            pos.y += newY * zoom;
            return pos;
        }

        @Override
        public void startDragging() {
            resizing = true;
        }

        @Override
        public void finishDragging() {
            app.cropImage(newX, newY, newWidth, newHeight);
            resizing = false;
        }

        @Override
        public boolean lockRatio() {
            return false;
        }

        @Override
        public int getX() {
            return newX;
        }

        @Override
        public void setX(int x) {
            newX = x;
        }

        @Override
        public int getY() {
            return newY;
        }

        @Override
        public void setY(int y) {
            newY = y;
        }

        @Override
        public int getWidth() {
            return newWidth;
        }

        @Override
        public void setWidth(int width) {
            newWidth = Math.clamp(width, MainApp.MIN_IMAGE_SIZE, MainApp.MAX_IMAGE_SIZE);
        }

        @Override
        public int getHeight() {
            return newHeight;
        }

        @Override
        public void setHeight(int height) {
            newHeight = Math.clamp(height, MainApp.MIN_IMAGE_SIZE, MainApp.MAX_IMAGE_SIZE);
        }
    }

}
