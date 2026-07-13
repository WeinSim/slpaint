package com.weinsim.slpaint.ui.components.toolContainers;

import com.weinsim.slpaint.main.apps.MainApp;
import com.weinsim.slpaint.main.tools.ImageTool;
import com.weinsim.slpaint.main.tools.SelectionTool;
import com.weinsim.sutil.math.SVector;
import com.weinsim.sutil.ui.elements.UIImage;

public final class SelectionToolContainer extends DragToolContainer<SelectionTool> {

    public SelectionToolContainer(MainApp app) {
        super(ImageTool.SELECTION, app);

        zeroMargin();

        add(new SelectionImage());
    }

    private class SelectionImage extends UIImage {

        SelectionImage() {
            super(() -> ImageTool.SELECTION.getSelection().getTextureID(), new SVector());
            setVisibilitySupplier(() -> ImageTool.SELECTION.getSelection() != null);
        }

        // @Override
        // public void update() {
        // super.update();
        // setTextureID(ImageTool.SELECTION.getSelection().textureID);
        // }

        @Override
        public void setPreferredSize() {
            int width = ImageTool.SELECTION.getWidth(),
                    height = ImageTool.SELECTION.getHeight();

            size.set(width, height);
            size.scale(app.getImageZoom());
        }
    }

}
