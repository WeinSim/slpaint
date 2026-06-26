package com.weinsim.slpaint.main.tools;

import static org.lwjgl.glfw.GLFW.*;

public final class LineTool extends ImageTool {

    public static final LineTool INSTANCE = new LineTool();

    public static final int INITIAL_DRAG = 0x02, IDLE = 0x04;

    public static final int MIN_SIZE = 1, MAX_SIZE = 16;

    private int size = 1;

    private int x1, y1;
    private int x2, y2;

    private Draggable drag1, drag2;

    private LineTool() {
        super();

        drag1 = createDrag1();
        drag2 = createDrag2();
    }

    @Override
    public void click(int x, int y, int mouseButton) {
        if (state == NONE) {
            if (mouseButton == GLFW_MOUSE_BUTTON_LEFT) {
                x1 = x;
                y1 = y;

                state = INITIAL_DRAG;
            }
        } else {
            finish();
        }
    }

    @Override
    public void finish() {
        if (state == IDLE) {
            app.drawLine(x1, y1, x2, y2, size, app.getPrimaryColor());
            app.addImageSnapshot();
            state = NONE;
        }
    }

    public void cancel() {
        state = NONE;
    }

    @Override
    public void createKeyboardShortcuts() {
        addShortcut("line_finish", GLFW_KEY_ESCAPE, 0, IDLE, this::finish);
    }

    @Override
    public String getName() {
        return "Line";
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = Math.clamp(size, MIN_SIZE, MAX_SIZE);
    }

    public Draggable getDrag1() {
        return drag1;
    }

    public Draggable getDrag2() {
        return drag2;
    }

    private Draggable createDrag1() {
        return new Draggable() {
            @Override
            public void startDragging() {
            }

            @Override
            public void finishDragging() {
            }

            @Override
            public int getX() {
                return x1;
            }

            @Override
            public void setX(int x) {
                x1 = x;
            }

            @Override
            public int getY() {
                return y1;
            }

            @Override
            public void setY(int y) {
                y1 = y;
            }
        };
    }

    private Draggable createDrag2() {
        return new Draggable() {
            @Override
            public void startDragging() {
            }

            @Override
            public void finishDragging() {
            }

            @Override
            public int getX() {
                return x2;
            }

            @Override
            public void setX(int x) {
                x2 = x;
            }

            @Override
            public int getY() {
                return y2;
            }

            @Override
            public void setY(int y) {
                y2 = y;
            }
        };
    }

}
