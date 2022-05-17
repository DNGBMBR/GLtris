#version 460 core

out vec4 fragColour;

in vec3 fColour;
in vec2 fTexCoord;

uniform sampler2D uFontTexture;

void main() {
	fragColour = texture(uFontTexture, fTexCoord) * vec4(fColour, 1.0);
	//fragColour = vec4(fTexCoord, 1.0, 1.0);
}
