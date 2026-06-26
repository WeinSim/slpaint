package com.weinsim.slpaint.ui;

import static org.lwjgl.glfw.GLFW.*;

import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import org.lwjglx.util.vector.Vector4f;

import com.weinsim.slpaint.main.apps.MainApp;
import com.weinsim.slpaint.main.effects.Brightness;
import com.weinsim.slpaint.main.effects.Effect;
import com.weinsim.slpaint.main.image.Image;
import com.weinsim.slpaint.main.image.ImageFormat;
import com.weinsim.slpaint.main.tools.*;
import com.weinsim.slpaint.renderengine.font.TextFont;
import com.weinsim.slpaint.ui.components.ColorPickContainer;
import com.weinsim.slpaint.ui.components.CustomColorContainer;
import com.weinsim.slpaint.ui.components.ImageCanvas;
import com.weinsim.slpaint.ui.components.UIColorElement;
import com.weinsim.sutil.SUtil;
import com.weinsim.sutil.ui.UI;
import com.weinsim.sutil.ui.UISizes;
import com.weinsim.sutil.ui.UIStyle;
import com.weinsim.sutil.ui.elements.*;

public class MainUI extends AppUI<MainApp> {

    public static final int NUM_COLOR_BUTTONS_PER_ROW = 10;

    private String debugString = "";

    public MainUI(MainApp app) {
        super(app);
    }

    @Override
    protected void createKeyboardShortcuts() {
        super.createKeyboardShortcuts();

        // general keyboard shortcuts
        UI.addKeyboardShortcut("new", GLFW_KEY_N, GLFW_MOD_CONTROL, true, app::newImage);
        UI.addKeyboardShortcut("open", GLFW_KEY_O, GLFW_MOD_CONTROL, true, app::openImage);
        UI.addKeyboardShortcut("save", GLFW_KEY_S, GLFW_MOD_CONTROL, true, app::saveImage);
        UI.addKeyboardShortcut("save_as", GLFW_KEY_S, GLFW_MOD_CONTROL | GLFW_MOD_SHIFT, true, app::saveImageAs);
        UI.addKeyboardShortcut("undo", GLFW_KEY_Z, GLFW_MOD_CONTROL, app::canUndo, app::undo);
        UI.addKeyboardShortcut("redo", GLFW_KEY_Y, GLFW_MOD_CONTROL, app::canRedo, app::redo);
        UI.addKeyboardShortcut("reset_transform", GLFW_KEY_R, 0, false, app::resetImageTransform);
        UI.addKeyboardShortcut("zoom_in", GLFW_KEY_KP_ADD, GLFW_MOD_CONTROL, app::canZoomIn, app::zoomIn);
        UI.addKeyboardShortcut("zoom_out", GLFW_KEY_KP_SUBTRACT, GLFW_MOD_CONTROL, app::canZoomOut, app::zoomOut);
        UI.addKeyboardShortcut("reset_zoom", GLFW_KEY_0, GLFW_MOD_CONTROL, true, app::resetZoom);

        // all tool shortcuts
        for (ImageTool tool : ImageTool.INSTANCES) {
            tool.setApp(app);
            tool.createKeyboardShortcuts();
        }
    }

    @Override
    protected void init() {
        root.setOrientation(VERTICAL).setHAlignment(LEFT).withSeparators(false);

        root.add(createMenuBar());

        // UIContainer mainRow = new UIContainer(HORIZONTAL, TOP);
        // mainRow.withSeparators(false).noOutline();
        // mainRow.setFillSize();
        // mainRow.add(createSidePanel());
        // UIContainer mainArea = new UIContainer(VERTICAL, TOP);
        // mainArea.withSeparators(false).noOutline();
        // mainArea.setFillSize();
        // mainArea.add(createToolRow());
        // mainArea.add(new ImageCanvas(VERTICAL, RIGHT, TOP, app));
        // mainRow.add(mainArea);
        // root.add(mainRow);

        root.add(createToolRow());
        UIContainer mainRow = new UIContainer(HORIZONTAL, TOP);
        mainRow.withSeparators(false).noOutline();
        mainRow.setFillSize();
        mainRow.add(createSidePanel());
        mainRow.add(new ImageCanvas(VERTICAL, RIGHT, TOP, app));
        root.add(mainRow);

        UIContainer statusBar = createStatusBar();

        root.add(statusBar);
    }

