#version 400 core

in vec2 relativeBoundingBoxMin;
in vec2 relativeBoundingBoxMax;
in vec2 textureCoords;
in vec4 color;

out vec4 outColor;

uniform sampler2D textureSamplers[4];

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

    int page = int(floor(textureCoords.x));
    vec2 actualTextureCoords = vec2(textureCoords.x - page, textureCoords.y);

    vec4 textureColor = texture(textureSamplers[page], actualTextureCoords);
    float alpha = color.a * textureColor.r;

    outColor = vec4(color.xyz, alpha);
}
