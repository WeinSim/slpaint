package com.weinsim.slpaint.main.effects;

/**
 * This "Effect" simply outputs the input color as its pixel color. It is used
 * for resizing images.
 */
public class Resize extends EffectInstance {

    Resize() {
    }

    @Override
    public Effect getEffect() {
        return Effect.RESIZE;
    }

}
