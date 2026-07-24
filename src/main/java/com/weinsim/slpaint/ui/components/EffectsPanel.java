package com.weinsim.slpaint.ui.components;

import java.util.List;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import com.weinsim.slpaint.main.apps.MainApp;
import com.weinsim.slpaint.main.effects.Effect;
import com.weinsim.slpaint.main.effects.EffectInstance;
import com.weinsim.sutil.math.SVector;
import com.weinsim.sutil.ui.UIColors;
import com.weinsim.sutil.ui.UIShape;
import com.weinsim.sutil.ui.UISizes;
import com.weinsim.sutil.ui.UIStyle;
import com.weinsim.sutil.ui.elements.UIButton;
import com.weinsim.sutil.ui.elements.UIContainer;
import com.weinsim.sutil.ui.elements.UILabel;
import com.weinsim.sutil.ui.elements.UIText;

public class EffectsPanel extends UIContainer {

    private final MainApp app;

    private boolean bottomPanelVisible;

    private InstanceContainer selectedInstance;

    public EffectsPanel(MainApp app) {
        super(VERTICAL, CENTER);
        this.app = app;
        bottomPanelVisible = false;
        selectedInstance = null;

        noOutline();
        setVFillSize();
        withSeparators(true);

        UIContainer top = new UIContainer(VERTICAL, LEFT);
        top.zeroMargin().noOutline();
        top.setFillSize();
        top.addLeftClickAction(() -> selectedInstance = null);
        UIContainer buttonRow = new UIContainer(HORIZONTAL, LEFT, CENTER);
        buttonRow.zeroMargin().noOutline();
        buttonRow.setHFillSize();
        buttonRow.add(new UIText("Active Effects", UISizes.TEXT_SMALL));
        buttonRow.add(new UIContainer(0, 0).setHFillSize().zeroMargin().noOutline());
        // buttonRow.add(new UIButton(
        // UILabel.icon("plus").small(),
        // // UILabel.iconText("plus", "Add Effect"),
        // this::showBottomPanel));
        buttonRow.add(new UIButton(
                UILabel.icon("close").small(),
                () -> app.removeAllPreviewEffects())
                .setVisibilitySupplier(() -> !app.getPreviewEffects().isEmpty()));
        top.add(buttonRow);
        top.add(new ActiveEffectsContainer().addScrollbars());
        top.add(new UIButton(
                UILabel.iconText("plus", "Add Effect"),
                this::showBottomPanel));
        add(top);

        UIContainer bottom = new UIContainer(VERTICAL, LEFT);
        bottom.zeroMargin().noOutline();
        bottom.setFillSize();
        bottom.setVisibilitySupplier(() -> bottomPanelVisible);
        bottom.add(new UIText("Available Effects", UISizes.TEXT_SMALL));
        UIContainer available = new UIContainer(VERTICAL, LEFT, TOP, VERTICAL);
        available.setFillSize();
        for (Effect effect : Effect.values()) {
            UIContainer row = new UIContainer(HORIZONTAL, CENTER);
            row.zeroMargin().noOutline();
            row.add(new UIButton(UILabel.icon("plus"),
                    () -> {
                        app.addPreviewEffect(effect);
                        hideBottomPanel();
                    }));
            row.add(new UIText(effect.name));
            row.add(new CountContainer(
                    () -> app.getPreviewEffects()
                            .stream()
                            .filter(e -> e.getEffect() == effect)
                            .count()));
            available.add(row);
        }
        bottom.add(available.addScrollbars());
        add(bottom);
    }

    private void showBottomPanel() {
        bottomPanelVisible = true;
    }

    private void hideBottomPanel() {
        bottomPanelVisible = false;
    }

    private class ActiveEffectsContainer extends UIContainer {

        public ActiveEffectsContainer() {
            super(VERTICAL, CENTER, TOP, VERTICAL);
            setHFillSize();
            // setMarginScale(2.0);
            // setPaddingScale(2.0);

            add(new UIText("No Effects Selected", UISizes.TEXT_SMALL)
                    .setColor(UIColors.TEXT_INVALID)
                    .setVisibilitySupplier(this::previewEmpty));
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
            setAlignment(previewEmpty() ? CENTER : TOP);
            super.update();
        }

        private boolean previewEmpty() {
            return app.getPreviewEffects().isEmpty();
        }

    }

    private class InstanceContainer extends UIContainer {

        final EffectInstance effect;

        public InstanceContainer(EffectInstance effect) {
            super(CENTER, HORIZONTAL);
            this.effect = effect;
            setHFillSize();
            zeroMargin();
            // noOutline();
            UIStyle.setSelectableButtonStyle(this, () -> selectedInstance == this);
            addLeftClickAction(() -> selectedInstance = this);

            // add(createButtonContainer(
            // new UILabel[] {
            // UILabel.icon("arrow_up"),
            // UILabel.icon("arrow_down")
            // },
            // new Runnable[] {
            // () -> app.movePreviewEffectUp(effect),
            // () -> app.movePreviewEffectDown(effect)
            // }));
            add(UILabel.text(effect.getEffect().name).setMarginScale(1.0));
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

        // TODO: inline this
        private UIContainer createButtonContainer(UILabel[] labels, Runnable[] actions) {
            UIContainer buttons = new UIContainer(HORIZONTAL, CENTER);
            // buttons.zeroMargin().zeroPadding().noOutline();
            buttons.zeroMargin().noOutline();
            for (int i = 0; i < labels.length; i++) {
                labels[i].setActive(() -> true);
                buttons.add(new UIButton(labels[i], actions[i]));
            }
            return buttons;
        }

    }

    private static class CountContainer extends UIContainer {

        public CountContainer(LongSupplier counter) {
            this(counter, 1000);
        }

        public CountContainer(LongSupplier counter, long max) {
            super(HORIZONTAL, CENTER);

            noOutline();
            setStyle(new UIStyle(UIColors.BACKGROUND_HIGHLIGHT, null, null).setShape(UIShape.ELLIPSE));
            setVisibilitySupplier(() -> counter.getAsLong() != 0);
            Supplier<String> textSupplier = () -> {
                long count = counter.getAsLong();
                return count < 1000 ? Long.toString(count) : "999+";
            };
            add(new UIText(textSupplier).setColor(UIColors.TEXT_INVALID));
        }

        @Override
        public void update() {
            setFixedSize(new SVector(1, 1).scale(UISizes.TEXT.get1f() * 1.5));
            super.update();
        }

    }

}
