#version 400 core

in vec2 position;
in vec2 in_uv;

out vec2 uv;

void main(void) {
    gl_Position = vec4(position, 0.0, 1.0);
    uv = in_uv;
}