    private UIMenuBar createMenuBar() {
        UIMenuBar menuBar = new UIMenuBar();

        UIFloatMenu fileMenu = menuBar.addMenu("File");
        fileMenu.addLabel("New", getKeyboardShortcut("new"));
        fileMenu.addLabel("Open", getKeyboardShortcut("open"));
        fileMenu.addLabel("Save", getKeyboardShortcut("save"));
        fileMenu.addLabel("Save As", getKeyboardShortcut("save_as"));
        fileMenu.addSeparator();
        fileMenu.addLabel("Settings", () -> app.showDialog(MainApp.SETTINGS_DIALOG));
        fileMenu.addSeparator();
        fileMenu.addLabel("Quit", app::requestClose);

        UIFloatMenu editMenu = menuBar.addMenu("Edit");
        editMenu.addLabel("Undo", getKeyboardShortcut("undo"));
        editMenu.addLabel("Redo", getKeyboardShortcut("redo"));

        UIFloatMenu selectionMenu = menuBar.addMenu("Selection");
        selectionMenu.addLabel("Select everything", getKeyboardShortcut("select_all"));
        selectionMenu.addSeparator();
        selectionMenu.addLabel("Copy", getKeyboardShortcut("copy"));
        selectionMenu.addLabel("Cut", getKeyboardShortcut("cut"));
        selectionMenu.addLabel("Paste", getKeyboardShortcut("paste"));
        selectionMenu.addSeparator();
        selectionMenu.addLabel("Crop image to selection", getKeyboardShortcut("crop_to_selection"));
        selectionMenu.addSeparator();
        final SelectionTool selection = ImageTool.SELECTION;
        final BooleanSupplier selectionActive = () -> selection.getState() == SelectionTool.IDLE;
        addRotateFlipMenues(selectionMenu, selectionActive, selection::rotateRight, selection::rotateLeft,
                selection::rotate180, selection::flipHorizontal, selection::flipVertical);

        UIFloatMenu viewMenu = menuBar.addMenu("View");
        viewMenu.addLabel("Zoom in", getKeyboardShortcut("zoom_in"));
        viewMenu.addLabel("Zoom out", getKeyboardShortcut("zoom_out"));
        viewMenu.addLabel("Reset zoom", getKeyboardShortcut("reset_zoom"));
        viewMenu.addLabel("Reset view", getKeyboardShortcut("reset_transform"));

        UIFloatMenu imageMenu = menuBar.addMenu("Image");
        imageMenu.addLabel(UILabel.iconText("resize", "Resize"), () -> app.showDialog(MainApp.RESIZE_DIALOG));
        imageMenu.addLabel(UILabel.iconText("crop", "Crop"), () -> app.showDialog(MainApp.CROP_DIALOG));
        imageMenu.addSeparator();
        addRotateFlipMenues(imageMenu, () -> true, app::rotateImageRight, app::rotateImageLeft, app::rotateImage180,
                app::flipImageHorizontal, app::flipImageVertical);

        if (MainApp.DEV_BUILD) {
            UIFloatMenu debugMenu = menuBar.addMenu("Debug");
            debugMenu.addLabel("Toggle element outline", getKeyboardShortcut("cycle_debug"));
            debugMenu.addLabel("Reload shaders", getKeyboardShortcut("reload_shaders"));
            debugMenu.addLabel("Reload UI", getKeyboardShortcut("reload_ui"));
        }

        UIFloatMenu helpMenu = menuBar.addMenu("Help");
        helpMenu.addLabel("About", () -> app.showDialog(MainApp.ABOUT_DIALOG));

        return menuBar;
    }

