#version 140

in vec2 texCoord;
in vec2 fragCoord;

uniform sampler2D uPositionSampler;
uniform sampler2D uVelocitySampler;
uniform sampler2D uAccelerationSampler;

uniform float uDeltaTime;

void main() {
    vec2 oldPosition = texture2D(uPositionSampler, texCoord).xy;
    vec2 oldVelocity = texture2D(uVelocitySampler, texCoord).xy;
    ivec2 accTexSize = textureSize(uAccelerationSampler, 0);
    vec2 acceleration = texture2D(uAccelerationSampler, vec2(oldPosition.x / accTexSize.x, oldPosition.y / accTexSize.y)).xy;
    //vec2 acceleration = vec2(1, 1);
    vec2 newPosition = vec2(0);
    vec2 newVelocity = vec2(0);
    newPosition = oldPosition + oldVelocity * uDeltaTime;
    newVelocity = oldVelocity + acceleration * uDeltaTime * 10000 - oldVelocity * uDeltaTime * 10;
    gl_FragData[0] = vec4(newPosition, 0, 1);
    gl_FragData[1] = vec4(newVelocity, 0, 0);
}
