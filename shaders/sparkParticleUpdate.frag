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

    float bounceDampening = 0.75;

    if (newPosition.x < 0) {
        newPosition.x = -newPosition.x;
        newVelocity.x = -newVelocity.x * bounceDampening;
    }
    if (newPosition.y < 0) {
        newPosition.y = -newPosition.y;
        newVelocity.y = -newVelocity.y * bounceDampening;
    }

    gl_FragData[0] = vec4(newPosition, 0, 1);
    gl_FragData[1] = vec4(newVelocity, 0, 1);
}
