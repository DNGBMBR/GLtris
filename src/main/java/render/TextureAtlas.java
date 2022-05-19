package render;

import java.io.IOException;

public class TextureAtlas extends Texture2D {
	protected int elementWidth;
	protected int elementHeight;

	public TextureAtlas(String fileName, int textureSlot, int elementWidth, int elementHeight) throws IOException {
		super(fileName, textureSlot);
		this.elementWidth = elementWidth;
		this.elementHeight = elementHeight;
	}

	public float[] getElementUVs(int px, int py, int width, int height) {
		float p0x = (px * elementWidth + 0.5f) / textureWidth;
		float p0y = (py * elementHeight + 0.5f) / textureHeight;
		float p1x = ((px + width) * elementWidth - 0.5f) / textureWidth;
		float p1y = ((py + height) * elementHeight - 0.5f) / textureHeight;

		return new float[] {p0x, p0y, p1x, p1y};
	}

	public int getElementWidth() {
		return elementWidth;
	}

	public int getElementHeight() {
		return elementHeight;
	}
}
