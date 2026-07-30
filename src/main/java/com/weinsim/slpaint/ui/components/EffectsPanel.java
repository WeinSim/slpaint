package com.weinsim.slpaint.ui.components;

import java.util.List;
import java.util.function.BooleanSupplier;
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
    // private boolean bottomPanelPinned;

    public EffectsPanel(MainApp app) {
        super(VERTICAL, CENTER);
        this.app = app;
        bottomPanelVisible = true;
        // bottomPanelPinned = false;

        setVFillSize();
        withSeparators(true);

        BooleanSupplier prevEffectsNotEmpty = () -> !app.getPreviewEffects().isEmpty();
        UIContainer top = new Section(
                "Active Effects",
                new UILabel[] {
                        UILabel.icon("eye_open"),
                        UILabel.icon("eye_closed"),
                        UILabel.icon("close")
                },
                new Runnable[] {
                        app::showAllPreviewEffects,
                        app::hideAllPreviewEffects,
                        app::removeAllPreviewEffects
                },
                prevEffectsNotEmpty,
                new ActiveEffectsContainer());
        // top.add(new UIButton(
        // UILabel.iconText("plus", "Add Effect"),
        // () -> bottomPanelVisible = true));
        top.add(new UIButton(
                UILabel.iconText("arrow_right", "Apply Effects", app::hasActiveEffects),
                app::applyAllPreviewEffects)
                .setVisibilitySupplier(prevEffectsNotEmpty));
        add(top);

        UIContainer available = new UIContainer(VERTICAL, LEFT, TOP, VERTICAL);
        available.withMargin().setHFillSize();
        available.withOutline();
        BooleanSupplier bottomVis = () -> bottomPanelVisible;
        available.setVisibilitySupplier(bottomVis);
        for (Effect effect : Effect.values()) {
            // skip "fake" effects (for now just Resize)
            if (!effect.userAccessible)
                continue;
            UIContainer row = new UIContainer(HORIZONTAL, CENTER);
            row.add(new UIButton(UILabel.icon("plus"),
                    () -> {
                        app.addPreviewEffect(effect);
                        // if (!bottomPanelPinned)
                        // bottomPanelVisible = false;
                    }));
            row.add(new UIText(effect.name));
            row.add(new CountContainer(
                    () -> app.getPreviewEffects().stream()
                            .filter(e -> e.getEffect() == effect)
                            .count()));
            available.add(row);
        }
        UIContainer bottom = new Section(
                "Available Effects",
                new UILabel[] {
                        // UILabel.text("P"),
                        UILabel.icons("expand_down", "expand_up", bottomVis).alwaysActive()
                },
                new Runnable[] {
                        // () -> bottomPanelPinned = !bottomPanelPinned,
                        () -> {
                            // bottomPanelPinned = false;
                            bottomPanelVisible = !bottomPanelVisible;
                        }
                },
                null,
                available) {
            @Override
            public void update() {
                super.update();
                if (bottomPanelVisible)
                    setVFillSize();
                else
                    setVMinimalSize();
            }
        };
        // bottom.setVisibilitySupplier(() -> bottomPanelVisible)
        add(bottom);
    }

    private class Section extends UIContainer {

        public Section(String title, UILabel[] buttonLabels, Runnable[] buttonActions, BooleanSupplier buttonsVisible,
                UIContainer mainArea) {

            super(VERTICAL, CENTER);
            setFillSize();
            UIContainer buttons = new UIContainer(HORIZONTAL, CENTER);
            buttons.setHFillSize();
            buttons.add(new UIText(title, UISizes.TEXT_SMALL));
            buttons.addFill(false);
            for (int i = 0; i < buttonLabels.length; i++) {
                UIButton button = new UIButton(
                        buttonLabels[i].small(),
                        buttonActions[i]);
                if (buttonsVisible != null)
                    button.setVisibilitySupplier(buttonsVisible);
                buttons.add(button);
            }
            add(buttons);
            add(mainArea.addScrollbars());
        }
    }

    private class ActiveEffectsContainer extends UIContainer {

        public ActiveEffectsContainer() {
            super(VERTICAL, CENTER, TOP, VERTICAL);
            setHFillSize();
            withOutline();

            add(new UIText("No Effects Selected", UISizes.TEXT_SMALL)
                    .setColor(UIColors.TEXT_INVALID)
                    .setVisibilitySupplier(this::previewEmpty));
        }

        @Override
        public void createChildren() {
            super.createChildren();

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
                    addFirst(container);
                    containers.remove(container);
                } else {
                    addFirst(new InstanceContainer(effect));
                }
            }
        }

        @Override
        public void update() {
            super.update();

            boolean previewEmpty = previewEmpty();
            setAlignment(previewEmpty ? CENTER : TOP);
            setMarginScale(previewEmpty ? 3 : 1);
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
            zeroPadding();
            setStyle(new UIStyle(UIColors.BACKGROUND_2, null, null));

            UIContainer topRow = new UIContainer(HORIZONTAL, CENTER);
            topRow.setHFillSize();
            topRow.add(UILabel.text(effect.getEffect().name).setMarginScale(1.0));
            topRow.addFill(false);
            UILabel[] labels = {
                    UILabel.icon("arrow_up"),
                    UILabel.icon("arrow_down"),
                    UILabel.icons("eye_open", "eye_closed", effect::isVisible).alwaysActive(),
                    UILabel.icon("trash_can")
            };
            Runnable[] actions = {
                    () -> app.movePreviewEffectUp(effect),
                    () -> app.movePreviewEffectDown(effect),
                    () -> effect.toggleVisibility(),
                    () -> app.removePreviewEffect(effect)
            };
            UIContainer buttons = new UIContainer(HORIZONTAL, CENTER);
            buttons.zeroPadding();
            for (int i = 0; i < actions.length; i++)
                buttons.add(new UIButton(labels[i].small(), actions[i]));
            topRow.add(buttons);
            add(topRow);
            UIContainer bottomRow = new UIContainer(HORIZONTAL, CENTER);
            bottomRow.withMargin();
            bottomRow.setHFillSize();
            switch (effect) {
                case Contrast contrast -> {
                    // a bit of jank logic for the contrast scale
                    final double exponent = 4;
                    bottomRow.add(new UIScale(HORIZONTAL,
                            () -> Math.pow(
                                    SUtil.map(contrast.getMultiplier(),
                                            Contrast.MIN_CONTRAST, Contrast.MAX_CONTRAST,
                                            0, 1),
                                    1 / exponent),
                            s -> contrast.setMultiplier(
                                    SUtil.map(Math.pow(s, exponent),
                                            0, 1,
                                            Contrast.MIN_CONTRAST, Contrast.MAX_CONTRAST))));
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
            super.update();
            setFixedSize(new SVector(1, 1).scale(UISizes.TEXT.get1f() * 1.5));
        }

    }

}
