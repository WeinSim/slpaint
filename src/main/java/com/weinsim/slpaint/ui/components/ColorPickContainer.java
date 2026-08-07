package com.weinsim.slpaint.ui.components;

import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import com.weinsim.slpaint.main.ColorPicker;
import com.weinsim.slpaint.main.apps.App;
import com.weinsim.slpaint.main.apps.MainApp;
import com.weinsim.sutil.color.Color;
import com.weinsim.sutil.math.SVector;
import com.weinsim.sutil.ui.UIColors;
import com.weinsim.sutil.ui.UISizes;
import com.weinsim.sutil.ui.UIStyle;
import com.weinsim.sutil.ui.elements.UIButton;
import com.weinsim.sutil.ui.elements.UIContainer;
import com.weinsim.sutil.ui.elements.UINumberInput;
import com.weinsim.sutil.ui.elements.UIScale;
import com.weinsim.sutil.ui.elements.UIText;

public class ColorPickContainer extends UIContainer {

    // private static final String[] RGB_NAMES = { "Red", "Green", "Blue" };
    // private static final String[] HSL_NAMES = { "Hue", "Sat", "Light" };
    private static final String[] RGB_NAMES = { "R", "G", "B" };
    private static final String[] HSL_NAMES = { "H", "S", "L" };
    private static final String[] HSV_NAMES = { "H", "S", "V" };

    private final ColorPicker colorPicker;

    public ColorPickContainer(ColorPicker colorPicker, Consumer<Color> buttonAction, int orientation, boolean addAlpha) {
        this(colorPicker, buttonAction, orientation, addAlpha, null);
    }

    public ColorPickContainer(ColorPicker colorPicker, Consumer<Color> buttonAction, int orientation, boolean addAlpha,
            Color initialColor) {

        super(orientation, orientation == VERTICAL ? CENTER : TOP);
        this.colorPicker = colorPicker;
        setPaddingScale(2.0);

        UIContainer row1 = createRow1();
        UIContainer row2 = addAlpha ? createRow2() : null;
        UIContainer row3 = createRow3(initialColor);
        UIContainer row4 = createRow4(buttonAction);

        add(row1);
        if (orientation == VERTICAL) {
            if (addAlpha) {
                add(row2);
            }
            add(row3);
            add(row4);
        } else {
            UIContainer right = new UIContainer(VERTICAL, CENTER);
            right.setPaddingScale(2.0);
            if (addAlpha) {
                right.add(row2);
            }
            right.add(row3);
            right.add(row4);
            add(right);
        }
    }

    private UIContainer createRow1() {
        UIContainer row1 = new UIContainer(HORIZONTAL, TOP);
        row1.setPaddingScale(2);
        HueSatField hueSatField = new HueSatField(colorPicker);
        row1.add(hueSatField);
        UIScale lightnessScale = new LightnessScale(VERTICAL, colorPicker);
        row1.add(lightnessScale);
        return row1;
    }

    private UIContainer createRow2() {
        UIContainer row2 = new UIContainer(HORIZONTAL, CENTER);
        row2.setHFillSize();

        row2.add(new UIText("Alpha:"));

        UINumberInput alphaInput = new UINumberInput(colorPicker::getAlpha, colorPicker::setAlpha);
        row2.add(alphaInput);

        UIScale alphaScale = new AlphaScale(HORIZONTAL, colorPicker);
        row2.add(alphaScale);
        return row2;
    }

