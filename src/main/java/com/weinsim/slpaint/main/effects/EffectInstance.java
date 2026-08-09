package com.weinsim.slpaint.main.effects;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.function.Supplier;

public abstract sealed class EffectInstance permits IntEffect, FloatEffect, VoidEffect {

    private boolean visible;

    private HashMap<String, Supplier<?>> uniforms;

    public EffectInstance() {
        visible = true;
        uniforms = new HashMap<>();
        addUniform("textureSampler", () -> 0);
    }

    protected void addUniform(String name, Supplier<?> value) {
        uniforms.put(name, value);
    }

    public final void loadUniforms() {
        Effect effect = getEffect();
        for (Entry<String, Supplier<?>> e : uniforms.entrySet())
            effect.loadUniform(e.getKey(), e.getValue().get());
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
