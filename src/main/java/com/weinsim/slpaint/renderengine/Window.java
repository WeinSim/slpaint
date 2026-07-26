package com.weinsim.slpaint.renderengine;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWNativeCocoa.*;
import static org.lwjgl.glfw.GLFWNativeWin32.*;
import static org.lwjgl.glfw.GLFWNativeX11.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.util.nfd.NativeFileDialog.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWCharCallbackI;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Platform;

import com.weinsim.slpaint.main.Loader;
import com.weinsim.sutil.SUtil;
import com.weinsim.sutil.math.SVector;

public class Window {

    private enum Cursor {

        ARROW(GLFW_ARROW_CURSOR),
        IBEAM(GLFW_IBEAM_CURSOR),
        CROSSHAIR(GLFW_CROSSHAIR_CURSOR),
        POINTING_HAND(GLFW_POINTING_HAND_CURSOR),
        RESIZE_EW(GLFW_RESIZE_EW_CURSOR),
        RESIZE_NS(GLFW_RESIZE_NS_CURSOR),
        RESIZE_NWSE(GLFW_RESIZE_NWSE_CURSOR),
        RESIZE_NESW(GLFW_RESIZE_NESW_CURSOR),
        RESIZE_ALL(GLFW_RESIZE_ALL_CURSOR),
        NOT_ALLOWED(GLFW_NOT_ALLOWED_CURSOR);

        public final int shape;
        private long cursor;

        private Cursor(int shape) {
            this.shape = shape;
        }

        public void setCursor(long cursor) {
            this.cursor = cursor;
        }

        public static Cursor get(long shape) {
            for (Cursor cursor : values()) {
                if (cursor.shape == shape)
                    return cursor;
            }
            return null;
        }
    }

    public static final int NORMAL = 0, MAXIMIZED = 1, FULLSCREEN = 2;

    /**
     * Incoming keys from key events are first put through this map to adjust for my
     * German keybaord layout / Esc + CapsLock swap.
     */
    private static final HashMap<Integer, Integer> KEY_MAP = new HashMap<>();

    private static HashMap<Long, Window> windows = new HashMap<>();

    static {
        KEY_MAP.put(GLFW_KEY_ESCAPE, GLFW_KEY_CAPS_LOCK);
        KEY_MAP.put(GLFW_KEY_CAPS_LOCK, GLFW_KEY_ESCAPE);

        KEY_MAP.put(GLFW_KEY_Z, GLFW_KEY_Y);
        KEY_MAP.put(GLFW_KEY_Y, GLFW_KEY_Z);

        KEY_MAP.put(GLFW_KEY_RIGHT_BRACKET, GLFW_KEY_KP_ADD);
        KEY_MAP.put(GLFW_KEY_SLASH, GLFW_KEY_KP_SUBTRACT);
    }

    private final long windowHandle;

    // for NFD
    private final int nativeHandleType;
    private final long nativeWindowHandle;

    private SVector mousePos;

    private String charBuffer;
    private ArrayList<KeyPressInfo> keyPressInfos;
    private ArrayList<MouseButtonInfo> mouseButtonInfos;
    private ArrayList<ScrollInfo> scrollInfos;

    public Window(int width, int height, int windowMode, boolean resizable, String title) {
        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE);

        // anti-aliasing
        // glfwWindowHint(GLFW_SAMPLES, 4);

        if (windowMode == MAXIMIZED) {
            glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
        }

