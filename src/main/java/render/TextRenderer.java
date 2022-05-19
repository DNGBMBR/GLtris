package render;

import org.joml.Matrix4f;
import render.manager.ResourceManager;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;

public class TextRenderer{
	private Shader textShader;

	private Matrix4f projection = new Matrix4f().setOrtho(0.0f, 1920.0f, 0.0f, 1080.0f, 0.0f,  100.0f);
	private Camera camera = new Camera();
	private Matrix4f transform = new Matrix4f();

	private BatchFont batch;
	private TextureAtlas fontTexture;

	private static TextRenderer instance;

	//fontLocation must point to an image file
	//TODO: Add support for font configs, rather than just supplying an image location
	private TextRenderer(TextureAtlas atlas, int capacity) {
		if (capacity <= 0) {
			throw new IllegalArgumentException("Cannot have less than or equal to 0 vertices in the batch.");
		}

		fontTexture = atlas;
		textShader = ResourceManager.getShaderByName("shaders/text_vertex.glsl", "shaders/text_fragment.glsl");
		if (textShader == null) {
			try {
				textShader = ResourceManager.createShader("shaders/text_vertex.glsl", "shaders/text_fragment.glsl");
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
				assert false;
			}
		}

		batch = new BatchFont(capacity * 6);
	}

	public static TextRenderer getInstance() {
		if (instance == null) {
			TextureAtlas fontTexture = ResourceManager.getAtlasByName("fonts/font.png");
			if (fontTexture == null) {
				try {
					fontTexture = ResourceManager.createTextureAtlas("fonts/font.png", 0, 8, 8);
				} catch (IOException e) {
					e.printStackTrace();
					assert false;
				}
			}
			instance = new TextRenderer(fontTexture, 100);
		}
		return instance;
	}

	public void updateProjection(long windowID) {
		float projectionWidth = 1920.0f;
		int[] windowWidth = new int[1];
		int[] windowHeight = new int[1];
		glfwGetWindowSize(windowID, windowWidth, windowHeight);
		float windowAspect = (float) windowHeight[0] / windowWidth[0];
		float projectionHeight = projectionWidth * windowAspect;
		projection = new Matrix4f().identity().ortho(
			0.0f, projectionWidth,
			0.0f, projectionHeight,
			0.001f, 10000.0f);
	}

	public void bind() {
		textShader.bind();
		fontTexture.bind(textShader, "uFontTexture");

		float[] buffer = new float[16];
		textShader.uploadUniformMatrix4fv("uProjection", false, projection.get(buffer));
		textShader.uploadUniformMatrix4fv("uView", false, camera.getView().get(buffer));
		textShader.uploadUniformMatrix4fv("uTransform", false, transform.get(buffer));
	}

	private void addCharacter(char c, float scale, float xPos, float yPos, float r, float g, float b) {
		float[] uvData = fontTexture.getElementUVs(c, 0, 1, 1);

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