    private UIContainer createToolRow() {
        UIContainer toolRow = new UIContainer(HORIZONTAL, LEFT, CENTER, HORIZONTAL);
        toolRow.withSeparators(true).setHFillSize().setHAlignment(LEFT).withBackground().noOutline();

        UIContainer fileButtons = addToolRowSection(toolRow, "File");
        fileButtons.add(new UIButton(UILabel.icons("undo", "undo_inactive", app::canUndo), app::undo));
        fileButtons.add(new UIButton(UILabel.icons("redo", "redo_inactive", app::canRedo), app::redo));
        fileButtons.add(new UISeparator());
        fileButtons.add(new UIButton(UILabel.icon("new"), app::newImage));
        fileButtons.add(new UIButton(UILabel.icon("open"), app::openImage));
        fileButtons.add(new UIButton(UILabel.icon("save"), app::saveImage));

        UIContainer imageOptions = addToolRowSection(toolRow, "Image");
        imageOptions.add(new UIButton(
                UILabel.iconText("resize", "Resize"),
                () -> app.showDialog(MainApp.RESIZE_DIALOG)));
        UIDropdown[] imageRotateFlipDropdowns = createRotateFlipDropdowns(() -> true, app::rotateImageRight,
                app::rotateImageLeft, app::rotateImage180, app::flipImageHorizontal, app::flipImageVertical);
        imageOptions.add(imageRotateFlipDropdowns[0]);
        imageOptions.add(imageRotateFlipDropdowns[1]);

        UIContainer toolbox = addToolRowSection(toolRow, "Tools");
        toolbox.setOrientation(VERTICAL);
        final int toolsPerRow = ImageTool.INSTANCES.length;
        UIContainer currentToolRow = null;
        for (int i = 0; i < ImageTool.INSTANCES.length; i++) {
            if (i % toolsPerRow == 0) {
                currentToolRow = new UIContainer(HORIZONTAL, CENTER);
                currentToolRow.zeroMargin().noOutline();
            }
            ImageTool tool = ImageTool.INSTANCES[i];
            String iconName = String.format("%s_tool", tool.getName().toLowerCase());
            UIButton toolButton = new UIButton(UILabel.icon(iconName), () -> app.setActiveTool(tool));
            UIStyle.setSelectableButtonStyle(toolButton, () -> app.getActiveTool() == tool);
            toolButton.setHandCursor();
            currentToolRow.add(toolButton);
            if ((i + 1) % toolsPerRow == 0 || i == ImageTool.INSTANCES.length - 1)
                toolbox.add(currentToolRow);
        }

        // Used for pencil size and line size
        UIContainer sizeTools = new UIContainer(VERTICAL, CENTER);
        sizeTools.zeroMargin().noOutline();
        sizeTools.setVisibilitySupplier(() -> switch (app.getActiveTool()) {
            case PencilTool _,LineTool _ -> true;
            default -> false;
        });

        IntSupplier sizeSupplier = () -> switch (app.getActiveTool()) {
            case PencilTool _ -> ImageTool.PENCIL.getSize();
            case LineTool _ -> ImageTool.LINE.getSize();
            default -> 0;
        };
        IntConsumer sizeConsumer = i -> {
            switch (app.getActiveTool()) {
                case PencilTool _ -> ImageTool.PENCIL.setSize(i);
                case LineTool _ -> ImageTool.LINE.setSize(i);
                default -> {
                }
            }
        };

        final double min = PencilTool.MIN_SIZE,
                max = PencilTool.MAX_SIZE;
        UIScale sizeScale = new UIScale(HORIZONTAL,
                () -> SUtil.map(sizeSupplier.getAsInt(), min, max, 0, 1),
                x -> sizeConsumer.accept((int) Math.round(SUtil.map(x, 0, 1, min, max))));
        sizeTools.add(sizeScale);

        UIContainer sizeBottomRow = new UIContainer(HORIZONTAL, CENTER);
        sizeBottomRow.zeroMargin().noOutline();
        sizeBottomRow.add(new UIText("Size:", UIText.SMALL));
        sizeBottomRow.add(new UIContainer(0, 0).setHFillSize().noOutline());
        sizeBottomRow.add(createIntPicker(sizeSupplier, sizeConsumer));

        sizeTools.add(sizeBottomRow);
        toolRow.add(sizeTools);

        UIToggleList pencilTools = new UIToggleList("Apply transparency",
                ImageTool.PENCIL::isApplyTransparency,
                ImageTool.PENCIL::setApplyTransparency);
        toolRow.add(pencilTools);

        UIContainer selectionTools = new UIContainer(VERTICAL, CENTER);
        selectionTools.zeroMargin().setPaddingScale(2.0).noOutline();
        selectionTools.setVisibilitySupplier(() -> app.getActiveTool() == ImageTool.SELECTION);
        UIContainer selectionToolsTop = new UIContainer(HORIZONTAL, CENTER);
        selectionToolsTop.zeroMargin().setPaddingScale(2.0).noOutline();

        final SelectionTool selection = ImageTool.SELECTION;
        final BooleanSupplier selectionActive = () -> selection.getState() == SelectionTool.IDLE;
        UIContainer selectionButtons = new UIContainer(HORIZONTAL, LEFT);
        selectionButtons.zeroMargin().noOutline();
        UIDropdown[] selectionRotateFlipDropdowns = createRotateFlipDropdowns(selectionActive, selection::rotateRight,
                selection::rotateLeft, selection::rotate180, selection::flipHorizontal, selection::flipVertical);
        selectionButtons.add(selectionRotateFlipDropdowns[0]);
        selectionButtons.add(selectionRotateFlipDropdowns[1]);
        selectionToolsTop.add(selectionButtons);

        UIToggleList selectionToggles = new UIToggleList();
        selectionToggles.setOrientation(HORIZONTAL).setPaddingScale(2.0);
        selectionToggles.addToggle("Transparent selection",
                MainApp::isTransparentSelection,
                MainApp::setTransparentSelection);
        selectionToggles.addToggle("Lock aspect ratio",
                MainApp::isLockSelectionRatio,
                MainApp::setLockSelectionRatio);
        selectionToolsTop.add(selectionToggles);

        selectionTools.add(selectionToolsTop);
        selectionTools.add(new UIText("Selection", UISizes.TEXT_SMALL));
        toolRow.add(selectionTools);

        UIContainer textTools = new UIContainer(HORIZONTAL, CENTER);
        textTools.zeroMargin().setVFillSize().noOutline();
        textTools.setVisibilitySupplier(() -> app.getActiveTool() == ImageTool.TEXT);
        textTools.setPaddingScale(2);

        UIContainer textSizeContainer = new UIContainer(VERTICAL, CENTER);
        textSizeContainer.zeroMargin().setPaddingScale(2).setVFillSize().noOutline();
        textSizeContainer.add(createIntPicker(ImageTool.TEXT::getSize, ImageTool.TEXT::setSize));
        textSizeContainer.add(new UIText("Size", UISizes.TEXT_SMALL));
        textTools.add(textSizeContainer);

        UIContainer textFontContainer = new UIContainer(VERTICAL, CENTER);
        textFontContainer.zeroMargin().setPaddingScale(2).setVFillSize().noOutline();
        UIContainer textFontRow1 = new UIContainer(HORIZONTAL, CENTER);
        textFontRow1.zeroMargin().noOutline();
        // textFontRow1.add(new UIDropdown(
        // TextTool.FONT_NAMES,
        // () -> 0,
        // _ -> {
        // }, true));
        textFontRow1.add(new UIText(TextFont::getCurrentFontName));
        textFontContainer.add(textFontRow1);
        textFontContainer.add(new UIText("Font", UISizes.TEXT_SMALL));
        textTools.add(textFontContainer);

        toolRow.add(textTools);

        return toolRow.addScrollbars();
    }

