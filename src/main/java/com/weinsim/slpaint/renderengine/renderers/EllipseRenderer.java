package com.weinsim.slpaint.renderengine.renderers;

import java.util.ArrayList;

import org.lwjglx.util.vector.Vector4f;

import com.weinsim.slpaint.renderengine.bufferobjects.FloatVBO;
import com.weinsim.slpaint.renderengine.bufferobjects.IntVBO;
import com.weinsim.slpaint.renderengine.bufferobjects.MatrixVBO;
import com.weinsim.slpaint.renderengine.drawcalls.EllipseDrawCall;

public class EllipseRenderer extends InstanceShapeRenderer<EllipseDrawCall> {

    public EllipseRenderer() {
        super("ellipse");
    }

    @Override
    protected void loadVBOs(ArrayList<Batch> batches, int vertexCount) {
        IntVBO gIndex = model.getIntVBO("gIndex");
        MatrixVBO transformationMatrix = model.getMatrixVBO("transformationMatrix");
        FloatVBO position = model.getFloatVBO("position");
        FloatVBO depth = model.getFloatVBO("depth");
        FloatVBO size = model.getFloatVBO("size");
        FloatVBO color = model.getFloatVBO("color_in");

        int batchIndex = 0;
        for (Batch batch : batches) {
            for (EllipseDrawCall drawCall : batch.getDrawCalls()) {
                gIndex.putData(batchIndex);
                transformationMatrix.putData(drawCall.uiMatrix);
                position.putData(drawCall.position);
                depth.putData(drawCall.depth);
                size.putData(drawCall.size);
                color.putData(new Vector4f(
                        drawCall.color.x,
                        drawCall.color.y,
                        drawCall.color.z,
                        drawCall.color.w));
            }
            batchIndex++;
        }
    }

}
