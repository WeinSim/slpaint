#version 400 core

in vec2 relativePos;
in vec2 relativeBoundingBoxMin;
in vec2 relativeBoundingBoxMax;
in vec4 color1;
in vec4 color2;
in float checkerboardSize;

out vec4 outColor;

void main(void) {

    if (relativePos.x < relativeBoundingBoxMin.x) {
        discard;
    }
    if (relativePos.y < relativeBoundingBoxMin.y) {
        discard;
    }
    if (relativePos.x > relativeBoundingBoxMax.x) {
        discard;
    }
    if (relativePos.y > relativeBoundingBoxMax.y) {
        discard;
    }

    if (checkerboardSize < 0) {
        outColor = color1;
    } else {
        int xComp = int(relativePos.x / checkerboardSize);
        int yComp = int(relativePos.y / checkerboardSize);
        outColor = (xComp + yComp) % 2 == 0 ? color1 : color2;
    }

}
