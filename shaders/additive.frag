#version 140

in vec2 texCoord;

uniform sampler2D uSampler;
uniform sampler2D uExistingSampler;

void main() {
    vec4 final_color = texture2D(uExistingSampler, texCoord);
    final_color += texture2D(uSampler, texCoord);
    
    gl_FragColor = vec4(vec3(final_color.xyz), 1);
}
