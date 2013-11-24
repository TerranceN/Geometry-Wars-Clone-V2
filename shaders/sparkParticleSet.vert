#version 130

in vec2 aCoord;
in vec2 aTexCoord;

out vec2 texCoord;

uniform mat4 uProjectionMatrix;
uniform mat4 uModelViewMatrix;

void main() {
    vec4 transformedCoord = uModelViewMatrix * vec4(aCoord, 0.0, 1.0);
    vec4 finishedCoord = uProjectionMatrix * transformedCoord;

    texCoord = aTexCoord;
    gl_Position = finishedCoord;
}
