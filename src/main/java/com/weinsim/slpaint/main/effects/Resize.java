package com.weinsim.slpaint.main.effects;

/**
 * This "Effect" simply outputs the input color as its pixel color. It is used
 * for resizing images.
 */
public final class Resize extends VoidEffect {

    Resize() {
    }

    @Override
    public Effect getEffect() {
        return Effect.RESIZE;
    }

}
