#version 140

in vec2 aCoord;

out vec2 texCoord;
out vec2 vertexIndex;

uniform mat4 uProjectionMatrix;
uniform mat4 uModelViewMatrix;

uniform sampler2D uPositionSampler;
uniform sampler2D uOffsetSampler;

void main() {
    vec2 particleTexSize = textureSize(uPositionSampler, 0);
    vec2 index = aCoord / particleTexSize;
    vec2 drawPosition = texture2D(uPositionSampler, index).xy;
    if (aCoord.x > 0 && aCoord.y > 0 && aCoord.x < particleTexSize.x && aCoord.y < particleTexSize.y) {
        drawPosition += texture2D(uOffsetSampler, index).xy;
    }
    texCoord = index;
    vertexIndex = aCoord;
    gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(drawPosition, 0, 1);
}
