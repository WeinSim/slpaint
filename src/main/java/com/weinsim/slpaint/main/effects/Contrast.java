package com.weinsim.slpaint.main.effects;

import com.weinsim.slpaint.main.image.Image;
import com.weinsim.sutil.SUtil;

public class Contrast extends Effect {

    static final Contrast INSTANCE = new Contrast();

    private double multiplier;

    private Contrast() {
    }

    @Override
    public void apply(Image image) {
        image.forEachPixel(color -> {
            int red = SUtil.red(color),
                    green = SUtil.green(color),
                    blue = SUtil.blue(color),
                    alpha = SUtil.alpha(color);
            // SVector hsl = SUtil.rgbToHSL(red, green, blue);
            // hsl.y *= multiplier;
            // SVector rgb = SUtil.hslToRGB(hsl);
            // return SUtil.toARGB(rgb.x, rgb.y, rgb.z, alpha);
            red = (int) ((red - 127) * multiplier + 127);
            green = (int) ((green - 127) * multiplier + 127);
            blue = (int) ((blue - 127) * multiplier + 127);
            return SUtil.toARGB(red, green, blue, alpha);
        });
    }

    @Override
    public void init() {
        multiplier = 1.0;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

}
