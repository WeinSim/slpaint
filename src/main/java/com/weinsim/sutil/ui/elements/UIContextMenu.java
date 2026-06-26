package com.weinsim.sutil.ui.elements;

import com.weinsim.sutil.math.SVector;

public class UIContextMenu extends UIFloatMenu {

    private boolean visible;

    public UIContextMenu(UIContainer container, boolean scroll) {
        super(null, null, scroll);

        visibilitySupplier = () -> visible;
        closeAction = () -> visible = false;

        container.addRightClickAction(() -> {
            expandedLabel = null;
            visible = true;
            clearAnchors();
            SVector basePos = new SVector(container.mousePosition).sub(container.position);
            addAnchor(Anchor.TOP_LEFT, basePos);
            addAnchor(Anchor.TOP_RIGHT, basePos);
            addAnchor(Anchor.BOTTOM_LEFT, basePos);
            addAnchor(Anchor.BOTTOM_RIGHT, basePos);
        });
        container.add(this);
    }

}
