package com.weinsim.slpaint.renderengine.renderers;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjglx.util.vector.Matrix3f;

import com.weinsim.slpaint.renderengine.bufferobjects.FloatVBO;
import com.weinsim.slpaint.renderengine.bufferobjects.IntVBO;
import com.weinsim.slpaint.renderengine.drawcalls.TextDrawCall;
import com.weinsim.slpaint.renderengine.font.TextFont;

public class TextRenderer extends InstanceShapeRenderer<TextDrawCall> {

    private final TextFont font;
    private int[] textureIDs;

    public TextRenderer(TextFont font) {
        super("text");

        this.font = font;
        try {
            textureIDs = font.loadTextures();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        shaderProgram.start();
        for (int i = 0; i < 4; i++) {
            String name = String.format("textureSamplers[%d]", i);
            shaderProgram.loadUniform(name, i);
        }
        shaderProgram.stop();
    }

    @Override
    public void render(Matrix3f viewMatrix) {
        for (int i = 0; i < textureIDs.length; i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, textureIDs[i]);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        }

        // This looks a little wasteful, but it only takes ~3 microseconds
        // long startTime = System.nanoTime();
        float[] uboData = font.getUBOData();
        ByteBuffer uboBuffer = BufferUtils.createByteBuffer(uboData.length * Float.BYTES);
        for (float f : uboData)
            uboBuffer.putFloat(f);
        uboBuffer.flip();
        // long duration = System.nanoTime() - startTime;
        // System.out.format("Text data upload: %.3f µs\n", duration * 1e-3);

        shaderProgram.start();
        shaderProgram.loadUBOData("FontData", uboBuffer);

        super.render(viewMatrix);
    }

    @Override
    protected void loadVBOs(ArrayList<Batch> batches, int vertexCount) {
        IntVBO gIndex = model.getIntVBO("gIndex");
        FloatVBO position = model.getFloatVBO("position");
        FloatVBO depth = model.getFloatVBO("depth");
        IntVBO charIndex = model.getIntVBO("charIndex");

        int batchIndex = 0;
        for (Batch batch : batches) {
            for (TextDrawCall drawCall : batch.getDrawCalls()) {
                drawCall.font.putCharsIntoVBOs(drawCall, gIndex, position, depth, charIndex, batchIndex);
            }
            batchIndex++;
        }
    }

    @Override
    protected int getNumInstances(Batch batch) {
        int numVertices = 0;
        for (TextDrawCall drawCall : batch.getDrawCalls()) {
            numVertices += drawCall.text.length();
        }
        return numVertices;
    }

}
