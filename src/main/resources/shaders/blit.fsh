#version 330

uniform sampler2D uSampler0;

in vec2 vTexCoord;

out vec4 outColor;

void main() {
#ifdef INVERTED
    outColor = vec4(1.0 - texture(uSampler0, vTexCoord).rgb, 1.0);
#else
    outColor = texture(uSampler0, vTexCoord);
#endif
}