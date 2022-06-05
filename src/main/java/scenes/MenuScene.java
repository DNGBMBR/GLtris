package scenes;

import menu.component.TopFrame;
import menu.widgets.*;
import org.joml.Math;
import org.joml.Matrix4f;
import render.*;
import render.batch.WidgetBatch;
import render.manager.ResourceManager;
import render.manager.TextRenderer;
import render.texture.TextureNineSlice;
import util.*;

import static org.lwjgl.glfw.GLFW.*;

public class MenuScene extends Scene{
	private static final String START_GAME = "Start Game";
	private static final String SETTINGS = "Settings";

	private static final double BUTTON_WIDTH = 600.0;
	private static final double BUTTON_HEIGHT = 100.0;
	private static final double BUTTON_BORDER_WIDTH = 20.0;

	Shader menuShader;
	WidgetBatch widgetBatch;
	TextureNineSlice widgetTexture;
	TextRenderer textRenderer;

	Matrix4f projection;

	TopFrame mainFrame = new TopFrame(Constants.VIEWPORT_W, Constants.VIEWPORT_H, true);

	boolean shouldChangeScene = false;
	Scene nextScene;

	public MenuScene(long windowID) {
		super(windowID);

		menuShader = ResourceManager.getShaderByName("shaders/widget_vertex.glsl", "shaders/widget_fragment.glsl");
		widgetTexture = ResourceManager.getTextureNineSliceByName("images/widgets.png");

		//TODO: make this WAY less dense. Group widgets?
		mainFrame.addComponent(
			new Button((Constants.VIEWPORT_W - BUTTON_WIDTH) * 0.5, Constants.VIEWPORT_H * 0.4, true,
				BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_BORDER_WIDTH, START_GAME,
				widgetTexture, Constants.BUTTON_PX, Constants.BUTTON_PY,
				(double mouseX, double mouseY, int button, int action, int mods) -> {
				if (action == GLFW_RELEASE) {
					nextScene = new GameSetupScene(windowID);
					shouldChangeScene = true;
				}
			}));
		mainFrame.addComponent(new Button((Constants.VIEWPORT_W - BUTTON_WIDTH) * 0.5, Constants.VIEWPORT_H * 0.4 - (BUTTON_HEIGHT + 50.0), true,
			BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_BORDER_WIDTH, SETTINGS,
			widgetTexture, Constants.BUTTON_PX, Constants.BUTTON_PY,
			(double mouseX, double mouseY, int button, int action, int mods) -> {
				if (action == GLFW_RELEASE) {
					nextScene = new SettingsScene(windowID);
					shouldChangeScene = true;
				}
			}));

		widgetBatch = new WidgetBatch(40);

		textRenderer = TextRenderer.getInstance();
	}

	@Override
	public void updateProjection(long windowID) {
		float projectionWidth = Constants.VIEWPORT_W;
		float projectionHeight = Constants.VIEWPORT_H;
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
			0.0f, 100.0f);
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
		widgetTexture.bind(menuShader, "uTexture");

		float[] buffer = new float[16];
		menuShader.uploadUniformMatrix4fv("uProjection", false, projection.get(buffer));

		widgetBatch.addComponent(mainFrame);

		widgetBatch.flush();

		textRenderer.bind();

		textRenderer.addText(mainFrame);

		textRenderer.draw();
	}

	@Override
	public boolean shouldChangeScene() {
		return shouldChangeScene;
	}

	@Override
	public Scene nextScene() {
		return nextScene;
	}

	@Override
	public void destroy() {
		mainFrame.destroy();
		widgetBatch.destroy();
	}
}
