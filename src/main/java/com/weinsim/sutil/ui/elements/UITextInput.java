package com.weinsim.sutil.ui.elements;

import static org.lwjgl.glfw.GLFW.*;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.weinsim.sutil.math.SVector;
import com.weinsim.sutil.ui.UI;
import com.weinsim.sutil.ui.UISizes;

public class UITextInput extends UIContainer {

    private static final double BLINK_INTERVAL = 0.6;

    protected UIText uiText;

    private Supplier<String> textUpdater;
    private Consumer<String> valueUpdater;

    private boolean multiline;
    private int cursorPosition;

    private double blinkStart;

    public UITextInput(Supplier<String> textUpdater, Consumer<String> valueUpdater) {
        this(textUpdater, valueUpdater, false);
    }

    public UITextInput(Supplier<String> textUpdater, Consumer<String> valueUpdater, boolean multiline) {
        super(HORIZONTAL, LEFT, CENTER);

        this.textUpdater = textUpdater;
        this.valueUpdater = valueUpdater;
        this.multiline = multiline;

        hMarginScale = 0.5;
        vMarginScale = 0.5;
        outlineNormal = true;

        setHFillSize();

        setCursorShape(() -> mouseAbove ? GLFW_IBEAM_CURSOR : null);

        selectable = true;
        selectOnClick = true;

        uiText = new UIText(textUpdater);
        add(uiText);
        add(new Cursor());

        cursorPosition = 0;

        addLeftClickAction(() -> {
            cursorPosition = uiText.getCharIndex(mousePosition.x - position.x - uiText.getPosition().x);
            resetTimer();
        });

        addCharInputAction(c -> {
            String text = this.textUpdater.get();
            boundCursorPosition();

            String newText = text;
            boolean validKey = isValidChar(c);
            if (!validKey)
                return;

            newText = text.substring(0, cursorPosition) + c + text.substring(cursorPosition);
            cursorPosition++;
            this.valueUpdater.accept(newText);
            resetTimer();
        });

        addKeyPressAction(GLFW_KEY_BACKSPACE, 0, () -> {
            boundCursorPosition();
            String text = this.textUpdater.get();
            if (cursorPosition > 0) {
                String newText = text.substring(0, cursorPosition - 1) + text.substring(cursorPosition);
                cursorPosition--;
                this.valueUpdater.accept(newText);
                resetTimer();
            }
        });
        addKeyPressAction(GLFW_KEY_DELETE, 0, () -> {
            boundCursorPosition();
            String text = this.textUpdater.get();
            if (cursorPosition < text.length()) {
                String newText = text.substring(0, cursorPosition) + text.substring(cursorPosition + 1);
                this.valueUpdater.accept(newText);
                resetTimer();
            }
        });
        addKeyPressAction(GLFW_KEY_LEFT, 0, () -> {
            cursorPosition--;
            resetTimer();
        });
        addKeyPressAction(GLFW_KEY_RIGHT, 0, () -> {
            cursorPosition++;
            resetTimer();
        });
        addKeyPressAction(GLFW_KEY_ENTER, 0, () -> {
            if (multiline)
                charInput('\n');
            else
                UI.select(null);
        });
    }

    protected boolean isValidChar(char c) {
        return (c >= 32 && c <= 126) || (c >= 160 && c < 255) || (c == '\n' && multiline);
    }

    @Override
    public void update() {
        super.update();
        boundCursorPosition();
    }

    private void boundCursorPosition() {
        String text = textUpdater.get();
        cursorPosition = Math.clamp(cursorPosition, 0, text.length());
    }

    public void resetTimer() {
        blinkStart = System.nanoTime() * 1e-9;
    }

    @Override
    public void select() {
        cursorPosition = textUpdater.get().length();
        resetTimer();
    }

    protected void setTextUpdater(Supplier<String> textUpdater) {
        this.textUpdater = textUpdater;
        uiText.setText(textUpdater);
    }

    protected void setValueUpdater(Consumer<String> valueUpdater) {
        this.valueUpdater = valueUpdater;
    }

    private class Cursor extends UIFloatContainer {

        public Cursor() {
            super(0, 0);

            addAnchor(Anchor.TOP_LEFT, () -> {
                SVector pos = new SVector(uiText.getPosition());
                pos.x += uiText.textWidth(cursorPosition);
                return pos;
            });

            setVisibilitySupplier(() -> UITextInput.this.isSelected()
                    && ((System.nanoTime() * 1e-9 - blinkStart) / BLINK_INTERVAL) % 2 < 1);
        }

        @Override
        public void update() {
            setFixedSize(new SVector(UISizes.STROKE_WEIGHT.get1f(), uiText.getTextSize()));
        }
    }

}
