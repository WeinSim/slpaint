package com.weinsim.slpaint.main.effects;

public abstract sealed class IntEffect extends EffectInstance permits Brightness {

    private final int min, max;

    private int value;

    protected IntEffect(String name, int min, int max, double shaderFactor) {
        this.min = min;
        this.max = max;
        addUniform(name, () -> value / shaderFactor);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = Math.clamp(value, min, max);
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

}
