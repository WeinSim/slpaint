package com.weinsim.slpaint.main.apps;

import com.weinsim.slpaint.main.ColorPicker;
import com.weinsim.slpaint.renderengine.Window;
import com.weinsim.slpaint.ui.AppUI;
import com.weinsim.slpaint.ui.SettingsUI;
import com.weinsim.sutil.color.Color;
import com.weinsim.sutil.ui.UISizes;

public final class SettingsApp extends App {

    private ColorPicker colorPicker;

    public SettingsApp(MainApp mainApp) {
        super(UISizes.SETTINGS_APP, Window.NORMAL, true, false, "Settings", mainApp);
        colorPicker = AppUI.getBaseColorPicker();
        loadUI();
    }

    public void setUIColor(Color color) {
        colorPicker.setColor(color);
    }

    public void setDarkMode(boolean darkMode) {
        if (AppUI.isDarkMode() == darkMode)
            return;

        Color[] oldColors = AppUI.getDefaultUIColors();
        AppUI.setDarkMode(darkMode);
        Color[] newColors = AppUI.getDefaultUIColors();

        Color baseColor = AppUI.getBaseColor();

        // this is a bit of a hack
        for (int i = 0; i < oldColors.length; i++) {
            if (baseColor.equals(oldColors[i]))
                setUIColor(newColors[i]);
        }
    }

    @Override
    protected App createChildApp(int dialogType) {
        return null;
    }

    public ColorPicker getColorPicker() {
        return colorPicker;
    }

    @Override
    protected AppUI<?> createUI() {
        return new SettingsUI(this);
    }

}
