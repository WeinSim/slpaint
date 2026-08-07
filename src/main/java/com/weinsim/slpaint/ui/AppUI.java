package com.weinsim.slpaint.ui;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL21.*;
import static org.lwjgl.opengl.GL30.*;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.weinsim.slpaint.main.ColorPicker;
import com.weinsim.slpaint.main.Loader;
import com.weinsim.slpaint.main.apps.App;
import com.weinsim.slpaint.main.apps.MainApp;
import com.weinsim.slpaint.main.image.Image;
import com.weinsim.slpaint.renderengine.Cleanable;
import com.weinsim.slpaint.renderengine.font.TextFont;
import com.weinsim.slpaint.settings.BooleanSetting;
import com.weinsim.slpaint.settings.ColorSetting;
import com.weinsim.sutil.color.Color;
import com.weinsim.sutil.math.SVector;
import com.weinsim.sutil.ui.UI;

public abstract class AppUI<T extends App> extends UI implements Cleanable {

    private static final String ICON_BASE_PATH = "icons/%s.png",
            MISSING_ICON_NAME = "questionmark";

    private static final Color[] DEFAULT_UI_COLORS_DARK = {
            Color.sRGB(0.3, 0.3, 0.3),
            Color.sRGB(0.07, 0.35, 0.5),
            Color.sRGB(0.5, 0.07, 0.35),
            Color.sRGB(0.42, 0.14, 0.14)
    };

    private static final Color[] DEFAULT_UI_COLORS_LIGHT = {
            Color.sGrey(1.0),
            Color.sRGB(0.67, 0.85, 0.95),
            Color.sRGB(0.95, 0.63, 0.84),
            Color.sRGB(0.84, 0.51, 0.51)
    };

    protected final T app;

    private static BooleanSetting darkMode = new BooleanSetting("darkMode");
    private static ColorSetting baseColor = new ColorSetting("baseColor");
    private static ColorPicker baseColorPicker = new ColorPicker(getBaseColor());

    private ArrayList<Integer> iconTextures;

    public AppUI(T app) {
        this.app = app;
        super(app.getUIScale(), app.getWindowSize());
        iconTextures = new ArrayList<>();
    }

    @Override
    protected void createKeyboardShortcuts() {
        if (MainApp.DEV_BUILD) {
            UI.addKeyboardShortcut("cycle_debug", GLFW_KEY_COMMA, 0, false, App::cycleDebugOutline);
            UI.addKeyboardShortcut("reload_shaders", GLFW_KEY_S, GLFW_MOD_SHIFT, true, app::reloadShaders);
            UI.addKeyboardShortcut("reload_ui", GLFW_KEY_R, GLFW_MOD_SHIFT, true, () -> app.queueEvent(app::loadUI));
        }
    }

    @Override
    public void update(SVector mousePos, boolean focus) {
        baseColor.set(baseColorPicker.getColor());
        super.update(mousePos, focus);
    }

    @Override
    public double textWidthImpl(String text, double textSize, String fontName, int len) {
        TextFont font = TextFont.getFont(fontName);
        len = Math.clamp(len, 0, text.length());
        return font.textWidth(text, len) * textSize / font.size;
    }

    @Override
    public int getCharIndexImpl(String text, double textSize, String fontName, double x) {
        TextFont font = TextFont.getFont(fontName);
        return font.getCharIndex(text, x / textSize * font.size);
    }

    @Override
    protected int loadIconTextureID(String name) {
        String filename = String.format(ICON_BASE_PATH, name);
        if (!Loader.exists(filename))
            filename = String.format(ICON_BASE_PATH, MISSING_ICON_NAME);
        BufferedImage image;
        try {
            image = Image.toARGB(ImageIO.read(Loader.getInputStream(filename)));
        } catch (IOException e) {
            // e.printStackTrace();
            throw new RuntimeException(String.format("Unable to load icon \"%s\"", name));
        }
        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        int[] pixelData = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        glTexImage2D(GL_TEXTURE_2D, 0, GL_SRGB8_ALPHA8, image.getWidth(), image.getHeight(), 0, GL_BGRA,
                GL_UNSIGNED_INT_8_8_8_8_REV, pixelData);
        glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, 0);
        return textureID;
    }

    @Override
    protected int getModifiersImpl() {
        return app.getModifierKeys();
    }

    /**
     * Returns a {@code String[]} containing the first {@code numWords} words of
     * lorem ipsum, split into lines of {@code lineLength} words each.
     * 
     * @param numWords
     * @param lineLength
     * @return
     */
    public static String[] lipsum(int numWords, int lineLength) {
        String lipsum;
        try {
            lipsum = Loader.getString("misc/lipsum.txt");
        } catch (IOException e) {
            e.printStackTrace();
            return new String[] { "[unable to load lipsum]" };
        }

        String[] words = lipsum.split(" ");
        numWords = Math.min(numWords, words.length);
        String[] ret = new String[numWords / lineLength];
        int index = 0;
        for (int i = 0; i < ret.length; i++) {
            ret[i] = "";
            for (int j = 0; j < lineLength; j++) {
                String nextWord = words[index++];
                boolean newLine = nextWord.endsWith("\n");
                if (newLine) {
                    ret[i] += nextWord.substring(0, nextWord.length() - 1);
                    break;
                } else {
                    ret[i] += nextWord;
                    ret[i] += " ";
                }
            }
        }

        return ret;
    }

    public static boolean isDarkMode() {
        return darkMode.get();
    }

    @Override
    protected boolean isDarkModeImpl() {
        return isDarkMode();
    }

    public static void setDarkMode(boolean darkMode) {
        AppUI.darkMode.set(darkMode);
    }

    public static Color getBaseColor() {
        return baseColor.get();
    }

    @Override
    protected Color getBaseColorImpl() {
        return getBaseColor();
    }

    public static Color[] getDefaultUIColors() {
        return isDarkMode() ? DEFAULT_UI_COLORS_DARK : DEFAULT_UI_COLORS_LIGHT;
    }

    public static int getNumDefaultUIColors() {
        return DEFAULT_UI_COLORS_DARK.length;
    }

    public static ColorPicker getBaseColorPicker() {
        return baseColorPicker;
    }

    @Override
    public void cleanUp() {
        for (int texture : iconTextures) {
            glDeleteTextures(texture);
        }
    }

}
