#version 420 core

struct GroupData {
    vec2 boundingBoxMin;
    vec2 boundingBoxMax;
    vec4 color2;
    // If size == -1, then no checkerboard at all
    float checkerboardSize;
    // 3 * 4 bytes padding
};

layout(std140, binding = 0) uniform GroupAttributes {
    GroupData groupAttributes[256];
};

uniform mat3 viewMatrix;

// per vertex
in vec2 cornerPos;
in vec2 offset;
// per instance
in int gIndex;
in mat3 transformationMatrix;
in vec2 position;
in float depth;
in vec2 size_in;
in vec4 color1_in;
in float strokeWeight_in;

out vec2 relativePos;
out vec2 relativeBoundingBoxMin;
out vec2 relativeBoundingBoxMax;
out vec4 color1;
out vec4 color2;
out float checkerboardSize;
out vec2 size;
out float strokeWeight;

/* Before screen space coordinates ([0, 1920] x [0, 1080]) are converted
 * to OpenGL coordinates ([-1, 1] x [-1, 1]), they are rounded to integer
 * values. This is to avoid inconsistent stroke weights of rectangles (a
 * stroke weight of 1px could appear as 0, 1 or 2 pixels without
 * rounding).
 */
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
    vec3 basePos = transformationMatrix * vec3(position, 1.0);
    relativePos = (transformationMatrix * vec3(size_in * cornerPos + strokeWeight_in * offset, 0.0)).xy;
    gl_Position = getGLPos(basePos + vec3(relativePos, 0.0), depth);

    GroupData gData = groupAttributes[gIndex];

    float scale = sqrt(transformationMatrix[0].x * transformationMatrix[1].y);
    color1 = color1_in;
    color2 = gData.color2;
    checkerboardSize = scale * gData.checkerboardSize;
    strokeWeight = scale * strokeWeight_in;
    size = (transformationMatrix * vec3(size_in, 0.0)).xy;

    relativeBoundingBoxMin = gData.boundingBoxMin - basePos.xy;
    relativeBoundingBoxMax = gData.boundingBoxMax - basePos.xy;
}
