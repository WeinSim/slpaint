package com.weinsim.slpaint.main.apps;

import static org.lwjgl.glfw.GLFW.*;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.lwjglx.util.vector.Vector4f;

import com.weinsim.slpaint.main.ColorArray;
import com.weinsim.slpaint.main.ColorPicker;
import com.weinsim.slpaint.main.Loader;
import com.weinsim.slpaint.main.effects.Effect;
import com.weinsim.slpaint.main.image.Image;
import com.weinsim.slpaint.main.image.ImageFormat;
import com.weinsim.slpaint.main.image.ImageManager;
import com.weinsim.slpaint.main.tools.ImageTool;
import com.weinsim.slpaint.renderengine.Window;
import com.weinsim.slpaint.renderengine.font.TextFont;
import com.weinsim.slpaint.settings.BooleanSetting;
import com.weinsim.slpaint.settings.ColorArraySetting;
import com.weinsim.slpaint.settings.Settings;
import com.weinsim.slpaint.ui.AppUI;
import com.weinsim.slpaint.ui.MainUI;
import com.weinsim.slpaint.ui.components.ImageCanvas;
import com.weinsim.sutil.SUtil;
import com.weinsim.sutil.math.SVector;
import com.weinsim.sutil.ui.UI;
import com.weinsim.sutil.ui.elements.UITextInput;

