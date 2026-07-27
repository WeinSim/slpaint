package com.weinsim.slpaint.ui.components;

import java.util.List;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import com.weinsim.slpaint.main.apps.MainApp;
import com.weinsim.slpaint.main.effects.*;
import com.weinsim.sutil.SUtil;
import com.weinsim.sutil.math.SVector;
import com.weinsim.sutil.ui.UIColors;
import com.weinsim.sutil.ui.UIShape;
import com.weinsim.sutil.ui.UISizes;
import com.weinsim.sutil.ui.UIStyle;
import com.weinsim.sutil.ui.elements.*;

public class EffectsPanel extends UIContainer {

    private final MainApp app;

    private boolean bottomPanelVisible;

    public EffectsPanel(MainApp app) {
        super(VERTICAL, CENTER);
        this.app = app;
        bottomPanelVisible = false;

        noOutline();
        setVFillSize();
        withSeparators(true);

        UIContainer top = new UIContainer(VERTICAL, CENTER);
        top.zeroMargin().noOutline();
        top.setFillSize();
        UIContainer buttonsTop = new UIContainer(HORIZONTAL, CENTER);
        buttonsTop.zeroMargin().noOutline();
        buttonsTop.setHFillSize();
        buttonsTop.add(new UIText("Active Effects", UISizes.TEXT_SMALL));
        buttonsTop.add(new UIContainer(0, 0).setHFillSize().zeroMargin().noOutline());
        buttonsTop.add(new UIButton(
                UILabel.icon("close").small(),
                () -> app.removeAllPreviewEffects())
                .setVisibilitySupplier(() -> !app.getPreviewEffects().isEmpty()));
        top.add(buttonsTop);
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
            boolean previewEmpty = previewEmpty();
            setAlignment(previewEmpty ? CENTER : TOP);
            setMarginScale(previewEmpty ? 3 : 1);
            super.update();
        }

        private boolean previewEmpty() {
            return app.getPreviewEffects().isEmpty();
        }

    }

    private class InstanceContainer extends UIContainer {

        final EffectInstance effect;

        public InstanceContainer(EffectInstance effect) {
            super(VERTICAL, CENTER);
            this.effect = effect;

            setHFillSize();
            zeroMargin();
            zeroPadding();
            setStyle(new UIStyle(UIColors.BACKGROUND_2, null, null));

            UIContainer topRow = new UIContainer(HORIZONTAL, CENTER);
            topRow.zeroMargin().noOutline();
            topRow.setHFillSize();
            topRow.add(UILabel.text(effect.getEffect().name).setMarginScale(1.0));
            topRow.add(new UIContainer(0, 0).setHFillSize().zeroMargin().noOutline());
            UILabel[] labels = {
                    UILabel.icon("arrow_up"),
                    UILabel.icon("arrow_down"),
                    UILabel.icons("eye_open", "eye_closed", effect::isVisible).setActive(() -> true),
                    UILabel.icon("trash_can")
            };
            Runnable[] actions = {
                    () -> app.movePreviewEffectUp(effect),
                    () -> app.movePreviewEffectDown(effect),
                    () -> effect.toggleVisibility(),
                    () -> app.removePreviewEffect(effect)
            };
            UIContainer buttons = new UIContainer(HORIZONTAL, CENTER);
            buttons.zeroMargin().noOutline();
            buttons.zeroPadding();
            for (int i = 0; i < actions.length; i++)
                buttons.add(new UIButton(labels[i].small(), actions[i]));
            topRow.add(buttons);
            // topRow.add(new UIButton(
            // UILabel.icon("arrow_up"),
            // () -> app.movePreviewEffectUp(effect)));
            // topRow.add(new UIButton(
            // UILabel.icon("arrow_down"),
            // () -> app.movePreviewEffectDown(effect)));
            // topRow.add(new UIButton(
            // UILabel.icons("eye_open", "eye_closed", effect::isVisible).setActive(() ->
            // true),
            // () -> effect.toggleVisibility()));
            // topRow.add(new UIButton(
            // UILabel.icon("trash_can"),
            // () -> app.removePreviewEffect(effect)));
            add(topRow);
            UIContainer bottomRow = new UIContainer(HORIZONTAL, CENTER);
            // bottomRow.zeroMargin().noOutline();
            bottomRow.noOutline();
            bottomRow.setHFillSize();
            switch (effect) {
                case Contrast contrast -> {
                    // a bit of jank logic for the contrast scale
                    final double contrastMin = 0,
                            contrastMax = 20;
                    final double exponent = 2;
                    bottomRow.add(new UIScale(HORIZONTAL,
                            () -> Math.pow(
                                    SUtil.map(contrast.getMultiplier(), contrastMin, contrastMax, 0, 1),
                                    1 / exponent),
                            s -> contrast.setMultiplier(
                                    SUtil.map(Math.pow(s, exponent), 0, 1, contrastMin, contrastMax))));
                    final double scale = 100;
                    bottomRow.add(new UINumberInput(
                            () -> (int) Math.round(contrast.getMultiplier() * scale),
                            m -> contrast.setMultiplier(m / scale)));
                    bottomRow.add(new UIText("%"));
                    add(bottomRow);
                }
                case Brightness brightness -> {
                    bottomRow.add(new UIScale(HORIZONTAL,
                            () -> SUtil.map(brightness.getBrightness(),
                                    Brightness.MIN_BRIGHTNESS, Brightness.MAX_BRIGHTNESS,
                                    0, 1),
                            s -> brightness.setBrightness(
                                    (int) SUtil.map(s,
                                            0, 1,
                                            Brightness.MIN_BRIGHTNESS, Brightness.MAX_BRIGHTNESS))));
                    bottomRow.add(new UINumberInput(
                            () -> Math.round(brightness.getBrightness()),
                            b -> brightness.setBrightness(b)));
                    add(bottomRow);
                }
                case Resize _,BlackWhite _ -> {
                }
                default -> {
                    String name = effect.getEffect().name();
                    add(new UIText(String.format("[unable to load UI for %s]", name)));
                }
            }
        }

    }

    private static class CountContainer extends UIContainer {

        public CountContainer(LongSupplier counter) {
            this(counter, 999);
        }

        public CountContainer(LongSupplier counter, long max) {
            super(HORIZONTAL, CENTER);

            noOutline();
            setStyle(new UIStyle(UIColors.BACKGROUND_HIGHLIGHT, null, null).setShape(UIShape.ELLIPSE));
            setVisibilitySupplier(() -> counter.getAsLong() != 0);
            Supplier<String> textSupplier = () -> {
                long count = counter.getAsLong();
                return count < max ? Long.toString(count) : String.format("%d+", max);
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
