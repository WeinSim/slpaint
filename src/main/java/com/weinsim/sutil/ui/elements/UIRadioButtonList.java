package com.weinsim.sutil.ui.elements;

import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import com.weinsim.sutil.ui.UI;
import com.weinsim.sutil.ui.UIColors;
import com.weinsim.sutil.ui.UIShape;
import com.weinsim.sutil.ui.UISizes;

public class UIRadioButtonList extends UIContainer {

    public UIRadioButtonList(int orientation, String[] options, IntSupplier stateSupplier, IntConsumer stateConsumer) {
        this(orientation, options, null, stateSupplier, stateConsumer);
    }

    public UIRadioButtonList(int orientation, String[] options, String[] icons, IntSupplier stateSupplier,
            IntConsumer stateConsumer) {

        super(orientation, orientation == UI.VERTICAL ? UI.LEFT : UI.CENTER);

        noOutline();
        zeroMargin();
        setPaddingScale(2.0);

        for (int i = 0; i < options.length; i++) {
            UIContainer row = new UIContainer(UI.HORIZONTAL, UI.CENTER);
            row.zeroMargin().noOutline();

            final int j = i;
            row.addLeftClickAction(() -> stateConsumer.accept(j));
            // row.addKeyPressAction(GLFW.GLFW_KEY_SPACE, 0, true, clickAction);
            row.setHandCursor();
            row.setSelectable(true);

            row.add(new UIRadioButton(i, stateSupplier));
            String iconName = icons != null ? icons[i] : null;
            String text = options != null ? options[i] : null;
            UILabel label;
            if (iconName == null) {
                if (text == null)
                    label = new UILabel();
                else
                    label = UILabel.text(text);
            } else {
                if (text == null)
                    label = UILabel.icon(iconName);
                else
                    label = UILabel.iconText(iconName, text);
            }
            row.add(label);

            add(row);
        }
    }

    private static class UIRadioButton extends UIContainer {

        UIRadioButton(int index, IntSupplier stateSupplier) {
            super(UI.VERTICAL, UI.CENTER);

            noOutline();
            zeroMargin();

            style.setBackgroundColor(UIColors.BACKGROUND_2);
            style.setShape(UIShape.ELLIPSE);

            setFixedSize(UISizes.RADIO.getWidthHeight());

            add(new RadioButtonInside(() -> stateSupplier.getAsInt() == index));
        }
    }

    private static class RadioButtonInside extends UIElement {

        RadioButtonInside(BooleanSupplier visibilitySupplier) {
            setVisibilitySupplier(visibilitySupplier);

            style.setBackgroundColor(UIColors.HIGHLIGHT);
            style.setShape(UIShape.ELLIPSE);
        }

        @Override
        public void setPreferredSize() {
            size.set(UISizes.RADIO_INSIDE.getWidthHeight());
        }
    }

}
