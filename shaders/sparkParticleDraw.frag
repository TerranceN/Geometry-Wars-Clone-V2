#version 130

in vec2 texCoord;
in float intensity;

uniform sampler2D uColorSampler;

void main() {
    gl_FragColor = texture2D(uColorSampler, texCoord) * intensity;
}
