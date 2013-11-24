#version 130

in vec3 aCoord;

uniform mat4 uProjectionMatrix;
uniform mat4 uModelViewMatrix;

void main() {
    gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aCoord, 1.0);
}
