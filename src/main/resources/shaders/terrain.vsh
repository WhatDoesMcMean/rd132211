#version 330

uniform mat4 uProjection;
uniform mat4 uModelView;
uniform vec3 uChunkOffset;

layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec2 aUV;
layout(location = 2) in vec4 aColor;

out vec2 vUV;
out vec4 vColor;
#ifdef FOG
out float vVertexDistance;
#endif

void main() {
    vec4 pos = uModelView * vec4(aPosition + uChunkOffset, 1.0);
    gl_Position = uProjection * pos;
#ifdef FOG
    vVertexDistance = length(pos.xyz);
#endif
    vUV = aUV;
    vColor = aColor;
}