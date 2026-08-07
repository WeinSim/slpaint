package com.weinsim.sutil.color;

/**
 * 
 * @param hue        0 - 360
 * @param saturation 0 - 1
 * @param lightness  0 - 1
 * @param alpha      0 - 1
 */
public record HSL(double hue, double saturation, double lightness, double alpha) implements ColorType {

    public HSL(double hue, double saturation, double lightness, double alpha) {
        this.hue = ((hue % 360) + 360) % 360;
        this.saturation = Math.clamp(saturation, 0, 1);
        this.lightness = Math.clamp(lightness, 0, 1);
        this.alpha = Math.clamp(alpha, 0, 1);
    }

    public HSL(SRGBFloat sRGBFloat) {
        double r = sRGBFloat.red(),
                g = sRGBFloat.green(),
                b = sRGBFloat.blue();
        double cMax = Math.max(Math.max(r, g), b);
        double cMin = Math.min(Math.min(r, g), b);
        double delta = cMax - cMin;
        double h;
        if (delta < 0.000_001)
            h = 0;
        else if (cMax == r)
            h = 60 * ((((g - b) / delta) + 6) % 6);
        else if (cMax == g)
            h = 60 * ((b - r) / delta + 2);
        else
            h = 60 * ((r - g) / delta + 4);
        double l = (cMax + cMin) / 2;
        double s;
        if (delta < 0.000_001)
            s = 0;
        else
            s = delta / (1 - Math.abs(2 * l - 1));
        this(h, s, l, sRGBFloat.alpha());
    }

    @Override
    public SRGBFloat toSRGBFloat() {
        double c = (1 - Math.abs(2 * lightness - 1)) * saturation;
        double x = c * (1 - Math.abs((hue / 60) % 2 - 1));
        double m = lightness - c / 2;
        double r, g, b;
        if (hue < 60) {
            r = c;
            g = x;
            b = 0;
        } else if (hue < 120) {
            r = x;
            g = c;
            b = 0;
        } else if (hue < 180) {
            r = 0;
            g = c;
            b = x;
        } else if (hue < 240) {
            r = 0;
            g = x;
            b = c;
        } else if (hue < 300) {
            r = x;
            g = 0;
            b = c;
        } else {
            r = c;
            g = 0;
            b = x;
        }
        r = r + m;
        g = g + m;
        b = b + m;
        return new SRGBFloat(r, g, b, alpha);
    }

}
