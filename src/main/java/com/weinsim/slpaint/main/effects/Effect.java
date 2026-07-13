package com.weinsim.slpaint.main.effects;

import java.util.List;

import com.weinsim.slpaint.renderengine.Cleanable;
import com.weinsim.slpaint.renderengine.RawModel;
import com.weinsim.slpaint.renderengine.bufferobjects.FloatVBO;
import com.weinsim.slpaint.renderengine.shader.ShaderProgram;
import com.weinsim.slpaint.renderengine.shader.ShaderType;
import com.weinsim.sutil.math.SVector;

public abstract class Effect implements Cleanable {

    public static final BlackWhite BLACK_WHITE = new BlackWhite();
    public static final Contrast CONTRAST = new Contrast();
    public static final Brightness BRIGHTNESS = new Brightness();
    public static final Resize RESIZE = new Resize();

    private static final Effect[] INSTANCES = { BLACK_WHITE, CONTRAST, BRIGHTNESS, RESIZE };

    protected ShaderProgram shader;

    public final String name;

    public Effect(String name) {
        this.name = name;
        loadShader();
        putQuadData(shader.getRawModel());
        init();
    }

    private void loadShader() {
        shader = new ShaderProgram(name, ShaderType.EFFECT, List.of("position", "in_uv"));
    }

    /**
     * If this effect stores some state (usually something that can be set by the
     * user via the UI), this method should reset that state. It is automatically
     * called from the constructor.
     */
    public abstract void init();

    public void loadUniforms() {
        shader.loadUniform("textureSampler", 0);
    }

    private static void putQuadData(RawModel model) {
        final SVector[] positions = {
                new SVector(-1.0, -1.0),
                new SVector(1.0, -1.0),
                new SVector(-1.0, 1.0),
                new SVector(1.0, 1.0)
        };
        final SVector[] uvs = {
                new SVector(0.0, 0.0),
                new SVector(1.0, 0.0),
                new SVector(0.0, 1.0),
                new SVector(1.0, 1.0)
        };
        model.initVertexVBOs(4);
        FloatVBO position = model.getFloatVBO("position");
        FloatVBO uv = model.getFloatVBO("in_uv");
        position.createBuffer(4);
        uv.createBuffer(4);
        for (int i = 0; i < positions.length; i++) {
            position.putData(positions[i]);
            uv.putData(uvs[i]);
        }
        model.finishVertexVBOs();
    }

    public ShaderProgram getShader() {
        return shader;
    }

    @Override
    public void cleanUp() {
        shader.cleanUp();
    }

    public static void reloadShaders() {
        for (Effect effect : INSTANCES) {
            effect.cleanUp();
            effect.loadShader();
        }
    }

    // // this is not yet being called from anywhere, but it doesn't really matter
    // // because effects should live for the entire duration of the program
    // public static void cleanUpEffects() {
    // for (Effect effect : INSTANCES)
    // effect.cleanUp();
    // }

}
