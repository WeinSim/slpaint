package com.weinsim.slpaint.renderengine.drawcalls;

import org.lwjglx.util.vector.Matrix3f;

import com.weinsim.sutil.color.Color;
import com.weinsim.sutil.math.SVector;

public final class RectOutlineDrawCall extends RectDrawCall {

    public final double strokeWeight;

    public RectOutlineDrawCall(SVector position, double depth, SVector size, Matrix3f uiMatrix,
            ClipAreaInfo clipAreaInfo, Color color1, double strokeWeight, Color color2, double checkerboardSize,
            boolean applyCheckerboard) {

        super(position, depth, size, uiMatrix, clipAreaInfo, color1, color2, checkerboardSize, applyCheckerboard);
        this.strokeWeight = strokeWeight;
    }

}