    private UIContainer addToolRowSection(UIContainer toolRow, String name) {
        return addToolRowSection(toolRow, name, null);
    }

    private UIContainer addToolRowSection(UIContainer toolRow, String name, BooleanSupplier visibilitySupplier) {
        UIContainer options = new UIContainer(VERTICAL, CENTER);
        options.zeroMargin().noOutline();
        options.setVFillSize();
        options.setPaddingScale(2);
        // options.zeroPadding();
        if (visibilitySupplier != null)
            options.setVisibilitySupplier(visibilitySupplier);

        UIContainer optionButtons = new UIContainer(HORIZONTAL, CENTER);
        optionButtons.zeroMargin().noOutline();
        options.add(optionButtons);

        // UIContainer fill = new UIContainer(0, 0);
        // fill.setVFillSize().zeroMargin().noOutline();
        // options.add(fill);

        options.add(new UIText(name, UIText.SMALL));

        toolRow.add(options);
        return optionButtons;
    }

    private void addRotateFlipMenues(UIFloatMenu menu, BooleanSupplier active, Runnable rotateRight,
            Runnable rotateLeft, Runnable rotate180, Runnable flipHorizontal, Runnable flipVertical) {

        UIFloatMenu rotateMenu = menu.addNestedMenu(UILabel.iconText("rotate_right", "Rotate"));
        UIFloatMenu flipMenu = menu.addNestedMenu(UILabel.iconText("flip_horizontal", "Flip"));
        addRotateFlipLabels(rotateMenu, flipMenu, active, rotateRight, rotateLeft, rotate180, flipHorizontal,
                flipVertical);
    }

