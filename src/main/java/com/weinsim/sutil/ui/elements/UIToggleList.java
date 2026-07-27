package com.weinsim.sutil.ui.elements;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import com.weinsim.sutil.math.SVector;
import com.weinsim.sutil.ui.UIColors;
import com.weinsim.sutil.ui.UIShape;
import com.weinsim.sutil.ui.UISizes;

public class UIToggleList extends UIContainer {

    public UIToggleList(String label, BooleanSupplier supplier, Consumer<Boolean> consumer) {
        this();

        addToggle(label, supplier, consumer);
    }

    public UIToggleList() {
        super(VERTICAL, 0);

        zeroMargin();
        noOutline();
    }

    public void addToggle(String label, BooleanSupplier supplier, Consumer<Boolean> consumer) {
        UIContainer container = new UIContainer(HORIZONTAL, CENTER);
        container.setHFillSize().zeroMargin().noOutline();

        container.addLeftClickAction(() -> consumer.accept(!supplier.getAsBoolean()));
        // container.addKeyPressAction(GLFW.GLFW_KEY_SPACE, 0, true, clickAction);
        container.setHandCursor();
        container.setSelectable(true);

        container.add(new UIText(label));
        container.add(new UIContainer(0, 0).setVMarginScale(0).setHFillSize().noOutline());
        container.add(new UIToggle(supplier));

        add(container);
    }

    private static class UIToggle extends UIContainer {

        BooleanSupplier stateSupplier;

        UIToggle(BooleanSupplier stateSupplier) {
            super(HORIZONTAL, LEFT);
            this.stateSupplier = stateSupplier;

            noOutline();
            double yDiff = UISizes.RADIO.get1f() - UISizes.RADIO_INSIDE.get1f();
            double margin = UISizes.MARGIN.get1f();
            setMarginScale(yDiff / 2 / margin);

            style.setBackgroundColor(UIColors.BACKGROUND_SELECTED);
            style.setShape(UIShape.ROUND_RECTANGLE);

            setHandCursor();

            setFixedSize(new SVector(UISizes.TOGGLE_WIDTH.get1f(), UISizes.RADIO.get1f()));

            add(new ToggleInside());
        }

        @Override
        public void update() {
            super.update();

            setAlignment(stateSupplier.getAsBoolean() ? RIGHT : LEFT, CENTER);
        }
    }

    private static class ToggleInside extends UIElement {

        ToggleInside() {
            style.setShape(UIShape.ELLIPSE);
            style.setBackgroundColor(UIColors.HIGHLIGHT);

            size.set(UISizes.RADIO_INSIDE.get2f());
        }

        @Override
        public void setPreferredSize() {
        }
    }

}
