package com.weinsim.slpaint.main.effects;

public class Contrast extends EffectInstance {

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
        this.multiplier = multiplier;
    }

}
