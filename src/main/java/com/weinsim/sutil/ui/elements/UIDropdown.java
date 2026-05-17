package com.weinsim.sutil.ui.elements;

import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import com.weinsim.sutil.ui.UIColors;
import com.weinsim.sutil.ui.UIIcon;

public class UIDropdown extends UIButton {

    private boolean expanded;

    protected final UIFloatMenu floatMenu;

    public UIDropdown(UILabel label) {
        this(label, false);
    }

    public UIDropdown(UIIcon icon, Supplier<String> textSupplier, boolean scroll) {
        this(icon == null ? new UILabel() : new UILabel(icon));

        label.add(new UIText(textSupplier));
    }

    public UIDropdown(UILabel label, boolean scroll) {
        super(label, null);

        style.setBackgroundColor(() -> mouseAbove || expanded ? UIColors.BACKGROUND_HIGHLIGHT.get() : null);

        add(new UIImage(new UIIcon("expand_up")).setVisibilitySupplier(() -> expanded));
        add(new UIImage(new UIIcon("expand_down")).setVisibilitySupplier(() -> !expanded));

        floatMenu = new UIFloatMenu(() -> expanded, () -> expanded = false, scroll, UIText.NORMAL);
        floatMenu.addAnchor(UIFloatContainer.Anchor.TOP_LEFT, UIFloatContainer.Anchor.BOTTOM_LEFT);
        floatMenu.addAnchor(UIFloatContainer.Anchor.BOTTOM_LEFT, UIFloatContainer.Anchor.TOP_LEFT);
        add(floatMenu);

        addLeftClickAction(() -> expanded = !expanded);

        expanded = false;
    }

    public UIDropdown(String[] options, IntSupplier valueSupplier, IntConsumer valueSetter) {
        this(options, valueSupplier, valueSetter, false);
    }

    public UIDropdown(String[] options, IntSupplier valueSupplier, IntConsumer valueSetter,
            boolean scroll) {

        this(null, () -> options[valueSupplier.getAsInt()], scroll);

        outlineNormal = true;

        for (int i = 0; i < options.length; i++) {
            final int j = i;
            addLabel(options[i], () -> valueSetter.accept(j));
        }
    }

    public UIDropdown(String[] options, Supplier<String> nameSupplier, Consumer<String> nameConsumer) {
        this(options, nameSupplier, nameConsumer, false);
    }

    public UIDropdown(String[] options, Supplier<String> nameSupplier, Consumer<String> nameConsumer,
            boolean scroll) {

        this(null, nameSupplier, scroll);

        outlineNormal = true;

        for (int i = 0; i < options.length; i++) {
            final int j = i;
            addLabel(options[i], () -> nameConsumer.accept(options[j]));
        }
    }

    public void addLabel(String text, Runnable clickAction) {
        floatMenu.addLabel(text, clickAction);
    }

    public UIFloatMenu getFloatMenu() {
        return floatMenu;
    }
}