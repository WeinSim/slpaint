package com.weinsim.slpaint.main.tools;

public interface Resizable extends Draggable {

    public boolean lockRatio();

    public int getWidth();

    public void setWidth(int width);

    public int getHeight();

    public void setHeight(int height);
}
