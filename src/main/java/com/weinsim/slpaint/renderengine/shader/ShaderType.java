package com.weinsim.slpaint.renderengine.shader;

public enum ShaderType {

    INSTANCE(
            "shaders/vertex_instance/%s.glsl",
            null,
            "shaders/fragment/%s.glsl");

    public final String vertexPath,
            geometryPath,
            fragmentPath;

    private ShaderType(String vertexPath, String geometryPath, String fragmentPath) {
        this.vertexPath = vertexPath;
        this.geometryPath = geometryPath;
        this.fragmentPath = fragmentPath;
    }

    public boolean hasGeometry() {
        return geometryPath != null;
    }

}
