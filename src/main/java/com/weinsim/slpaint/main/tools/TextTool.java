package com.weinsim.slpaint.main.tools;

import static org.lwjgl.glfw.GLFW.*;

import com.weinsim.slpaint.renderengine.font.TextFont;
import com.weinsim.sutil.math.SVector;
import com.weinsim.sutil.ui.UI;

public final class TextTool extends DragTool {

    public static final TextTool INSTANCE;

    // public static final String[] FONT_NAMES;
    // private static final String DEFAULT_FONT_NAME;

    private static final int MIN_TEXT_SIZE = 0,
            MAX_TEXT_SIZE = 128,
            DEFAULT_TEXT_SIZE = 32;

    public static final int MARGIN = 5;

    static {
        // this method takes ~300 milliseconds to run!
        // long startTime = System.nanoTime();
        // FONT_NAMES =
        // GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        // Font[] fonts =
        // GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        // FONT_NAMES = new String[fonts.length];
        // for (int i = 0; i < fonts.length; i++) {
        // FONT_NAMES[i] = fonts[i].getName();
        // }
        // System.out.format("getFontFamilyNames: %.3fms\n", (System.nanoTime() -
        // startTime) * 1e-6);

        // DEFAULT_FONT_NAME = TextFont.DEFAULT_FONT_NAME;
        // FONT_NAMES = new String[] { DEFAULT_FONT_NAME };

        INSTANCE = new TextTool();
    }

    private String text;
    private int size;
    // private String font = DEFAULT_FONT_NAME;

    private TextTool() {
        super();

        size = DEFAULT_TEXT_SIZE;
        text = "";
    }

    @Override
    public void createKeyboardShortcuts() {
        addShortcut("text_finish", GLFW_KEY_ESCAPE, 0, IDLE, this::finish);
    }

    @Override
    public void start() {
        UI.select(app.getTextToolInput());
        text = "";

        state = IDLE;
    }

    @Override
    public void finish() {
        if (!text.isEmpty()) {
            SVector position = app.getImagePosition(app.getTextToolInput().getAbsolutePosition());
            app.renderTextToImage(text, position.x, position.y, size, TextFont.getCurrentFont());
            text = "";
        }

        state = NONE;
    }

    @Override
    public void cancel() {
        text = "";
        state = NONE;
    }

    @Override
    public int getMargin() {
        return MARGIN;
    }

    @Override
    public boolean enterIdle() {
        return true;
    }

    @Override
    public String getName() {
        return "Text";
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(Math.max(1, width));
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(Math.max(1, height));
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = Math.clamp(size, MIN_TEXT_SIZE, MAX_TEXT_SIZE);
    }

    // public String getFont() {
    // return font;
    // }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean lockRatio() {
        return false;
    }
}