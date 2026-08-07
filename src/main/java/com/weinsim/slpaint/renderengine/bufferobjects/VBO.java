package com.weinsim.slpaint.renderengine.bufferobjects;

import static org.lwjgl.opengl.GL15.*;

import java.nio.ByteBuffer;

import org.lwjglx.util.vector.Matrix2f;
import org.lwjglx.util.vector.Matrix3f;
import org.lwjglx.util.vector.Matrix4f;
import org.lwjglx.util.vector.Vector2f;
import org.lwjglx.util.vector.Vector3f;
import org.lwjglx.util.vector.Vector4f;

import com.weinsim.slpaint.renderengine.Cleanable;
import com.weinsim.sutil.color.Color;
import com.weinsim.sutil.color.LinearRGB;
import com.weinsim.sutil.math.SVector;

public abstract class VBO implements Cleanable {

    protected final int vboID;

    public VBO() {
        vboID = glGenBuffers();
    }

    public static void put(Object data, int coordinateSize, ByteBuffer buffer, boolean addStd140Padding) {
        String datatype = data.getClass().getSimpleName();
        switch (coordinateSize) {
            case 1 -> {
                switch (data) {
                    case Integer i -> buffer.putInt(i);
                    case Float f -> buffer.putFloat(f);
                    case Double d -> buffer.putFloat(d.floatValue());
                    default -> invalidDatatype(datatype, coordinateSize);
                }
            }
            case 2 -> {
                switch (data) {
                    case SVector s -> {
                        buffer.putFloat((float) s.x);
                        buffer.putFloat((float) s.y);
                    }
                    case Vector2f v -> {
                        buffer.putFloat(v.x);
                        buffer.putFloat(v.y);
                    }
                    case Matrix2f m -> {
                        buffer.putFloat(m.m00);
                        buffer.putFloat(m.m01);
                        buffer.putFloat(m.m10);
                        buffer.putFloat(m.m11);
                    }
                    default -> invalidDatatype(datatype, coordinateSize);
                }
            }
            case 3 -> {
                switch (data) {
                    case SVector s -> {
                        buffer.putFloat((float) s.x);
                        buffer.putFloat((float) s.y);
                        buffer.putFloat((float) s.z);
                    }
                    case Vector3f v -> {
                        buffer.putFloat(v.x);
                        buffer.putFloat(v.y);
                        buffer.putFloat(v.z);
                    }
                    case Color c -> {
                        LinearRGB sRGB = c.linearRGB();
                        buffer.putFloat((float) sRGB.red());
                        buffer.putFloat((float) sRGB.green());
                        buffer.putFloat((float) sRGB.blue());
                    }
                    case Matrix3f m -> {
                        buffer.putFloat(m.m00);
                        buffer.putFloat(m.m01);
                        buffer.putFloat(m.m02);
                        if (addStd140Padding)
                            putPadding(Float.BYTES, buffer);
                        buffer.putFloat(m.m10);
                        buffer.putFloat(m.m11);
                        buffer.putFloat(m.m12);
                        if (addStd140Padding)
                            putPadding(Float.BYTES, buffer);
                        buffer.putFloat(m.m20);
                        buffer.putFloat(m.m21);
                        buffer.putFloat(m.m22);
                        if (addStd140Padding)
                            putPadding(Float.BYTES, buffer);
                    }
                    default -> invalidDatatype(datatype, coordinateSize);
                }
            }
            case 4 -> {
                switch (data) {
                    case Vector4f v -> {
                        buffer.putFloat(v.x);
                        buffer.putFloat(v.y);
                        buffer.putFloat(v.z);
                        buffer.putFloat(v.w);
                    }
                    case Color c -> {
                        LinearRGB sRGB = c.linearRGB();
                        buffer.putFloat((float) sRGB.red());
                        buffer.putFloat((float) sRGB.green());
                        buffer.putFloat((float) sRGB.blue());
                        buffer.putFloat((float) sRGB.alpha());
                    }
                    case Matrix4f m -> {
                        buffer.putFloat(m.m00);
                        buffer.putFloat(m.m01);
                        buffer.putFloat(m.m02);
                        buffer.putFloat(m.m03);
                        buffer.putFloat(m.m10);
                        buffer.putFloat(m.m11);
                        buffer.putFloat(m.m12);
                        buffer.putFloat(m.m13);
                        buffer.putFloat(m.m20);
                        buffer.putFloat(m.m21);
                        buffer.putFloat(m.m22);
                        buffer.putFloat(m.m23);
                        buffer.putFloat(m.m30);
                        buffer.putFloat(m.m31);
                        buffer.putFloat(m.m32);
                        buffer.putFloat(m.m33);
                    }
                    default -> invalidDatatype(datatype, coordinateSize);
                }
            }
            case 0 -> throw new IllegalArgumentException("Missing coordinate size");
            default -> invalidDatatype(datatype, coordinateSize);
        }
    }

    public static void putPadding(int numBytes, ByteBuffer buffer) {
        for (int i = 0; i < numBytes; i++) {
            buffer.put((byte) 0x00);
        }
    }

    private static void invalidDatatype(String datatype, int coordinateSize) {
        final String exceptionBaseText = "Invalid datatype / coordinate size combination (%d / %s)";
        throw new IllegalArgumentException(String.format(exceptionBaseText, coordinateSize, datatype));
    }

    @Override
    public void cleanUp() {
        glDeleteBuffers(vboID);
    }

}
