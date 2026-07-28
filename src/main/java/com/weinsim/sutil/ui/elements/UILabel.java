package com.weinsim.sutil.ui.elements;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import com.weinsim.sutil.SUtil;
import com.weinsim.sutil.math.SVector;
import com.weinsim.sutil.ui.UIColors;
import com.weinsim.sutil.ui.UIIcon;
import com.weinsim.sutil.ui.UISizes;

public class UILabel extends UIContainer {

    private static final BooleanSupplier TRUE = () -> true;

    protected BooleanSupplier active;

    protected Supplier<SVector> iconSize;
    protected DoubleSupplier textSize;

    private UILabel(BooleanSupplier active) {
        super(HORIZONTAL, CENTER);
        this.active = active;

        outlineNormal = false;

        iconSize = UISizes.ICON;
        textSize = UISizes.TEXT;
    }

    private UILabel addIcon(String iconName) {
        return addIcon(iconName, TRUE);
    }

    private UILabel addIcon(String iconName, BooleanSupplier visibilitySupplier) {
        UIElement child = iconName != null
                ? new UIImage(new UIIcon(iconName), this::getIconSize)
                : new UIEmpty(this::getIconSize);
        child.setVisibilitySupplier(visibilitySupplier);
        add(child);
        return this;
    }

    private UILabel addTwoIcons(String activeIcon, String inactiveIcon, BooleanSupplier active) {
        addIcon(activeIcon, active);
        addIcon(inactiveIcon, () -> !active.getAsBoolean());
        return this;
    }

    private UILabel addText(String text) {
        add(new UIText(text, this::getTextSize));
        return this;
    }

    private UILabel addText(String text, BooleanSupplier active) {
        UIText uiText = new UIText(text, this::getTextSize);
        uiText.setColor(SUtil.ifThenElse(active, UIColors.TEXT, UIColors.TEXT_INVALID));
        add(uiText);
        return this;
    }

    public static UILabel empty() {
        return new UILabel(TRUE);
    }

    public static UILabel icon(String iconName) {
        return new UILabel(TRUE).addIcon(iconName);
    }

    public static UILabel icon(String iconName, BooleanSupplier active) {
        return new UILabel(active).addIcon(iconName);
    }

    public static UILabel icons(String activeIconName, String inactiveIconName, BooleanSupplier active) {
        return new UILabel(active).addTwoIcons(activeIconName, inactiveIconName, active);
    }

    public static UILabel text(String text) {
        return new UILabel(TRUE).addText(text);
    }

    public static UILabel text(String text, BooleanSupplier active) {
        return new UILabel(active).addText(text, active);
    }

    public static UILabel iconText(String iconName, String text) {
        return new UILabel(TRUE).addIcon(iconName).addText(text);
    }

    public static UILabel iconText(String iconName, String text, BooleanSupplier active) {
        return new UILabel(active).addIcon(iconName).addText(text, active);
    }

    public static UILabel iconsText(String activeIconName, String inactiveIconName, String text,
            BooleanSupplier active) {
        return new UILabel(active).addTwoIcons(activeIconName, inactiveIconName, active).addText(text, active);
    }

    public <T extends Supplier<SVector> & DoubleSupplier> UILabel setSize(T iconTextSize) {
        setSize(iconTextSize, iconTextSize);
        return this;
    }

    public UILabel setSize(Supplier<SVector> iconSize, DoubleSupplier textSize) {
        this.iconSize = iconSize;
        this.textSize = textSize;
        return this;
    }

    public UILabel setIconSize(Supplier<SVector> iconSize) {
        this.iconSize = iconSize;
        return this;
    }

    public UILabel setTextSize(DoubleSupplier textSize) {
        this.textSize = textSize;
        return this;
    }

    public UILabel small() {
        setSize(UISizes.ICON_SMALL, UISizes.TEXT_SMALL);
        return this;
    }

    private SVector getIconSize() {
        return iconSize.get();
    }

    private double getTextSize() {
        return textSize.getAsDouble();
    }

    public UILabel setActive(BooleanSupplier active) {
        this.active = active;
        return this;
    }

    public boolean isActive() {
        return active.getAsBoolean();
    }

}
