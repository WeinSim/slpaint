package com.weinsim.slpaint.renderengine.renderers;

import java.util.ArrayList;

import com.weinsim.slpaint.renderengine.bufferobjects.FloatVBO;
import com.weinsim.slpaint.renderengine.bufferobjects.IntVBO;
import com.weinsim.slpaint.renderengine.bufferobjects.MatrixVBO;
import com.weinsim.slpaint.renderengine.drawcalls.HSLDrawCall;

public class HSLRenderer extends InstanceShapeRenderer<HSLDrawCall> {

    public HSLRenderer() {
        super("hsl");
    }

    @Override
    protected void loadVBOs(ArrayList<Batch> batches, int vertexCount) {
        IntVBO gIndex = model.getIntVBO("gIndex");
        MatrixVBO transformationMatrix = model.getMatrixVBO("transformationMatrix");
        FloatVBO position = model.getFloatVBO("position");
        FloatVBO depth = model.getFloatVBO("depth");
        FloatVBO size = model.getFloatVBO("size");
        FloatVBO color = model.getFloatVBO("color_in");
        IntVBO flags = model.getIntVBO("flags_in");

        int batchIndex = 0;
        for (Batch batch : batches) {
            for (HSLDrawCall drawCall : batch.getDrawCalls()) {
                gIndex.putData(batchIndex);
                transformationMatrix.putData(drawCall.uiMatrix);
                position.putData(drawCall.position);
                depth.putData(drawCall.depth);
                // depth.putData(0.8f);
                size.putData(drawCall.size);

                color.putData(drawCall.color);
                flags.putData(drawCall.flags);
            }
            batchIndex++;
        }
    }

}
