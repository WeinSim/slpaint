#version 400 core

in vec2 uv;

out vec4 outColor;

uniform sampler2D textureSampler;
uniform float multiplier;

void main(void) {
    vec4 color = texture(textureSampler, uv);
    outColor = vec4((color.rgb - 0.5) * multiplier + 0.5, color.a);
}
