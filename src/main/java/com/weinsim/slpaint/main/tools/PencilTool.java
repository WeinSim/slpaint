package com.weinsim.slpaint.main.tools;

import static org.lwjgl.glfw.GLFW.*;

import com.weinsim.slpaint.main.image.Image;
import com.weinsim.slpaint.settings.BooleanSetting;

public final class PencilTool extends ImageTool {

    public static final PencilTool INSTANCE = new PencilTool();

    public static final int DRAWING_PRIMARY = 0x02, DRAWING_SECONDARY = 0x04;

    public static final int MIN_SIZE = 1, MAX_SIZE = 16;

    private int size = 1;

    private BooleanSetting applyTransparency = new BooleanSetting("applyTransparency");

    private PencilTool() {
        super();
    }

    @Override
    public void click(int x, int y, int mouseButton) {
        if (mouseButton != GLFW_MOUSE_BUTTON_LEFT && mouseButton != GLFW_MOUSE_BUTTON_RIGHT)
            return;

        Image image = app.getImage();

        if (!image.isInside(x, y))
            return;

        if (x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight()) {
            state = switch (mouseButton) {
                case GLFW_MOUSE_BUTTON_LEFT -> DRAWING_PRIMARY;
                case GLFW_MOUSE_BUTTON_RIGHT -> DRAWING_SECONDARY;
                // can never happen
                default -> 0;
            };
        }
    }

    @Override
    public void finish() {
    }

    @Override
    public void cancel() {
    }

    @Override
    public void createKeyboardShortcuts() {
    }

    @Override
    public String getName() {
        return "Pencil";
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = Math.clamp(size, MIN_SIZE, MAX_SIZE);
    }

    public boolean isApplyTransparency() {
        return applyTransparency.get();
    }

    public void setApplyTransparency(boolean applyTransparency) {
        this.applyTransparency.set(applyTransparency);
    }

}
