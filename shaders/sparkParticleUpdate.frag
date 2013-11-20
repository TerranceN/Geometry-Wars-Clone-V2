#version 140

in vec2 texCoord;
in vec2 fragCoord;

uniform sampler2D uPositionSampler;
uniform sampler2D uVelocitySampler;

uniform float uDeltaTime;

void main() {
    vec2 oldPosition = texture2D(uPositionSampler, texCoord).xy;
    vec2 oldVelocity = texture2D(uVelocitySampler, texCoord).xy;

    vec2 newPosition = vec2(0);
    vec2 newVelocity = vec2(0);
    newPosition = oldPosition + oldVelocity * uDeltaTime;
    newVelocity = oldVelocity - oldVelocity * uDeltaTime * 4;

    gl_FragData[0] = vec4(newPosition, 0, 1);
    gl_FragData[1] = vec4(newVelocity, 0, 1);
}
