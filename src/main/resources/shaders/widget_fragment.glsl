#version 460 core
out vec4 fragColour;

in vec2 fTexCoord;

uniform sampler2D uTexture;

void main() {
	fragColour = texture(uTexture, fTexCoord);
	//fragColour = vec4(fTexCoord, 1.0f, 1.0f);
}
