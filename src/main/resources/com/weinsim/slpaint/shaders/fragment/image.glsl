#version 400 core

in vec2 relativeBoundingBoxMin;
in vec2 relativeBoundingBoxMax;
in vec2 uvCoords;
flat in int samplerID;

out vec4 outColor;

uniform sampler2D textureSamplers[32];

void main(void) {

    if (relativeBoundingBoxMin.x > 0) {
        discard;
    }
    if (relativeBoundingBoxMin.y > 0) {
        discard;
    }
    if (relativeBoundingBoxMax.x < 0) {
        discard;
    }
    if (relativeBoundingBoxMax.y < 0) {
        discard;
    }

    vec4 sampleColor = texture(textureSamplers[samplerID], uvCoords);
    outColor = sampleColor;
}
