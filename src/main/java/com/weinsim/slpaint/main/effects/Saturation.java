package com.weinsim.slpaint.main.effects;

public final class Saturation extends FloatEffect {

    private static final double MIN_FACTOR = 0, MAX_FACTOR = 5;

    public Saturation() {
        super("factor", MIN_FACTOR, MAX_FACTOR);
        addUniform("luminanceWeights", () -> Effect.LUMINANCE_WEIGHTS);
    }

    @Override
    public Effect getEffect() {
        return Effect.SATURATION;
    }
    
}
