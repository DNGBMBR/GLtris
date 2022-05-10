#version 460 core

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec2 aTexCoord;

uniform mat4 uTransform;
uniform mat4 uProjection;
uniform mat4 uView;

uniform vec4 uColour;

out vec4 fColor;

void main() {
	fColor = uColour;
	gl_Position = uProjection * uView * uTransform * vec4(aPosition, 1.0);
	//gl_Position = vec4(aPosition, 1.0);
}