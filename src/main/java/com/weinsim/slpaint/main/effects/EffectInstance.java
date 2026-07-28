package com.weinsim.slpaint.main.effects;

public abstract class EffectInstance {

    private boolean visible;

    public EffectInstance() {
        visible = true;
    }

    public void loadUniforms() {
        loadUniform("textureSampler", 0);
    }

    protected void loadUniform(String name, Object value) {
        getEffect().loadUniform(name, value);
    }

    public abstract Effect getEffect();

    public void toggleVisibility() {
        visible = !visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void show() {
        setVisible(true);
    }

    public void hide() {
        setVisible(false);
    }

    public boolean isVisible() {
        return visible;
    }

}
