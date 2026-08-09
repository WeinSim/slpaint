package com.weinsim.sutil.color;

/**
 * 
 * @param hue        0 - 360
 * @param saturation 0 - 1
 * @param value      0 - 1
 * @param alpha      0 - 1
 */
public record HSV(double hue, double saturation, double value, double alpha) implements ColorType {

    public HSV(double hue, double saturation, double value, double alpha) {
        this.hue = ((hue % 360) + 360) % 360;
        this.saturation = Math.clamp(saturation, 0, 1);
        this.value = Math.clamp(value, 0, 1);
        this.alpha = Math.clamp(alpha, 0, 1);
    }

    public HSV(SRGBFloat sRGBFloat) {
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
            h = 60 * (((g - b) / delta + 6) % 6);
        else if (cMax == g)
            h = 60 * ((b - r) / delta + 2);
        else
            h = 60 * ((r - g) / delta + 4);
        double s;
        if (cMax < 0.000_001)
            s = 0;
        else
            s = delta / cMax;
        double v = cMax;
        this(h, s, v, sRGBFloat.alpha());
    }

    @Override
    public SRGBFloat toSRGBFloat() {
        double c = value * saturation;
        double x = c * (1 - Math.abs((hue / 60) % 2 - 1));
        double m = value - c;
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
