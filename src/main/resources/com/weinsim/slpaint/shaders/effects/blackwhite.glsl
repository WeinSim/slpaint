#version 400 core

in vec2 uv;

out vec4 outColor;

uniform sampler2D textureSampler;
uniform vec3 luminanceWeights;

void main(void) {
    vec4 color = texture(textureSampler, uv);
    float luminance = dot(color.xyz, luminanceWeights);
    outColor = vec4(luminance, luminance, luminance, color.a);
}
