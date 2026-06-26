package com.weinsim.slpaint.renderengine.renderers;

import java.util.ArrayList;

import com.weinsim.slpaint.renderengine.bufferobjects.FloatVBO;
import com.weinsim.slpaint.renderengine.bufferobjects.IntVBO;
import com.weinsim.slpaint.renderengine.bufferobjects.MatrixVBO;
import com.weinsim.slpaint.renderengine.drawcalls.RectFillDrawCall;

public class RectFillRenderer extends InstanceShapeRenderer<RectFillDrawCall> {

    public RectFillRenderer() {
        super("rectFill");
    }

    @Override
    protected void loadVBOs(ArrayList<Batch> batches, int vertexCount) {
        // IntVBO dataIndex = model.getIntVBO("dataIndex");
        IntVBO dataIndex = model.getIntVBO("gIndex");
        MatrixVBO transformationMatrix = model.getMatrixVBO("transformationMatrix");
        FloatVBO position = model.getFloatVBO("position");
        FloatVBO depth = model.getFloatVBO("depth");
        FloatVBO size = model.getFloatVBO("size");
        FloatVBO color1 = model.getFloatVBO("color1_in");

        int i = 0;
        for (Batch batch : batches) {
            for (RectFillDrawCall drawCall : batch.getDrawCalls()) {
                dataIndex.putData(i);
                transformationMatrix.putData(drawCall.uiMatrix);
                position.putData(drawCall.position);
                depth.putData(drawCall.depth);
                size.putData(drawCall.size);
                color1.putData(drawCall.color1);
            }
            i++;
        }
    }

}
