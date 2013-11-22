#version 140

in vec2 texCoord;

uniform sampler2D uSampler;
uniform float horizontal;
uniform float uBlurSize;

void main() {
    /* 1: 1
     * 2: 1 1
     * 3: 1 2 1
     * 4: 1 3 3 1
     * 5: 1 4 6 4 1
     * 6: 1 5 10 10 5 1
     * 7: 1 6 15 20 15 6 1
     * 8: 1 7 21 35 35 21 7 1
     * 9: 1 8 28 56 70 56 28 8 1
     */

    float weights[5];
    // Gaussian blur
    weights[0] = 70.0 / 256;
    weights[1] = 56.0 / 256;
    weights[2] = 28.0 / 256;
    weights[3] =  8.0 / 256;
    weights[4] =  1.0 / 256;

    // Box blur
    //weights[0] = 1.0 / 9;
    //weights[1] = 1.0 / 9;
    //weights[2] = 1.0 / 9;
    //weights[3] = 1.0 / 9;
    //weights[4] = 1.0 / 9;

    vec2 imageSize = textureSize(uSampler, 0);

    vec2 direction = vec2(0, 1);
    float linearBlurSize = uBlurSize / imageSize.y;
    if (horizontal > 0.5) {
        direction = vec2(1, 0);
        linearBlurSize = uBlurSize / imageSize.x;
    }

    vec4 final_color = vec4(0);

    for (int i = -4; i <= 4; i++) {
        final_color += weights[abs(i)] * texture2D(uSampler, texCoord + direction * linearBlurSize * i);
    }

    gl_FragColor = vec4(final_color.xyz, 1);
}
