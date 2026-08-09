package com.weinsim.slpaint.main;

import com.weinsim.sutil.color.Color;
import com.weinsim.sutil.color.HSL;
import com.weinsim.sutil.color.HSV;
import com.weinsim.sutil.color.SRGBFloat;
import com.weinsim.sutil.color.SRGBInt;

public class ColorPicker {

    private Color color;

    public ColorPicker(Color color) {
        this.color = color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    // ---------- RGB ----------

    public int getRed() {
        return color.sRGBInt().red();
    }

    public void setRed(int red) {
        if (red == getRed())
            return;
        SRGBInt sRGB = color.sRGBInt();
        color = Color.sRGB(red, sRGB.green(), sRGB.blue(), sRGB.alpha());
    }

    public int getGreen() {
        return color.sRGBInt().green();
    }

    public void setGreen(int green) {
        if (green == getGreen())
            return;
        SRGBInt sRGB = color.sRGBInt();
        color = Color.sRGB(sRGB.red(), green, sRGB.blue(), sRGB.alpha());
    }

    public int getBlue() {
        return color.sRGBInt().blue();
    }

    public void setBlue(int blue) {
        if (blue == getBlue())
            return;
        SRGBInt sRGB = color.sRGBInt();
        color = Color.sRGB(sRGB.red(), sRGB.green(), blue, sRGB.alpha());
    }

    // ---------- Alpha ----------

    public int getAlpha() {
        return color.sRGBInt().alpha();
    }

    public void setAlpha(int alpha) {
        if (alpha == getAlpha())
            return;
        SRGBFloat sRGB = color.sRGBFloat();
        color = Color.sRGB(sRGB.red(), sRGB.green(), sRGB.blue(), alpha / 255.0);
    }

    // ---------- HSL / HSV ----------

    public double getHue() {
        return color.hsl().hue();
    }

    public void setHue(double hue) {
        if (hue == getHue())
            return;
        HSL hsl = color.hsl();
        color = Color.hsl(hue, hsl.saturation(), hsl.lightness(), hsl.alpha());
    }

    public double getHSLSaturation() {
        return color.hsl().saturation();
    }

    public void setHSLSaturation(double saturation) {
        if (saturation == getHSLSaturation())
            return;
        HSL hsl = color.hsl();
        color = Color.hsl(hsl.hue(), saturation, hsl.lightness(), hsl.alpha());
    }

    public double getLightness() {
        return color.hsl().lightness();
    }

    public void setLightness(double lightness) {
        if (lightness == getLightness())
            return;
        HSL hsl = color.hsl();
        color = Color.hsl(hsl.hue(), hsl.saturation(), lightness, hsl.alpha());
    }

    public double getHSVSaturation() {
        return color.hsv().saturation();
    }

    public void setHSVSaturation(double saturation) {
        if (saturation == getHSVSaturation())
            return;
        HSV hsv = color.hsv();
        color = Color.hsv(hsv.hue(), saturation, hsv.value(), hsv.alpha());
    }

    public double getValue() {
        return color.hsv().value();
    }

    public void setValue(double value) {
        if (value == getValue())
            return;
        HSV hsv = color.hsv();
        color = Color.hsv(hsv.hue(), hsv.saturation(), value, hsv.alpha());
    }

}
