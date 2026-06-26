package com.weinsim.slpaint.ui.components;

import java.util.function.Consumer;

import org.lwjglx.util.vector.Vector4f;

import com.weinsim.slpaint.main.ColorArray;
import com.weinsim.slpaint.main.apps.MainApp;
import com.weinsim.sutil.ui.UISizes;
import com.weinsim.sutil.ui.elements.UIContainer;

public class CustomColorContainer extends UIContainer {

    public CustomColorContainer(int orientation, ColorArray colors, Consumer<Vector4f> clickAction) {
        super(orientation, CENTER);

        for (int i = 0; i < colors.getCapacity(); i++) {
            final int j = i;
            UIColorElement button = new UIColorElement(() -> MainApp.toVector4f(colors.getColor(j)),
                    UISizes.COLOR_BUTTON);
            button.addLeftClickAction(() -> {
                Vector4f color = MainApp.toVector4f(colors.getColor(j));
                if (color != null) {
                    clickAction.accept(color);
                }
            });
            add(button);
        }
    }

}