/**
 * <pre>
 * TODO continue:
 * Effects
 *   Shader effects would be cool, currently b/w takes ~60ms and contrast
 *     takes ~120ms.
 * 
 * App:
 *   Keyboard shortcuts
 *     Selecting one of the radio buttons in the resize ui and pressing enter
 *       closes the resize window. => add option for keyboard shortcut to not
 *       run if something is currently selected (similar to text input).
 *   Pencil tool
 *     Drawing with a semi-transparent color has weird artifacts because some
 *       pixels are drawn multiple times on consecutive frames, resulting in
 *       the wrong opacity
 *       => Make the pencil tool also use the temp framebuffer?
 *     Sizes 1 & 2 and 3 & 4 look the same
 *     Make sizes UI prettier
 *   Selection (/ drag tools)
 *     Shift + initial drag should force square aspect ratio (combine with
 *       analogous feature for line tool)
 *     Shift + resize should lock the aspect ratio
 *       => remove "lock aspect ratio" setting?
 *   update() takes about twice as long when a modal dialog is open
 *     Maybe because of the many long textWidth() calculations?
 *   Undoing / redoing an operation that changes the image size doesn't change
 *     the temp FBO size
 *   Transparency
 *     Selecting a semi-transparent area and pasting it over a completely
 *       transparent area messes up the pixel colors: the semi-transparent area
 *       picks up the "color" (RGB values) of the transparent background.
 *     Simply selecting a transparent area and unselecting it causes the
 *       transparency to go away. Reason: selecting the area replaces that part
 *       of the image with the secondary color. Placing the transparent
 *       selection back onto the opaque background leaves the background
 *       unaffected. What is the expected behavior here?
 *     Pixels with an alpha value of 0 are considered different if they differ
 *       in their color information. What is the expected behavior here?
 *     Pixels with an alpha value of 0 lose color information when saving and
 *       reopening. (This is a property of the .png file format that can be
 *       changed I think (?). Also, what is the expected behavior?)
 *     Pasting / drawing a half-transparent red pixel over a fully opaque green
 *       one results in brown overlap instead of yellow (see MinutePhysics
 *       video)
 *       => Add correct gamma blending? (as a setting?)
 *         Have OpenGL also do correct gamma blending?
 *   Packaging:
 *     Why does startup take so long?
 *   (When parent app closes, children should also close)
 * 
 * UI:
 *   Combine user actions (keyboard, mouse). Combine with BooleanSupplier
 *     (active / possible)
 *   UISizes:
 *     There are multiple places where I want to set a larger margin but have
 *       to akwardly divide by the default margin because only a margin scale
 *       can be set. Solution: add UIContainer.setMargin()?
 *   Change UIContainer defaults? .zeroMargin().noOutline() is used in a ton of
 *     places and should maybe be the default.
 *   UITabs: make it a bit prettier
 *   Tool cursors
 *   Text
 *     Text input
 *       Selection (with mouse / arrow keys / Ctrl+A)
 *         Copy / cut / paste (=> conflicting keyboard shortcuts with selection
 *           tool!)
 *         Shift + cursor movement
 *       Multi-line text input
 *         Would require a variable number of UITexts as children (which is
 *           currently not possible. Why?)
 *     Text wrapping
 *   Selection
 *     When nothing is currently selected, the dropdowns for rotating and
 *       flipping the selection should be made inactive.
 *     Add option to crop selection?
 *     Add precise pixel input for scaling / cropping selection like ResizeUI?
 *   Tool + undo inconsistencies:
 *     Starting a tool action (e.g. putting a tool in the IDLE state) and then
 *       pressing Ctrl+Z should cancel (not finish) the current tool.
 *       Currently it does nothing (except sometimes with selection).
 *   Icons
 *     The current icons look kind of bad in light mode
 *       -> separate icons for light and dark mode?
 *     Create icons for:
 *       Basically everything in the menu bar (cut, copy, paste, zoom)
 *   The window icon does not work (Window.setIcon())
 *   On the first frame that the UI is rendered, the root has a black background
 *     and parts of the UI are not yet visible. This is visible when opening a
 *     child app.
 *   Ctrl + 0 can cause the image to appear completely outside of the canvas
 *     Reason: the point at the center of the canvas stays fixed. For a very
 *     zoomed out image, this is likely to be outside of the image.
 *   Mouse input: tapping the touchpad triggers a mouse press event but not
 *     mouse release event (=> logic that sets leftMousePressed and
 *     rightMousePressed based on mouse press / release events is flawed.)
 *   UIMouseButtonAction: the mods parameter in mousePressed() is not used
 *   Modal dialogs
 *     Convert other small dialogs into modal dialogs? (e.g. ResizeUI)
 *     Add options for custom button labels like in JOptionPane
 *       For what?
 *   Using Tab + Enter, multiple dropdowns can be opened simultaneously
 *   Layering inconsistency: the SizeKnobs' outline appears above the dropdowns
 *     of the top row (Rotate, Flip, font selection)
 *   Make side panel collapsable?
 *   About
 *     Make hyperlinks clickable? (=> underlined text!)
 *     Title "About" looks weird. It is a bit off-center because of the "X"
 *       button
 *   Add Alt + Letter navigation options for menu bar
 *     Would require underlined letters
 *   Add tooltips (on mouse hover)
 * 
 * Rendering:
 *   Improve UITextInput cursor visibility
 *   A one-frame flicker occurs when flattening the text tool or the selection.
 *     Maybe this comes from some glEnable / glDisable transparency flag being
 *     set incorrectly.
 *   Text rendering
 *     Orange text on image has yellow edges (on the left)
 *     Generate distance map (SDF) from highres, non-anti-aliassed font texture
 *       => should allow for fonts of different sizes
 *       => glyphs aren't locked to integer positions
 *     How to handle fonts?
 *       How to handle big font sizes?
 *         Generate texture atlas using fontbm on demand?
 *         Use SDFs (either in addition to or instead of regular bitmap fonts)?
 *     Have different subdirectories for different sizes of the same font
 *     Glitchy pixels: when using Courier New (size 36), the lowecase 'u' has a
 *       diagonal line of flickering pixels going bottom-left to top-right.
 *     Text renders inconsistently: some letters are blurry and other are not.
 *       For example, using Courier New Bold with a rasterized text size of 32,
 *       the letters 'e', 'r', 'i' and 'd' are blurry, whereas 'p', 'u', 'm'
 *       and 'b' are sharp. (it seems like most blurry letters are on page 2.)
 *     Potential speedups for text rendering:
 *       Only override the parts of the text VAOs that actually change from one
 *         frame to the next
 *   Anti aliasing doesn't work despite being enabled
 *     (glfwWindowHint(GLFW_SAMPLES, 4) and glEnable(GL_MULTISAMPLE))
 *   Fix stuttering artifact when resizing windows on Linux
 *     (see https://www.glfw.org/docs/latest/window.html#window_refresh)
 *     Rename transformationMatrix to uiMatrix
 *   Maximized windows don't show up correctly on Windows 11
 *   Possible ideas for future rendering improvements:
 *     Currently, all fragment shaders are quite similarly. => Combine all
 *       fragment shaders into a single one (that gets an int containing various
 *       flags as an input)?
 *     Perhaps even combine all vertex shaders into one? (Would allow for just a
 *       single draw call, though it would probably also be a massive pain).
 *   Extras (optional):
 *     3D view
 *     Debug view
 * 
 * Backend:
 *   Proper package names / structure
 *   Make SUtil a git submodule / maven subproject?
 *   Make MainApp static?
 *   Sizes
 *     Move things that should not be part of sutil.ui into ui package
 *   GLFW key input: automatically recognize keyboard layout and remappings to
 *     avoid manual conversion between Y/Z and Esc/CapsLock (see Window.KEY_MAP)
 *   Performance: only ~50fps on Microsoft Surface
 *     Mouse movement has a huge impact: keeping the mouse still drops the
 *       update time from >10ms to <2ms. This even happens when we don't even
 *       call glfwGetCursorPos() and even when the mouse is not even about the
 *       window.
 *   Error handling
 * </pre>
 */
