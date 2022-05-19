#version 460 core

layout (location = 0) in vec2 aPosition;
layout (location = 1) in vec2 aTexCoord;

uniform mat4 uTransform;
uniform mat4 uProjection;
uniform mat4 uView;

out vec2 fTexCoord;

void main() {
	fTexCoord = aTexCoord;
	gl_Position = uProjection * uView * uTransform * vec4(aPosition, 0.0, 1.0);
	//gl_Position = vec4(aPosition, 1.0);
}