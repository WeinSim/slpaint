package com.weinsim.slpaint.renderengine.drawcalls;

import org.lwjglx.util.vector.Matrix3f;
import org.lwjglx.util.vector.Vector4f;

import com.weinsim.slpaint.renderengine.bufferobjects.UBOEntry;
import com.weinsim.sutil.math.SVector;

public abstract sealed class RectDrawCall extends DrawCall permits RectFillDrawCall, RectOutlineDrawCall {

    public final Vector4f color1;
    public final Vector4f color2;
    public final double checkerboardSize;
    public final boolean applyCheckerboard;

    public RectDrawCall(SVector position, double depth, SVector size, Matrix3f uiMatrix, ClipAreaInfo clipAreaInfo,
            Vector4f color1, Vector4f color2, double checkerboardSize, boolean applyCheckerboard) {

        super(position, depth, size, clipAreaInfo, uiMatrix);

        this.color1 = color1;
        this.color2 = color2;
        this.checkerboardSize = checkerboardSize;
        this.applyCheckerboard = applyCheckerboard;
    }

    @Override
    public UBOEntry getGroupAttributes() {
        boolean clip = clipAreaInfo.isEnabled();
        SVector boundingBoxPos = clipAreaInfo.getPosition();
        SVector boundingBoxSize = clipAreaInfo.getSize();
        UBOEntry ubo = new UBOEntry(12 * Float.BYTES);
        if (clip) {
            ubo.put(boundingBoxPos.x);
            ubo.put(boundingBoxPos.y);
            ubo.put(boundingBoxPos.x + boundingBoxSize.x);
            ubo.put(boundingBoxPos.y + boundingBoxSize.y);
        } else {
            ubo.put(-Float.MAX_VALUE);
            ubo.put(-Float.MAX_VALUE);
            ubo.put(Float.MAX_VALUE);
            ubo.put(Float.MAX_VALUE);
        }
        ubo.put(color2);
        ubo.put(applyCheckerboard ? (float) checkerboardSize : -1);
        ubo.putPadding(3 * Float.BYTES);
        return ubo.finish();
    }

    @Override
    public boolean usesAlpha() {
        if (applyCheckerboard) {
            return color1.w < 1 || color2.w < 1;
        } else {
            return color1.w < 1;
        }
    }

}
