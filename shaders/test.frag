#version 130

const int MAX_POSITIONS = 50;

in vec2 texCoord;
in vec2 fragCoord;

uniform sampler2D uPushSampler;
uniform float uPushStrength;

void main() {
    vec4 texColor = texture2D(uPushSampler, texCoord);
    vec4 final_color = vec4(texColor.xy, 0, 1);
    //final_color = final_color * 2.0 - 1.0;
    final_color = final_color - 0.5;
    final_color.y = -final_color.y;
    if (texColor.a < 0.1) {
        final_color = vec4(0);
    }
    gl_FragColor = final_color * uPushStrength;
}