        // Create the window
        // window = glfwCreateWindow(width, height, "Hello World!",
        // MemoryUtil.NULL, MemoryUtil.NULL);
        long primaryMonitor = glfwGetPrimaryMonitor();
        GLFWVidMode pmVideoMode = glfwGetVideoMode(primaryMonitor);
        if (windowMode == FULLSCREEN) {
            width = pmVideoMode.width();
            height = pmVideoMode.height();
        }
        windowHandle = glfwCreateWindow(width, height, title,
                windowMode == FULLSCREEN ? primaryMonitor : MemoryUtil.NULL,
                MemoryUtil.NULL);
        windows.put(windowHandle, this);
        if (windowHandle == MemoryUtil.NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated
        // or released.
        glfwSetCharCallback(windowHandle, new CharCallback());
        glfwSetKeyCallback(windowHandle, new KeyCallback());
        glfwSetMouseButtonCallback(windowHandle, new MouseButtonCallback());
        glfwSetScrollCallback(windowHandle, new ScrollCallback());

        center();

        // Make the OpenGL context current
        makeContextCurrent();
        // Enable v-sync.
        // Note: this limits the application to 60fps, which can make very fast paint
        // strokes look jagged. To improve this, the rendering logic could be separated
        // onto its own thread. For now though, this is not neccessary.
        glfwSwapInterval(1);

        // glfwSetWindowSizeLimits(windowHandle, 200, 200, Integer.MAX_VALUE,
        // Integer.MAX_VALUE);
        glfwSetWindowSizeLimits(windowHandle, 200, 200, GLFW_DONT_CARE, GLFW_DONT_CARE);

        // Make the window visible
        glfwShowWindow(windowHandle);

        switch (Platform.get()) {
            case FREEBSD, LINUX -> {
                nativeHandleType = NFD_WINDOW_HANDLE_TYPE_X11;
                nativeWindowHandle = glfwGetX11Window(windowHandle);
            }
            case MACOSX -> {
                nativeHandleType = NFD_WINDOW_HANDLE_TYPE_COCOA;
                nativeWindowHandle = glfwGetCocoaWindow(windowHandle);
            }
            case WINDOWS -> {
                nativeHandleType = NFD_WINDOW_HANDLE_TYPE_WINDOWS;
                nativeWindowHandle = glfwGetWin32Window(windowHandle);
            }
            default -> {
                nativeHandleType = NFD_WINDOW_HANDLE_TYPE_UNSET;
                nativeWindowHandle = NULL;
            }
        }

        GL.createCapabilities();
        // glEnable(GL_DEBUG_OUTPUT);
        // glDebugMessageCallback((source, type, id, severity, length, message,
        // userParam) -> {
        // System.err.println(GLDebugMessageCallback.getMessage(length, message));
        // }, 0);

        mousePos = new SVector();

        charBuffer = "";
        keyPressInfos = new ArrayList<>();
        mouseButtonInfos = new ArrayList<>();
        scrollInfos = new ArrayList<>();
    }

    public static void createCursors() {
        for (Cursor cursor : Cursor.values()) {
            cursor.setCursor(glfwCreateStandardCursor(cursor.shape));
        }
    }

    public void makeContextCurrent() {
        glfwMakeContextCurrent(windowHandle);
    }

    public void updateDisplay() {
        mousePos = null;
        charBuffer = "";

        glfwSwapBuffers(windowHandle);
    }

    public boolean isCloseRequested() {
        return glfwWindowShouldClose(windowHandle);
    }

    public void requestClose() {
        glfwSetWindowShouldClose(windowHandle, true); // We will detect this in the rendering loop
    }

    // very bad method name
    public void unrequestClose() {
        glfwSetWindowShouldClose(windowHandle, false);
    }

    public void closeDisplay() {
        // Free the window callbacks and destroy the window
        Callbacks.glfwFreeCallbacks(windowHandle);
        glfwDestroyWindow(windowHandle);
    }

