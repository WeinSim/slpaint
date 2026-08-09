package com.weinsim.sutil.ui.elements;

import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import com.weinsim.slpaint.renderengine.font.TextFont;
import com.weinsim.sutil.color.Color;
import com.weinsim.sutil.math.SVector;
import com.weinsim.sutil.ui.UI;
import com.weinsim.sutil.ui.UIColors;
import com.weinsim.sutil.ui.UISizes;

public class UIText extends UIElement {

    public static final DoubleSupplier NORMAL = UISizes.TEXT,
            SMALL = UISizes.TEXT_SMALL;

    private String text;
    private Supplier<String> textUpdater = this::getText;

    private double textSize;
    private DoubleSupplier textSizeUpdater;

    private String fontName;
    private Supplier<String> fontUpdater = TextFont::getCurrentFontName;

    private Color color;
    private Supplier<Color> colorUpdater = UIColors.TEXT;

    public UIText(String text) {
        this(text, NORMAL);
    }

    public UIText(String text, DoubleSupplier textSizeUpdater) {
        this.text = text;
        this.textSizeUpdater = textSizeUpdater;
    }

    public UIText(Supplier<String> textUpdater) {
        this(textUpdater, NORMAL);
    }

    public UIText(Supplier<String> textUpdater, DoubleSupplier textSizeUpdater) {
        this.textUpdater = textUpdater;
        this.textSizeUpdater = textSizeUpdater;
    }

    @Override
    public void update() {
        text = textUpdater.get();
        textSize = textSizeUpdater.getAsDouble();
        fontName = fontUpdater.get();
        color = colorUpdater.get();
    }

    @Override
    public void setPreferredSize() {
        size = new SVector(textWidth(text.length()), textSize);
    }

    public double textWidth(int len) {
        return UI.textWidth(text, textSize, fontName, len);
    }

    public int getCharIndex(double x) {
        return UI.getCharIndex(text, textSize, fontName, x);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        textUpdater = this::getText;
    }

    public void setText(Supplier<String> textUpdater) {
        this.textUpdater = textUpdater;
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
        fontUpdater = this::getFontName;
    }

    public void setFontName(Supplier<String> fontUpdater) {
        this.fontUpdater = fontUpdater;
    }

    public double getTextSize() {
        return textSize;
    }

    public void setTextSize(double textSize) {
        this.textSize = textSize;
        textSizeUpdater = this::getTextSize;
    }

    public void setTextSize(DoubleSupplier textSizeUpdater) {
        this.textSizeUpdater = textSizeUpdater;
    }

    public Color getColor() {
        return color;
    }

    public UIText setColor(Color color) {
        this.color = color;
        colorUpdater = this::getColor;
        return this;
    }

    public UIText setColor(Supplier<Color> colorUpdater) {
        this.colorUpdater = colorUpdater;
        return this;
    }

}