    private UIDropdown[] createRotateFlipDropdowns(BooleanSupplier active, Runnable rotateRight, Runnable rotateLeft,
            Runnable rotate180, Runnable flipHorizontal, Runnable flipVertical) {

        UIDropdown rotate = new UIDropdown(UILabel.iconText("rotate_right", "Rotate"));
        UIDropdown flip = new UIDropdown(UILabel.iconText("flip_horizontal", "Flip"));
        addRotateFlipLabels(rotate.getFloatMenu(), flip.getFloatMenu(), active, rotateRight, rotateLeft, rotate180,
                flipHorizontal, flipVertical);

        return new UIDropdown[] { rotate, flip };
    }

    private void addRotateFlipLabels(UIFloatMenu rotateMenu, UIFloatMenu flipMenu, BooleanSupplier active,
            Runnable rotateRight, Runnable rotateLeft, Runnable rotate180, Runnable flipHorizontal,
            Runnable flipVertical) {

        rotateMenu.addLabel(UILabel.iconText("rotate_right", "Rotate 90° right", active), rotateRight);
        rotateMenu.addLabel(UILabel.iconText("rotate_left", "Rotate 90° left", active), rotateLeft);
        rotateMenu.addLabel(UILabel.iconText("rotate_180", "Rotate 180°", active), rotate180);
        flipMenu.addLabel(UILabel.iconText("flip_horizontal", "Flip horizontally", active), flipHorizontal);
        flipMenu.addLabel(UILabel.iconText("flip_vertical", "Flip vertically", active), flipVertical);
    }

    private UIContainer createIntPicker(IntSupplier sizeGetter, IntConsumer sizeSetter) {
        UIContainer container = new UIContainer(HORIZONTAL, CENTER);
        container.zeroMargin().zeroPadding().noOutline();
        UIButton minusButton = new UIButton("-", () -> sizeSetter.accept(sizeGetter.getAsInt() - 1));
        minusButton.setCursorShape(() -> minusButton.mouseAbove() ? GLFW_POINTING_HAND_CURSOR : null);
        container.add(minusButton);
        UINumberInput textSizeInput = new UINumberInput(sizeGetter, sizeSetter);
        textSizeInput.setVFillSize();
        textSizeInput.setHAlignment(CENTER);
        container.add(textSizeInput);
        UIButton plusButton = new UIButton("+", () -> sizeSetter.accept(sizeGetter.getAsInt() + 1));
        plusButton.setCursorShape(() -> plusButton.mouseAbove() ? GLFW_POINTING_HAND_CURSOR : null);
        container.add(plusButton);
        return container;
    }

