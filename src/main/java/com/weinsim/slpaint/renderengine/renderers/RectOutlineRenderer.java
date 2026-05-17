package com.weinsim.slpaint.renderengine.renderers;

import java.util.ArrayList;

import com.weinsim.slpaint.renderengine.bufferobjects.FloatVBO;
import com.weinsim.slpaint.renderengine.bufferobjects.IntVBO;
import com.weinsim.slpaint.renderengine.bufferobjects.MatrixVBO;
import com.weinsim.slpaint.renderengine.drawcalls.RectOutlineDrawCall;
import com.weinsim.sutil.math.SVector;

public class RectOutlineRenderer extends InstanceShapeRenderer<RectOutlineDrawCall> {

    private static final boolean INSET_OUTLINES = false;

    public RectOutlineRenderer() {
        super("rectOutline");
    }

    @Override
    protected void loadVBOs(ArrayList<Batch> batches, int vertexCount) {
        IntVBO gData = model.getIntVBO("gIndex");
        MatrixVBO transformationMatrix = model.getMatrixVBO("transformationMatrix");
        FloatVBO position = model.getFloatVBO("position");
        FloatVBO depth = model.getFloatVBO("depth");
        FloatVBO size = model.getFloatVBO("size_in");
        FloatVBO color1 = model.getFloatVBO("color1_in");
        FloatVBO strokeWeight = model.getFloatVBO("strokeWeight_in");

        int batchIndex = 0;
        for (Batch batch : batches) {
            for (RectOutlineDrawCall drawCall : batch.getDrawCalls()) {
                gData.putData(batchIndex);
                transformationMatrix.putData(drawCall.uiMatrix);
                position.putData(drawCall.position);
                depth.putData(drawCall.depth);
                size.putData(drawCall.size);
                color1.putData(drawCall.color1);
                strokeWeight.putData(drawCall.strokeWeight);
            }
            batchIndex++;
        }
    }

    @Override
    protected void initRawModel() {
        model.initVertexVBOs(10);

        FloatVBO cornerPos = model.getFloatVBO("cornerPos"),
                offset = model.getFloatVBO("offset");
        final float[] x = { 0, 0, 1, 1 },
                y = { 0, 1, 1, 0 };
        for (int i = 0; i < 5; i++) {
            SVector c = new SVector(x[i % 4], y[i % 4]);
            cornerPos.putData(c);
            cornerPos.putData(c);

            SVector oBase = new SVector(c.x - 0.5, c.y - 0.5); // points outwards
            if (INSET_OUTLINES) {
                offset.putData(new SVector());
                oBase.scale(-2);
                offset.putData(oBase);
            } else {
                offset.putData(oBase);
                oBase.scale(-1);
                offset.putData(oBase);
            }
        }

        model.finishVertexVBOs();
    }
}