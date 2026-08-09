package com.weinsim.slpaint.ui.components;

import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import com.weinsim.sutil.color.Color;
import com.weinsim.sutil.math.SVector;
import com.weinsim.sutil.ui.UIColors;
import com.weinsim.sutil.ui.UISizes;
import com.weinsim.sutil.ui.elements.UIElement;

public class UIColorElement extends UIElement {

    private Supplier<Color> colorSupplier;
    private Supplier<SVector> sizeSupplier;

    public UIColorElement(Supplier<Color> colorSupplier, DoubleSupplier sizeSupplier) {
        this(
                colorSupplier,
                () -> {
                    double wh = sizeSupplier.getAsDouble();
                    return new SVector(wh, wh);
                },
                true);
    }

    public UIColorElement(Supplier<Color> colorSupplier, Supplier<SVector> sizeSupplier, boolean outline) {
        this.colorSupplier = colorSupplier;
        this.sizeSupplier = sizeSupplier;

        style.setBackgroundColor(this::getColor);
        style.setBackgroundCheckerboard(
                () -> getColor() != null,
                UIColors.TRANSPARENCY_1,
                UIColors.TRANSPARENCY_2,
                UISizes.CHECKERBOARD);
        if (outline) {
            style.setStrokeColor(() -> getColor() == null ? UIColors.INVALID.get() : UIColors.HIGHLIGHT.get());
        }
    }

    @Override
    public void setPreferredSize() {
        size.set(sizeSupplier.get());
    }

    public Color getColor() {
        return colorSupplier.get();
    }

}
