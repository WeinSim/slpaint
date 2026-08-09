#version 400 core

in vec2 uv;

out vec4 outColor;

uniform sampler2D textureSampler;
uniform float factor;
uniform vec3 luminanceWeights;

void main(void) {
    vec4 color = texture(textureSampler, uv);
    float luminance = dot(color.rgb, luminanceWeights);
    vec3 luminanceVec = vec3(1, 1, 1) * luminance;
    outColor = vec4(mix(luminanceVec, color.rgb, factor), color.a);
}
