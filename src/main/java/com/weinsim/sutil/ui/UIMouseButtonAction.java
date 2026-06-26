package com.weinsim.sutil.ui;

public record UIMouseButtonAction(int button, boolean mouseAbove, Runnable action) {

    public UIMouseButtonAction(int button, Runnable action) {
        this(button, true, action);
    }

    // Either mouse press or mouse release
    public void mouseAction(int button, int mods, boolean isMouseAbove) {
        if ((!mouseAbove || isMouseAbove) && this.button == button)
            action.run();
    }

}
