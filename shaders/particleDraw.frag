#version 140

in vec2 texCoord;
in vec2 vertexIndex;

uniform sampler2D uVelocitySampler;

void main() {
    highp int index = -1;

    float epsilon = 0.01;
    if (abs(floor(vertexIndex.x + epsilon) - ceil(vertexIndex.x - epsilon)) < 0.1) {
        index = int(floor(vertexIndex.x));
    }

    if (abs(floor(vertexIndex.y + epsilon) - ceil(vertexIndex.y - epsilon)) < 0.1) {
        index = int(floor(vertexIndex.y));
    }

    vec4 final_color = vec4(0.001, 0.001, 0.75, 1);

    if (index % 10 == 0) {
        final_color = vec4(0.05, 0.05, 1, 1);
    }

    gl_FragColor = final_color;// * clamp(length(texture2D(uVelocitySampler, texCoord).xy), 0.5, 1);
}
