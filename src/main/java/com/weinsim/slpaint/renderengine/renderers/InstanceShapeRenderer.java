package com.weinsim.slpaint.renderengine.renderers;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL31.*;

import org.lwjglx.util.vector.Matrix3f;
import org.lwjglx.util.vector.Vector2f;

import com.weinsim.slpaint.renderengine.bufferobjects.FloatVBO;
import com.weinsim.slpaint.renderengine.drawcalls.DrawCall;
import com.weinsim.slpaint.renderengine.shader.ShaderType;

public abstract class InstanceShapeRenderer<C extends DrawCall> extends ShapeRenderer<C> {

    protected boolean useSRGB;

    public InstanceShapeRenderer(String name) {
        super(name, ShaderType.INSTANCE);
        useSRGB = false;

        initRawModel();
    }

    @Override
    public void render(Matrix3f viewMatrix) {
        shaderProgram.start();
        shaderProgram.loadUniform("viewMatrix", viewMatrix);

        glEnable(GL_BLEND);
        glBlendFuncSeparate(
                GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA,
                GL_ONE_MINUS_DST_ALPHA, GL_ONE);

        model.bind();
        model.enableVBOs();
        while (prepareNextDrawcall())
            glDrawArraysInstanced(GL_TRIANGLE_STRIP, 0, model.vertexCount(), model.instanceCount());
        model.disableVBOs();

        shaderProgram.stop();
    }

    protected void initRawModel() {
        initQuad();
    }

    protected void initQuad() {
        model.initVertexVBOs(4);

        FloatVBO cornerPos = model.getFloatVBO("cornerPos");
        cornerPos.putData(new Vector2f(0f, 0f));
        cornerPos.putData(new Vector2f(1f, 0f));
        cornerPos.putData(new Vector2f(0f, 1f));
        cornerPos.putData(new Vector2f(1f, 1f));

        model.finishVertexVBOs();
    }

}
