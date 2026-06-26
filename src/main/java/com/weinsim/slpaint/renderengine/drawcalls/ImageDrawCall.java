package com.weinsim.slpaint.renderengine.drawcalls;

import org.lwjglx.util.vector.Matrix3f;

import com.weinsim.slpaint.renderengine.bufferobjects.UBOEntry;
import com.weinsim.sutil.math.SVector;

public final class ImageDrawCall extends DrawCall {

    public final int textureID;
    public int samplerID;

    public ImageDrawCall(SVector position, double depth, SVector size, Matrix3f uiMatrix, ClipAreaInfo clipAreaInfo,
            int textureID) {

        super(position, depth, size, clipAreaInfo, uiMatrix);

        this.textureID = textureID;
    }

    @Override
    public UBOEntry getGroupAttributes() {
        boolean clip = clipAreaInfo.isEnabled();
        SVector boundingBoxPos = clipAreaInfo.getPosition();
        SVector boundingBoxSize = clipAreaInfo.getSize();
        UBOEntry ubo = new UBOEntry(8 * Float.BYTES);
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
        ubo.put(samplerID);
        ubo.putPadding(3 * Integer.BYTES);
        return ubo.finish();
    }

    @Override
    public boolean usesAlpha() {
        return true;
    }

}
