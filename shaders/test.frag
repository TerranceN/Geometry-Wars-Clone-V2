#version 140

in vec2 texCoord;
in vec2 fragCoord;

uniform mat4 uProjectionMatrix;
uniform vec2 uMousePosition;

void main() {
    float size = 20;
    vec4 final_color = vec4(vec3(0), 1);
    vec2 diff = (fragCoord - uMousePosition);
    vec2 invDiff = normalize(vec2(diff.x, -diff.y)) * clamp((size - length(diff)) / size, 0, 0.2) * 1;

    final_color = vec4(invDiff, 0, 1);

    gl_FragColor = vec4(final_color.xyz, 1);
}
