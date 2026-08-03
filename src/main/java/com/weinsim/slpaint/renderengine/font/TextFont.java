package com.weinsim.slpaint.renderengine.font;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import com.weinsim.slpaint.main.Loader;
import com.weinsim.slpaint.main.apps.MainApp;
import com.weinsim.slpaint.renderengine.UIRenderMaster;
import com.weinsim.slpaint.renderengine.bufferobjects.FloatVBO;
import com.weinsim.slpaint.renderengine.bufferobjects.IntVBO;
import com.weinsim.slpaint.renderengine.drawcalls.TextDrawCall;
import com.weinsim.slpaint.settings.StringSetting;
import com.weinsim.sutil.SUtil;
import com.weinsim.sutil.json.JSONParser;
import com.weinsim.sutil.json.values.JSONArray;
import com.weinsim.sutil.json.values.JSONObject;
import com.weinsim.sutil.math.SVector;
import com.weinsim.sutil.ui.UI;

public record TextFont(String name, int size, int lineHeight, int base, String[] textureFilenames,
        int textureWidth, int textureHeight, FontChar[] fontChars, HashMap<Character, Integer> charIDs,
        int unknownCharIndex, float[] uboData) {

    private static final String FONT_DIRECTORY = "fonts/";
    private static final String FONT_FILE = "fonts.json";

    private static final char[] CHAR_RANGES = {
            0x0020, 0x007E,
            0x00A0, 0x00FF
    };
    private static final char UNKNOWN_CHAR = 0x25A1; // □ (WHITE SQUARE)
    // private static final char BULLET_CHAR = 0x2022; // • (BULLET)
    private static final char[] EXTRA_CHARS = {
            UNKNOWN_CHAR,
            // BULLET_CHAR,
    };

    static {
        init();
    }

    private static HashMap<String, TextFont> fonts;
    private static String[] availableFonts;
    private static TextFont defaultFont;
    private static StringSetting currentFont = new StringSetting("font");

    /**
     * Parses the font info file and loads the default font.
     */
    private static void init() {
        String defaultFontName;
        try {
            JSONObject fonts = JSONParser.parseObject(Loader.getString(FONT_DIRECTORY + FONT_FILE));
            JSONArray fontsArray = fonts.getArray("fonts");
            availableFonts = new String[fontsArray.size()];
            for (int i = 0; i < availableFonts.length; i++) {
                availableFonts[i] = fontsArray.getString(i);
            }
            defaultFontName = fonts.getString("defaultFont");
        } catch (IOException e) {
            final String message = String.format("Unable to load font info file (%s)", FONT_FILE);
            throw new RuntimeException(message, e);
        }
        fonts = new HashMap<>();
        try {
            defaultFont = load(defaultFontName);
            fonts.put(defaultFontName, defaultFont);
        } catch (IOException e) {
            String message = String.format("Unable to load default font (%s)", defaultFontName);
            throw new RuntimeException(message, e);
        }
    }

    private static TextFont load(String name) throws IOException {
        // this is just a hack for now
        int fontSize;
        fontSize = UI.getUIScale() > 1.5 ? 36 : 18;
        // fontSize = 36;
        // fontSize = 50;

        String fontInfoFile = String.format("%s%s/output_%d.fnt", FONT_DIRECTORY, name, fontSize);
        String[] allLines = null;
        try {
            allLines = Loader.getString(fontInfoFile).split("\n");
        } catch (IOException e) {
            loadFail("Unable to read file %s", name, fontInfoFile);
        }

        ArrayList<FontChar> chars = new ArrayList<>();
        int size = 0, lineHeight = 0, base = 0, pages = 0, textureWidth = 0, textureHeight = 0;
        String[] textureFilenames = null;
        for (String line : allLines) {
            ArrayList<String> parts = new ArrayList<>();
            for (String string : line.split(" ")) {
                if (!string.isEmpty())
                    parts.add(string);
            }
            if (parts.isEmpty())
                continue;
            String lineType = parts.get(0);
            HashMap<String, Object> properties = new HashMap<>();
            for (int i = 1; i < parts.size(); i++) {
                String[] keyValue = parts.get(i).split("=");
                if (keyValue.length != 2)
                    continue;
                String key = keyValue[0];
                if (keyValue[1].startsWith("\"")) {
                    properties.put(key, keyValue[1].replaceAll("\"", ""));
                } else {
                    try {
                        properties.put(key, Integer.parseInt(keyValue[1]));
                    } catch (NumberFormatException e) {
                    }
                }
            }
            switch (lineType) {
                case "info" -> {
                    size = Math.abs((int) properties.get("size"));
                }
                case "common" -> {
                    lineHeight = (int) properties.get("lineHeight");
                    base = (int) properties.get("base");
                    pages = (int) properties.get("pages");
                    textureFilenames = new String[pages];
                    textureWidth = (int) properties.get("scaleW");
                    textureHeight = (int) properties.get("scaleH");
                }
                case "page" -> {
                    int id = (int) properties.get("id");
                    textureFilenames[id] = (String) properties.get("file");
                }
                case "char" ->
                    chars.add(new FontChar(
                            (int) properties.get("id"),
                            (pages > 1) ? (int) properties.get("page") : 0,
                            (int) properties.get("x"),
                            (int) properties.get("y"),
                            (int) properties.get("width"),
                            (int) properties.get("height"),
                            (int) properties.get("xoffset"),
                            (int) properties.get("yoffset"),
                            (int) properties.get("xadvance")));
            }
        }

        if (chars.size() > UIRenderMaster.MAX_FONT_CHARS)
            loadFail("Too many characters (%d). Maximum is %d.", name, chars.size(), UIRenderMaster.MAX_FONT_CHARS);

        if (pages > UIRenderMaster.MAX_FONT_ATLASSES)
            loadFail("Too many texture atlasses (%d). Maximum is %d.", name, pages, UIRenderMaster.MAX_FONT_ATLASSES);

        FontChar[] fontChars = new FontChar[chars.size()];
        HashMap<Character, Integer> charIDs = new HashMap<>();
        float[] uboData = new float[UIRenderMaster.MAX_FONT_CHARS * 4 + 2];
        int unknownCharIndex = -1;
        int i = 0;
        int uboIndex = 0;
        for (FontChar fontChar : chars) {
            char c = (char) fontChar.id();
            if (c == UNKNOWN_CHAR)
                unknownCharIndex = i;
            charIDs.put(c, i);
            uboData[uboIndex++] = fontChar.x() + textureWidth * fontChar.page();
            uboData[uboIndex++] = fontChar.y();
            uboData[uboIndex++] = fontChar.width();
            uboData[uboIndex++] = fontChar.height();

            fontChars[i++] = fontChar;
        }
        uboIndex = UIRenderMaster.MAX_FONT_CHARS * 4;
        uboData[uboIndex++] = textureWidth;
        uboData[uboIndex++] = textureHeight;

        if (unknownCharIndex == -1)
            // loadFail("\"Unknown character\" (\"%c\", id %d) missing", name, UNKNOWN_CHAR,
            // (int) UNKNOWN_CHAR);
            unknownCharIndex = 0;

        return new TextFont(name, size, lineHeight, base, textureFilenames, textureWidth, textureHeight, fontChars,
                charIDs, unknownCharIndex, uboData);
    }

    private static void loadFail(String message, String name, Object... params) throws IOException {
        message = String.format(message, params);
        message = String.format("Could not load font \"%s\": %s", name, message);
        throw new IOException(message);
    }

    public int[] loadTextures() throws IOException {
        int[] textureIDs = new int[textureFilenames.length];
        for (int i = 0; i < textureFilenames.length; i++) {
            String textureFile = String.format("%s%s/%s", FONT_DIRECTORY, name, textureFilenames[i]);
            Texture texture = null;
            try {
                texture = TextureLoader.getTexture("PNG", Loader.getInputStream(textureFile));
            } catch (Exception e) {
                loadFail("Unable to load texture \"%s\"", name, textureFile);
            }
            int textureID = texture.getTextureID();
            textureIDs[i] = textureID;
            glBindTexture(GL_TEXTURE_2D, textureID);
            glGenerateMipmap(GL_TEXTURE_2D);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        return textureIDs;
    }

    private FontChar getFontChar(char c) {
        return fontChars[charIDs.getOrDefault(c, unknownCharIndex)];
    }

    public double textWidth(String text) {
        return textWidth(text, text.length());
    }

    public double textWidth(String text, int len) {
        char[] chars = text.toCharArray();
        len = Math.clamp(len, 0, chars.length);
        double sum = 0;
        for (int i = 0; i < len; i++)
            sum += getFontChar(chars[i]).xAdvance();
        return sum;
    }

    public int getCharIndex(String text, double x) {
        if (text.isEmpty())
            return 0;
        char[] chars = text.toCharArray();
        double sum = 0;
        int index = chars.length;
        for (int i = 0; i < chars.length; i++) {
            double current = sum;
            double next = sum + getFontChar(chars[i]).xAdvance();
            double middle = (current + next) / 2;
            if (middle > x) {
                index = i;
                break;
            }
            sum = next;
        }
        return index;
    }

    public void putCharsIntoVBOs(TextDrawCall drawCall, IntVBO dataIndex, FloatVBO position, FloatVBO depth,
            IntVBO charIndex, int batchIndex) {

        double x = drawCall.position.x,
                y = drawCall.position.y;
        char[] chars = drawCall.text.toCharArray();
        for (char c: chars) {
            FontChar fontChar = getFontChar(c);
            dataIndex.putData(batchIndex);
            charIndex.putData(charIDs.get((char) fontChar.id()));
            SVector vertexPos = new SVector(x + fontChar.xOffset() * drawCall.relativeSize,
                    y + (fontChar.yOffset() - base + 0.8 * size) * drawCall.relativeSize);
            position.putData(vertexPos);
            depth.putData(drawCall.depth);
            x += fontChar.xAdvance() * drawCall.relativeSize;
        }
    }

    public float[] getUBOData() {
        return uboData;
    }

    public static TextFont getFont(String name) {
        // try returning already loaded font
        TextFont font = null;
        if (fonts.containsKey(name)) {
            font = fonts.get(name);
        } else {
            // load new font
            try {
                font = load(name);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // even if we fail to load the font, we still put null into the font cache to
            // indicate that we already tried to load this font
            fonts.put(name, font);
        }
        return font != null ? font : defaultFont;
    }

    public static String[] getAvailableFonts() {
        return availableFonts;
    }

    public static TextFont getCurrentFont() {
        return getFont(getCurrentFontName());
    }

    public static String getCurrentFontName() {
        return currentFont.get();
    }

    public static void setCurrentFontName(String name) {
        // test if this font is actually available
        TextFont font = getFont(name);
        if (font.name.equals(name))
            currentFont.set(name);
    }

    public static void createFontAtlas(String name, int textSize) {
        // doesn't work, don't know why
        // delete old files
        // ArrayList<String> deleteArgs = new ArrayList<>();
        // deleteArgs.add("rm");
        // deleteArgs.add("output*");
        // MainApp.runCommand(String.format("%s%s", FONT_DIRECTORY, name), deleteArgs);

        String directoryName = String.format("res/%s%s/", FONT_DIRECTORY, name);
        MainApp.runCommand(directoryName,
                getFontGenerationCommand(name, 2, 256, 256, textSize,
                        CHAR_RANGES, EXTRA_CHARS, 0));
    }

    private static void addArgument(ArrayList<String> commands, String argument, int value) {
        addArgument(commands, argument, Integer.toString(value));
    }

    private static void addArgument(ArrayList<String> commands, String argument, String value) {
        commands.add("--" + argument);
        commands.add(value);
    }

    private static ArrayList<String> getFontGenerationCommand(String fontName, int padding, int textureWidth,
            int textureHeight, int fontSize, char[] charRanges, char[] extraChars, int bgColor) {

        ArrayList<String> commands = new ArrayList<>();
        // commands.add("/home/simon/code/executables/fontbm/fontbm");
        commands.add("fontbm");
        addArgument(commands, "font-file", "%s.ttf".formatted(fontName));
        addArgument(commands, "output", "output_%s".formatted(fontSize));
        addArgument(commands, "padding-up", padding);
        addArgument(commands, "padding-down", padding);
        addArgument(commands, "padding-left", padding);
        addArgument(commands, "padding-right", padding);
        addArgument(commands, "texture-size", "%dx%d".formatted(textureWidth, textureHeight));
        addArgument(commands, "font-size", fontSize);
        StringBuilder charsBuilder = new StringBuilder();
        for (int i = 0; i < charRanges.length / 2; i++)
            charsBuilder.append("%d-%d,".formatted((int) charRanges[2 * i], (int) charRanges[2 * i + 1]));
        for (int extraChar : extraChars)
            charsBuilder.append("%d,".formatted((int) extraChar));
        int len = charsBuilder.length();
        if (len > 0)
            charsBuilder.deleteCharAt(len - 1);
        addArgument(commands, "chars", charsBuilder.toString());
        addArgument(commands, "background-color", "%d,%d,%d".formatted(
                SUtil.red(bgColor), SUtil.green(bgColor), SUtil.blue(bgColor)));
        // System.out.print("Generated command: ");
        // for (String str : commands) {
        // System.out.print(str + " ");
        // }
        // System.out.println();
        return commands;
    }

}
