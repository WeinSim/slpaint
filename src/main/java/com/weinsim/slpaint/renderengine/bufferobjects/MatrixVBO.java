package com.weinsim.slpaint.renderengine.bufferobjects;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL33.*;

public final class MatrixVBO extends AttributeVBO {

    /**
     * Use {@code coordinateSize = n} for an nxn matrix.
     */
    public MatrixVBO(String attribuetName, int attributeNumber, int coordinateSize, VBOType type) {
        super(attribuetName, attributeNumber, coordinateSize, type);
    }

    @Override
    public void init() {
        bind();
        int stride = coordinateSize * coordinateSize * Float.BYTES;
        int divisor = switch (type) {
            case VERTEX -> 0;
            case INSTANCE -> 1;
        };
        for (int i = 0; i < coordinateSize; i++) {
            int offset = coordinateSize * i * Float.BYTES;
            glVertexAttribPointer(attributeNumber + i, coordinateSize, GL_FLOAT, false, stride, offset);
            glVertexAttribDivisor(attributeNumber + i, divisor);
        }
        unbind();
    }

    @Override
    protected int getBufferSize(int vertexCount) {
        final int bytes = 4;
        return bytes * vertexCount * coordinateSize * coordinateSize;
    }

    @Override
    public int getNumAttributes() {
        return coordinateSize;
    }

}
