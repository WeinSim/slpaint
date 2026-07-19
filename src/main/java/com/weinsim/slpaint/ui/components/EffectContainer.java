package com.weinsim.slpaint.ui.components;

import java.util.List;

import com.weinsim.slpaint.main.apps.MainApp;
import com.weinsim.slpaint.main.effects.EffectInstance;
import com.weinsim.sutil.ui.UIColors;
import com.weinsim.sutil.ui.UISizes;
import com.weinsim.sutil.ui.elements.UIButton;
import com.weinsim.sutil.ui.elements.UIContainer;
import com.weinsim.sutil.ui.elements.UILabel;
import com.weinsim.sutil.ui.elements.UIText;

public class EffectContainer extends UIContainer {

    private final MainApp app;

    public EffectContainer(MainApp app) {
        super(VERTICAL, CENTER);
        this.app = app;
        setHFillSize();
        setMarginScale(2.0);
        setPaddingScale(2.0);

        add(new UIText("No Effects Selected", UISizes.TEXT_SMALL)
                .setColor(UIColors.TEXT_INVALID)
                .setVisibilitySupplier(() -> app.getPreviewEffects().isEmpty()));
    }

    @Override
    public void update() {
        // make sure that the actual effects are synced with this container's children
        List<InstanceContainer> containers = removeAll(InstanceContainer.class);
        for (EffectInstance effect : app.getPreviewEffects()) {
            InstanceContainer container = null;
            for (InstanceContainer c : containers) {
                if (c.effect == effect) {
                    container = c;
                    break;
                }
            }
            if (container != null) {
                // container already existed. place it into the list
                add(0, container);
                containers.remove(container);
            } else {
                add(0, new InstanceContainer(effect));
            }
        }
        super.update();
    }

    private class InstanceContainer extends UIContainer {

        final EffectInstance effect;

        public InstanceContainer(EffectInstance effect) {
            super(CENTER, HORIZONTAL);
            this.effect = effect;
            noOutline();
            setHFillSize();
            zeroMargin();
            // zeroPadding();
            // setMarginScale(0.5);
            // setPaddingScale(0.5);

            add(createButtonContainer(
                    new UILabel[] {
                            UILabel.icon("arrow_up"),
                            UILabel.icon("arrow_down")
                    },
                    new Runnable[] {
                            () -> app.movePreviewEffectUp(effect),
                            () -> app.movePreviewEffectDown(effect)
                    }));
            add(new UIText(() -> effect.getEffect().name));
            add(new UIContainer(0, 0).setHFillSize().zeroMargin().noOutline());
            add(createButtonContainer(
                    new UILabel[] {
                            UILabel.icons("eye_open", "eye_closed", effect::isVisible),
                            UILabel.icon("trash_can")
                    },
                    new Runnable[] {
                            () -> effect.toggleVisibility(),
                            () -> app.removePreviewEffect(effect)
                    }));
        }

        private UIContainer createButtonContainer(UILabel[] labels, Runnable[] actions) {
            UIContainer buttons = new UIContainer(HORIZONTAL, CENTER);
            buttons.zeroMargin().zeroPadding().noOutline();
            for (int i = 0; i < labels.length; i++) {
                buttons.add(new UIButton(
                        // labels[i].setIconSize(UISizes.ICON_SMALL),
                        labels[i].setActive(() -> true),
                        actions[i])
                        .setMarginScale(1.0));
            }
            return buttons;
        }

    }

}
