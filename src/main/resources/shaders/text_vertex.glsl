#version 460 core

layout (location = 0) in vec2 aPos;
layout (location = 1) in vec3 aCol;
layout (location = 2) in vec2 aTexCoord;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uTransform;

out vec3 fColour;
out vec2 fTexCoord;

void main() {
	fColour = aCol;
	fTexCoord = aTexCoord;
	gl_Position = uProjection * uView * uTransform * vec4(aPos, 0.0, 1.0);
}