public final class MainApp extends App {

    /**
     * Setting {@code DEV_BUILD} to {@code true} does the following things:
     * <ul>
     * <li>Prints the total number of lines to the console on startup
     * <li>Prints "illegal" imports to the console on startup
     * <li>Prints the startup time to the console
     * <li>Adds options (keyboard shortcuts + menu bar items) for showing element
     * outlines ({@code ,}), reloading the UI ({@code Shift + R}) and and reloading
     * the shaders ({@code Shift + S})
     * <li>Disables prompting to save changes when closing the window
     * <li>Adds a test context menu to the settings window
     * </ul>
     */
    public static final boolean DEV_BUILD = true;

    /**
     * https://images.minitool.com/de.minitool.com/images/uploads/news/2022/02/microsoft-paint-herunterladen-installieren/microsoft-paint-herunterladen-installieren-1.png
     */
    public static final int[] DEFAULT_COLORS = {
            SUtil.toARGB(0),
            SUtil.toARGB(63),
            SUtil.toARGB(132, 15, 24),
            SUtil.toARGB(230, 46, 46),
            SUtil.toARGB(249, 131, 57),
            SUtil.toARGB(255, 242, 65),
            SUtil.toARGB(61, 176, 85),
            SUtil.toARGB(39, 161, 228),
            SUtil.toARGB(62, 73, 199),
            SUtil.toARGB(159, 77, 161),
            SUtil.toARGB(255),
            SUtil.toARGB(127),
            SUtil.toARGB(182, 123, 91),
            SUtil.toARGB(250, 176, 201),
            SUtil.toARGB(253, 202, 59),
            SUtil.toARGB(239, 229, 180),
            SUtil.toARGB(186, 229, 64),
            SUtil.toARGB(159, 216, 235),
            SUtil.toARGB(114, 146, 187),
            SUtil.toARGB(199, 191, 230),
    };

    public static final int NEW_DIALOG = 2, RESIZE_DIALOG = 3, NEW_COLOR_DIALOG = 4,
            SETTINGS_DIALOG = 7, CROP_DIALOG = 8, ABOUT_DIALOG = 9;

    public static final int PRIMARY_COLOR = 0, SECONDARY_COLOR = 1;
    public static final int INITIAL_PRIMARY_COLOR = SUtil.toARGB(0), INITIAL_SECONDARY_COLOR = SUtil.toARGB(255);

    public static final int MIN_IMAGE_SIZE = 1, MAX_IMAGE_SIZE = 65535;

