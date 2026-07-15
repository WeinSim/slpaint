package com.weinsim.slpaint.main.apps;

import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.weinsim.slpaint.main.MainLoop;
import com.weinsim.slpaint.settings.BooleanSetting;
import com.weinsim.slpaint.renderengine.AppRenderer;
import com.weinsim.slpaint.renderengine.Window;
import com.weinsim.slpaint.renderengine.font.TextFont;
import com.weinsim.slpaint.ui.AppUI;
import com.weinsim.sutil.math.SVector;
import com.weinsim.sutil.ui.UI;
import com.weinsim.sutil.ui.elements.UIRoot;

public sealed abstract class App permits MainApp, ColorEditorApp, SettingsApp, ResizeApp {

    private static final double FRAME_TIME_GAMMA = 0.02;

    /**
     * 0 = normal 1 = mouse above, 2 = always
     */
    private static int debugOutline = 0;
    private static BooleanSetting circularHueSatField = new BooleanSetting("hueSatCircle");
    private static BooleanSetting hslColorSpace = new BooleanSetting("hslColorSpace");

    protected Window window;

    protected boolean[] mouseButtons;
    protected SVector mousePos, prevMousePos;

    private LinkedList<Runnable> eventQueue;

    private AppUI<?> ui;
    protected boolean adjustSizeOnInit = false;

    protected AppRenderer renderer;
    /**
     * The font that was loaded the last time render() was called. This field is
     * used to load the renderer both on startup and whenever the font changes.
     */
    private String lastFont = "";

    /**
     * Only used if this app is the child app of another app.
     */
    private App parent;
    /**
     * The type of dialog in the parent app that this app represents.
     */
    private int dialogType;
    private HashMap<Integer, App> childApps;

    protected double avgFrameTime = -1;
    protected double avgUpdateTime = -1;
    protected int frameCount = 0;

    public App(int width, int height, int windowMode, String title) {
        this(width, height, windowMode, true, false, title, null);
    }

    public App(int width, int height, int windowMode, boolean resizable, boolean adjustSizeOnInit, String title,
            App parent) {

        this.parent = parent;
        this.adjustSizeOnInit = adjustSizeOnInit;

        MainLoop.addApp(this);

        window = new Window(width, height, windowMode, resizable, title);
        childApps = new HashMap<>();
        eventQueue = new LinkedList<>();

        mouseButtons = new boolean[2];
        mousePos = new SVector();
        prevMousePos = new SVector();

        ArrayList<String> iconNames = new ArrayList<>();
        final int[] resolutions = { 16, 32, 256 };
        for (int res : resolutions) {
            iconNames.add(String.format("logo/logo_%d.png", res));
        }
        window.setIcon(iconNames);
    }

    public final void loadUI() {
        ui = createUI();

        if (adjustSizeOnInit) {
            // calling ui.update here to ensure the root has the correct size
            ui.update(window.getMousePosition(), window.isFocused());

            UIRoot root = UI.getRoot();
            SVector rootSize = root.getSize();
            int width = (int) rootSize.x;
            int height = (int) rootSize.y;
            window.setSizeAndCenter(width, height);
        }
    }

    protected abstract AppUI<?> createUI();

    public void makeContextCurrent() {
        window.makeContextCurrent();
        UI.setContext(ui);
    }

