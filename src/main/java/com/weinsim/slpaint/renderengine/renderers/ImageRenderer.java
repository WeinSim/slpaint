package com.weinsim.slpaint.renderengine.renderers;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

import java.util.ArrayList;

import org.lwjglx.util.vector.Matrix3f;

import com.weinsim.slpaint.renderengine.bufferobjects.FloatVBO;
import com.weinsim.slpaint.renderengine.bufferobjects.IntVBO;
import com.weinsim.slpaint.renderengine.bufferobjects.MatrixVBO;
import com.weinsim.slpaint.renderengine.drawcalls.ImageDrawCall;

public class ImageRenderer extends InstanceShapeRenderer<ImageDrawCall> {

    private static final int NO_TEXTURE = -1;

    /**
     * Texture sampler i will sample from texture textureIDs[i]
     */
    private int[] textureIDs;

    public ImageRenderer() {
        super("image");

        textureIDs = new int[NUM_TEXTURE_UNITS];
        clearTextureIDs();

        shaderProgram.start();
        for (int i = 0; i < NUM_TEXTURE_UNITS; i++) {
            String name = String.format("textureSamplers[%d]", i);
            shaderProgram.loadUniform(name, i);
        }
        shaderProgram.stop();
    }

    @Override
    public void addShape(ImageDrawCall drawCall) {
        // determine sampler ID
        int samplerID = -1;
        for (int i = 0; i < NUM_TEXTURE_UNITS; i++) {
            if (textureIDs[i] == drawCall.textureID) {
                // use existing entry
                samplerID = i;
                break;
            }
            if (textureIDs[i] == NO_TEXTURE) {
                // create new entry
                textureIDs[i] = drawCall.textureID;
                samplerID = i;
                break;
            }
        }

        if (samplerID != -1)
            drawCall.samplerID = samplerID;
        else
            throw new RuntimeException("Maximum number of image textures used. Unable to draw image.");

        super.addShape(drawCall);
    }

    @Override
    public void render(Matrix3f viewMatrix) {
        for (int i = 0; i < textureIDs.length; i++) {
            int textureID = textureIDs[i];
            if (textureID != NO_TEXTURE) {
                glActiveTexture(GL_TEXTURE0 + i);
                glBindTexture(GL_TEXTURE_2D, textureID);

                // glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            }
        }

        super.render(viewMatrix);

        clearTextureIDs();
    }

    private void clearTextureIDs() {
        for (int i = 0; i < textureIDs.length; i++) {
            textureIDs[i] = NO_TEXTURE;
        }
    }

    @Override
    protected void loadVBOs(ArrayList<Batch> batches, int vertexCount) {
        IntVBO gIndex = model.getIntVBO("gIndex");
        MatrixVBO transformationMatrix = model.getMatrixVBO("transformationMatrix");
        FloatVBO position = model.getFloatVBO("position");
        FloatVBO depth = model.getFloatVBO("depth");
        FloatVBO size = model.getFloatVBO("size");

        int batchIndex = 0;
        for (Batch batch : batches) {
            for (ImageDrawCall drawCall : batch.getDrawCalls()) {
                gIndex.putData(batchIndex);
                transformationMatrix.putData(drawCall.uiMatrix);
                position.putData(drawCall.position);
                depth.putData(drawCall.depth);
                size.putData(drawCall.size);
            }
            batchIndex++;
        }
    }

    @Override
    public void cleanUp() {
        super.cleanUp();

        glDeleteTextures(textureIDs);
    }

}
