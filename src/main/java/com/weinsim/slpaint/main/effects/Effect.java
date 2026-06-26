package com.weinsim.slpaint.main.effects;

import com.weinsim.slpaint.main.image.Image;

public abstract class Effect {

    public static final BlackWhite BLACK_WHITE = BlackWhite.INSTANCE;
    public static final Contrast CONTRAST = Contrast.INSTANCE;
    public static final Brightness BRIGHTNESS = Brightness.INSTANCE;

    public Effect() {
        init();
    }

    public abstract void apply(Image image);

    /**
     * If this effect stores some state (usually something that can be set by the
     * user via the UI), this method should reset that state. It is automatically
     * called from the constructor.
     */
    public abstract void init();

}
