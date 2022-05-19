package render;

import java.io.IOException;

//https://en.wikipedia.org/wiki/9-slice_scaling
public class TextureNineSlice extends TextureAtlas{
	//assumes the four borders have an equal amount of space dedicated to them
	protected int borderWidth;
	protected int borderHeight;

	public TextureNineSlice(String fileName, int textureSlot, int elementWidth, int elementHeight, int borderWidth, int borderHeight) throws IOException {
		super(fileName, textureSlot, elementWidth, elementHeight);
		this.borderWidth = borderWidth;
		this.borderHeight = borderHeight;
	}

	/*
	* D - - - - - - C
	* - H - - - - G -
	* - - . . . . - -
	* - - . . . . - -
	* - - . . . . - -
	* - - . . . . - -
	* - E - - - - F -
	* A - - - - - - B
	* */
	//even indices are x coords, odd are y coords
	//returns uvs in order ABCDEFGH

	public int getBorderWidth() {
		return borderWidth;
	}

	public int getBorderHeight() {
		return borderHeight;
	}

	@Override
	public float[] getElementUVs(int px, int py, int width, int height) {
		float p0x = (px * elementWidth + 0.5f) / textureWidth;
		float p0y = (py * elementHeight + 0.5f) / textureHeight;
		//coordinates for p1 and p2 are probably off by 1
		float p1x = (px * elementWidth + borderWidth + 0.5f) / textureWidth;
		float p1y = (py * elementHeight + borderHeight + 0.5f) / textureHeight;
		float p2x = ((px + width) * elementWidth - borderWidth - 0.5f) / textureWidth;
		float p2y = ((py + height) * elementHeight - borderHeight - 0.5f) / textureHeight;
		float p3x = ((px + width) * elementWidth - 0.5f) / textureWidth;
		float p3y = ((py + height) * elementHeight - 0.5f) / textureHeight;

		float[] out = {
			p0x, p0y,
			p1x, p1y,
			p2x, p2y,
			p3x, p3y,
		};

		return out;
	}
}
