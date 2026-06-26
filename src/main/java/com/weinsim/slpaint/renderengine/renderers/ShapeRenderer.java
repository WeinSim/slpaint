package com.weinsim.slpaint.renderengine.renderers;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjglx.util.vector.Matrix3f;

import com.weinsim.slpaint.renderengine.Cleanable;
import com.weinsim.slpaint.renderengine.RawModel;
import com.weinsim.slpaint.renderengine.bufferobjects.UBOEntry;
import com.weinsim.slpaint.renderengine.bufferobjects.UniformBufferObject;
import com.weinsim.slpaint.renderengine.drawcalls.DrawCall;
import com.weinsim.slpaint.renderengine.shader.ShaderProgram;
import com.weinsim.slpaint.renderengine.shader.ShaderType;

public abstract class ShapeRenderer<C extends DrawCall> implements Cleanable {

    protected static final int NUM_TEXTURE_UNITS = glGetInteger(GL_MAX_TEXTURE_IMAGE_UNITS);

    protected final RawModel model;
    protected final ShaderProgram shaderProgram;

    protected ArrayList<Batch> batches;

    public ShapeRenderer(String shaderName, ShaderType shaderType) {
        shaderProgram = new ShaderProgram(shaderName, shaderType);
        model = shaderProgram.getRawModel();
        batches = new ArrayList<>();
    }

    public void addShape(C drawCall) {
        UBOEntry newUBO = drawCall.getGroupAttributes();
        Batch existingBatch = getExistingBatch(newUBO);
        if (existingBatch == null) {
            existingBatch = new Batch(newUBO);
            batches.add(existingBatch);
        }
        existingBatch.addDrawCall(drawCall);
    }

    protected Batch getExistingBatch(UBOEntry uboEntry) {
        for (Batch batch : batches) {
            if (batch.getGroupAttributes().equals(uboEntry))
                return batch;
        }
        return null;
    }

    protected boolean prepareNextDrawcall() {
        if (batches.isEmpty())
            return false;

        int numBatches = Math.min(batches.size(), UniformBufferObject.UBO_ARRAY_LENGTH);
        int startIndex = batches.size() - numBatches;

        ArrayList<Batch> nextBatches = new ArrayList<>();
        int instanceCount = 0;

        int uboSize = batches.get(startIndex).getGroupAttributes().size() * numBatches;
        ByteBuffer uboBuffer = BufferUtils.createByteBuffer(uboSize);
        for (int i = 0; i < numBatches; i++) {
            Batch batch = batches.removeLast();
            uboBuffer.put(batch.getGroupAttributes().getBuffer());
            nextBatches.add(batch);
            instanceCount += getNumInstances(batch);
        }
        uboBuffer.flip();
        shaderProgram.loadUBOData("GroupAttributes", uboBuffer);

        model.initInstanceVBOs(instanceCount);
        loadVBOs(nextBatches, instanceCount);
        model.finishInstanceVBOs();

        return true;
    }

    public abstract void render(Matrix3f viewMatrix);

    protected abstract void loadVBOs(ArrayList<Batch> batches, int vertexCount);

    protected int getNumInstances(Batch batch) {
        return batch.getDrawCalls().size();
    }

    @Override
    public void cleanUp() {
        model.cleanUp();
        shaderProgram.cleanUp();
    }

    /**
     * Represents a list of draw calls that share a UBO entry.
     */
    protected class Batch {

        private UBOEntry groupAttributes;
        private ArrayList<C> drawCalls;

        public Batch(UBOEntry groupAttributes) {
            this.groupAttributes = groupAttributes;
            drawCalls = new ArrayList<>();
        }

        public void addDrawCall(C drawCall) {
            drawCalls.add(drawCall);
        }

        public UBOEntry getGroupAttributes() {
            return groupAttributes;
        }

        public ArrayList<C> getDrawCalls() {
            return drawCalls;
        }
    }

}