    public void hideCursor() {
        glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    public void setTitle(String title) {
        glfwSetWindowTitle(windowHandle, title);
    }

    public int[] getDisplaySize() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(windowHandle, pWidth, pHeight);

            return new int[] { pWidth.get(0), pHeight.get(0) };
        }
    }

    public double getWindowContentScale() {
        float[] xScale = new float[1],
                yScale = new float[1];
        // Note: the return value of this method does not change if the system's UI
        // scale is changed while the app is running (on linux mint: System Settings >
        // Font Selection > Text Scaling Factor).
        glfwGetWindowContentScale(windowHandle, xScale, yScale);
        // System.out.format("GLFW window content scale: x = %.1f, y = %.1f\n",
        // xScale[0], yScale[0]);
        return Math.sqrt(xScale[0] * yScale[0]);
    }

    public int getNativeHandleType() {
        return nativeHandleType;
    }

    public long getNativeWindowHandle() {
        return nativeWindowHandle;
    }

    public int getModifierKeys() {
        int mods = 0;
        int[] flags = { GLFW_MOD_SHIFT, GLFW_MOD_CONTROL, GLFW_MOD_ALT, GLFW_MOD_SUPER };
        int[] keys = {
                GLFW_KEY_LEFT_SHIFT, GLFW_KEY_RIGHT_SHIFT,
                GLFW_KEY_LEFT_CONTROL, GLFW_KEY_RIGHT_CONTROL,
                GLFW_KEY_LEFT_ALT, GLFW_KEY_RIGHT_ALT,
                GLFW_KEY_LEFT_SUPER, GLFW_KEY_RIGHT_SUPER
        };
        for (int i = 0; i < flags.length; i++) {
            if (glfwGetKey(windowHandle, keys[2 * i]) == GLFW_PRESS
                    || glfwGetKey(windowHandle, keys[2 * i + 1]) == GLFW_PRESS)
                mods |= flags[i];
        }
        return mods;
    }

    public boolean isKeyPressed(int key) {
        return glfwGetKey(windowHandle, key) == GLFW_PRESS;
    }

    public SVector getMousePosition() {
        if (mousePos == null) {
            double[] x = { 0 };
            double[] y = { 0 };
            glfwGetCursorPos(windowHandle, x, y);
            mousePos = new SVector(x[0], y[0]);
        }
        return mousePos;
    }

    public void showCursor() {
        glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }

    public void setCursor(int cursorShape) {
        Cursor cursor = Cursor.get(cursorShape);
        if (cursor == null) {
            final String baseString = "Invalid cursor shape (%d)";
            throw new RuntimeException(baseString.formatted(cursorShape));
        }
        glfwSetCursor(windowHandle, cursor.cursor);
    }

    public boolean isFocused() {
        return glfwGetWindowAttrib(windowHandle, GLFW_FOCUSED) == GLFW_TRUE;
    }

    public void setSizeAndCenter(int width, int height) {
        setSize(width, height);

        center(width, height);
    }

    public void setSize(int width, int height) {
        glfwSetWindowSize(windowHandle, width, height);
    }

    public void center() {
        int[] displaySize = getDisplaySize();
        center(displaySize[0], displaySize[1]);
    }

    private void center(int width, int height) {
        long primaryMonitor = glfwGetPrimaryMonitor();
        GLFWVidMode pmVideoMode = glfwGetVideoMode(primaryMonitor);
        PointerBuffer monitors = glfwGetMonitors();
        int offset = monitors.capacity() == 2 ? 1920 : 0;
        offset = 0;
        glfwSetWindowPos(windowHandle,
                offset + (pmVideoMode.width() - width) / 2,
                (pmVideoMode.height() - height) / 2);
    }

    public void setIcon(ArrayList<String> images) {
        GLFWImage.Buffer icons = GLFWImage.malloc(images.size());
        for (String path : images) {
            GLFWImage icon = loadGLFWImage(path);
            icons.put(icon);
        }
        glfwSetWindowIcon(windowHandle, icons);
        // there are probably more things that should be freed here (like the individual
        // icons and buffers)
        icons.free();
    }

    private static GLFWImage loadGLFWImage(String path) {
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(Loader.getInputStream(path));
        } catch (IOException e) {
            final String message = String.format("Unable to load image %s", path);
            throw new RuntimeException(message, e);
        }
        int width = bufferedImage.getWidth(),
                height = bufferedImage.getHeight();
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = bufferedImage.getRGB(x, y);
                buffer.put((byte) SUtil.red(color));
                buffer.put((byte) SUtil.green(color));
                buffer.put((byte) SUtil.blue(color));
                buffer.put((byte) SUtil.alpha(color));
            }
        }
        buffer.flip();
        GLFWImage image = GLFWImage.malloc();
        image.set(width, height, buffer);
        return image;
    }

    public void requestFocus() {
        glfwFocusWindow(windowHandle);
    }

    private void addCharacter(int codepoint) {
        charBuffer += (char) codepoint;
    }

    public Character getNextCharacter() {
        if (charBuffer.isEmpty()) {
            return null;
        }
        char c = charBuffer.charAt(0);
        charBuffer = charBuffer.substring(1);
        return c;
    }

    private void addKeyPressInfo(KeyPressInfo keyPressInfo) {
        keyPressInfos.add(keyPressInfo);
    }

    public KeyPressInfo getNextKeyPressInfo() {
        if (keyPressInfos.isEmpty()) {
            return null;
        }
        return keyPressInfos.removeFirst();
    }

    private void addMouseButtonInfo(MouseButtonInfo mouseButtonInfo) {
        mouseButtonInfos.add(mouseButtonInfo);
    }

    public MouseButtonInfo getNextMouseButtonInfo() {
        if (mouseButtonInfos.isEmpty()) {
            return null;
        }
        return mouseButtonInfos.removeFirst();
    }

    private void addScrollInfo(ScrollInfo scrollInfo) {
        scrollInfos.add(scrollInfo);
    }

    public ScrollInfo getNextScrollInfo() {
        if (scrollInfos.isEmpty()) {
            return null;
        }
        return scrollInfos.removeFirst();
    }

    private static class CharCallback implements GLFWCharCallbackI {

        @Override
        public void invoke(long window, int codepoint) {
            windows.get(window).addCharacter(codepoint);
        }
    }

    private static class KeyCallback implements GLFWKeyCallbackI {

        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            key = KEY_MAP.getOrDefault(key, key);
            windows.get(window).addKeyPressInfo(new KeyPressInfo(key, scancode, action, mods));
        }
    }

    public record KeyPressInfo(int key, int scancode, int action, int mods) {
    }

    private static class MouseButtonCallback implements GLFWMouseButtonCallbackI {

        @Override
        public void invoke(long window, int button, int action, int mods) {
            windows.get(window).addMouseButtonInfo(new MouseButtonInfo(button, action, mods));
        }
    }

    public record MouseButtonInfo(int button, int action, int mods) {
    }

    private static class ScrollCallback implements GLFWScrollCallbackI {

        @Override
        public void invoke(long window, double xoffset, double yoffset) {
            windows.get(window).addScrollInfo(new ScrollInfo(xoffset, yoffset));
        }
    }

    public record ScrollInfo(double xoffset, double yoffset) {
    }

}
