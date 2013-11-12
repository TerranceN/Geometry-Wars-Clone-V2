#version 140

in vec2 texCoord;
in vec2 vertexIndex;

uniform sampler2D uVelocitySampler;

void main() {
    highp int index = 0;

    if (abs(floor(vertexIndex.x) - ceil(vertexIndex.x)) < 0.1) {
        index = int(floor(vertexIndex.x));
    }

    if (abs(floor(vertexIndex.y) - ceil(vertexIndex.y)) < 0.1) {
        index = int(floor(vertexIndex.y));
    }

    vec4 final_color = vec4(0, 0, 0.5, 1);

    if (index % 10 == 0) {
        final_color = vec4(0, 0, 1, 1);
    }

    gl_FragColor = final_color;// * clamp(length(texture2D(uVelocitySampler, texCoord).xy), 0.5, 1);
}
