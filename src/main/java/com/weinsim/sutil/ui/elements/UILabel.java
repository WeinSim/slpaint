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

    protected final BooleanSupplier active;

    protected Supplier<SVector> iconSize;
    protected DoubleSupplier textSize;

    public UILabel() {
        this(TRUE);
    }

    public UILabel(UIIcon icon) {
        this(TRUE);
        addIcon(icon);
    }

    public UILabel(UIIcon icon, BooleanSupplier active) {
        this(active);
        addIcon(icon);
    }

    public UILabel(UIIcon activeIcon, UIIcon inactiveIcon, BooleanSupplier active) {
        this(active);
        addTwoIcons(activeIcon, inactiveIcon, active);
    }

    public UILabel(String text) {
        this(TRUE);
        addText(text);
    }

    public UILabel(String text, BooleanSupplier active) {
        this(active);
        addText(text, active);
    }

    public UILabel(UIIcon icon, String text) {
        this(TRUE);
        addIcon(icon);
        addText(text);
    }

    public UILabel(UIIcon icon, String text, BooleanSupplier active) {
        this(active);
        addIcon(icon);
        addText(text, active);
    }

    public UILabel(UIIcon activeIcon, UIIcon inactiveIcon, String text, BooleanSupplier active) {
        this(active);
        addTwoIcons(activeIcon, inactiveIcon, active);
        addText(text, active);
    }

    private UILabel(BooleanSupplier active) {
        super(HORIZONTAL, CENTER);
        this.active = active;

        outlineNormal = false;
        zeroMargin();

        active = TRUE;
        iconSize = UISizes.ICON::getWidthHeight;
        textSize = UISizes.TEXT;

    }

    private void addIcon(UIIcon icon) {
        if (icon != null)
            add(new UIImage(icon, this::getIconSize));
        else
            add(new UIEmpty(this::getIconSize));
    }

    private void addTwoIcons(UIIcon activeIcon, UIIcon inactiveIcon, BooleanSupplier active) {
        add(new UIImage(activeIcon, this::getIconSize).setVisibilitySupplier(active));
        add(new UIImage(inactiveIcon, this::getIconSize).setVisibilitySupplier(() -> !active.getAsBoolean()));
    }

    private void addText(String text) {
        add(new UIText(text, this::getTextSize));
    }

    private void addText(String text, BooleanSupplier active) {
        UIText uiText = new UIText(text, this::getTextSize);
        uiText.setColor(SUtil.ifThenElse(active, UIColors.TEXT, UIColors.TEXT_INVALID));
        add(uiText);
    }

    public static UILabel icon(String iconName) {
        return new UILabel(new UIIcon(iconName));
    }

    public static UILabel icon(String iconName, BooleanSupplier active) {
        return new UILabel(new UIIcon(iconName), active);
    }

    public static UILabel icons(String activeIconName, String inactiveIconName, BooleanSupplier active) {
        return new UILabel(new UIIcon(activeIconName), new UIIcon(inactiveIconName), active);
    }

    public static UILabel text(String text) {
        return new UILabel(text);
    }

    public static UILabel text(String text, BooleanSupplier active) {
        return new UILabel(text);
    }

    public static UILabel iconText(String iconName, String text) {
        return new UILabel(new UIIcon(iconName), text);
    }

    public static UILabel iconText(String iconName, String text, BooleanSupplier active) {
        return new UILabel(new UIIcon(iconName), text, active);
    }

    public static UILabel iconsText(String activeIconName, String inactiveIconName, String text,
            BooleanSupplier active) {
        return new UILabel(new UIIcon(activeIconName), new UIIcon(inactiveIconName), text, active);
    }

    public void setSize(Supplier<SVector> iconSize, DoubleSupplier textSize) {
        this.iconSize = iconSize;
        this.textSize = textSize;
    }

    public void setIconSize(Supplier<SVector> iconSize) {
        this.iconSize = iconSize;
    }

    public void setTextSize(DoubleSupplier textSize) {
        this.textSize = textSize;
    }

    private SVector getIconSize() {
        return iconSize.get();
    }

    private double getTextSize() {
        return textSize.getAsDouble();
    }

    public BooleanSupplier getActive() {
        return active;
    }

    public boolean isActive() {
        return active.getAsBoolean();
    }
}