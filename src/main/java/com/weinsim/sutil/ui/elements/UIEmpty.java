package com.weinsim.sutil.ui.elements;

import java.util.function.Supplier;

import com.weinsim.sutil.math.SVector;

public class UIEmpty extends UIElement {

    private Supplier<SVector> sizeSupplier = null;

    public UIEmpty(SVector size) {
        this.size.set(size);
    }

    public UIEmpty(Supplier<SVector> sizeSupplier) {
        this.sizeSupplier = sizeSupplier;
    }

    @Override
    public void setPreferredSize() {
        if (sizeSupplier != null)
            size.set(sizeSupplier.get());
    }
}