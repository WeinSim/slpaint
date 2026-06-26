package com.weinsim.slpaint.main.apps;

import com.weinsim.slpaint.main.ColorPicker;
import com.weinsim.slpaint.renderengine.Window;
import com.weinsim.slpaint.ui.AppUI;
import com.weinsim.slpaint.ui.ColorEditorUI;

public final class ColorEditorApp extends App {

    private ColorPicker colorPicker;

    private MainApp mainApp;

    public ColorEditorApp(MainApp mainApp, int initialColor) {
        super(500, 500, Window.NORMAL, false, true, "Color Editor", mainApp);
        this.mainApp = mainApp;

        colorPicker = new ColorPicker(initialColor);
    }

    public ColorPicker getColorPicker() {
        return colorPicker;
    }

    public MainApp getMainApp() {
        return mainApp;
    }

    @Override
    protected App createChildApp(int dialogType) {
        return null;
    }

    @Override
    protected AppUI<?> createUI() {
        return new ColorEditorUI(this);
    }

}
