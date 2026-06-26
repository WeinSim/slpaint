#version 420 core

struct GroupData {
    vec2 boundingBoxMin;
    vec2 boundingBoxMax;
    int samplerID;
    // 3 * 4 bytes padding
};

layout(std140, binding = 0) uniform GroupAttributes {
    GroupData groupAttributes[256];
};

uniform mat3 viewMatrix;

// per vertex
in vec2 cornerPos;
// per instance
in int gIndex;
in mat3 transformationMatrix;
in vec2 position;
in float depth;
in vec2 size;

out vec2 relativeBoundingBoxMin;
out vec2 relativeBoundingBoxMax;
out vec2 uvCoords;
flat out int samplerID;

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
    vec3 basePos = transformationMatrix * vec3(position + size * cornerPos, 1.0);
    gl_Position = getGLPos(basePos, depth);

    GroupData gData = groupAttributes[gIndex];

    uvCoords = cornerPos;
    samplerID = gData.samplerID;

    relativeBoundingBoxMin = gData.boundingBoxMin - basePos.xy;
    relativeBoundingBoxMax = gData.boundingBoxMax - basePos.xy;
}
