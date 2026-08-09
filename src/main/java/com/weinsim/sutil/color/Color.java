package com.weinsim.sutil.color;

import java.util.HashMap;
import java.util.function.Function;

public class Color {

    private final SRGBFloat sRGBFloat;

    private final HashMap<Class<? extends ColorType>, ColorType> cache;

    private Color(ColorType color) {
        this.sRGBFloat = color.toSRGBFloat();
        cache = new HashMap<>();
        cache.put(color.getClass(), color);
    }

    // ---------- factory methods ----------

    public Color color(ColorType color) {
        return new Color(color);
    }

    public static Color sRGB(int sRGB) {
        return new Color(new SRGBPacked(sRGB));
    }

    public static Color sGrey(double grey) {
        return new Color(new SRGBFloat(grey, grey, grey, 1.0));
    }

    public static Color sGrey(double grey, double alpha) {
        return new Color(new SRGBFloat(grey, grey, grey, alpha));
    }

    public static Color sGrey(int grey) {
        return new Color(new SRGBInt(grey, grey, grey, 255));
    }

    public static Color sGrey(int grey, int alpha) {
        return new Color(new SRGBInt(grey, grey, grey, alpha));
    }

    public static Color sRGB(double red, double green, double blue) {
        return new Color(new SRGBFloat(red, green, blue, 1.0));
    }

    public static Color sRGB(double red, double green, double blue, double alpha) {
        return new Color(new SRGBFloat(red, green, blue, alpha));
    }

    public static Color sRGB(int red, int green, int blue) {
        return new Color(new SRGBInt(red, green, blue, 255));
    }

    public static Color sRGB(int red, int green, int blue, int alpha) {
        return new Color(new SRGBInt(red, green, blue, alpha));
    }

    public static Color linearGrey(double grey) {
        return new Color(new LinearRGB(grey, grey, grey, 1.0));
    }

    public static Color linearGrey(double grey, double alpha) {
        return new Color(new LinearRGB(grey, grey, grey, alpha));
    }

    public static Color linearRGB(double red, double green, double blue) {
        return new Color(new LinearRGB(red, green, blue, 1.0));
    }

    public static Color linearRGB(double red, double green, double blue, double alpha) {
        return new Color(new LinearRGB(red, green, blue, alpha));
    }

    public static Color hsv(double hue, double saturation, double value) {
        return new Color(new HSV(hue, saturation, value, 1.0));
    }

    public static Color hsv(double hue, double saturation, double value, double alpha) {
        return new Color(new HSV(hue, saturation, value, alpha));
    }

    public static Color hsl(double hue, double lightness, double value) {
        return new Color(new HSL(hue, lightness, value, 1.0));
    }

    public static Color hsl(double hue, double lightness, double value, double alpha) {
        return new Color(new HSL(hue, lightness, value, alpha));
    }

    // ---------- getters ----------

    public SRGBFloat sRGBFloat() {
        return sRGBFloat;
    }

    public SRGBInt sRGBInt() {
        return getCached(SRGBInt.class, SRGBInt::new);
    }

    public SRGBPacked sRGBPacked() {
        return getCached(SRGBPacked.class, SRGBPacked::new);
    }

    public LinearRGB linearRGB() {
        return getCached(LinearRGB.class, LinearRGB::new);
    }

    public HSV hsv() {
        return getCached(HSV.class, HSV::new);
    }

    public HSL hsl() {
        return getCached(HSL.class, HSL::new);
    }

    @SuppressWarnings("unchecked")
    private <T extends ColorType> T getCached(Class<T> clazz, Function<SRGBFloat, T> constructor) {
        return (T) cache.computeIfAbsent(clazz, _ -> constructor.apply(sRGBFloat));
    }

    public boolean equals(Color other) {
        final double margin = 1e-6;
        return Math.abs(sRGBFloat.red() - other.sRGBFloat.red()) < margin
                && Math.abs(sRGBFloat.green() - other.sRGBFloat.green()) < margin
                && Math.abs(sRGBFloat.blue() - other.sRGBFloat.blue()) < margin
                && Math.abs(sRGBFloat.alpha() - other.sRGBFloat.alpha()) < margin;
    }

}
