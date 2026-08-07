package com.weinsim.slpaint.ui.components;

import java.util.function.Consumer;

import com.weinsim.slpaint.main.ColorArray;
import com.weinsim.sutil.color.Color;
import com.weinsim.sutil.ui.UISizes;
import com.weinsim.sutil.ui.elements.UIContainer;

public class CustomColorContainer extends UIContainer {

    public CustomColorContainer(int orientation, ColorArray colors, Consumer<Color> clickAction) {
        super(orientation, CENTER);

        for (int i = 0; i < colors.getCapacity(); i++) {
            final int j = i;
            UIColorElement button = new UIColorElement(() -> colors.getColor(j), UISizes.COLOR_BUTTON);
            button.addLeftClickAction(() -> {
                Color color = colors.getColor(j);
                if (color != null)
                    clickAction.accept(color);
            });
            add(button);
        }
    }

}
