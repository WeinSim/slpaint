package com.weinsim.slpaint.main.tools;

public interface Draggable {

    public void startDragging();

    public void finishDragging();

    public int getX();

    public void setX(int x);

    public int getY();

    public void setY(int y);
    
}
