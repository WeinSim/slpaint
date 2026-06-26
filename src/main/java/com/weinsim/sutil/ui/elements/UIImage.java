package com.weinsim.sutil.ui.elements;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

import com.weinsim.sutil.math.SVector;
import com.weinsim.sutil.ui.UIIcon;
import com.weinsim.sutil.ui.UISizes;

public class UIImage extends UIElement {

    protected IntSupplier textureIDSupplier;

    protected Supplier<SVector> sizeSupplier;

    public UIImage(UIIcon icon) {
        this(icon.textureID(), UISizes.ICON::getWidthHeight);
    }

    public UIImage(UIIcon icon, Supplier<SVector> sizeSupplier) {
        this(icon.textureID(), sizeSupplier);
    }

    public UIImage(int textureID, SVector size) {
        setTextureID(textureID);
        setSize(size);
    }

    public UIImage(IntSupplier textureIDSupplier, SVector size) {
        setTextureID(textureIDSupplier);
        setSize(size);
    }

    public UIImage(int textureID, Supplier<SVector> sizeSupplier) {
        setTextureID(textureID);
        setSize(sizeSupplier);
    }

    public UIImage(IntSupplier textureIDSupplier, Supplier<SVector> sizeSupplier) {
        setTextureID(textureIDSupplier);
        setSize(sizeSupplier);
    }

    @Override
    public void setPreferredSize() {
        size.set(sizeSupplier.get());
    }

    public int getTextureID() {
        return textureIDSupplier.getAsInt();
    }

    public void setTextureID(int textureID) {
        setTextureID(() -> textureID);
    }

    public void setTextureID(IntSupplier textureIDSupplier) {
        this.textureIDSupplier = textureIDSupplier;
    }

    public void setSize(SVector size) {
        setSize(() -> size);
    }

    public void setSize(Supplier<SVector> sizeSupplier) {
        this.sizeSupplier = sizeSupplier;
    }

}
