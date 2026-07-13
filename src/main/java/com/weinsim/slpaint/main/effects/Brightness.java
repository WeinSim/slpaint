package com.weinsim.slpaint.main.effects;

public class Brightness extends Effect {

    public static final int MIN_BRIGHTNESS = -255,
            MAX_BRIGHTNESS = 255;

    private int brightness;

    Brightness() {
        super("brightness");
    }

    @Override
    public void init() {
        brightness = 0;
    }

    @Override
    public void loadUniforms() {
        super.loadUniforms();
        shader.loadUniform("brightness", brightness / 255.0);
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = Math.clamp(brightness, MIN_BRIGHTNESS, MAX_BRIGHTNESS);
    }

}
