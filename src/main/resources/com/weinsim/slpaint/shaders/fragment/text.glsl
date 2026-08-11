#version 400 core

in vec2 relativeBoundingBoxMin;
in vec2 relativeBoundingBoxMax;
in vec2 textureCoords;
in float relativeTextSize;
in vec4 color;

out vec4 outColor;

// see UIRenderMaster.MAX_FONT_ATLASSES
uniform sampler2D textureSamplers[8];
uniform float sdfMaxDist;

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
    // distance in screen-space pixels
    float dist = (textureColor.r - 0.5) * (2 * sdfMaxDist) * relativeTextSize;
    float alpha = color.a * clamp(0.5 - dist, 0, 1);

    outColor = vec4(color.rgb, alpha);
}
