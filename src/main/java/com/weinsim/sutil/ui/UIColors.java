package com.weinsim.sutil.ui;

import java.util.function.Supplier;

import org.lwjglx.util.vector.Vector4f;

public enum UIColors implements Supplier<Vector4f> {

    BACKGROUND(0.24, 0.95),
    BACKGROUND_2(0.7, 0.82),
    BACKGROUND_HIGHLIGHT(0.55, 0.9),
    OUTLINE(1.1, 0.45),
    SCROLLBAR_HIGHLIGHT(0.9, 0.6),
    SEPARATOR(0.56, 0.7),
    HIGHLIGHT(new Vector4f(1f, 1f, 1f, 1f), new Vector4f(0f, 0f, 0f, 1f)),
    TEXT(new Vector4f(0.75f, 0.75f, 0.75f, 1f), new Vector4f(0f, 0f, 0f, 1f)),
    TEXT_INVALID(new Vector4f(0.45f, 0.45f, 0.45f, 1f), new Vector4f(0.35f, 0.35f, 0.35f, 1f)),
    CANVAS(0.4, 0.85),
    TRANSPARENCY_1(0.6, 1),
    TRANSPARENCY_2(0.0, 0.85),
    SELECTION_BORDER_1(new Vector4f(0f, 0f, 0f, 1f)),
    SELECTION_BORDER_2(new Vector4f(1f, 1f, 1f, 1f)),
    INVALID(new Vector4f(0.5f, 0.5f, 0.5f, 1f));

    public final double darkModeBrightness, lightModeBrightness;

    public final Vector4f darkColor, lightColor;

    public final boolean useBrightness;

    private UIColors(double darkModeBrightness, double lightModeBrightness) {
        this.darkModeBrightness = darkModeBrightness;
        this.lightModeBrightness = lightModeBrightness;

        useBrightness = true;

        darkColor = null;
        lightColor = null;
    }

    private UIColors(Vector4f color) {
        this(color, color);
    }

    private UIColors(Vector4f darkColor, Vector4f lightColor) {
        this.darkColor = darkColor;
        this.lightColor = lightColor;

        useBrightness = false;

        darkModeBrightness = 0;
        lightModeBrightness = 0;
    }

    @Override
    public Vector4f get() {
        // This method is being called ~5520 times per second (which is too much!)
        // Maybe cache the results?

        boolean darkMode = UI.isDarkMode();
        if (useBrightness) {
            double brightness = darkMode ? darkModeBrightness : lightModeBrightness;
            Vector4f ret = (Vector4f) new Vector4f(UI.getBaseColor()).scale((float) brightness);
            ret.w = 1.0f;
            return ret;
        } else {
            return darkMode ? darkColor : lightColor;
        }
    }
}