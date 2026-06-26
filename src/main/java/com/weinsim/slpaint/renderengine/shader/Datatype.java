package com.weinsim.slpaint.renderengine.shader;

public enum Datatype {

    INT("int", 1),
    FLOAT("float", 1),
    VEC2("vec2", 2),
    VEC3("vec3", 3),
    VEC4("vec4", 4),
    MAT2("mat2", 2),
    MAT3("mat3", 3),
    MAT4("mat4", 4),
    SAMPLER_2D("sampler2D", 1);

    public final String identifier;
    public final int coordinateSize;

    private Datatype(String identifier, int coordinateSize) {
        this.identifier = identifier;
        this.coordinateSize = coordinateSize;
    }

    public static Datatype fromIdentifier(String identifier) {
        for (Datatype datatype : values()) {
            if (identifier.equals(datatype.identifier))
                return datatype;
        }
        return null;
    }

}
