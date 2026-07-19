package com.weinsim.slpaint.main.effects;

public class Brightness extends EffectInstance {

    public static final int MIN_BRIGHTNESS = -255,
            MAX_BRIGHTNESS = 255;

    private int brightness;

    Brightness() {
        brightness = 0;
    }

    @Override
    public Effect getEffect() {
        return Effect.BRIGHTNESS;
    }

    @Override
    public void loadUniforms() {
        super.loadUniforms();
        loadUniform("brightness", brightness / 255.0);
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = Math.clamp(brightness, MIN_BRIGHTNESS, MAX_BRIGHTNESS);
    }

}
