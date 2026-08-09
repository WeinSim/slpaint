package com.weinsim.slpaint.main;

import com.weinsim.sutil.color.Color;

public class ColorArray {

    private Color[] colors;

    public ColorArray(int capacity) {
        reset(capacity);
    }

    public void reset(int capacity) {
        colors = new Color[capacity];
    }

    public void clear() {
        reset(colors.length);
    }

    public void addColor(Color color) {
        for (int i = colors.length - 1; i >= 1; i--)
            colors[i] = colors[i - 1];
        colors[0] = color;
    }

    public Color getColor(int index) {
        return colors[index];
    }

    public int getCapacity() {
        return colors.length;
    }

}