    private static final String ABOUT_TEXT_FILE = "about.txt";
    private static final String ABOUT_TEXT;

    static {
        String about;
        try {
            about = Loader.getString(ABOUT_TEXT_FILE);
        } catch (IOException e) {
            e.printStackTrace();
            about = "[unable to load about]";
        }
        ABOUT_TEXT = about;
    }

    private static BooleanSetting transparentSelection = new BooleanSetting("transparentSelection");
    private static BooleanSetting lockSelectionRatio = new BooleanSetting("lockSelectionRatio");

    private static ColorArraySetting customUIBaseColors = new ColorArraySetting("customUIColors");

    private final ImageManager imageManager;

    // this is needed for prompting the user for unsaved changes when closing the
    // window
    private boolean canClose;

    /**
     * used for restoring the tool that was selected before the color picker
     */
    private ImageTool prevTool;
    private ImageTool activeTool;

    private UITextInput textToolInput;

    private int primaryColor;
    private int secondaryColor;
    /**
     * ColorPicker for the currently selected color
     */
    private ColorPicker selectedColorPicker;
    /**
     * 0 = primary color is selected, 1 = secondary color is selcted
     */
    private int colorSelection;
    private ColorArray customColorButtonArray;

    private ImageCanvas canvas;

    /**
     * 
     * @param initialFile The initial image file to open. If it is {@code null}, a
     *                    new image will be opened.
     */
    public MainApp(String initialFile) {
        super(1280, 720, Window.MAXIMIZED, "SLPaint");

        primaryColor = INITIAL_PRIMARY_COLOR;
        secondaryColor = INITIAL_SECONDARY_COLOR;
        colorSelection = PRIMARY_COLOR;
        selectedColorPicker = new ColorPicker(getSelectedColor());
        customColorButtonArray = new ColorArray(MainUI.NUM_COLOR_BUTTONS_PER_ROW);

        imageManager = initialFile == null ? new ImageManager(this) : new ImageManager(this, initialFile);

        setActiveTool(ImageTool.PENCIL);
        prevTool = ImageTool.PENCIL;
        for (ImageTool tool : ImageTool.INSTANCES)
            tool.setApp(this);

        // load UI
        loadUI();
    }

    @Override
    public void update(double deltaT) {
        super.update(deltaT);

        if (frameCount == 1)
            resetImageTransform();

        if (colorSelection == PRIMARY_COLOR)
            primaryColor = selectedColorPicker.getRGB();
        else
            secondaryColor = selectedColorPicker.getRGB();

        String filename = getFilename();
        boolean hasUnsavedChanges = imageManager.hasUnsavedChanges();
        if (filename == null) {
            filename = "[Unnamed]";
            hasUnsavedChanges = false;
        }
        window.setTitle(String.format("%s%s - SLPaint", hasUnsavedChanges ? "" + (char) 0x2022 + " " : "", filename));

        // update image texture
        getImage().updateOpenGLTexture();
    }

    @Override
    public boolean finish() {
        if (canClose || !imageManager.hasUnsavedChanges() || !isAskUnsavedChanges()) {
            if (super.finish()) {
                Settings.finish();
                return true;
            }
            return false;
        } else {
            (new Thread(() -> {
                if (!imageManager.checkUnsavedChanges()) {
                    canClose = true;
                    requestClose();
                }
            })).start();
            return false;
        }
    }

    @Override
    protected AppUI<?> createUI() {
        return new MainUI(this);
    }

    @Override
    public void showDialog(int dialogType) {
        super.showDialog(dialogType);

        switch (dialogType) {
            case ABOUT_DIALOG -> {
                (new Thread(() -> UI.showModalDialog("About", ABOUT_TEXT, UI.INFO_DIALOG))).start();
            }
        }
    }

    @Override
    protected App createChildApp(int dialogType) {
        return switch (dialogType) {
            case NEW_COLOR_DIALOG -> new ColorEditorApp(this, getSelectedColor());
            case SETTINGS_DIALOG -> new SettingsApp(this);
            case CROP_DIALOG -> new ResizeApp(this, ResizeApp.CROP);
            case RESIZE_DIALOG -> new ResizeApp(this, ResizeApp.SCALE);
            default -> null;
        };
    }

