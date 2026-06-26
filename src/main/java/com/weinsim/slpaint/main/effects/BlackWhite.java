package com.weinsim.slpaint.main.effects;

import com.weinsim.slpaint.main.image.Image;
import com.weinsim.sutil.SUtil;

public class BlackWhite extends Effect {

    static final BlackWhite INSTANCE = new BlackWhite();

    private BlackWhite() {
    }

    public void apply(Image image) {
        image.forEachPixel(color -> {
            final int red = SUtil.red(color),
                    green = SUtil.green(color),
                    blue = SUtil.blue(color),
                    alpha = SUtil.alpha(color);
            // no gamma blending for now
            double brightness = (red + green + blue) / 3;
            return SUtil.toARGB((int) brightness, alpha);
        });
    }

    @Override
    public void init() {
    }

}
