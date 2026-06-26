#version 420 core

struct GroupData {
    vec2 boundingBoxMin;
    vec2 boundingBoxMax;
    vec4 color;
    mat3 transformationMatrix;
    float relativeTextSize;
    // 3 * 4 bytes padding
};

layout(std140, binding = 1) uniform GroupAttributes {
    GroupData groupAttributes[256];
};

struct Glyph {
    vec2 position;
    vec2 size;
};

layout(std140, binding = 0) uniform FontData {
    Glyph[256] glyphs;
    vec2 textureSize;
    // 2 * 4 bytes padding
};

uniform mat3 viewMatrix;

// per vertex
in vec2 cornerPos;
// per instance
in int gIndex;
in vec2 position;
in float depth;
in int charIndex;

out vec2 relativeBoundingBoxMin;
out vec2 relativeBoundingBoxMax;
out vec2 textureCoords;
out vec4 color;

vec4 getGLPos(vec3 screenPos, float depth) {
    screenPos.x = floor(screenPos.x);
    screenPos.y = floor(screenPos.y);
    return vec4(
        (viewMatrix * screenPos).xy,
        depth,
        1.0
    );
}

void main(void) {
    GroupData gData = groupAttributes[gIndex];
    Glyph glyph = glyphs[charIndex];

    vec3 basePos = gData.transformationMatrix * vec3(
        position + cornerPos * glyph.size * gData.relativeTextSize,
        1.0);
    gl_Position = getGLPos(basePos, depth);

    color = gData.color;
    textureCoords = (glyph.position + cornerPos * glyph.size) / textureSize;

    relativeBoundingBoxMin = gData.boundingBoxMin - basePos.xy;
    relativeBoundingBoxMax = gData.boundingBoxMax - basePos.xy;
}
