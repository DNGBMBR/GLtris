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
	private static final String BACK = "Back";
	private static final String SAVE = "Save";
	private static final String SDF = "SDF";
	private static final String ARR = "ARR";
	private static final String DAS = "DAS";

	private static final double BUTTON_WIDTH = 600.0;
	private static final double BUTTON_HEIGHT = 100.0;
	private static final double BUTTON_BORDER_WIDTH = 20.0;

	private static final double SLIDER_LENGTH = 600.0;
	private static final double SLIDER_POSITION_X = (Constants.VIEWPORT_W - SLIDER_LENGTH) * 0.5;
	private static final double SLIDER_POSITION_Y = 720.0;
	private static final double SLIDER_SPACING = 80.0;
	private static final double SLIDER_CLICKER_SIZE = 30.0;
	private static final double SLIDER_WIDTH = 10.0;

	Shader menuShader;
	WidgetBatch widgetBatch;
	TextureNineSlice widgetTexture;
	TextRenderer textRenderer;

	Matrix4f projection;
	Camera camera = new Camera();
	Matrix4f transform = new Matrix4f().identity();

	TopFrame mainFrame = new TopFrame(Constants.VIEWPORT_W, Constants.VIEWPORT_H, true);
	TopFrame settingsFrame = new TopFrame(Constants.VIEWPORT_W, Constants.VIEWPORT_H, false);

	double sdf = LocalSettings.getSDF();
	double arr = LocalSettings.getARR();
	double das = LocalSettings.getDAS();

	public MenuScene(long windowID) {
		super(windowID);

		menuShader = ResourceManager.getShaderByName("shaders/widget_vertex.glsl", "shaders/widget_fragment.glsl");
		widgetTexture = ResourceManager.getTextureNineSliceByName("images/widgets.png");

		//TODO: make this WAY less dense. Group widgets?
		mainFrame.addComponent(
			new Button((Constants.VIEWPORT_W - BUTTON_WIDTH) * 0.5, Constants.VIEWPORT_H * 0.4, true,
				BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_BORDER_WIDTH, START_GAME,
				widgetTexture, 0, 2,
				(double mouseX, double mouseY, int button, int action, int mods) -> {
				if (action == GLFW_RELEASE) {
					shouldChangeScene = true;
				}
			}));
		mainFrame.addComponent(new Button((Constants.VIEWPORT_W - BUTTON_WIDTH) * 0.5, Constants.VIEWPORT_H * 0.4 - (BUTTON_HEIGHT + 50.0), true,
			BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_BORDER_WIDTH, SETTINGS,
			widgetTexture, 0, 2,
			(double mouseX, double mouseY, int button, int action, int mods) -> {
				if (action == GLFW_RELEASE) {
					mainFrame.setActive(false);
					settingsFrame.setActive(true);
				}
			}));

		settingsFrame.addComponent(new Button(100.0, 100.0, true,
			BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_BORDER_WIDTH, BACK,
			widgetTexture, 0, 2,
			(double mouseX, double mouseY, int button, int action, int mods) -> {
				mainFrame.setActive(true);
				settingsFrame.setActive(false);
			}));
		settingsFrame.addComponent(new Slider(SLIDER_POSITION_X, SLIDER_POSITION_Y,true, SDF,
			Utils.inverseLerp(Constants.MIN_SDF, Constants.MAX_SDF, sdf),
			SLIDER_LENGTH, Constants.MIN_SDF, Constants.MAX_SDF, SLIDER_CLICKER_SIZE, SLIDER_WIDTH, true,
			widgetTexture, 0, 1,
			(double percentage) -> {
				sdf = Math.lerp(Constants.MIN_SDF, Constants.MAX_SDF, percentage);
			}));
		settingsFrame.addComponent(new Slider(SLIDER_POSITION_X, SLIDER_POSITION_Y - SLIDER_SPACING,true, ARR,
			Utils.inverseLerp(Constants.MIN_ARR, Constants.MAX_ARR, arr),
			SLIDER_LENGTH, Constants.MIN_ARR, Constants.MAX_ARR, SLIDER_CLICKER_SIZE, SLIDER_WIDTH, true,
			widgetTexture, 0, 1,
			(double percentage) -> {
				arr = Math.lerp(Constants.MIN_ARR, Constants.MAX_ARR, percentage);
			}));
		settingsFrame.addComponent(new Slider(SLIDER_POSITION_X, SLIDER_POSITION_Y - SLIDER_SPACING * 2.0, true, DAS,
			Utils.inverseLerp(Constants.MIN_DAS, Constants.MAX_DAS, das),
			SLIDER_LENGTH, Constants.MIN_DAS, Constants.MAX_DAS, SLIDER_CLICKER_SIZE, SLIDER_WIDTH, true,
			widgetTexture, 0, 1,
			(double percentage) -> {
				das = Math.lerp(Constants.MIN_DAS, Constants.MAX_DAS, percentage);
			}));
		settingsFrame.addComponent(new Button(Constants.VIEWPORT_W - BUTTON_WIDTH - 100.0, 100.0, true,
			BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_BORDER_WIDTH, SAVE,
			widgetTexture, 0, 2,
			(double mouseX, double mouseY, int button, int action, int mods) -> {
				LocalSettings.setARR(arr);
				LocalSettings.setDAS(das);
				LocalSettings.setSDF(sdf);
				LocalSettings.saveSettings();
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

		if (mainFrame.isActive()) {
			widgetBatch.addComponent(mainFrame);
		}
		if (settingsFrame.isActive()) {
			widgetBatch.addComponent(settingsFrame);
		}

		widgetBatch.flush();

		textRenderer.bind();

		if (settingsFrame.isActive()) {
			textRenderer.addText(settingsFrame);
		}

		if (mainFrame.isActive()) {
			textRenderer.addText(mainFrame);
		}

		textRenderer.draw();
	}

	@Override
	public boolean shouldChangeScene() {
		return shouldChangeScene;
	}

	@Override
	public Scene nextScene() {
		return new GameSetupScene(windowID);
	}

	@Override
	public void destroy() {
		mainFrame.destroy();
		settingsFrame.destroy();
		widgetBatch.destroy();
	}
}
