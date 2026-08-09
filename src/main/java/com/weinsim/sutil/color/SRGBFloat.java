package com.weinsim.sutil.color;

import org.lwjglx.util.vector.Vector4f;

import com.weinsim.sutil.math.SVector;

public record SRGBFloat(double red, double green, double blue, double alpha) implements ColorType {

    @Override
    public SRGBFloat toSRGBFloat() {
        return this;
    }

    public SVector toSVector() {
        return new SVector(red, green, blue);
    }

    public Vector4f toVector4f() {
        return new Vector4f((float) red, (float) green, (float) blue, (float) alpha);
    }

}
