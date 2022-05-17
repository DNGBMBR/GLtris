#version 460 core
out vec4 fragColour;

in vec2 fTexCoord;

uniform sampler2D uTexture;

void main() {
	fragColour = texture(uTexture, fTexCoord);
}