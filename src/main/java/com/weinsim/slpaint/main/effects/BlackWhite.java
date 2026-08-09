package com.weinsim.slpaint.main.effects;

public final class BlackWhite extends VoidEffect {

    BlackWhite() {
        addUniform("luminanceWeights", () -> Effect.LUMINANCE_WEIGHTS);
    }

    @Override
    public Effect getEffect() {
        return Effect.BLACK_WHITE;
    }

}
