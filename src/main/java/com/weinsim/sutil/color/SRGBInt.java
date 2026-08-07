package com.weinsim.sutil.color;

public record SRGBInt(int red, int green, int blue, int alpha) implements ColorType {

    public SRGBInt(int red, int green, int blue, int alpha) {
        this.red = Math.clamp(red, 0, 255);
        this.green = Math.clamp(green, 0, 255);
        this.blue = Math.clamp(blue, 0, 255);
        this.alpha = Math.clamp(alpha, 0, 255);
    }

    public SRGBInt(SRGBFloat sRGBFloat) {
        this((int) (sRGBFloat.red() * 255),
                (int) (sRGBFloat.green() * 255),
                (int) (sRGBFloat.blue() * 255),
                (int) (sRGBFloat.alpha() * 255));
    }

    @Override
    public SRGBFloat toSRGBFloat() {
        return new SRGBFloat(red / 255.0, green / 255.0, blue / 255.0, alpha / 255.0);
    }

}
