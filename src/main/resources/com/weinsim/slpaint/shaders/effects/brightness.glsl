#version 400 core

in vec2 uv;

out vec4 outColor;

uniform sampler2D textureSampler;
uniform float brightness;

void main(void) {
    vec4 color = texture(textureSampler, uv);
    outColor = vec4(color.rgb + vec3(1, 1, 1) * brightness, color.a);
    // outColor = vec4(1, 1, 0, 1);
}
