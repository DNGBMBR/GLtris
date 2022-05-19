package scenes;

import menu.widgets.Button;
import org.joml.Matrix4f;
import render.*;
import render.manager.ResourceManager;
import util.MouseListener;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.lwjgl.glfw.GLFW.*;

public class MenuScene extends Scene{
	private static final String START_GAME = "Start Game";

	Shader menuShader;
	WidgetBatch widgetBatch;
	TextureNineSlice buttonTexture;
	TextRenderer textRenderer;


	Matrix4f projection;
	Camera camera = new Camera();
	Matrix4f transform = new Matrix4f().identity();

	Button pog;

	public MenuScene(long windowID) {
		super(windowID);
		pog = new Button(20.0, 20.0, 600.0, 100.0, 25.0, "Press me!", (long window, int button, int action, int mods) -> {
			if (action == GLFW_PRESS) {
				System.out.println("pressed");
			}
			if (action == GLFW_RELEASE) {
				System.out.println("released");
			}
		});
		MouseListener.registerMouseClickCallback(pog);

		menuShader = ResourceManager.getShaderByName("shaders/widget_vertex.glsl", "shaders/widget_fragment.glsl");
		if (menuShader == null) {
			try {
				menuShader = ResourceManager.createShader("shaders/widget_vertex.glsl", "shaders/widget_fragment.glsl");
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
				assert false;
			}
		}

		buttonTexture = ResourceManager.getTextureNineSliceByName("images/button.png");
		if (buttonTexture == null) {
			try {
				buttonTexture = ResourceManager.createTextureNineSlice("images/button.png", 2, 32, 32, 8, 8);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		widgetBatch = new WidgetBatch(20);

		textRenderer = TextRenderer.getInstance();
	}

	@Override
	public void updateProjection(long windowID) {
		float projectionWidth = 1920.0f;
		float projectionHeight = 1080.0f;
		int[] windowWidth = new int[1];
		int[] windowHeight = new int[1];
		glfwGetWindowSize(windowID, windowWidth, windowHeight);
		float windowAspect = (float) windowWidth[0] / windowHeight[0];
		if (windowAspect < 16.0f / 9.0f) {
			projectionWidth = projectionHeight * windowAspect;
		}
		else {
			projectionHeight = projectionWidth / windowAspect;
		}
		projection = new Matrix4f().identity().ortho(
			0.0f, projectionWidth,
			0.0f, projectionHeight,
			0.001f, 10000.0f);
	}

	@Override
	public void init() {
		updateProjection(windowID);
	}

	@Override
	public void update(double dt) {

	}

	@Override
	public void draw() {
		menuShader.bind();

		//bind texture for button here
		buttonTexture.bind(menuShader, "uTexture");

		float[] buffer = new float[16];
		menuShader.uploadUniformMatrix4fv("uProjection", false, projection.get(buffer));
		menuShader.uploadUniformMatrix4fv("uView", false, camera.getView().get(buffer));
		menuShader.uploadUniformMatrix4fv("uTransform", false, transform.get(buffer));

		/*
		float xPos = (float) pog.xPos;
		float yPos = (float) pog.yPos;
		float width = (float) pog.width;
		float height = (float) pog.height;
		float[] bottomLeft = {xPos, yPos, 0.0f, 0.0f};
		float[] bottomRight = {xPos + width, yPos, 1.0f, 0.0f};
		float[] topRight = {xPos + width, yPos + height, 1.0f, 1.0f};
		float[] topLeft = {xPos, yPos + height, 0.0f, 1.0f};
		*/


		if (pog.isPressed) {
			widgetBatch.addButton(pog, buttonTexture, 1, 0);
		}
		else {
			widgetBatch.addButton(pog, buttonTexture, 0, 0);
		}

		widgetBatch.flush();

		textRenderer.bind();
		float centerX = ((float) (2.0 * pog.xPos + pog.width)) * 0.5f;
		float centerY = ((float) (2.0f * pog.yPos + pog.height)) * 0.5f;
		float fontSize = (float) pog.height * 0.5f;
		float startX = centerX - fontSize * START_GAME.length() * 0.5f;
		float startY = centerY - (float) pog.height * 0.25f;
		textRenderer.addText(START_GAME, fontSize, startX, startY, 1.0f, 1.0f, 1.0f);
		textRenderer.draw();
	}

	@Override
	public boolean shouldChangeScene() {
		return false;
	}

	@Override
	public Scene nextScene() {
		return null;
	}

	@Override
	public void destroy() {
		pog.destroy();
	}
}