    /**
     * Stretches / squishes the image.
     * Not to be confused with {@link MainApp#cropImage(int, int)}.
     */
    public void resizeImage(int newWidth, int newHeight) {
        finishActiveTool();

        newWidth = Math.clamp(newWidth, MIN_IMAGE_SIZE, MAX_IMAGE_SIZE);
        newHeight = Math.clamp(newHeight, MIN_IMAGE_SIZE, MAX_IMAGE_SIZE);

        renderer.setTempFBOSize(newWidth, newHeight);
        renderer.resizeImage(getImage(), newWidth, newHeight);

        addImageSnapshot();
    }

    /**
     * Crops the image. Not to be confused with
     * {@link MainApp#resizeImage(int, int)}.
     */
    public void cropImage(int newWidth, int newHeight) {
        cropImage(0, 0, newWidth, newHeight);
    }

    /**
     * Crops the image. Not to be confused with
     * {@link MainApp#resizeImage(int, int)}.
     */
    public void cropImage(int x, int y, int newWidth, int newHeight) {
        finishActiveTool();

        Image image = getImage();
        newWidth = Math.clamp(newWidth, MIN_IMAGE_SIZE, MAX_IMAGE_SIZE);
        newHeight = Math.clamp(newHeight, MIN_IMAGE_SIZE, MAX_IMAGE_SIZE);

        image.crop(x, y, newWidth, newHeight, secondaryColor);
        renderer.setTempFBOSize(newWidth, newHeight);
        // If the top left corner of the image changes, its translation should change in
        // the opposite way such that the rest of the image stays in the same place.
        translateImage(x, y);

        addImageSnapshot();
    }

    public void rotateImageRight() {
        finishActiveTool();
        Image image = getImage();
        image.rotateRight();
        renderer.setTempFBOSize(image.getWidth(), image.getHeight());
        addImageSnapshot();
    }

    public void rotateImageLeft() {
        finishActiveTool();
        Image image = getImage();
        image.rotateLeft();
        renderer.setTempFBOSize(image.getWidth(), image.getHeight());
        addImageSnapshot();
    }

    public void rotateImage180() {
        finishActiveTool();
        getImage().rotate180();
        addImageSnapshot();
    }

    public void flipImageHorizontal() {
        finishActiveTool();
        getImage().flipHorizontal();
        addImageSnapshot();
    }

    public void flipImageVertical() {
        finishActiveTool();
        getImage().flipVertical();
        addImageSnapshot();
    }

    public void setImage(BufferedImage image) {
        getImage().setBufferedImage(image);
        renderer.setTempFBOSize(image.getWidth(), image.getHeight());
        addImageSnapshot();
    }

    public void magic(int x, int y, int mouseButton) {
        getImage().magic(x, y, mouseButton == GLFW_MOUSE_BUTTON_LEFT ? primaryColor : secondaryColor);
        addImageSnapshot();
    }

    public void renderImageToImage(Image image, int x, int y, int width, int height) {
        renderer.renderImageToImage(image, x, y, width, height, getImage());
        addImageSnapshot();
    }

    public void renderTextToImage(String text, double x, double y, double size, TextFont font) {
        renderer.renderTextToImage(text, x, y, size, toVector4f(primaryColor), font, getImage());
        addImageSnapshot();
    }

    public void drawLine(int x0, int y0, int x1, int y1, int size, int color) {
        getImage().drawLine(x0, y0, x1, y1, size, color);
    }

    public void drawLine(int x0, int y0, int x1, int y1, int size, int color, boolean ignoreAlpha) {
        getImage().drawLine(x0, y0, x1, y1, size, color, ignoreAlpha);
    }

    public void translateImage(int x, int y) {
        canvas.translateImage(new SVector(x, y));
    }

    public void finishActiveTool() {
        activeTool.finish();
    }