    public final void update(double deltaT) {
        long updateStart = System.nanoTime();
        if (avgFrameTime < 0) {
            avgFrameTime = deltaT;
        } else {
            avgFrameTime = (1 - FRAME_TIME_GAMMA) * avgFrameTime + FRAME_TIME_GAMMA * deltaT;
        }
        frameCount++;

        if (ui == null) {
            final String baseStr = "%s has no UI. The UI must be created in the app's constructor using loadUI().";
            throw new RuntimeException(String.format(baseStr, getClass().getName()));
        }

        prevMousePos.set(mousePos);
        mousePos.set(window.getMousePosition());

        boolean focus = window.isFocused();

        // process char input
        Character c;
        while ((c = window.getNextCharacter()) != null) {
            if (focus)
                ui.charInput(c);
        }

        // process key input
        Window.KeyPressInfo keyPressInfo;
        while ((keyPressInfo = window.getNextKeyPressInfo()) != null) {
            int key = keyPressInfo.key();
            int mods = keyPressInfo.mods();
            switch (keyPressInfo.action()) {
                case GLFW_PRESS, GLFW_REPEAT -> {
                    if (focus)
                        ui.keyPressed(key, mods);
                }
            }
        }

        // process mouse button input
        Window.MouseButtonInfo mouseButtonInfo;
        while ((mouseButtonInfo = window.getNextMouseButtonInfo()) != null) {
            int button = mouseButtonInfo.button();
            int mods = mouseButtonInfo.mods();
            switch (mouseButtonInfo.action()) {
                case GLFW_PRESS -> {
                    if (0 <= button && button < mouseButtons.length)
                        mouseButtons[button] = true;

                    if (focus)
                        ui.mousePressed(button, mods);
                }
                case GLFW_RELEASE -> {
                    if (0 <= button && button < mouseButtons.length)
                        mouseButtons[button] = false;

                    if (focus)
                        ui.mouseReleased(button, mods);
                }
            }
        }

        // process scroll input
        Window.ScrollInfo scrollInfo;
        while ((scrollInfo = window.getNextScrollInfo()) != null) {
            if (focus)
                ui.mouseWheel(new SVector(scrollInfo.xoffset(), scrollInfo.yoffset()));
        }

        // empty event queue
        while (!eventQueue.isEmpty())
            eventQueue.removeFirst().run();

        int[] displaySize = window.getDisplaySize();
        ui.setRootSize(displaySize[0], displaySize[1]);

        ui.update(mousePos, focus);

        window.setCursor(ui.getCursorShape());

        childUpdate(deltaT);

        double updateDuration = (System.nanoTime() - updateStart) * 1e-9;
        if (avgUpdateTime < 0) {
            avgUpdateTime = deltaT;
        } else {
            avgUpdateTime = (1 - FRAME_TIME_GAMMA) * avgUpdateTime + FRAME_TIME_GAMMA * updateDuration;
        }
    }

    protected void childUpdate(double deltaT) {
    }

    public void reloadShaders() {
        renderer.reloadShaders();
    }

    /**
     * 
     * @return Whether to actually close the window
     */
    public boolean finish() {
        renderer.cleanUp();
        ui.cleanUp();
        if (parent != null)
            parent.clearChildApp(dialogType);
        return true;
    }

    public int getModifierKeys() {
        return window.getModifierKeys();
    }

    public void requestFocus() {
        window.requestFocus();
    }

    public int[] getDisplaySize() {
        return window.getDisplaySize();
    }

    public double getWindowContentScale() {
        float[] scale = window.getWindowContentScale();
        return Math.sqrt(scale[0] * scale[1]);
    }

    public int getNativeHandleType() {
        return window.getNativeHandleType();
    }

    public long getNativeWindowHandle() {
        return window.getNativeWindowHandle();
    }

    public boolean isCloseRequested() {
        return window.isCloseRequested();
    }

    public void requestClose() {
        window.requestClose();
    }

    public void unrequestClose() {
        window.unrequestClose();
    }

    public void closeDisplay() {
        window.closeDisplay();
    }

    public void showDialog(int dialogType) {
        MainLoop.queueEvent(() -> {
            App child = childApps.get(dialogType);
            if (child != null) {
                child.requestFocus();
            } else {
                child = createChildApp(dialogType);
                if (child != null) {
                    child.setDialogType(dialogType);
                    childApps.put(dialogType, child);
                }
            }
        });
    }

    protected abstract App createChildApp(int dialogType);

    public void clearChildApp(int dialogType) {
        childApps.remove(dialogType);
    }

    public void closeChildApp(int dialogType) {
        App childApp = childApps.get(dialogType);
        if (childApp != null)
            childApp.requestClose();
    }

    public final void render() {
        // if (renderer == null)
        String currentFont = TextFont.getCurrentFontName();
        if (!lastFont.equals(currentFont))
            renderer = new AppRenderer(this);

        renderer.render();
        window.updateDisplay();
        lastFont = currentFont;
    }

    public void queueEvent(Runnable action) {
        eventQueue.add(action);
    }

    public SVector getWindowSize() {
        int[] size = window.getDisplaySize();
        return new SVector(size[0], size[1]);
    }

    public AppUI<?> getUI() {
        return ui;
    }

    public static void cycleDebugOutline() {
        debugOutline = (debugOutline + 1) % 3;
    }

    public static int getDebugOutline() {
        return debugOutline;
    }

    public static boolean isCircularHueSatField() {
        return circularHueSatField.get();
    }

    public static boolean isHSLColorSpace() {
        return hslColorSpace.get();
    }

    public static void setHSLColorSpace(boolean hslColorSpace) {
        App.hslColorSpace.set(hslColorSpace);
    }

    public static void setCircularHueSatField(boolean circularHueSatField) {
        App.circularHueSatField.set(circularHueSatField);
    }

    public double getFrameRate() {
        return 1.0 / avgFrameTime;
    }

    public double getUpdateTime() {
        return avgUpdateTime;
    }

    public int getFrameCount() {
        return frameCount;
    }

    public void setDialogType(int dialogType) {
        this.dialogType = dialogType;
    }

}
