package com.weinsim.slpaint.ui.components;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import org.lwjglx.util.vector.Vector4f;

import com.weinsim.slpaint.main.ColorPicker;
import com.weinsim.slpaint.main.apps.App;
import com.weinsim.slpaint.main.apps.MainApp;
import com.weinsim.sutil.SUtil;
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

    private ColorPicker colorPicker;

    public ColorPickContainer(ColorPicker colorPicker, IntConsumer buttonAction, int orientation, boolean addAlpha,
            boolean addPreview) {

        super(orientation, orientation == VERTICAL ? CENTER : TOP);
        this.colorPicker = colorPicker;

        zeroMargin().noOutline();

        paddingScale = 2;

        UIContainer row1 = createRow1();
        UIContainer row2 = addAlpha ? createRow2() : null;
        UIContainer row3 = createRow3(addPreview);
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
            right.zeroMargin().setPaddingScale(2.0).noOutline();
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
        row1.zeroMargin().noOutline();
        HueSatField hueSatField = new HueSatField(colorPicker);
        row1.add(hueSatField);
        UIScale lightnessScale = new LightnessScale(VERTICAL, colorPicker);
        row1.add(lightnessScale);
        return row1;
    }

    private UIContainer createRow2() {
        UIContainer row2 = new UIContainer(HORIZONTAL, CENTER);
        row2.zeroMargin().noOutline();
        row2.setHFillSize();

        row2.add(new UIText("Alpha:"));

        IntSupplier getter = () -> SUtil.alpha(colorPicker.getRGB());
        IntConsumer setter = alpha -> colorPicker.setAlpha(alpha);
        UINumberInput alphaInput = new UINumberInput(getter, setter);
        row2.add(alphaInput);

        UIScale alphaScale = new AlphaScale(HORIZONTAL, colorPicker);
        row2.add(alphaScale);
        return row2;
    }

    private UIContainer createRow3(boolean addPreview) {
        UIContainer row3 = new UIContainer(HORIZONTAL, CENTER);
        row3.zeroMargin().noOutline();
        row3.setHFillSize();

        UIContainer hslInput = new UIContainer(VERTICAL, RIGHT);
        hslInput.zeroMargin().noOutline();
        hslInput.setVisibilitySupplier(App::isHSLColorSpace);
        for (int i = 0; i < HSL_NAMES.length; i++) {
            UIContainer colorRow = new UIContainer(HORIZONTAL, CENTER);
            colorRow.zeroMargin().noOutline();
            colorRow.add(new UIText(HSL_NAMES[i] + ":"));
            IntSupplier getter = switch (i) {
                case 0 -> () -> (int) colorPicker.getHue();
                case 1 -> () -> (int) (colorPicker.getHSLSaturation() * 100);
                case 2 -> () -> (int) (colorPicker.getLightness() * 100);
                default -> null;
            };
            IntConsumer setter = switch (i) {
                case 0 -> hue -> colorPicker.setHSLHue(hue);
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
        hsvInput.zeroMargin().noOutline();
        hsvInput.setVisibilitySupplier(() -> !MainApp.isHSLColorSpace());
        for (int i = 0; i < HSV_NAMES.length; i++) {
            UIContainer colorRow = new UIContainer(HORIZONTAL, CENTER);
            colorRow.zeroMargin().noOutline();
            colorRow.add(new UIText(HSV_NAMES[i] + ":"));
            IntSupplier getter = switch (i) {
                case 0 -> () -> (int) colorPicker.getHue();
                case 1 -> () -> (int) (colorPicker.getHSVSaturation() * 100);
                case 2 -> () -> (int) (colorPicker.getValue() * 100);
                default -> null;
            };
            IntConsumer setter = switch (i) {
                case 0 -> hue -> colorPicker.setHSVHue(Math.clamp(hue, 0, 360));
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
        rgbInput.zeroMargin().noOutline();
        for (int i = 0; i < RGB_NAMES.length; i++) {
            UIContainer colorRow = new UIContainer(HORIZONTAL, CENTER);
            colorRow.zeroMargin().noOutline();
            colorRow.add(new UIText(RGB_NAMES[i] + ":"));
            IntSupplier getter = switch (i) {
                case 0 -> () -> SUtil.red(colorPicker.getRGB());
                case 1 -> () -> SUtil.green(colorPicker.getRGB());
                case 2 -> () -> SUtil.blue(colorPicker.getRGB());
                default -> null;
            };
            final int j = i;
            IntConsumer setter = component -> {
                component = Math.clamp(component, 0, 255);
                int color = colorPicker.getRGB();
                int shiftAmount = 8 * (2 - j);
                int mask = 0xFF << shiftAmount;
                color &= ~mask;
                color |= component << shiftAmount;
                colorPicker.setRGB(color);
            };
            UINumberInput colorInput = new UINumberInput(getter, setter);
            colorRow.add(colorInput);
            rgbInput.add(colorRow);
        }
        row3.add(rgbInput);

        UIContainer gap = new UIContainer(0, 0);
        gap.zeroMargin().noOutline();
        gap.setHFillSize();
        row3.add(gap);

        UIContainer colorPreview = new UIContainer(VERTICAL, CENTER);
        colorPreview.zeroMargin().noOutline();
        UIContainer colorBox = new UIContainer(HORIZONTAL, 0);
        colorBox.setStyle(new UIStyle(() -> null, UIColors.HIGHLIGHT, () -> 2.0));
        colorBox.zeroMargin().zeroPadding().noOutline();
        Supplier<SVector> previewSize = () -> {
            SVector size = UISizes.COLOR_PICKER_PREVIEW.getWidthHeight();
            if (addPreview) {
                size.x /= 2;
            }
            return size;
        };
        for (int i = addPreview ? 0 : 1; i < 2; i++) {
            Supplier<Vector4f> bgColorSupplier = i == 0
                    ? () -> MainApp.toVector4f(colorPicker.getInitialColor())
                    : () -> MainApp.toVector4f(colorPicker.getRGB());
            colorBox.add(new UIColorElement(bgColorSupplier, previewSize, false));
        }
        colorPreview.add(colorBox);
        colorPreview.add(new UIText("Preview", UIText.SMALL));
        row3.add(colorPreview);
        return row3;
    }

    private UIContainer createRow4(IntConsumer buttonAction) {
        UIButton customColor = new UIButton("+ Add to Custom Colors", () -> buttonAction.accept(colorPicker.getRGB()));
        customColor.setAlignment(CENTER);
        customColor.setHFillSize();
        return customColor;
    }

}