    public void cancelActiveTool() {
        activeTool.cancel();
    }

    public void applyEffect(Effect effect) {
        long start = System.nanoTime();
        effect.apply(getImage());
        long end = System.nanoTime();
        System.out.format("Effect \"%s\" took %.1fms\n",
                effect.getClass().getSimpleName(),
                (end - start) * 1e-6);
        effect.init();
        imageManager.addSnapshot();
    }

    public void undo() {
        cancelActiveTool();
        imageManager.undo();
    }

    public boolean canUndo() {
        return imageManager.canUndo();
    }

    public void redo() {
        cancelActiveTool();
        imageManager.redo();
    }

    public boolean canRedo() {
        return imageManager.canRedo();
    }

    public void openImage() {
        finishActiveTool();
        closeChildApp(RESIZE_DIALOG);
        imageManager.open();
    }

    public void newImage() {
        finishActiveTool();
        closeChildApp(RESIZE_DIALOG);
        Image image = getImage();
        imageManager.newImage(image.getWidth(), image.getHeight());
    }

    /**
     * Gets called when Ctrl+S is pressed
     */
    public void saveImage() {
        finishActiveTool();
        imageManager.save();
    }

    /**
     * Gets called when Ctrl+Shift+S is pressed
     * 
     */
    public void saveImageAs() {
        finishActiveTool();
        imageManager.saveAs();
    }

    public void addImageSnapshot() {
        imageManager.addSnapshot();
    }

    public Image getImage() {
        return imageManager.getImage();
    }

    public long getFilesize() {
        return imageManager.getFilesize();
    }

    public ImageFormat getImageFormat() {
        return imageManager.getSavedFormat();
    }

    public String getFilename() {
        return imageManager.getFilename();
    }

    public boolean isImageResizing() {
        return canvas.isImageResizing();
    }

    public int getNewImageWidth() {
        return canvas.getNewImageWidth();
    }

    public int getNewImageHeight() {
        return canvas.getNewImageHeight();
    }

    public static ColorArray getCustomUIBaseColors() {
        return customUIBaseColors.get();
    }

    public static void addCustomUIBaseColor(int color) {
        customUIBaseColors.get().addColor(color);
    }

    public void resetImageTransform() {
        canvas.resetImageTransform();
    }

    public void zoomIn() {
        canvas.zoomIn();
    }

    public boolean canZoomIn() {
        return canvas.canZoomIn();
    }

    public void zoomOut() {
        canvas.zoomOut();
    }

    public boolean canZoomOut() {
        return canvas.canZoomOut();
    }

    public void resetZoom() {
        canvas.resetZoom();
    }

    public double getImageZoom() {
        return canvas.getImageZoom();
    }

    public void setCanvas(ImageCanvas element) {
        canvas = element;
    }

    public ImageCanvas getCanvas() {
        return canvas;
    }

    public UITextInput getTextToolInput() {
        return textToolInput;
    }

    public void setTextToolInput(UITextInput textToolInput) {
        this.textToolInput = textToolInput;
    }

    public ColorPicker getSelectedColorPicker() {
        return selectedColorPicker;
    }

    public void selectColor(int color) {
        if (colorSelection == PRIMARY_COLOR) {
            setPrimaryColor(color);
        } else {
            setSecondaryColor(color);
        }
    }

    public void setPrimaryColor(int primaryColor) {
        if (colorSelection == PRIMARY_COLOR) {
            selectedColorPicker.setRGB(primaryColor);
        } else {
            this.primaryColor = primaryColor;
        }
    }

    public int getPrimaryColor() {
        return primaryColor;
    }

    public void setSecondaryColor(int secondaryColor) {
        if (colorSelection == 1) {
            selectedColorPicker.setRGB(secondaryColor);
        } else {
            this.secondaryColor = secondaryColor;
        }
    }

    public int getSecondaryColor() {
        return secondaryColor;
    }

    public int getSelectedColor() {
        return colorSelection == PRIMARY_COLOR ? primaryColor : secondaryColor;
    }

