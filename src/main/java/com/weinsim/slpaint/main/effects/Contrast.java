package com.weinsim.slpaint.main.effects;

public final class Contrast extends FloatEffect {

    public static final double MIN_CONTRAST = 0,
            MAX_CONTRAST = 20;

    Contrast() {
        super("multiplier", MIN_CONTRAST, MAX_CONTRAST, 4.0);
    }

    @Override
    public Effect getEffect() {
        return Effect.CONTRAST;
    }

}
