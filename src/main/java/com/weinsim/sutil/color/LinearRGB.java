package com.weinsim.sutil.color;

public record LinearRGB(double red, double green, double blue, double alpha) implements ColorType {

    public LinearRGB(double red, double green, double blue, double alpha) {
        this.red = Math.clamp(red, 0, 1);
        this.green = Math.clamp(green, 0, 1);
        this.blue = Math.clamp(blue, 0, 1);
        this.alpha = Math.clamp(alpha, 0, 1);
    }

    public LinearRGB(SRGBFloat sRGBFloat) {
        this(
                sRGBToLinear(sRGBFloat.red()),
                sRGBToLinear(sRGBFloat.green()),
                sRGBToLinear(sRGBFloat.blue()),
                sRGBFloat.alpha());
    }

    @Override
    public SRGBFloat toSRGBFloat() {
        return new SRGBFloat(
                linearToSRGB(red),
                linearToSRGB(green),
                linearToSRGB(blue),
                alpha);
    }

    private static double sRGBToLinear(double sRGB) {
        return sRGB <= 0.04045
                ? sRGB / 12.92
                : Math.pow((sRGB + 0.055) / 1.055, 2.4);
    }

    private static double linearToSRGB(double linear) {
        return linear < 0.0031308
                ? linear * 12.92
                : 1.055 * Math.pow(linear, 1 / 2.4) - 0.055;
    }

}
