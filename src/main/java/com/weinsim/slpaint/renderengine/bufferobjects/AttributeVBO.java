package com.weinsim.slpaint.renderengine.bufferobjects;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;

public abstract class AttributeVBO extends VBO {

    protected final String attributeName;
    protected final int coordinateSize;
    protected final VBOType type;
    protected final int attributeNumber;

    protected int vertexCount;
    protected ByteBuffer buffer;

    public AttributeVBO(String attribuetName, int attributeNumber, int coordinateSize, VBOType type) {
        super();
        this.attributeName = attribuetName;
        this.attributeNumber = attributeNumber;
        this.coordinateSize = coordinateSize;
        this.type = type;
    }

    public abstract void init();

    public void createBuffer(int vertexCount) {
        buffer = BufferUtils.createByteBuffer(getBufferSize(vertexCount));
        this.vertexCount = vertexCount;
    }

    public void putData(Object data) {
        put(data, coordinateSize, buffer, false);
    }

    public void storeDataInAttributeList() {
        bind();
        buffer.flip();
        glBufferData(GL_ARRAY_BUFFER, buffer, 
            switch (type) {
                case VERTEX -> GL_STATIC_DRAW;
                case INSTANCE -> GL_DYNAMIC_DRAW;
            }
        );
        unbind();
    }

    protected int getBufferSize(int vertexCount) {
        final int bytes = 4; // float and int both use 4 bytes
        return bytes * vertexCount * coordinateSize;
    }

    public int getNumAttributes() {
        return 1;
    }

    public void bind() {
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
    }

    public void unbind() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void enable() {
        for (int i = 0; i < getNumAttributes(); i++)
            glEnableVertexAttribArray(attributeNumber + i);
    }

    public void disable() {
        for (int i = 0; i < getNumAttributes(); i++)
            glDisableVertexAttribArray(attributeNumber + i);
    }

    public VBOType type() {
        return type;
    }

    public int vertexCount() {
        return vertexCount;
    }

    public String attributeName() {
        return attributeName;
    }

}
