package com.weinsim.slpaint.renderengine.drawcalls;

import org.lwjglx.util.vector.Matrix3f;

import com.weinsim.slpaint.renderengine.bufferobjects.UBOEntry;
import com.weinsim.sutil.math.SVector;

public final class HSLDrawCall extends DrawCall {

    public static final int HUE_SAT_FIELD_CIRC = 0,
            HUE_SAT_FIELD_RECT = 1,
            LIGHTNESS_SCALE = 2,
            ALPHA_SCALE = 3;

    public static final int HSL = 0,
            HSV = 4;

    public static final int VERTICAL = 0,
            HORIZONTAL = 8;

    public final SVector color;
    public final int flags;

    public HSLDrawCall(SVector position, double depth, SVector size, Matrix3f uiMatrix, ClipAreaInfo clipAreaInfo,
            SVector color, int flags) {

        super(position, depth, size, clipAreaInfo, uiMatrix);

        this.color = color;
        this.flags = flags;
    }

    @Override
    public UBOEntry getGroupAttributes() {
        boolean clip = clipAreaInfo.isEnabled();
        SVector boundingBoxPos = clipAreaInfo.getPosition();
        SVector boundingBoxSize = clipAreaInfo.getSize();
        UBOEntry ubo = new UBOEntry(4 * Float.BYTES);
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
        return ubo.finish();
    }

    @Override
    public boolean usesAlpha() {
        return ((flags & 0x03) == HUE_SAT_FIELD_CIRC) |
                ((flags & 0x03) == ALPHA_SCALE);
    }
}