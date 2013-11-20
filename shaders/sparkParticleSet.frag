#version 140

in vec2 texCoord;
uniform vec2 uPosition;
uniform vec2 uVelocity = vec2(0, 0);

float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

void main() {
    float lowSpeed = 200;
    float highSpeed = 1500;
    vec2 pos = (uPosition / 100);
    pos = vec2(mod(pos.x, 1), mod(pos.y, 1));
    float rx = rand(texCoord + pos);
    float ry = rand(vec2(rx));
    vec2 vel = vec2(rx - 0.5, ry - 0.5);
    float rs = rand(vec2(ry));
    float t = 1 - (pow(rs, 1.5));
    float speed = lowSpeed + (highSpeed - lowSpeed) * t;
    vec2 velocity = normalize(vel) * speed;
    velocity += uVelocity;
    gl_FragData[0] = vec4(uPosition, 0, 1);
    gl_FragData[1] = vec4(velocity, 0, 1);
}
