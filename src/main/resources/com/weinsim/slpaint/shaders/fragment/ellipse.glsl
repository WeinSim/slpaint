#version 400 core

in vec2 relativeBoundingBoxMin;
in vec2 relativeBoundingBoxMax;
in float radius;
in vec2 uvCoords;
in vec4 color;

out vec4 outColor;

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

    float mag = uvCoords.x * uvCoords.x + uvCoords.y * uvCoords.y;

    // if (mag > 1) {
    //     discard;
    // }

    float alpha = clamp((1 - mag) * radius, 0, 1) * color.a;
    outColor = vec4(color.xyz, alpha);
}
