package render.texture;

import java.io.IOException;

public class TextureAtlas extends Texture2D {
	protected int elementWidth;
	protected int elementHeight;

	public TextureAtlas(String fileName, int textureSlot, Texture2DSettings settings, int elementWidth, int elementHeight) throws IOException {
		super(fileName, textureSlot, settings);
		this.elementWidth = elementWidth;
		this.elementHeight = elementHeight;
	}

	//specify lower left segment of texture wanted
	public float[] getElementUVs(int px, int py, int width, int height) {
		int numElements = this.textureHeight / elementHeight;

		float p0x = (px * elementWidth + 0.5f) / textureWidth;
		float p0y = ((numElements - 1 - py) * elementHeight + 0.5f) / textureHeight;
		float p1x = ((px + width) * elementWidth - 0.5f) / textureWidth;
		float p1y = ((numElements - 1 - py + height) * elementHeight - 0.5f) / textureHeight;

		return new float[] {p0x, p0y, p1x, p1y};
	}

	public int getElementWidth() {
		return elementWidth;
	}

	public int getElementHeight() {
		return elementHeight;
	}
}
