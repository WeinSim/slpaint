package com.weinsim.sutil.ui;

import java.util.function.DoubleSupplier;

import com.weinsim.sutil.math.SVector;

public enum UISizes implements DoubleSupplier {

    MARGIN(8),
    PADDING(8),
    STROKE_WEIGHT(1.2, true),
    TEXT(18),
    TEXT_SMALL(14),
    ICON(18),
    ICON_SMALL(14),
    SCROLLBAR(10),
    SCALE_NARROW(2),
    SCALE_WIDE(18),
    SCALE_SLIDER_LENGTH(10),
    SCALE_SLIDER_WIDTH(2),
    RADIO(20),
    RADIO_INSIDE(10),
    TOGGLE_WIDTH(45),
    DIALOG_MARGIN(50),
    // SLPaint specific stuff:
    MAIN_APP(1280, 720),
    SETTINGS_APP(900, 650),
    COLOR_BUTTON(24),
    BIG_COLOR_BUTTON(36),
    SELECTION_BORDER(2),
    CHECKERBOARD(12),
    HUE_SAT_FIELD(200),
    SIZE_KNOB(12),
    COLOR_PICKER_EXTRA_WINDOW(300),
    COLOR_PICKER_PREVIEW(96, 60);

    public final double size;
    public final double width;
    public final double height;
    public final boolean forceInteger;

    private UISizes(double size) {
        this(size, false);
    }

    private UISizes(double size, boolean forceInteger) {
        this(size, size, size, forceInteger);
    }

    private UISizes(double width, double height) {
        this(width, height, false);
    }

    private UISizes(double width, double height, boolean forceInteger) {
        this(0, width, height, false);
    }

    private UISizes(double size, double width, double height, boolean forceInteger) {
        this.size = size;
        this.width = width;
        this.height = height;
        this.forceInteger = forceInteger;
    }

    @Override
    public double getAsDouble() {
        return get(this);
    }

    public double get() {
        return getAsDouble();
    }

    public SVector getWidthHeight() {
        return new SVector(getSize(width, forceInteger), getSize(height, forceInteger));
    }

    private static double get(UISizes s) {
        return getSize(s.size, s.forceInteger);
    }

    private static double getSize(double s, boolean forceInteger) {
        double size = s * UI.getUIScale();
        if (forceInteger)
            size = (int) Math.round(size);
        return size;
    }

}
