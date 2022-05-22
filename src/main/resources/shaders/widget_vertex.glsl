#version 460 core
layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aTexCoord;

uniform mat4 uProjection;

out vec2 fTexCoord;

void main() {
	fTexCoord = aTexCoord;
	gl_Position = uProjection * vec4(aPos, 0.0f, 1.0f);
}