    private UIContainer createSidePanel() {
        UITabs tabs = new UITabs();
        tabs.addTab("Colors", createColorPanel());
        tabs.addTab("Effects", createEffectsPanel());
        return tabs;
    }

    private UIContainer createColorPanel() {
        UIContainer colorPanel = new UIContainer(VERTICAL, CENTER, TOP, VERTICAL);
        colorPanel.withSeparators(true).setVFillSize().withBackground().noOutline();

        UIContainer primSecColorContainer = new UIContainer(HORIZONTAL, TOP);
        primSecColorContainer.zeroMargin().setPaddingScale(1.0).noOutline();
        for (int i = 0; i < 2; i++) {
            final int index = i;
            UIContainer colorContainer = new UIContainer(VERTICAL, CENTER);
            // colorContainer.zeroMargin();
            UIStyle.setSelectableButtonStyle(colorContainer, () -> app.getColorSelection() == index);
            colorContainer.setSelectable(true);
            Supplier<Vector4f> cg = i == 0
                    ? () -> MainApp.toVector4f(app.getPrimaryColor())
                    : () -> MainApp.toVector4f(app.getSecondaryColor());
            colorContainer.add(new UIColorElement(cg, UISizes.BIG_COLOR_BUTTON));
            UIContainer textContainer = new UIContainer(VERTICAL, CENTER);
            textContainer.zeroMargin().zeroPadding().noOutline();
            textContainer.add(new UIText(i == 0 ? "Primary" : "Secondary", UISizes.TEXT_SMALL));
            textContainer.add(new UIText("Color", UISizes.TEXT_SMALL));
            colorContainer.add(textContainer);
            colorContainer.addLeftClickAction(() -> app.setColorSelection(index));
            primSecColorContainer.add(colorContainer);
        }
        colorPanel.add(primSecColorContainer);

        final double colorPaddingScale = 1.0;
        UIContainer allColors = new UIContainer(VERTICAL, LEFT);
        allColors.zeroMargin().setPaddingScale(colorPaddingScale).noOutline();
        UIContainer currentRow = null;
        for (int i = 0; i < MainApp.DEFAULT_COLORS.length; i++) {
            if (currentRow == null) {
                currentRow = new UIContainer(HORIZONTAL, CENTER);
                currentRow.zeroMargin().setPaddingScale(colorPaddingScale).noOutline();
            }

            final int colorInt = MainApp.DEFAULT_COLORS[i];
            final Vector4f color = MainApp.toVector4f(colorInt);
            UIColorElement button = new UIColorElement(() -> color, UISizes.COLOR_BUTTON);
            button.addLeftClickAction(() -> app.selectColor(colorInt));
            button.setCursorShape(() -> button.mouseAbove() ? GLFW_POINTING_HAND_CURSOR : null);
            currentRow.add(button);

            if ((i + 1) % NUM_COLOR_BUTTONS_PER_ROW == 0 || i + 1 == MainApp.DEFAULT_COLORS.length) {
                allColors.add(currentRow);
                currentRow = null;
            }
        }
        CustomColorContainer ccc = new CustomColorContainer(HORIZONTAL,
                app.getCustomColorButtonArray(),
                c -> app.selectColor(MainApp.toInt(c)));
        ccc.zeroMargin().setPaddingScale(colorPaddingScale).noOutline();
        allColors.add(ccc);
        colorPanel.add(allColors);

        colorPanel.add(new ColorPickContainer(
                app.getSelectedColorPicker(),
                app::addCustomColor,
                VERTICAL,
                true,
                false)
                .setHFillSize());

        UIContainer debugPanel = new UIContainer(VERTICAL, LEFT, TOP, VERTICAL);
        debugPanel.setFillSize().noOutline();

        // UIImage debugImage = new UIImage(() -> app.getImage().getTextureID(), new
        // SVector(200, 200));
        // debugImage.withOutline();
        // debugPanel.add(debugImage);

        debugPanel.add(new UIText("Tools"));
        for (ImageTool tool : ImageTool.INSTANCES) {
            debugPanel.add(new UIText(() -> String.format(" %s: state = %d", tool.getName(), tool.getState())));
        }
        debugPanel.add(new UIText(() -> String.format("Active tool: %s", app.getActiveTool().getName())));
        debugPanel.add(new UIText(() -> String.format(" State: %d", app.getActiveTool().getState())));
        debugPanel.add(new UIText(() -> String.format("TextTool.text: \"%s\"", ImageTool.TEXT.getText())));
        // debugPanel.add(new UIText(() -> String.format("TextTool.font: \"%s\"",
        // ImageTool.TEXT.getFont())));

        // debugPanel.add(new UIText(" "));
        // String[] lipsum = lipsum(Integer.MAX_VALUE, 3);
        // for (int i = 0; i < 20; i++) {
        // for (String line : lipsum)
        // debugPanel.add(new UIText(line, UISizes.TEXT_SMALL));
        // }

        // debugPanel.add(new UITextInput(this::getDebugString, this::setDebugString,
        // true));

        // sidePanel.add(debugPanel.addScrollbars());

        return colorPanel.addScrollbars();
    }