    private UIContainer createRow3(Color initialColor) {
        UIContainer row3 = new UIContainer(HORIZONTAL, CENTER);
        row3.setHFillSize();

        UIContainer hslInput = new UIContainer(VERTICAL, RIGHT);
        hslInput.setVisibilitySupplier(App::isHSLColorSpace);
        for (int i = 0; i < HSL_NAMES.length; i++) {
            UIContainer colorRow = new UIContainer(HORIZONTAL, CENTER);
            colorRow.add(new UIText(HSL_NAMES[i] + ":"));
            IntSupplier getter = switch (i) {
                case 0 -> () -> (int) colorPicker.getHue();
                case 1 -> () -> (int) (colorPicker.getHSLSaturation() * 100);
                case 2 -> () -> (int) (colorPicker.getLightness() * 100);
                default -> null;
            };
            IntConsumer setter = switch (i) {
                case 0 -> hue -> colorPicker.setHue(hue);
                case 1 -> saturation -> colorPicker.setHSLSaturation(saturation / 100.0);
                case 2 -> lightness -> colorPicker.setLightness(lightness / 100.0);
                default -> null;
            };
            UINumberInput colorInput = new UINumberInput(getter, setter);
            colorRow.add(colorInput);
            hslInput.add(colorRow);
        }
        row3.add(hslInput);

        UIContainer hsvInput = new UIContainer(VERTICAL, RIGHT);
        hsvInput.setVisibilitySupplier(() -> !MainApp.isHSLColorSpace());
        for (int i = 0; i < HSV_NAMES.length; i++) {
            UIContainer colorRow = new UIContainer(HORIZONTAL, CENTER);
            colorRow.add(new UIText(HSV_NAMES[i] + ":"));
            IntSupplier getter = switch (i) {
                case 0 -> () -> (int) colorPicker.getHue();
                case 1 -> () -> (int) (colorPicker.getHSVSaturation() * 100);
                case 2 -> () -> (int) (colorPicker.getValue() * 100);
                default -> null;
            };
            IntConsumer setter = switch (i) {
                case 0 -> hue -> colorPicker.setHue(hue);
                case 1 -> saturation -> colorPicker.setHSVSaturation(saturation / 100.0);
                case 2 -> value -> colorPicker.setValue(value / 100.0);
                default -> null;
            };
            UINumberInput colorInput = new UINumberInput(getter, setter);
            colorRow.add(colorInput);
            hsvInput.add(colorRow);
        }
        row3.add(hsvInput);

        UIContainer rgbInput = new UIContainer(VERTICAL, RIGHT);
        for (int i = 0; i < RGB_NAMES.length; i++) {
            UIContainer colorRow = new UIContainer(HORIZONTAL, CENTER);
            colorRow.add(new UIText(RGB_NAMES[i] + ":"));
            IntSupplier getter = switch (i) {
                case 0 -> colorPicker::getRed;
                case 1 -> colorPicker::getGreen;
                case 2 -> colorPicker::getBlue;
                default -> null;
            };
            IntConsumer setter = switch (i) {
                case 0 -> colorPicker::setRed;
                case 1 -> colorPicker::setGreen;
                case 2 -> colorPicker::setBlue;
                default -> null;
            };
            UINumberInput colorInput = new UINumberInput(getter, setter);
            colorRow.add(colorInput);
            rgbInput.add(colorRow);
        }
        row3.add(rgbInput);

        row3.addFill(false);

        UIContainer colorPreview = new UIContainer(VERTICAL, CENTER);
        UIContainer colorBox = new UIContainer(HORIZONTAL, CENTER);
        colorBox.setStyle(new UIStyle(null, UIColors.HIGHLIGHT, () -> 2.0));
        colorBox.zeroPadding();
        colorBox.setHFillSize();
        Supplier<SVector> previewSize = () -> {
            SVector size = UISizes.COLOR_PICKER_PREVIEW.get2f();
            if (initialColor != null) {
                size.x /= 2;
            }
            return size;
        };
        for (int i = initialColor == null ? 1 : 0; i < 2; i++) {
            Supplier<Color> bgColorSupplier = i == 0 ? () -> initialColor : colorPicker::getColor;
            colorBox.add(new UIColorElement(bgColorSupplier, previewSize, false));
        }
        colorPreview.add(colorBox);
        colorPreview.add(new UIText("Preview", UIText.SMALL));
        row3.add(colorPreview);
        return row3;
    }

    private UIContainer createRow4(Consumer<Color> buttonAction) {
        UIButton customColor = new UIButton("+ Add to Custom Colors", () -> buttonAction.accept(colorPicker.getColor()));
        customColor.setAlignment(CENTER);
        customColor.setHFillSize();
        return customColor;
    }

}
