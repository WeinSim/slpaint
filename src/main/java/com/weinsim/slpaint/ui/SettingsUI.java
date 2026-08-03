package com.weinsim.slpaint.ui;

import static org.lwjgl.glfw.GLFW.*;

import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import com.weinsim.slpaint.main.ColorPicker;
import com.weinsim.slpaint.main.apps.App;
import com.weinsim.slpaint.main.apps.MainApp;
import com.weinsim.slpaint.main.apps.SettingsApp;
import com.weinsim.slpaint.renderengine.font.TextFont;
import com.weinsim.slpaint.settings.Settings;
import com.weinsim.slpaint.ui.components.ColorPickContainer;
import com.weinsim.slpaint.ui.components.CustomColorContainer;
import com.weinsim.slpaint.ui.components.UIColorElement;
import com.weinsim.sutil.ui.UI;
import com.weinsim.sutil.ui.UISizes;
import com.weinsim.sutil.ui.elements.UIButton;
import com.weinsim.sutil.ui.elements.UIContainer;
import com.weinsim.sutil.ui.elements.UIContextMenu;
import com.weinsim.sutil.ui.elements.UIDropdown;
import com.weinsim.sutil.ui.elements.UIFloatMenu;
import com.weinsim.sutil.ui.elements.UISeparator;
import com.weinsim.sutil.ui.elements.UIText;

public class SettingsUI extends AppUI<SettingsApp> {

    public static final int NUM_UI_BASE_COLOR_BUTTONS = 10;

    private boolean colorSelectionExpanded;

    public SettingsUI(SettingsApp app) {
        super(app);

        colorSelectionExpanded = false;
    }

    @Override
    protected void createKeyboardShortcuts() {
        super.createKeyboardShortcuts();

        UI.addKeyboardShortcut("close", GLFW_KEY_ESCAPE, 0, false, app::requestClose);
    }

    @Override
    protected void init() {
        root.setMarginScale(1);
        root.setPaddingScale(1);

        UIContainer mainContainer = new UIContainer(VERTICAL, LEFT, TOP,
                BOTH);
        mainContainer.setFillSize();

        mainContainer.add(createBaseColor());
        mainContainer.add(createDropdown(
                "Theme:",
                new String[] { "Dark", "Light" },
                () -> AppUI.isDarkMode() ? 0 : 1,
                i3 -> app.setDarkMode(i3 == 0)));
        mainContainer.add(createDropdown(
                "Shape of Hue-Saturation Field:",
                new String[] { "Circle", "Square" },
                () -> App.isCircularHueSatField() ? 0 : 1,
                i2 -> App.setCircularHueSatField(i2 == 0)));
        mainContainer.add(createDropdown(
                "Color space:",
                new String[] { "HSV", "HSL" },
                () -> App.isHSLColorSpace() ? 1 : 0,
                i1 -> App.setHSLColorSpace(i1 == 1)));
        final String[] fonts = TextFont.getAvailableFonts();
        mainContainer.add(createDropdown("Font:",
                fonts,
                TextFont::getCurrentFontName,
                s -> app.queueEvent(() -> TextFont.setCurrentFontName(s))));

        if (MainApp.DEV_BUILD) {
            UIContextMenu contextMenu = new UIContextMenu(mainContainer, false);
            contextMenu.addLabel("Label 1", () -> System.out.println("Label 1"));
            contextMenu.addSeparator();
            UIFloatMenu nestedMenu = contextMenu.addNestedMenu("Nested Menu", true);
            for (int i = 0; i < 20; i++) {
                nestedMenu.addLabel(String.format("Nested %d", i), () -> {
                });
            }
        }

        root.add(mainContainer.addScrollbars());

        UIContainer bottomRow = new UIContainer(HORIZONTAL, TOP);
        bottomRow.setHFillSize().zeroMargin().noOutline();
        bottomRow.add(new UIButton("Done", app::requestClose));
        UIContainer fill = new UIContainer(0, 0);
        fill.setHFillSize().noOutline();
        bottomRow.add(fill);
        bottomRow.add(new UIButton("Reset Settings", () -> Settings.setDefaultSettings()));

        root.add(bottomRow);
    }

