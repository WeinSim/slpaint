package com.weinsim.slpaint.ui;

import static org.lwjgl.glfw.GLFW.*;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import com.weinsim.slpaint.main.apps.ResizeApp;
import com.weinsim.sutil.ui.UI;
import com.weinsim.sutil.ui.elements.UIButton;
import com.weinsim.sutil.ui.elements.UIContainer;
import com.weinsim.sutil.ui.elements.UINumberInput;
import com.weinsim.sutil.ui.elements.UIRadioButtonList;
import com.weinsim.sutil.ui.elements.UIText;
import com.weinsim.sutil.ui.elements.UIToggleList;

public class ResizeUI extends AppUI<ResizeApp> {

    private int inputMode;

    public ResizeUI(ResizeApp app) {
        super(app);

        inputMode = ResizeApp.PIXELS;
    }

    @Override
    protected void createKeyboardShortcuts() {
        super.createKeyboardShortcuts();

        UI.addKeyboardShortcut("cancel", GLFW_KEY_ESCAPE, 0, false, app::requestClose);
        UI.addKeyboardShortcut("close", GLFW_KEY_ENTER, 0, true, app::done);
    }

    @Override
    protected void init() {
        root.setMinimalSize();

        root.setMarginScale(2.0);
        root.setPaddingScale(1.0);
        root.setAlignment(LEFT);

        UIRadioButtonList resizeMode = new UIRadioButtonList(
                VERTICAL,
                new String[] { "Resize (scale) image", "Crop image" },
                new String[] { "resize", "crop" },
                app::getResizeMode,
                app::setResizeMode);
        resizeMode.setMarginScale(1.0);
        root.add(resizeMode);

        UIContainer inner = new UIContainer(VERTICAL, CENTER);
        inner.setHFillSize();

        UIContainer inputMode = new UIContainer(HORIZONTAL, CENTER);
        inputMode.setHFillSize().noOutline();
        inputMode.add(new UIText("By:"));
        UIContainer radioButtons = new UIRadioButtonList(
                HORIZONTAL,
                new String[] { "Pixels", "Percentage" },
                this::getInputMode,
                this::setInputMode);
        inputMode.add(radioButtons);
        inner.add(inputMode);

        for (int i = 0; i < 2; i++) {
            UIContainer row = new UIContainer(HORIZONTAL, CENTER);
            row.setHFillSize().zeroMargin().noOutline();

            row.add(new UIText(i == 0 ? "Width:" : "Height:"));
            row.add(new UIContainer(0, 0).setHFillSize().noOutline());
            IntSupplier pixelGetter = i == 0 ? app::getWidthPixels : app::getHeightPixels,
                    percentageGetter = i == 0 ? app::getWidthPercentage : app::getHeightPercentage;
            IntConsumer pixelSetter = i == 0 ? app::setWidthPixels : app::setHeightPixels,
                    percentageSetter = i == 0 ? app::setWidthPercentage : app::setHeightPercentage;
            UINumberInput pixelInput = new UINumberInput(pixelGetter, pixelSetter),
                    percentageInput = new UINumberInput(percentageGetter, percentageSetter);

            pixelInput.setVisibilitySupplier(() -> getInputMode() == ResizeApp.PIXELS);
            percentageInput.setVisibilitySupplier(() -> getInputMode() == ResizeApp.PERCENTAGE);

            if (i == 0)
                app.setWidthInput(pixelInput);

            row.add(pixelInput);
            row.add(percentageInput);
            inner.add(row);
        }

        UIToggleList lockRatio = new UIToggleList("Lock ratio", app::isLockRatio, app::setLockRatio);
        lockRatio.setHFillSize().setMarginScale(1.0);
        inner.add(lockRatio);

        root.add(inner);

        UIContainer buttonRow = new UIContainer(HORIZONTAL, RIGHT, CENTER);
        buttonRow.setHFillSize().zeroMargin().noOutline();

        buttonRow.add(new UIButton("OK", app::done));
        buttonRow.add(new UIButton("Cancel", app::requestClose));

        root.add(buttonRow);
    }

    public int getInputMode() {
        return inputMode;
    }

    public void setInputMode(int sizeMode) {
        this.inputMode = sizeMode;
    }
}