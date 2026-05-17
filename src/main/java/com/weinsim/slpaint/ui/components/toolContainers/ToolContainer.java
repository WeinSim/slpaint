package com.weinsim.slpaint.ui.components.toolContainers;

import com.weinsim.slpaint.main.apps.MainApp;
import com.weinsim.slpaint.main.tools.ImageTool;
import com.weinsim.sutil.ui.elements.UIFloatContainer;

public sealed class ToolContainer<T extends ImageTool> extends UIFloatContainer
        permits DragToolContainer, PencilToolContainer, LineToolContainer {

    protected final T tool;

    protected MainApp app;

    public ToolContainer(T tool, MainApp app) {
        super(0, 0);

        this.tool = tool;
        this.app = app;

        noOutline();
        noBackground();

        setVisibilitySupplier(() -> app.getActiveTool() == tool && (tool.getState() & getVisibleStates()) != 0);
    }

    protected int getVisibleStates() {
        return 0;
    }
}