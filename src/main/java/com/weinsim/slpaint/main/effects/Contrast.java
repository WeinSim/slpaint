package com.weinsim.slpaint.main.effects;

public class Contrast extends EffectInstance {

    public static final double MIN_CONTRAST = 0,
            MAX_CONTRAST = 20;

    private double multiplier;

    Contrast() {
        multiplier = 1.0;
    }

    @Override
    public Effect getEffect() {
        return Effect.CONTRAST;
    }

    @Override
    public void loadUniforms() {
        super.loadUniforms();
        loadUniform("multiplier", multiplier);
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = Math.clamp(multiplier, MIN_CONTRAST, MAX_CONTRAST);
    }

}
