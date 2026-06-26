package com.weinsim.sutil.ui;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import com.weinsim.sutil.math.SVector;

public record UIMouseWheelAction(int mods, boolean mouseAbove, BooleanSupplier possible, Predicate<SVector> action) {

    private static final BooleanSupplier TRUE = () -> true;

    public UIMouseWheelAction(int mods, Predicate<SVector> action) {
        this(mods, true, TRUE, action);
    }

    public UIMouseWheelAction(int mods, boolean mouseAbove, Predicate<SVector> action) {
        this(mods, mouseAbove, TRUE, action);
    }

    public boolean mouseWheel(SVector scroll, int mods, boolean isMouseAbove) {
        if ((!mouseAbove || isMouseAbove) && possible.getAsBoolean() && this.mods == mods)
            return action.test(scroll);
        return false;
    }

}
