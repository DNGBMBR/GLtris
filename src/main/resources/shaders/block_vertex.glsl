#version 330 core

layout (location = 0) in vec2 aPosition;
layout (location = 1) in vec2 aTexCoord;

uniform mat4 uProjection;

out vec2 fTexCoord;

void main() {
	fTexCoord = aTexCoord;
	gl_Position = uProjection * vec4(aPosition, 0.0, 1.0);
}