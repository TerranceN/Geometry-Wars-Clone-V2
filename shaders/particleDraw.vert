in vec2 aCoord;

out vec2 texCoord;

uniform mat4 uProjectionMatrix;
uniform mat4 uModelViewMatrix;

uniform sampler2D uPositionSampler;
uniform float uNumParticles;

void main() {
    vec2 particleTexSize = textureSize(uPositionSampler, 0);
    vec2 index = aCoord / particleTexSize;
    texCoord = index;
    gl_Position = uProjectionMatrix * uModelViewMatrix * texture2D(uPositionSampler, index);
}
