package com.weinsim.slpaint.ui.components;

import com.weinsim.slpaint.main.ColorPicker;
import com.weinsim.sutil.color.Color;
import com.weinsim.sutil.ui.elements.UIScale;

public class AlphaScale extends UIScale {

    private ColorPicker colorPicker;

    public AlphaScale(int orientation, ColorPicker colorPicker) {
        super(orientation, () -> colorPicker.getAlpha() / 255.0, d -> colorPicker.setAlpha((int) (d * 255)), false);
        this.colorPicker = colorPicker;
    }

    @Override
    protected Visuals getVisuals(int orientation) {
        return new ASVisuals(orientation);
    }

    public class ASVisuals extends Visuals {

        public ASVisuals(int orientation) {
            super(orientation);
        }

        public Color getColor() {
            return colorPicker.getColor();
        }
    }

}
