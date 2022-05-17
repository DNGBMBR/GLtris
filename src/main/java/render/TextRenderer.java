package render;

import org.lwjgl.system.NonnullDefault;

import java.io.IOException;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class TextRenderer{
	private BatchFont batch;
	private TextureAtlas fontTexture;

	//fontLocation must point to an image file
	//TODO: Add support for font configs, rather than just supplying an image location
	public TextRenderer(TextureAtlas atlas, int capacity) {
		if (capacity <= 0) {
			throw new IllegalArgumentException("Cannot have less than or equal to 0 vertices in the batch.");
		}

		fontTexture = atlas;

		batch = new BatchFont(capacity * 6);
	}

	private void addCharacter(char c, float scale, float xPos, float yPos, float r, float g, float b) {
		float[] uvData = fontTexture.getElementUVs(c, 0, 1, 1);

		//System.out.println(uvData[0] + ", " + uvData[1] + ", " + uvData[2] + ", " + uvData[3] + ", ");
		float[] vertexData = {
			xPos, yPos, r, g, b, uvData[0], uvData[1],
			xPos + scale, yPos, r, g, b, uvData[2], uvData[1],
			xPos + scale, yPos + scale, r, g, b, uvData[2], uvData[3],
			xPos + scale, yPos + scale, r, g, b, uvData[2], uvData[3],
			xPos, yPos + scale, r, g, b, uvData[0], uvData[3],
			xPos, yPos, r, g, b, uvData[0], uvData[1]
		};

		batch.addVertices(vertexData);
	}

	public void addText(String text, float scale, float xPos, float yPos, float r, float g, float b) {
		float horizontalOffset = xPos;

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			addCharacter(c, scale, horizontalOffset, yPos, r, g, b);
			horizontalOffset += scale;
		}
	}

	public void draw(){
		batch.flush();
	}
}
