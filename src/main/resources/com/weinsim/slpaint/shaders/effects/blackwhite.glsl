#version 400 core

in vec2 uv;

out vec4 outColor;

uniform sampler2D textureSampler;

// sRGB linear luminance weights
// https://en.wikipedia.org/wiki/Grayscale#Converting_color_to_grayscale
const vec3 luminanceWeights = vec3(0.2126, 0.7152, 0.0722);

void main(void) {
    vec4 color = texture(textureSampler, uv);
    float luminance = dot(color.xyz, luminanceWeights);
    outColor = vec4(luminance, luminance, luminance, color.a);
}