    private UIContainer createEffectsPanel() {
        UIContainer effects = new UIContainer(VERTICAL, CENTER);
        effects.noOutline();
        effects.withSeparators(true);
        // effects.setMarginScale(2.0);
        // effects.setPaddingScale(2.0);
        // final String[] effectNames = { "Black / White", "Contrast" };
        final Runnable[] effectActions = {
                () -> app.applyEffect(Effect.BLACK_WHITE),
                () -> app.applyEffect(Effect.CONTRAST),
                () -> app.applyEffect(Effect.BRIGHTNESS)
        };
        for (int i = 0; i < 3; i++) {
            UIContainer row = new UIContainer(HORIZONTAL, CENTER);
            row.zeroMargin().noOutline();
            row.setHFillSize();
            switch (i) {
                case 0 -> {
                    row.add(new UIText("Black / White"));
                    row.add(new UIContainer(0, 0).setHFillSize().zeroMargin().noOutline());
                }
                case 1 -> {
                    // a bit of jank logic for the contrast scale
                    UIContainer left = new UIContainer(VERTICAL, LEFT);
                    left.zeroMargin().noOutline();
                    left.setHFillSize();
                    left.add(new UIText("Contrast"));
                    final double contrastMin = 0,
                            contrastMax = 20;
                    final double exponent = 2;
                    left.add(new UIScale(HORIZONTAL,
                            () -> Math.pow(
                                    SUtil.map(Effect.CONTRAST.getMultiplier(), contrastMin, contrastMax, 0, 1),
                                    1 / exponent),
                            s -> Effect.CONTRAST.setMultiplier(
                                    SUtil.map(Math.pow(s, exponent), 0, 1, contrastMin, contrastMax))));
                    row.add(left);
                    final double scale = 100;
                    row.add(new UINumberInput(
                            () -> (int) Math.round(Effect.CONTRAST.getMultiplier() * scale),
                            m -> Effect.CONTRAST.setMultiplier(m / scale)));
                    row.add(new UIText("%"));
                }
                case 2 -> {
                    UIContainer left = new UIContainer(VERTICAL, LEFT);
                    left.zeroMargin().noOutline();
                    left.setHFillSize();
                    left.add(new UIText("Brightness"));
                    left.add(new UIScale(HORIZONTAL,
                            () -> SUtil.map(Effect.BRIGHTNESS.getBrightness(),
                                    Brightness.MIN_BRIGHTNESS, Brightness.MAX_BRIGHTNESS,
                                    0, 1),
                            s -> Effect.BRIGHTNESS.setBrightness(
                                    (int) SUtil.map(s,
                                            0, 1,
                                            Brightness.MIN_BRIGHTNESS, Brightness.MAX_BRIGHTNESS))));
                    row.add(left);
                    row.add(new UINumberInput(
                            () ->  Math.round(Effect.BRIGHTNESS.getBrightness()),
                            b -> Effect.BRIGHTNESS.setBrightness(b)));
                }
                default -> throw new RuntimeException(String.format("Missing Effect UI: %d", i));
            }
            row.add(new UIButton("->", effectActions[i]));
            effects.add(row);
        }
        return effects;
    }

