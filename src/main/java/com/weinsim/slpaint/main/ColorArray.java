package com.weinsim.slpaint.main;

public class ColorArray {

    private Integer[] colors;

    public ColorArray(int capacity) {
        reset(capacity);
    }

    public void reset(int capacity) {
        colors = new Integer[capacity];
    }

    public void clear() {
        reset(colors.length);
    }

    public void addColor(int color) {
        for (int i = colors.length - 1; i >= 1; i--) {
            colors[i] = colors[i - 1];
        }
        colors[0] = color;
    }

    public Integer getColor(int index) {
        return colors[index];
    }

    public int getCapacity() {
        return colors.length;
    }

}
