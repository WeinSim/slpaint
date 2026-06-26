package com.weinsim.slpaint.main.tools;

public final class MagicTool extends ImageTool {

    public static final MagicTool INSTANCE = new MagicTool();

    private MagicTool() {
    }

    @Override
    public void click(int x, int y, int mouseButton) {
        app.magic(x, y, mouseButton);
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
        return "Magic";
    }

}