    public int getColorSelection() {
        return colorSelection;
    }

    public void setColorSelection(int colorSelection) {
        this.colorSelection = colorSelection;

        selectedColorPicker.setRGB(getSelectedColor());
    }

    public void setActiveTool(ImageTool tool) {
        if (activeTool == tool)
            return;

        // quit old tool
        if (activeTool != null) {
            activeTool.finish();
            if (tool == ImageTool.PIPETTE) {
                prevTool = activeTool;
            }
        }

        activeTool = tool;
    }

    public void switchBackToPreviousTool() {
        queueEvent(() -> setActiveTool(prevTool));
    }

    public ImageTool getActiveTool() {
        return this.activeTool;
    }

    public void addCustomColor(int color) {
        selectColor(color);
        customColorButtonArray.addColor(color);
    }

    public static boolean isTransparentSelection() {
        return transparentSelection.get();
    }

    public static void setTransparentSelection(boolean transparentSelection) {
        MainApp.transparentSelection.set(transparentSelection);
    }

    public static boolean isLockSelectionRatio() {
        return lockSelectionRatio.get();
    }

    public static void setLockSelectionRatio(boolean lockSelectionRatio) {
        MainApp.lockSelectionRatio.set(lockSelectionRatio);
    }

    public static boolean isAskUnsavedChanges() {
        return !DEV_BUILD;
    }

    public int[] getPrevMouseImagePosition() {
        return getMouseImagePosition(prevMousePos);
    }

    public int[] getMouseImagePosition() {
        return getMouseImagePosition(mousePos);
    }

    public SVector getMouseImagePosVec() {
        return getImagePosition(mousePos);
    }

    private int[] getMouseImagePosition(SVector mouse) {
        SVector mouseImagePos = getImagePosition(mouse);
        return new int[] { (int) Math.floor(mouseImagePos.x), (int) Math.floor(mouseImagePos.y) };
    }

    public SVector getImagePosition(SVector screenSpacePos) {
        return canvas.getImagePosition(screenSpacePos);
    }

    public SVector getScreenPosition(SVector imagePos) {
        return canvas.getScreenPosition(imagePos);
    }

    public SVector getMousePosition() {
        return mousePos;
    }

    public SVector getPrevMousePosition() {
        return prevMousePos;
    }

    public ColorArray getCustomColorButtonArray() {
        return customColorButtonArray;
    }

    public static String formatFilesize(long filesize) {
        final String[] prefixes = { "k", "M", "G", "T" };
        long remainder = 0;
        for (int i = 0; i < prefixes.length + 1; i++) {
            if (filesize < 1024) {
                if (i == 0) {
                    return filesize + "B";
                } else {
                    String unit = prefixes[i - 1] + "B";
                    return "%d.%02d%s".formatted(filesize, (remainder * 100) / 1024, unit);
                }
            }
            remainder = (filesize & 0x3FF);
            filesize = filesize >> 10;
        }
        return "[Filesize too large!]";
    }

    public static int toInt(Vector4f color) {
        return SUtil.toARGB(color.x * 255, color.y * 255, color.z * 255, color.w * 255);
    }

    public static Vector4f toVector4f(Integer color) {
        if (color == null)
            return null;

        int red = SUtil.red(color);
        int green = SUtil.green(color);
        int blue = SUtil.blue(color);
        int alpha = SUtil.alpha(color);
        return (Vector4f) new Vector4f(red, green, blue, alpha).scale(1.0f / 255);
    }

    public static int runCommand(String directory, ArrayList<String> commands) {
        int exitVal = 1;
        try {
            // ProcessBuilder pb = new ProcessBuilder("sh", "-c", "ls");
            ProcessBuilder pb = new ProcessBuilder(commands);
            // pb.directory(new File(System.getProperty("user.home")));
            pb.directory(new File(directory));
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

            if (!output.isEmpty())
                System.out.print(output);

            exitVal = process.waitFor();
            // if (exitVal == 0) {
            // System.out.println(output);
            // }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return exitVal;
    }
}
