package com.weinsim.slpaint.main.effects;

import java.util.List;
import java.util.function.Supplier;

import com.weinsim.slpaint.main.apps.MainApp;
import com.weinsim.slpaint.renderengine.Cleanable;
import com.weinsim.slpaint.renderengine.RawModel;
import com.weinsim.slpaint.renderengine.bufferobjects.FloatVBO;
import com.weinsim.slpaint.renderengine.shader.ShaderProgram;
import com.weinsim.slpaint.renderengine.shader.ShaderType;
import com.weinsim.sutil.math.SVector;

public enum Effect implements Cleanable {

    BLACK_WHITE("Black / White", "blackwhite", BlackWhite::new),
    BRIGHTNESS("Brightness", "brightness", Brightness::new),
    CONTRAST("Contrast", "contrast", Contrast::new),
    RESIZE("Resize", "resize", Resize::new, false),
    SATURATION("Saturation", "saturation", Saturation::new);

    /*
     * sRGB linear luminance weights
     * https://en.wikipedia.org/wiki/Grayscale#Converting_color_to_grayscale
     */
    public static final SVector LUMINANCE_WEIGHTS = new SVector(0.2126, 0.7152, 0.0722);

    private ShaderProgram shader;
    public final String name;
    private final String shaderName;
    private final Supplier<EffectInstance> constructor;
    public final boolean userAccessible;

    private Effect(String name, String shaderName, Supplier<EffectInstance> instanceConstructor) {
        this(name, shaderName, instanceConstructor, true);
    }

    private Effect(String name, String shaderName, Supplier<EffectInstance> instanceConstructor,
            boolean userAccessible) {

        this.name = name;
        this.shaderName = shaderName;
        this.constructor = instanceConstructor;
        this.userAccessible = userAccessible;
        loadShader();
        putQuadData(shader.getRawModel());
    }

    private void loadShader() {
        shader = new ShaderProgram(shaderName, ShaderType.EFFECT, List.of("position", "in_uv"));
    }

    void loadUniform(String name, Object value) {
        shader.loadUniform(name, value);
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

    public EffectInstance createInstance() {
        EffectInstance instance = constructor.get();
        if (MainApp.DEV_BUILD) {
            // make sure that there is no mismatch between the constructor and
            // EffectInstance#getEffect
            Effect effect = instance.getEffect();
            if (effect != this) {
                String errorMessage = String.format(
                        "Effect instance \"%s\" does not specify the right effect (expected: \"%s\", actual: \"%s\")",
                        instance.getClass().getSimpleName(), name, effect.name);
                throw new RuntimeException(errorMessage);
            }
        }
        return instance;
    }

    public ShaderProgram getShader() {
        return shader;
    }

    @Override
    public void cleanUp() {
        shader.cleanUp();
    }

    public static void reloadShaders() {
        for (Effect effect : values()) {
            effect.cleanUp();
            effect.loadShader();
        }
    }

    // // this is not yet being called from anywhere, but it doesn't really matter
    // // because effects should live for the entire duration of the program
    // public static void cleanUpEffects() {
    // for (Effect effect : values())
    // effect.cleanUp();
    // }

}