    private UIContainer createStatusBar() {
        UIContainer statusBar = new UIContainer(HORIZONTAL, LEFT, CENTER);
        statusBar.withSeparators(false).withBackground().noOutline();
        statusBar.setHFillSize();
        addStatusBarLabel(statusBar, () -> {
            String ret = "Format: ";
            ImageFormat format = app.getImageFormat();
            if (format == null) {
                ret += "[no format], ";
            } else {
                ret += format.toString() + ", ";
            }
            String filename = app.getFilename();
            if (filename != null && filename.length() > 0) {
                long filesize = app.getFilesize();
                ret += "%s (%s)".formatted(filename, MainApp.formatFilesize(filesize));
            }
            return ret;
        });
        addStatusBarLabel(statusBar, () -> {
            int width, height;
            if (app.isImageResizing()) {
                width = app.getNewImageWidth();
                height = app.getNewImageHeight();
            } else {
                Image image = app.getImage();
                width = image.getWidth();
                height = image.getHeight();
            }
            return "Image Size: %d x %d px".formatted(width, height);
        });
        addStatusBarLabel(statusBar,
                () -> String.format("Selection size: %d x %d px",
                        ImageTool.SELECTION.getWidth(),
                        ImageTool.SELECTION.getHeight()))
                .setVisibilitySupplier(() -> ImageTool.SELECTION.getState() != DragTool.NONE);
        addStatusBarLabel(statusBar, () -> {
            int[] mouseImagePos = app.getMouseImagePosition();
            boolean inside = app.getImage().isInside(mouseImagePos[0], mouseImagePos[1]);
            String ret = "Mouse Position:";
            if (inside)
                ret += " %d, %d".formatted(mouseImagePos[0], mouseImagePos[1]);
            return ret;
        });

        if (MainApp.DEV_BUILD) {
            statusBar.add(new UIContainer(0, 0).setHFillSize().noOutline());
            addStatusBarLabel(statusBar, () -> String.format(
                    "%d UI elements",
                    countUIElements(UI.getRoot())));
            addStatusBarLabel(statusBar, () -> String.format(
                    "%5.3f ms update",
                    app.getAvgUpdateTime() / 1e-3));
            addStatusBarLabel(statusBar, () -> String.format("%4.1f fps", app.getFrameRate()));
            addStatusBarLabel(statusBar, () -> String.format("Frame %5d", app.getFrameCount()));
        }

        return statusBar;
    }

    private UIContainer addStatusBarLabel(UIContainer statusBar, Supplier<String> textSupplier) {
        UIContainer container = new UIContainer(HORIZONTAL, CENTER);
        container.noOutline();
        container.add(new UIText(textSupplier, UISizes.TEXT_SMALL));
        statusBar.add(container);
        return container;
    }

    public String getDebugString() {
        return debugString;
    }

    public void setDebugString(String debugString) {
        this.debugString = debugString;
    }

    private int countUIElements(UIElement element) {
        int sum = 1;
        if (element instanceof UIContainer container) {
            for (UIElement child : container.getChildren())
                sum += countUIElements(child);
        }
        return sum;
    }

}
