package com.weinsim.sutil.ui;

public record UIIcon(String name, int textureID) {

    public UIIcon(String name) {
        this(name, UI.getIconTextureID(name));
    }
}