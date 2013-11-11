#version 130

in vec2 aCoord;
in vec2 aTexCoord;

out vec2 texCoord;
out vec2 fragCoord;

uniform mat4 uProjectionMatrix;

void main() {
    vec4 transformedCoord = vec4(aCoord, 0, 1.0);
    vec4 finishedCoord = uProjectionMatrix * transformedCoord;

    texCoord = aTexCoord;
    fragCoord = transformedCoord.xy;

    gl_Position = finishedCoord;
}
