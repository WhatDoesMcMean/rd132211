#version 330

#ifdef COLORBLIND_MODE
#include "colorblindness.glsl"
#endif

uniform sampler2D uSampler0;

in vec2 vTexCoord;

out vec4 outColor;

void main() {
#if defined(INVERTED)
    outColor = vec4(1.0 - texture(uSampler0, vTexCoord).rgb, 1.0);
#elif defined(MATRIX)
    vec4 color = texture(uSampler0, vTexCoord);
    outColor = vec4(pow(color.r, 1.4), color.g, pow(color.b, 1.6), color.a);
#elif defined(MEXICO)
    vec4 color = texture(uSampler0, vTexCoord);
    outColor = vec4(color.r, min(pow(color.g, 1.25), 1.0), min(pow(color.b, 1.75), 1.0), color.a);
#elif defined(COLORBLIND_MODE)
    vec4 color = texture(uSampler0, vTexCoord);
	outColor = vec4(simulate_colorblindness(color.rgb, COLORBLIND_MODE), color.a);
#else
    outColor = texture(uSampler0, vTexCoord);
#endif
}