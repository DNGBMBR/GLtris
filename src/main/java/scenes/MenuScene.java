package scenes;

import menu.component.Component;
import menu.component.TopFrame;
import menu.widgets.*;
import org.joml.Math;
import org.joml.Matrix4f;
import render.*;
import render.manager.ResourceManager;
import util.*;

import java.util.*;

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

	TopFrame widgetsMain = new TopFrame(Constants.VIEWPORT_W, Constants.VIEWPORT_H, true);
	TopFrame widgetsSettings = new TopFrame(Constants.VIEWPORT_W, Constants.VIEWPORT_H, false);

	double sdf = LocalSettings.getSDF();
	double arr = LocalSettings.getARR();
	double das = LocalSettings.getDAS();

	public MenuScene(long windowID) {
		super(windowID);

		menuShader = ResourceManager.getShaderByName("shaders/widget_vertex.glsl", "shaders/widget_fragment.glsl");
		widgetTexture = ResourceManager.getTextureNineSliceByName("images/widgets.png");

		//TODO: make this WAY less dense. Group widgets?
		widgetsMain.addComponent(
			new Button((Constants.VIEWPORT_W - BUTTON_WIDTH) * 0.5, Constants.VIEWPORT_H * 0.4, true,
				BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_BORDER_WIDTH, START_GAME,
				widgetTexture, 0, 1,
				(double mouseX, double mouseY, int button, int action, int mods) -> {
				if (action == GLFW_RELEASE) {
					shouldChangeScene = true;
				}
			}));
		widgetsMain.addComponent(new Button((Constants.VIEWPORT_W - BUTTON_WIDTH) * 0.5, Constants.VIEWPORT_H * 0.4 - (BUTTON_HEIGHT + 50.0), true,
			BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_BORDER_WIDTH, SETTINGS,
			widgetTexture, 0, 1,
			(double mouseX, double mouseY, int button, int action, int mods) -> {
				if (action == GLFW_RELEASE) {
					widgetsMain.setActive(false);
					widgetsSettings.setActive(true);
				}
			}));

		widgetsSettings.addComponent(new Button(100.0, 100.0, true,
			BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_BORDER_WIDTH, BACK,
			widgetTexture, 0, 1,
			(double mouseX, double mouseY, int button, int action, int mods) -> {
				widgetsMain.setActive(true);
				widgetsSettings.setActive(false);
			}));
		widgetsSettings.addComponent(new Slider(SLIDER_POSITION_X, SLIDER_POSITION_Y,true, SDF,
			Utils.inverseLerp(Constants.MIN_SDF, Constants.MAX_SDF, sdf),
			SLIDER_LENGTH, SLIDER_CLICKER_SIZE, SLIDER_WIDTH, true,
			widgetTexture, 0, 0, 1, 0,
			(double percentage) -> {
				sdf = Math.lerp(Constants.MIN_SDF, Constants.MAX_SDF, percentage);
			}));
		widgetsSettings.addComponent(new Slider(SLIDER_POSITION_X, SLIDER_POSITION_Y - SLIDER_SPACING,true, ARR,
			Utils.inverseLerp(Constants.MIN_ARR, Constants.MAX_ARR, arr),
			SLIDER_LENGTH, SLIDER_CLICKER_SIZE, SLIDER_WIDTH, true,
			widgetTexture, 0, 0, 1, 0,
			(double percentage) -> {
				arr = Math.lerp(Constants.MIN_ARR, Constants.MAX_ARR, percentage);
			}));
		widgetsSettings.addComponent(new Slider(SLIDER_POSITION_X, SLIDER_POSITION_Y - SLIDER_SPACING * 2.0, true, DAS,
			Utils.inverseLerp(Constants.MIN_DAS, Constants.MAX_DAS, das),
			SLIDER_LENGTH, SLIDER_CLICKER_SIZE, SLIDER_WIDTH, true,
			widgetTexture, 0, 0, 1, 0,
			(double percentage) -> {
				das = Math.lerp(Constants.MIN_DAS, Constants.MAX_DAS, percentage);
			}));
		widgetsSettings.addComponent(new Button(Constants.VIEWPORT_W - BUTTON_WIDTH - 100.0, 100.0, true,
			BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_BORDER_WIDTH, SAVE,
			widgetTexture, 0, 1,
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

		if (widgetsMain.isActive()) {
			widgetBatch.addComponent(widgetsMain);
		}
		if (widgetsSettings.isActive()) {
			widgetBatch.addComponent(widgetsSettings);
		}

		widgetBatch.flush();

		textRenderer.bind();

		if (widgetsSettings.isActive()) {
			for (Component component : widgetsSettings.getComponents()) {
				if (!component.isActive()) {
					continue;
				}
				if (component instanceof Slider) {
					Slider slider = (Slider) component;
					float fontSize = (float) slider.getClickerSize() * 0.75f;

					String sliderName = slider.getDisplayText();
					float startX = (float) slider.getXPos() - fontSize * (slider.getDisplayText().length() + 1);
					float startY = (float) slider.getYPos();
					textRenderer.addText(sliderName, fontSize, startX, startY, 0.0f, 0.0f, 0.0f);

					startX = (float) (slider.getXPos() + slider.getClickerSize() + (slider.isHorizontal() ? slider.getLength() : 0));
					startY = (float) (slider.getYPos() + (!slider.isHorizontal() ? slider.getLength() : 0));
					String text = "";
					if (sliderName.equals(SDF)) {
						text = String.valueOf(Math.lerp(Constants.MIN_SDF, Constants.MAX_SDF, slider.getPercentage()));
					}
					else if (sliderName.equals(ARR)) {
						text = String.valueOf(Math.lerp(Constants.MIN_ARR, Constants.MAX_ARR, slider.getPercentage()));
					}
					else if (sliderName.equals(DAS)) {
						text = String.valueOf(Math.lerp(Constants.MIN_DAS, Constants.MAX_DAS, slider.getPercentage()));
					}
					textRenderer.addText(text, fontSize, startX, startY, 0.0f, 0.0f, 0.0f);
				}
				if (component instanceof Button) {
					Button button = (Button) component;
					textRenderer.addText(button, 1.0f, 1.0f, 1.0f);
				}
			}
		}
		if (widgetsMain.isActive()) {
			for (Component component : widgetsMain.getComponents()) {
				if (!component.isActive()) {
					continue;
				}
				if (component instanceof Button) {
					Button button = (Button) component;
					textRenderer.addText(button, 1.0f, 1.0f, 1.0f);
				}

			}
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
		widgetsMain.destroy();
		widgetsSettings.destroy();
		widgetBatch.destroy();
	}
}
