#version 140

in vec2 texCoord;
in float intensity;

void main() {
    vec4 final_color = vec4(1, 0.7, 0.05, 1);

    gl_FragColor = final_color * intensity;
}
