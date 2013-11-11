in vec2 aCoord;

uniform mat4 uProjectionMatrix;
uniform mat4 uModelViewMatrix;

uniform sampler2D uPositionSampler;
uniform float uNumParticles;

void main() {
    float index = aCoord.x / uNumParticles;
    gl_Position = uProjectionMatrix * uModelViewMatrix * texture2D(uPositionSampler, vec2(index, 0));
}
