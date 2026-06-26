package com.weinsim.slpaint.renderengine.bufferobjects;

import static org.lwjgl.opengl.GL15.*;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;

public class IndexVBO extends VBO {

    private final int[] indices;

    public IndexVBO(int... indices) {
        this.indices = indices;

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboID);
        IntBuffer buffer = BufferUtils.createIntBuffer(indices.length);
        buffer.put(indices);
        buffer.flip();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public int indexCount() {
        return indices.length;
    }

}
