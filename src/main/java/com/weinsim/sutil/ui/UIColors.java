package com.weinsim.sutil.ui;

import java.util.function.Supplier;

import com.weinsim.sutil.color.Color;
import com.weinsim.sutil.color.SRGBFloat;

public enum UIColors implements Supplier<Color> {

    BACKGROUND(0.24, 0.95),
    BACKGROUND_2(0.35, 0.91),
    BACKGROUND_HIGHLIGHT(0.5, 0.88),
    BACKGROUND_SELECTED(0.7, 0.82),
    OUTLINE(1.1, 0.45),
    SCROLLBAR_HIGHLIGHT(0.9, 0.6),
    SEPARATOR(0.56, 0.7),
    HIGHLIGHT(Color.sGrey(1.0), Color.sGrey(0.0)),
    TEXT(Color.sGrey(0.75), Color.sGrey(0.0)),
    TEXT_INVALID(Color.sGrey(0.45), Color.sGrey(0.40)),
    CANVAS(0.4, 0.85),
    TRANSPARENCY_1(0.6, 1),
    TRANSPARENCY_2(0.0, 0.85),
    SELECTION_BORDER_1(Color.sGrey(0.0)),
    SELECTION_BORDER_2(Color.sGrey(1.0)),
    INVALID(Color.sGrey(0.5));

    /**
     * These brightness factors are referring to sRGB values (where multiplication
     * doesn't make that much sense, but whatever).
     */
    private final float darkModeBrightness, lightModeBrightness;
    private final Color darkColor, lightColor;
    private final boolean useBrightness;

    private UIColorSettings settingsCache;
    private Color colorCache;

    private UIColors(double darkModeBrightness, double lightModeBrightness) {
        this(darkModeBrightness, lightModeBrightness, null, null, true);
    }

    private UIColors(Color color) {
        this(color, color);
    }

    private UIColors(Color darkColor, Color lightColor) {
        this(0, 0, darkColor, lightColor, false);
    }

    private UIColors(double darkModeBrightness, double lightModeBrightness, Color darkColor, Color lightColor,
            boolean useBrightness) {
        this.darkModeBrightness = (float) darkModeBrightness;
        this.lightModeBrightness = (float) lightModeBrightness;
        this.darkColor = darkColor;
        this.lightColor = lightColor;
        this.useBrightness = useBrightness;

        settingsCache = null;
        colorCache = null;
    }

    @Override
    public Color get() {
        UIColorSettings settings = new UIColorSettings(UI.getBaseColor(), UI.isDarkMode());
        if (settings.equals(settingsCache))
            return colorCache;

        // System.out.format("Recalculating UI color for %s\n", name());
        if (useBrightness) {
            float brightness = settings.darkMode() ? darkModeBrightness : lightModeBrightness;
            colorCache = Color.sRGB(
                    settings.red() * brightness,
                    settings.green() * brightness,
                    settings.blue() * brightness,
                    1.0f);
        } else {
            colorCache = settings.darkMode() ? darkColor : lightColor;
        }
        settingsCache = settings;
        return colorCache;
    }

    private record UIColorSettings(double red, double green, double blue, boolean darkMode) {

        UIColorSettings(Color baseColor, boolean darkMode) {
            SRGBFloat sRGB = baseColor.sRGBFloat();
            this(sRGB.red(), sRGB.green(), sRGB.blue(), darkMode);
        }

    }

}
