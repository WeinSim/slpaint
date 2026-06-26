package com.weinsim.sutil.ui.elements;

import com.weinsim.sutil.ui.UIColors;

public class UISeparator extends UIContainer {

    public UISeparator() {
        super(VERTICAL, LEFT);

        style.setStrokeColor(UIColors.SEPARATOR);
        zeroMargin();
        zeroPadding();
    }

    @Override
    public void update() {
        super.update();

        if (parent.getOrientation() == VERTICAL) {
            setHFillSize();
            setVMinimalSize();
        } else {
            setHMinimalSize();
            setVFillSize();
        }
    }

}
