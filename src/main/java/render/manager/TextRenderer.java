package render.manager;

import menu.component.*;
import org.joml.Math;
import org.joml.Matrix4f;
import render.Camera;
import render.Shader;
import render.batch.TextBatch;
import render.texture.TextureAtlas;
import util.Constants;
import util.Utils;

import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;

public class TextRenderer{
	private Shader textShader;

	private Matrix4f projection = new Matrix4f().setOrtho(0.0f, 1920.0f, 0.0f, 1080.0f, 0.0f,  100.0f);
	private Camera camera = new Camera();
	private Matrix4f transform = new Matrix4f();

	private TextBatch batch;
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

		batch = new TextBatch(capacity * Constants.WIDGET_ELEMENTS_PER_QUAD);
	}

	public static TextRenderer getInstance() {
		if (instance == null) {
			TextureAtlas fontTexture = ResourceManager.getAtlasByName("fonts/font.png");
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

	private void addCharacter(char c, float scale, float xPos, float yPos,
							  float left, float right, float bottom, float top,
							  float r, float g, float b) {
		float[] uvData = fontTexture.getElementUVs(c, 0, 1, 1);

		float p0x = Math.max(xPos, left);
		float p0y = Math.max(yPos, bottom);
		float p0u = xPos < left ? Math.lerp(uvData[0], uvData[2], (float) Utils.inverseLerp(xPos, xPos + scale, left)) : uvData[0];
		float p0v = yPos < bottom ? Math.lerp(uvData[1], uvData[3], (float) Utils.inverseLerp(yPos, yPos + scale, bottom)) : uvData[1];

		float p1x = Math.min(xPos + scale, right);
		float p1y = Math.min(yPos + scale, top);
		float p1u = xPos + scale > right ? Math.lerp(uvData[0], uvData[2], (float) Utils.inverseLerp(xPos, xPos + scale, right)) : uvData[2];
		float p1v = yPos + scale > top ? Math.lerp(uvData[1], uvData[3], (float) Utils.inverseLerp(yPos, yPos + scale, top)) : uvData[3];

		float[] vertexData = {
			p0x, p0y, r, g, b, p0u, p0v,
			p1x, p0y, r, g, b, p1u, p0v,
			p1x, p1y, r, g, b, p1u, p1v,
			p1x, p1y, r, g, b, p1u, p1v,
			p0x, p1y, r, g, b, p0u, p1v,
			p0x, p0y, r, g, b, p0u, p0v,
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

	private void addText(String text, float scale, float xPos, float yPos,
						float left, float right, float bottom, float top,
						float r, float g, float b) {
		if (xPos >= right || xPos + text.length() * scale <= left ||
			yPos >= top || yPos + scale <= bottom) {
			return;
		}

		float horizontalOffset = xPos;

		for (int i = 0; i < text.length(); i++) {
			if (horizontalOffset >= right) {
				break;
			}
			if (horizontalOffset + scale >= left) {
				char c = text.charAt(i);
				addCharacter(c, scale, horizontalOffset, yPos, left, right, bottom, top, r, g, b);
			}
			horizontalOffset += scale;
		}
	}

	public void addText(Frame frame) {
		float left = (float) frame.getXPos();
		float right = (float) (frame.getXPos() + frame.getWidth());
		float bottom = (float) frame.getYPos();
		float top = (float) (frame.getYPos() + frame.getHeight());
		for (Component component : frame.getComponents()) {
			if (component instanceof Frame subFrame) {
				addText(subFrame);
			}
			else {
				List<TextInfo> textInfo = component.getTextInfo();
				for (TextInfo info : textInfo) {
					addText(info.text, info.fontSize, left + info.startX, bottom + (float) frame.getCurrentScrollHeight() + info.startY,
						left, right, bottom, top, info.r, info.g, info.b);
				}
			}
		}
	}

	public void draw(){
		batch.flush();
	}
}
