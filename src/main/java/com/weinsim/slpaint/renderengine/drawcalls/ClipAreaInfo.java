package com.weinsim.slpaint.renderengine.drawcalls;

import com.weinsim.sutil.SUtil;
import com.weinsim.sutil.math.SVector;

public class ClipAreaInfo {

    private boolean enabled;
    private SVector position;
    private SVector size;

    public ClipAreaInfo() {
        clear();
    }

    public ClipAreaInfo(ClipAreaInfo other) {
        this.enabled = other.enabled;
        position = new SVector(other.position);
        size = new SVector(other.size);
    }

    public void set(SVector position, SVector size) {
        this.position.set(position);
        this.size.set(size);
        enabled = true;
    }

    public void clear() {
        enabled = false;
        position = new SVector();
        size = new SVector();
    }

    public boolean overlaps(ClipAreaInfo other) {
        if (!isEnabled() || !other.isEnabled())
            return true;

        return SUtil.rectsOverlap(position, size, other.position, other.size);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof ClipAreaInfo clipArea) {
            if (enabled != clipArea.enabled) {
                return false;
            }

            if (!enabled) {
                return true;
            }

            return position.equals(clipArea.position) && size.equals(clipArea.size);
        }

        return false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public SVector getPosition() {
        return position;
    }

    public SVector getSize() {
        return size;
    }

    public void setUBOData(float[] array) {
        SVector position = enabled ? this.position : new SVector(0, 0);
        array[0] = (float) position.x;
        array[1] = (float) position.y;

        SVector size = enabled ? this.size : new SVector(100000, 100000);
        array[2] = (float) (position.x + size.x);
        array[3] = (float) (position.y + size.y);
    }

}
