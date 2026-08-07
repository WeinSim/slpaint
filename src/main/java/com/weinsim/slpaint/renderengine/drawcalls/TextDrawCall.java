package com.weinsim.slpaint.renderengine.drawcalls;

import org.lwjglx.util.vector.Matrix3f;

import com.weinsim.slpaint.renderengine.bufferobjects.UBOEntry;
import com.weinsim.slpaint.renderengine.font.TextFont;
import com.weinsim.sutil.color.Color;
import com.weinsim.sutil.math.SVector;

public final class TextDrawCall extends DrawCall {

    public final double relativeSize;
    public final Color color;
    public final String text;
    public final TextFont font;

    public TextDrawCall(SVector position, double depth, double relativeSize, Matrix3f uiMatrix,
            ClipAreaInfo clipAreaInfo, Color color, String text, TextFont font) {

        // TODO: use TextFont.textWidth() to determine size
        super(position, depth, new SVector(), clipAreaInfo, uiMatrix);

        this.relativeSize = relativeSize;
        this.color = color;
        this.text = text;
        this.font = font;
    }

    @Override
    public UBOEntry getGroupAttributes() {
        boolean clip = clipAreaInfo.isEnabled();
        SVector boundingBoxPos = clipAreaInfo.getPosition();
        SVector boundingBoxSize = clipAreaInfo.getSize();
        UBOEntry ubo = new UBOEntry(24 * Float.BYTES);
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
        ubo.put(color);
        ubo.put(uiMatrix);
        ubo.put(relativeSize);
        ubo.putPadding(3 * Float.BYTES);
        return ubo.finish();
    }

    @Override
    public boolean usesAlpha() {
        return true;
    }

}
