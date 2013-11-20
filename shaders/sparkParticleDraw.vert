#version 140

in vec3 aCoord;

out vec2 texCoord;
out float intensity;

uniform mat4 uProjectionMatrix;
uniform mat4 uModelViewMatrix;

uniform sampler2D uPositionSampler;
uniform sampler2D uVelocitySampler;

void main() {
    vec2 particleTexSize = textureSize(uPositionSampler, 0);
    vec2 index = aCoord.xy / particleTexSize;
    vec2 drawPosition = texture2D(uPositionSampler, index).xy;
    float outIntensity = 1;
    if (int(aCoord.z) > 0) {
        vec2 vel = texture2D(uVelocitySampler, index).xy;
        float velLen = length(vel);
        drawPosition += vel * 0.1;
        if (velLen < 100) {
            outIntensity = length(clamp((velLen - 10) / 100, 0, 1));
        }
    } else {
        outIntensity = 0;
    }
    intensity = outIntensity;
    texCoord = index;
    gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(drawPosition, 0, 1);
}
