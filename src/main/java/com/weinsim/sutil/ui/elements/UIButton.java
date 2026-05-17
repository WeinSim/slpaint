package com.weinsim.sutil.ui.elements;

public class UIButton extends UIContainer {

    protected final UILabel label;

    public UIButton(String text, Runnable clickAction) {
        this(UILabel.text(text), clickAction);
        outlineNormal = true;
    }

    public UIButton(UILabel label, Runnable clickAction) {
        super(HORIZONTAL, CENTER);
        this.label = label;

        outlineNormal = false;
        backgroundHighlight = true;
        selectable = true;
        if (clickAction != null)
            addLeftClickAction(clickAction);

        add(label);
    }

    @Override
    public void update() {
        super.update();

        boolean isActive = label.isActive();
        selectable = isActive;
        backgroundHighlight = isActive;
    }
}