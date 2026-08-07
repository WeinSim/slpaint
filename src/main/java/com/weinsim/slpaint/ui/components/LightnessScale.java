package com.weinsim.slpaint.ui.components;

import com.weinsim.slpaint.main.ColorPicker;
import com.weinsim.slpaint.main.apps.App;
import com.weinsim.sutil.ui.UI;
import com.weinsim.sutil.ui.elements.UIScale;

public class LightnessScale extends UIScale {

    private ColorPicker colorPicker;

    public LightnessScale(int orientation, ColorPicker colorPicker) {
        super(orientation,
                () -> {
                    double value = App.isHSLColorSpace()
                            ? colorPicker.getLightness()
                            : colorPicker.getValue();
                    return (orientation == UI.VERTICAL)
                            ? (1 - value)
                            : value;
                },
                value -> {
                    if (orientation == UI.VERTICAL)
                        value = 1 - value;
                    if (App.isHSLColorSpace())
                        colorPicker.setLightness(value);
                    else
                        colorPicker.setValue(value);
                },
                false);
        this.colorPicker = colorPicker;
    }

    @Override
    protected Visuals getVisuals(int orientation) {
        return new LSVisuals(orientation);
    }

    public class LSVisuals extends Visuals {

        public LSVisuals(int orientation) {
            super(orientation);
        }

        public double getHue() {
            return colorPicker.getHue();
        }

        public double getSaturation() {
            return colorPicker.getHSLSaturation();
        }
    }

}
