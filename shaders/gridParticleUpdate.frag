#version 130

in vec2 texCoord;
in vec2 fragCoord;

uniform sampler2D uPositionSampler;
uniform sampler2D uOffsetSampler;
uniform sampler2D uVelocitySampler;
uniform sampler2D uAccelerationSampler;

uniform float uDeltaTime;

void main() {
    vec2 oldPosition = texture2D(uPositionSampler, texCoord).xy;
    vec2 oldOffset = texture2D(uOffsetSampler, texCoord).xy;
    vec2 oldVelocity = texture2D(uVelocitySampler, texCoord).xy;
    ivec2 accTexSize = textureSize(uAccelerationSampler, 0);
    vec2 diffToCenter = oldOffset;
    vec2 acceleration = texture2D(uAccelerationSampler, vec2((oldPosition.x + oldOffset.x) / accTexSize.x, 1 - ((oldPosition.y + oldOffset.y) / accTexSize.y))).xy;
    acceleration = vec2(acceleration.x, -acceleration.y);
    //vec2 acceleration = vec2(1, 1);

    vec2 newOffset = vec2(0);
    vec2 newVelocity = vec2(0);
    newOffset = oldOffset + oldVelocity * uDeltaTime;
    newVelocity = oldVelocity + (acceleration * uDeltaTime * 2000 - diffToCenter * uDeltaTime * 10) * 10 - oldVelocity * uDeltaTime * 10;

    gl_FragData[0] = vec4(newOffset, 0, 1);
    gl_FragData[1] = vec4(newVelocity, 0, 0);
}
