package com.weinsim.slpaint.main.tools;

import static org.lwjgl.glfw.GLFW.*;

import java.awt.image.BufferedImage;

import com.weinsim.slpaint.main.ClipboardManager;
import com.weinsim.slpaint.main.apps.MainApp;
import com.weinsim.slpaint.main.image.Image;
import com.weinsim.sutil.math.SVector;

public final class SelectionTool extends DragTool {

    public static final SelectionTool INSTANCE = new SelectionTool();

    private Image selection;

    private SelectionTool() {
        selection = null;
    }

    @Override
    public void createKeyboardShortcuts() {
        addShortcut("select_all", GLFW_KEY_A, GLFW_MOD_CONTROL, NONE | IDLE, this::selectEverything);
        addShortcut("finish_selection", GLFW_KEY_ESCAPE, 0, IDLE, this::finish);
        addShortcut("delete_selection", GLFW_KEY_DELETE, 0, IDLE, this::deleteSelection);
        addShortcut("delete_selection", GLFW_KEY_X, 0, IDLE, this::deleteSelection);
        addShortcut("paste", GLFW_KEY_V, GLFW_MOD_CONTROL, NONE | IDLE, this::pasteFromClipboard);
        addShortcut("copy", GLFW_KEY_C, GLFW_MOD_CONTROL, IDLE, this::copyToClipboard);
        addShortcut("cut", GLFW_KEY_X, GLFW_MOD_CONTROL, IDLE, this::cutToClipboard);
        addShortcut("crop_to_selection", GLFW_KEY_X, GLFW_MOD_CONTROL | GLFW_MOD_SHIFT, IDLE,
                this::cropImageToSelection);
        addShortcut("selection_up", GLFW_KEY_UP, 0, IDLE, () -> y--);
        addShortcut("selection_down", GLFW_KEY_DOWN, 0, IDLE, () -> y++);
        addShortcut("selection_left", GLFW_KEY_LEFT, 0, IDLE, () -> x--);
        addShortcut("selection_right", GLFW_KEY_RIGHT, 0, IDLE, () -> x++);
    }

    @Override
    public boolean enterIdle() {
        return width > 1 && height > 1;
    }

    @Override
    public void start() {
        createSubImage();
    }

    private void createSubImage() {
        selection = new Image(app.getImage().getSubImage(x, y, width, height,
                MainApp.isTransparentSelection() ? app.getSecondaryColor(): null));
        app.getImage().setPixels(x, y, width, height, app.getSecondaryColor());

        state = IDLE;
    }

    @Override
    public void finish() {
        finish(true);
    }

    @Override
    public void cancel() {
        finish(false);
    }

    public void deleteSelection() {
        finish(false);
        app.addImageSnapshot();
    }

    private void finish(boolean renderToImage) {
        if (selection != null) {
            if (renderToImage)
                app.renderImageToImage(selection, x, y, width, height);
            selection.cleanUp();
            selection = null;
        }

        state = NONE;
    }

    public void selectEverything() {
        finish();

        x = 0;
        y = 0;
        width = app.getImage().getWidth();
        height = app.getImage().getHeight();
        createSubImage();
    }

    public void copyToClipboard() {
        if (state != IDLE)
            return;

        ClipboardManager.setImage(selection.getBufferedImage());
    }

    public void cutToClipboard() {
        if (state != IDLE)
            return;

        copyToClipboard();
        deleteSelection();
    }

    public void pasteFromClipboard() {
        BufferedImage paste = ClipboardManager.getImage();
        if (paste == null)
            return;

        finish();

        SVector spawnPos = app.getImagePosition(app.getCanvas().getAbsolutePosition());
        x = Math.clamp((int) spawnPos.x, 0, app.getImage().getWidth() - paste.getWidth());
        y = Math.clamp((int) spawnPos.y, 0, app.getImage().getHeight() - paste.getHeight());
        width = paste.getWidth();
        height = paste.getHeight();

        selection = new Image(paste);

        app.setActiveTool(this);
        state = IDLE;
    }

    public void cropImageToSelection() {
        if (state != IDLE)
            return;

        app.setImage(selection.getBufferedImage());
        app.translateImage(x, y);
        finish(false);
    }

    public void rotateRight() {
        if (state != IDLE)
            return;
        selection.rotateRight();
        int temp = width;
        width = height;
        height = temp;
    }

    public void rotateLeft() {
        if (state != IDLE)
            return;
        selection.rotateLeft();
        int temp = width;
        width = height;
        height = temp;
    }

    public void rotate180() {
        if (state != IDLE)
            return;
        selection.rotate180();
    }

    public void flipHorizontal() {
        if (state != IDLE)
            return;
        selection.flipHorizontal();
    }

    public void flipVertical() {
        if (state != IDLE)
            return;
        selection.flipVertical();
    }

    @Override
    public int getMargin() {
        return 0;
    }

    @Override
    public String getName() {
        return "Selection";
    }

    public Image getSelection() {
        return selection;
    }

    @Override
    public boolean lockRatio() {
        return MainApp.isLockSelectionRatio();
    }

}
