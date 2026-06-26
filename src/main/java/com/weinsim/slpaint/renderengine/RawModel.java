package com.weinsim.slpaint.renderengine;

import static org.lwjgl.opengl.GL30.*;

import java.util.ArrayList;
import java.util.HashMap;

import com.weinsim.slpaint.renderengine.bufferobjects.AttributeVBO;
import com.weinsim.slpaint.renderengine.bufferobjects.FloatVBO;
import com.weinsim.slpaint.renderengine.bufferobjects.IntVBO;
import com.weinsim.slpaint.renderengine.bufferobjects.MatrixVBO;
import com.weinsim.slpaint.renderengine.bufferobjects.VBO;
import com.weinsim.slpaint.renderengine.bufferobjects.VBOType;

public class RawModel implements Cleanable {

    private final int vaoID;

    private final HashMap<String, AttributeVBO> vbos;

    private int vertexCount = -1;
    private int instanceCount = -1;

    public RawModel(ArrayList<AttributeVBO> vbos) {
        vaoID = glGenVertexArrays();
        bind();

        this.vbos = new HashMap<>();
        for (AttributeVBO vbo : vbos) {
            this.vbos.put(vbo.attributeName(), vbo);
            vbo.init();
        }

        unbind();
    }

    public void initVertexVBOs(int vertexCount) {
        this.vertexCount = vertexCount;
        initVBOs(vertexCount, VBOType.VERTEX);
    }

    public void initInstanceVBOs(int instanceCount) {
        this.instanceCount = instanceCount;
        initVBOs(instanceCount, VBOType.INSTANCE);
    }

    public void finishVertexVBOs() {
        finishVBOs(VBOType.VERTEX);
    }

    public void finishInstanceVBOs() {
        finishVBOs(VBOType.INSTANCE);
    }

    private void initVBOs(int count, VBOType type) {
        for (AttributeVBO vbo : vbos())
            if (vbo.type() == type)
                vbo.createBuffer(count);
    }

    private void finishVBOs(VBOType type) {
        for (AttributeVBO vbo : vbos())
            if (vbo.type() == type)
                vbo.storeDataInAttributeList();
    }

    public AttributeVBO getVBO(String name) {
        AttributeVBO vbo = vbos.get(name);
        if (vbo == null)
            vboNotFount(name);
        return vbo;
    }

    public FloatVBO getFloatVBO(String name) {
        AttributeVBO vbo = getVBO(name);
        if (vbo instanceof FloatVBO floatVBO)
            return floatVBO;
        invalidVBODatatype(name, "FloatVBO", vbo.getClass().getSimpleName());
        return null;
    }

    public MatrixVBO getMatrixVBO(String name) {
        AttributeVBO vbo = getVBO(name);
        if (vbo instanceof MatrixVBO matrixVBO)
            return matrixVBO;
        invalidVBODatatype(name, "MatrixVBO", vbo.getClass().getSimpleName());
        return null;
    }

    public IntVBO getIntVBO(String name) {
        AttributeVBO vbo = getVBO(name);
        if (vbo instanceof IntVBO intVBO)
            return intVBO;
        invalidVBODatatype(name, "IntVBO", vbo.getClass().getSimpleName());
        return null;
    }

    public void bind() {
        glBindVertexArray(vaoID);
    }

    public void unbind() {
        glBindVertexArray(0);
    }

    @Override
    public void cleanUp() {
        for (VBO vbo : vbos())
            vbo.cleanUp();

        glDeleteVertexArrays(vaoID);
    }

    public int vertexCount() {
        return vertexCount;
    }

    public int instanceCount() {
        return instanceCount;
    }

    public void enableVBOs() {
        for (AttributeVBO vbo : vbos())
            vbo.enable();
    }

    public void disableVBOs() {
        for (AttributeVBO vbo : vbos())
            vbo.enable();
    }

    public int vaoID() {
        return vaoID;
    }

    private Iterable<AttributeVBO> vbos() {
        return vbos.values();
    }

    private static void vboNotFount(String name) {
        final String baseStr = "VBO \"%s\" does not exist";
        throw new RuntimeException(String.format(baseStr, name));
    }

    private static void invalidVBODatatype(String name, String expected, String got) {
        final String baseStr = "Unexpected datatype for VBO \"%s\". Expected %s, got %s.";
        throw new RuntimeException(String.format(baseStr, name, expected, got));
    }

}
