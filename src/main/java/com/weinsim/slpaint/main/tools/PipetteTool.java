package com.weinsim.slpaint.main.tools;

import com.weinsim.slpaint.main.image.Image;

public final class PipetteTool extends ImageTool {

    public static final PipetteTool INSTANCE = new PipetteTool();

    private PipetteTool() {
        super();
    }

    @Override
    public void createKeyboardShortcuts() {
    }

    @Override
    public void click(int x, int y, int mouseButton) {
        Image image = app.getImage();

        if (!image.isInside(x, y))
            return;

        switch (mouseButton) {
            case 0 -> app.setPrimaryColor(image.getPixel(x, y));
            case 1 -> app.setSecondaryColor(image.getPixel(x, y));
            default -> {
                return;
            }
        }

        app.switchBackToPreviousTool();
    }

    @Override
    public void finish() {
    }

    @Override
    public void cancel() {
    }

    @Override
    public String getName() {
        return "Pipette";
    }

}
