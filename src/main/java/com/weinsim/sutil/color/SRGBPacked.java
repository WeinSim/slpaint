package com.weinsim.sutil.color;

public record SRGBPacked(int argb) implements ColorType {

    // no non-canonical constructor here since every packed integer represents a
    // valid color

    public SRGBPacked(SRGBFloat sRGBFloat) {
        int red = (int) (sRGBFloat.red() * 255),
                green = (int) (sRGBFloat.green() * 255),
                blue = (int) (sRGBFloat.blue() * 255),
                alpha = (int) (sRGBFloat.alpha() * 255);
        this((alpha << 24) | (red << 16) | (green << 8) | blue);
    }

    @Override
    public SRGBFloat toSRGBFloat() {
        int red = (argb >> 16) & 0xFF,
                green = (argb >> 8) & 0xFF,
                blue = (argb) & 0xFF,
                alpha = (argb >> 24) & 0xFF;
        return new SRGBFloat(red / 255.0, green / 255.0, blue / 255.0, alpha / 255.0);
    }

}
