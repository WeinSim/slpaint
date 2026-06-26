package com.weinsim.sutil.ui;

import java.util.function.Consumer;

/**
 * The {@code selected} field controls wether this UIElement has to be selected
 * in order for this char input action to run.
 */
public record UICharInputAction(boolean selected, Consumer<Character> action) {

    public UICharInputAction(Consumer<Character> action) {
        this(true, action);
    }

    public void charInput(char c, boolean isSelected) {
        if (!selected || isSelected)
            action.accept(c);
    }

}
