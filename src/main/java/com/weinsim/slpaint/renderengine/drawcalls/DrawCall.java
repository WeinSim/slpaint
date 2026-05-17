package com.weinsim.slpaint.renderengine.drawcalls;

import org.lwjglx.util.vector.Matrix3f;

import com.weinsim.slpaint.renderengine.bufferobjects.UBOEntry;
import com.weinsim.sutil.SUtil;
import com.weinsim.sutil.math.SVector;

public abstract sealed class DrawCall permits RectDrawCall, EllipseDrawCall, HSLDrawCall, ImageDrawCall, TextDrawCall {

    private int dataIndex;

    public final SVector position;
    public final double depth;
    public final SVector size;
    public final ClipAreaInfo clipAreaInfo;
    public final Matrix3f uiMatrix;

    public DrawCall(SVector position, double depth, SVector size, ClipAreaInfo clipAreaInfo, Matrix3f uiMatrix) {
        this.position = position;
        this.depth = depth;
        this.size = size;
        this.clipAreaInfo = clipAreaInfo;
        this.uiMatrix = uiMatrix;
    }

    public abstract UBOEntry getGroupAttributes();

    public abstract boolean usesAlpha();

    public int getDataIndex() {
        return dataIndex;
    }

    public void setDataIndex(int dataIndex) {
        this.dataIndex = dataIndex;
    }

    /**
     * 
     * @return -1 if {@code c1} needs to happen <i>before</i> {@code c2}, 1 if
     *         {@code c1} DrawCall needs to happen <i>after</i> {@code c2} and 0 if
     *         their order is irrelevant.
     */
    public static int alphaConflicts(DrawCall c1, DrawCall c2) {
        if (c1.depth == c2.depth)
            return 0;

        // TODO: shouldn't we respect the drawcalls' ui matrices here???
        if (!SUtil.rectsOverlap(c1.position, c1.size, c2.position, c2.size))
            return 0;

        if (!c1.clipAreaInfo.overlaps(c2.clipAreaInfo))
            return 0;

        DrawCall back, front;
        if (c1.depth > c2.depth) {
            back = c1;
            front = c2;
        } else {
            back = c2;
            front = c1;
        }

        if (!front.usesAlpha())
            return 0;

        return c1 == back ? -1 : 1;
    }
}