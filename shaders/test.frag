#version 140

const int MAX_POSITIONS = 50;

in vec2 texCoord;
in vec2 fragCoord;

uniform mat4 uProjectionMatrix;
uniform vec2 uPushPositions[MAX_POSITIONS];
uniform float uPushStrength[MAX_POSITIONS];
uniform vec2 uPushVelocity[MAX_POSITIONS];
uniform int uNumPositions;

void main() {
    float size = 25;
    vec2 final_color = vec2(0);

    int positionsToDraw = min(uNumPositions, MAX_POSITIONS);
    for (int i = 0; i < positionsToDraw; i++) {
        vec2 diff = (fragCoord - uPushPositions[i]);
        float scale = dot(-normalize(diff), normalize(uPushVelocity[i]));
        if (scale > 0) {
            scale = 0;
        } else {
            scale = 1;
        }
        vec2 invDiff = normalize(vec2(diff.x, -diff.y)) * clamp((size - length(diff)) / size, 0, 0.5) * uPushStrength[i] * scale;
        final_color += invDiff;
    }

    gl_FragColor = vec4(final_color.xy, 0, 1);
}