    private UIContainer createBaseColor() {
        UIContainer baseColor = new UIContainer(VERTICAL, LEFT);
        baseColor.withSeparators(true);

        UIContainer baseColorHeading = new UIContainer(HORIZONTAL, LEFT, CENTER);
        baseColorHeading.zeroMargin().noOutline();
        baseColorHeading.setBackgroundHighlight(true);
        baseColorHeading.setHFillSize();
        baseColorHeading.add(new UIText("UI Base Color:"));
        UIContainer gap = new UIContainer(0, 0).zeroMargin().zeroPadding();
        gap.noOutline();
        baseColorHeading.add(gap);
        UIColorElement baseColorButton = new UIColorElement(AppUI::getBaseColor, UISizes.COLOR_BUTTON);
        baseColorHeading.add(baseColorButton);
        baseColorHeading.addLeftClickAction(() -> colorSelectionExpanded = !colorSelectionExpanded);
        baseColor.add(baseColorHeading);

        UIContainer allColorsContainer = new UIContainer(HORIZONTAL, LEFT, CENTER);
        allColorsContainer.zeroMargin().setPaddingScale(2.0).noOutline();
        allColorsContainer.setVisibilitySupplier(() -> colorSelectionExpanded);

        UIContainer defaultColors = new UIContainer(VERTICAL, CENTER);
        defaultColors.zeroMargin().noOutline();
        UIContainer defaultColorContainer = new UIContainer(HORIZONTAL, CENTER);
        defaultColorContainer.zeroMargin().noOutline();
        int numDefaultColors = AppUI.getNumDefaultUIColors();
        for (int i = 0; i < numDefaultColors; i++) {
            final int j = i;
            UIColorElement button = new UIColorElement(() -> AppUI.getDefaultUIColors()[j], UISizes.COLOR_BUTTON);
            button.addLeftClickAction(() -> app.setUIColor(MainApp.toInt(AppUI.getDefaultUIColors()[j])));
            defaultColorContainer.add(button);
        }
        defaultColors.add(defaultColorContainer);
        defaultColors.add(new UIText("Default Colors", UIText.SMALL));
        allColorsContainer.add(defaultColors);

        allColorsContainer.add(new UISeparator());

        UIContainer customColors = new UIContainer(VERTICAL, CENTER);
        customColors.zeroMargin().noOutline();
        CustomColorContainer ccc = new CustomColorContainer(HORIZONTAL,
                MainApp.getCustomUIBaseColors(),
                c -> app.setUIColor(MainApp.toInt(c)));
        ccc.zeroMargin().noOutline();
        customColors.add(ccc);
        customColors.add(new UIText("Custom Colors", UIText.SMALL));
        allColorsContainer.add(customColors);

        allColorsContainer.add(new UIButton("Clear", () -> MainApp.getCustomUIBaseColors().clear()));

        baseColor.add(allColorsContainer);

        ColorPicker colorPicker = app.getColorPicker();
        ColorPickContainer colorPickContainer = new ColorPickContainer(
                colorPicker,
                MainApp::addCustomUIBaseColor,
                // UISizes.COLOR_PICKER_PANEL,
                HORIZONTAL, false, true);
        colorPickContainer.setVisibilitySupplier(() -> colorSelectionExpanded);
        baseColor.add(colorPickContainer);

        return baseColor;
    }

    private UIContainer createDropdown(String name, String[] options, IntSupplier stateSupplier,
            IntConsumer stateConsumer) {

        return createDropdown(name, new UIDropdown(options, stateSupplier, stateConsumer));
    }

    private UIContainer createDropdown(String name, String[] options, Supplier<String> nameSupplier,
            Consumer<String> nameConsumer) {

        return createDropdown(name, new UIDropdown(options, nameSupplier, nameConsumer));
    }

    private UIContainer createDropdown(String name, UIDropdown dropdown) {
        UIContainer container = new UIContainer(HORIZONTAL, CENTER);
        container.zeroMargin().noOutline();
        container.add(new UIText(name));
        container.add(dropdown);
        return container;
    }

}
