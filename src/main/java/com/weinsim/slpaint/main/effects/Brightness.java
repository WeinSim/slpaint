package com.weinsim.slpaint.main.effects;

import com.weinsim.slpaint.main.image.Image;
import com.weinsim.sutil.SUtil;

public class Brightness extends Effect {

    static final Brightness INSTANCE = new Brightness();

    public static final int MIN_BRIGHTNESS = -255,
            MAX_BRIGHTNESS = 255;

    private int brightness;

    private Brightness() {
    }

    @Override
    public void apply(Image image) {
        image.forEachPixel(color -> {
            int red = SUtil.red(color),
                    green = SUtil.green(color),
                    blue = SUtil.blue(color),
                    alpha = SUtil.alpha(color);
            red += brightness;
            green += brightness;
            blue += brightness;
            return SUtil.toARGB(red, green, blue, alpha);
        });
    }

    @Override
    public void init() {
        brightness = 0;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = Math.clamp(brightness, MIN_BRIGHTNESS, MAX_BRIGHTNESS);
    }

}
