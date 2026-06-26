package com.weinsim.slpaint.renderengine.bufferobjects;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL33.*;

public final class FloatVBO extends AttributeVBO {

    public FloatVBO(String attribuetName, int attributeNumber, int coordinateSize, VBOType type) {
        super(attribuetName, attributeNumber, coordinateSize, type);
    }

    @Override
    public void init() {
        bind();
        glVertexAttribPointer(attributeNumber, coordinateSize, GL_FLOAT, false, 0, 0);
        glVertexAttribDivisor(attributeNumber,
                switch (type) {
                    case VERTEX -> 0;
                    case INSTANCE -> 1;
                });
        unbind();
    }

}
