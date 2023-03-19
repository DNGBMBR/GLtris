package render.texture;

import org.lwjgl.BufferUtils;
import render.Shader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class Texture2D {
	protected int textureWidth;
	protected int textureHeight;
	private int textureID;
	private int textureSlot;

	//textureSlot is 0, 1, etc.
	public Texture2D(String fileName, int textureSlot, Texture2DSettings settings) throws IOException {
		InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
		if (is == null) {
			throw new IOException("Texture file " + fileName + "has not been found.");
		}

		BufferedImage image = ImageIO.read(is);

		this.textureWidth = image.getWidth();
		this.textureHeight = image.getHeight();

		int[] pixels = new int[image.getWidth() * image.getHeight()];
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0 , image.getWidth());
		ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);
		for (int i = image.getHeight() - 1; i >= 0; i--) {
			for (int j = 0; j < image.getWidth(); j++) {
				int pixel = pixels[i * image.getWidth() + j];
				buffer.put((byte) ((pixel >> 16) & 0xFF));
				buffer.put((byte) ((pixel >> 8) & 0xFF));
				buffer.put((byte) ((pixel >> 0) & 0xFF));
				buffer.put((byte) ((pixel >> 24) & 0xFF));
			}
		}
		buffer.flip();

		textureID = glGenTextures();
		this.textureSlot = textureSlot;
		glActiveTexture(GL_TEXTURE0 + textureSlot);
		glBindTexture(GL_TEXTURE_2D, textureID);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, settings.wrapS);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, settings.wrapT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, settings.magFilter);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, settings.minFilter);

		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
	}

	public int getTextureID() {
		return textureID;
	}

	public int getTextureSlot() {
		return textureSlot;
	}
}
