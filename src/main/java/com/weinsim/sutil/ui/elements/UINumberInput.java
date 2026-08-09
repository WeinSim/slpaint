package com.weinsim.sutil.ui.elements;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class UINumberInput extends UITextInput {

    // Kind of a low and random limit but it works for now
    private static final int MAX_LENGTH = 5;

    private String text;

    private IntSupplier valueSupplier;

    public UINumberInput(IntSupplier valueSupplier, IntConsumer valueConsumer) {
        super(null, null);

        this.valueSupplier = valueSupplier;

        // This is the same ugly design as in the constructor of UIContextMenu. Because
        // we cannot capture instance variables, we cannot pass () -> text as an
        // argument in the constructor.
        text = "";
        setTextUpdater(() -> text);
        setValueUpdater(
                (String s) -> {
                    if (s.length() > MAX_LENGTH)
                        s = s.substring(0, MAX_LENGTH);
                    text = s;
                    if (s.length() > 0) {
                        try {
                            valueConsumer.accept(Integer.parseInt(s));
                        } catch (NumberFormatException e) {
                            return;
                        }
                    }
                });
    }

    @Override
    public void handleEvents() {
        super.handleEvents();
        if (!isSelected())
            text = Integer.toString(valueSupplier.getAsInt());
    }

    @Override
    public void update() {
        super.update();
        double textSize = uiText.getTextSize();
        setHFixedSize(textSize * 3.3333);
    }

}
