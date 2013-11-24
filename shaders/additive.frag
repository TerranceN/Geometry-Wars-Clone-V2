#version 130

in vec2 texCoord;

uniform sampler2D uSampler;
uniform sampler2D uExistingSampler;

void main() {
    vec4 final_color = texture2D(uExistingSampler, texCoord);
    //if (length(final_color.xyz) > 0) {
    //    final_color = vec4(1);
    //}
    final_color += texture2D(uSampler, texCoord);
    
    gl_FragColor = vec4(vec3(final_color.xyz), 1);
}
