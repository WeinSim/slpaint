#version 400 core

in vec2 relativeBoundingBoxMin;
in vec2 relativeBoundingBoxMax;
in vec2 uvCoords;
in float radius;
in vec3 color; // (hue, saturation, 0) for lightness scale
flat in int flags;

out vec4 outColor;

vec3 hsvToRGB(float h, float s, float v);

vec3 hslToRGB(float h, float s, float l);

float mag(vec2 v);

float atan2(float y, float x);

const float PI = 3.1415926535;

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

    // 0 = hue sat (circ), 1 = hue sat (rect), 2 = lightness, 3 = alpha
    int shapeType = flags & 0x03;
    // 0 = hsl, 1 = hsv
    int colorSpace = (flags & 0x04) >> 2;
    // 0 = vertical, 1 = horizontal
    int orientation = (flags & 0x08) >> 3;

    switch (shapeType) {
        case 0:
            vec2 transformedUVCoords = 2 * uvCoords - 1;
            float mag = mag(transformedUVCoords);
            // if (mag > 1) {
            //     discard;
            // }
            float angle = atan2(transformedUVCoords.y, transformedUVCoords.x);
            outColor = vec4(colorSpace == 1
                    ? hsvToRGB(angle * 180 / PI, mag, 1.0)
                    : hslToRGB(angle * 180 / PI, mag, 0.5),
                // this makes the edges softer than just discarding the fragment for mag > 1
                clamp((1 - mag) * radius, 0, 1));
            break;
        case 1:
            outColor = vec4(colorSpace == 1
                    ? hsvToRGB(uvCoords.x * 360, 1 - uvCoords.y, 1.0)
                    : hslToRGB(uvCoords.x * 360, 1 - uvCoords.y, 0.5),
                1.0);
            break;
        case 2:
            if (orientation == 0) {
                outColor = vec4(colorSpace == 1
                        ? hsvToRGB(color.x, color.y, 1 - uvCoords.y)
                        : hslToRGB(color.x, color.y, 1 - uvCoords.y),
                    1.0);
            } else {
                outColor = vec4(colorSpace == 1
                        ? hsvToRGB(color.x, color.y, uvCoords.x)
                        : hslToRGB(color.x, color.y, uvCoords.x),
                    1.0);
            }
            break;
        case 3:
            outColor = vec4(color,
                orientation == 1 ? uvCoords.x : 1 - uvCoords.y);
            break;
    }
}

float mag(vec2 v) {
    return sqrt(v.x * v.x + v.y * v.y);
}

float atan2(float y, float x) {
    if (abs(x) < 1e-6) {
        return y < 0 ? 3 * PI / 2 : PI / 2;
    }
    if (x > 0) {
        return atan(y / x);
    } else {
        return atan(y / x) - PI;
    }
}

vec3 hslToRGB(float h, float s, float l) {
    h = mod(mod(h, 360) + 360, 360);
    float c = (1 - abs(2 * l - 1)) * s;
    float x = c * (1 - abs(mod(h / 60, 2) - 1));
    float m = l - c / 2;
    float r, g, b;
    if (h < 60) {
        r = c;
        g = x;
        b = 0;
    } else if (h < 120) {
        r = x;
        g = c;
        b = 0;
    } else if (h < 180) {
        r = 0;
        g = c;
        b = x;
    } else if (h < 240) {
        r = 0;
        g = x;
        b = c;
    } else if (h < 300) {
        r = x;
        g = 0;
        b = c;
    } else {
        r = c;
        g = 0;
        b = x;
    }
    r = (r + m);
    g = (g + m);
    b = (b + m);
    return vec3(r, g, b);
}

vec3 hsvToRGB(float h, float s, float v) {
    h = mod(mod(h, 360) + 360, 360);
    float c = v * s;
    float x = c * (1 - abs(mod(h / 60, 2) - 1));
    float m = v - c;
    float r, g, b;
    if (h < 60) {
        r = c;
        g = x;
        b = 0;
    } else if (h < 120) {
        r = x;
        g = c;
        b = 0;
    } else if (h < 180) {
        r = 0;
        g = c;
        b = x;
    } else if (h < 240) {
        r = 0;
        g = x;
        b = c;
    } else if (h < 300) {
        r = x;
        g = 0;
        b = c;
    } else {
        r = c;
        g = 0;
        b = x;
    }
    r = (r + m);
    g = (g + m);
    b = (b + m);
    return vec3(r, g, b);
}
