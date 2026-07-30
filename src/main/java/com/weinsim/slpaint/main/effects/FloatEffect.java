package com.weinsim.slpaint.main.effects;

public abstract sealed class FloatEffect extends EffectInstance permits Contrast, Saturation {

    private final double min, max;
    private final double exponent;

    private double value;

    protected FloatEffect(String name, double min, double max) {
        this(name, min, max, 1.0);
    }
    protected FloatEffect(String name, double min, double max, double exponent) {
        this.min = min;
        this.max = max;
        this.exponent = exponent;
        addUniform(name, this::getValue);
        value = 1.0;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = Math.clamp(value, min, max);
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getExponent() {
        return exponent;
    }
    
}
