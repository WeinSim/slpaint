package com.weinsim.slpaint.main.tools;

import java.util.function.BooleanSupplier;

import com.weinsim.slpaint.main.apps.MainApp;
import com.weinsim.sutil.ui.UI;

public abstract sealed class ImageTool permits PencilTool, LineTool, DragTool, FillBucketTool, PipetteTool, MagicTool {

    public static final PencilTool PENCIL = PencilTool.INSTANCE;
    public static final LineTool LINE = LineTool.INSTANCE;
    public static final FillBucketTool FILL_BUCKET = FillBucketTool.INSTANCE;
    public static final PipetteTool PIPETTE = PipetteTool.INSTANCE;
    public static final SelectionTool SELECTION = SelectionTool.INSTANCE;
    public static final TextTool TEXT = TextTool.INSTANCE;
    public static final MagicTool MAGIC = MagicTool.INSTANCE;

    public static final ImageTool[] INSTANCES = MainApp.DEV_BUILD
            ? new ImageTool[] { PENCIL, LINE, FILL_BUCKET, PIPETTE, SELECTION, TEXT, MAGIC }
            : new ImageTool[] { PENCIL, LINE, FILL_BUCKET, PIPETTE, SELECTION, TEXT };

    public static final int NONE = 0x01;

    protected int state;

    protected MainApp app;

    protected ImageTool() {
        state = NONE;
    }

    public abstract void click(int x, int y, int mouseButton);

    public abstract void finish();

    public abstract void cancel();

    public abstract void createKeyboardShortcuts();

    public abstract String getName();

    public void setApp(MainApp app) {
        this.app = app;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    protected void addShortcut(String identifier, int key, int modifiers, int possibleStates, Runnable action) {
        Runnable shortcutAction = () -> {
            app.setActiveTool(this);
            action.run();
        };
        BooleanSupplier possible = () -> (getState() & possibleStates) != 0;
        UI.addKeyboardShortcut(identifier, key, modifiers, possible, shortcutAction);
    }

}
