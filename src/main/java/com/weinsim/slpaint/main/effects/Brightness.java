package com.weinsim.slpaint.main.effects;

public final class Brightness extends IntEffect {

    public static final int MIN_BRIGHTNESS = -255,
            MAX_BRIGHTNESS = 255;

    Brightness() {
        super("brightness", MIN_BRIGHTNESS, MAX_BRIGHTNESS, 255.0);
    }

    @Override
    public Effect getEffect() {
        return Effect.BRIGHTNESS;
    }

}
