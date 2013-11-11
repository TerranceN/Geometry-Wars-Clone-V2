#version 140

in vec2 texCoord;

uniform sampler2D uVelocitySampler;

void main() {
    gl_FragColor = vec4(1, 0, 0, 1);// * clamp(length(texture2D(uVelocitySampler, texCoord).xy), 0.5, 1);
}
