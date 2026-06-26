package com.weinsim.sutil.ui;

import java.util.function.BooleanSupplier;

public class KeyboardShortcut extends UIKeyPressAction {

    private final String identifier;

    public KeyboardShortcut(String identifier, int key, int mods, BooleanSupplier possible, Runnable action) {
        super(key, mods, false, possible, action);
        this.identifier = identifier;
    }

    public void run() {
        if (isPossible())
            action.run();
    }

    public String getIdentifier() {
        return identifier;
    }

}
