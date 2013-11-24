#version 130

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

    vec4 final_color = vec4(0.005, 0.005, 0.75, 1);

    if (index % 5 == 0) {
        final_color = vec4(vec2(0.03), 1, 1);
    }

    gl_FragColor = final_color * 0.5;// * clamp(length(texture2D(uVelocitySampler, texCoord).xy), 0.5, 1);
}
