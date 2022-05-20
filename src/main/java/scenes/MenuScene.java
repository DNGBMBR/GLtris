package scenes;

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

	List<Widget> widgetsMain = new ArrayList<>();
	List<Widget> widgetsSettings = new ArrayList<>();

	double sdf = LocalSettings.getSDF();
	double arr = LocalSettings.getARR();
	double das = LocalSettings.getDAS();

	public MenuScene(long windowID) {
		super(windowID);

		menuShader = ResourceManager.getShaderByName("shaders/widget_vertex.glsl", "shaders/widget_fragment.glsl");
		widgetTexture = ResourceManager.getTextureNineSliceByName("images/widgets.png");

		//TODO: make this WAY less dense. Group widgets?
		widgetsMain.add(
			new Button((Constants.VIEWPORT_W - BUTTON_WIDTH) * 0.5, Constants.VIEWPORT_H * 0.4, true,
				BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_BORDER_WIDTH, START_GAME,
				widgetTexture, 0, 1,
				(long window, int button, int action, int mods) -> {
				if (action == GLFW_RELEASE) {
					shouldChangeScene = true;
				}
			}));
		widgetsMain.add(new Button((Constants.VIEWPORT_W - BUTTON_WIDTH) * 0.5, Constants.VIEWPORT_H * 0.4 - (BUTTON_HEIGHT + 50.0), true,
			BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_BORDER_WIDTH, SETTINGS,
			widgetTexture, 0, 1,
			(long window, int button, int action, int mods) -> {
				if (action == GLFW_RELEASE) {
					for (Widget widget : widgetsMain) {
						widget.setInteractable(false);
					}
					for (Widget widget : widgetsSettings) {
						widget.setInteractable(true);
					}
				}
			}));

		widgetsSettings.add(new Button(100.0, 100.0, false,
			BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_BORDER_WIDTH, BACK,
			widgetTexture, 0, 1,
			(long window, int button, int action, int mods) -> {
				for (Widget widget : widgetsMain) {
					widget.setInteractable(true);
				}
				for (Widget widget : widgetsSettings) {
					widget.setInteractable(false);
				}
			}));
		widgetsSettings.add(new Slider(SLIDER_POSITION_X, SLIDER_POSITION_Y,false, SDF,
			Utils.inverseLerp(Constants.MIN_SDF, Constants.MAX_SDF, sdf),
			SLIDER_LENGTH, SLIDER_CLICKER_SIZE, SLIDER_WIDTH, true,
			widgetTexture, 0, 0, 1, 0,
			(long window, double percentage) -> {
				sdf = Math.lerp(Constants.MIN_SDF, Constants.MAX_SDF, percentage);
			}));
		widgetsSettings.add(new Slider(SLIDER_POSITION_X, SLIDER_POSITION_Y - SLIDER_SPACING,false, ARR,
			Utils.inverseLerp(Constants.MIN_ARR, Constants.MAX_ARR, arr),
			SLIDER_LENGTH, SLIDER_CLICKER_SIZE, SLIDER_WIDTH, true,
			widgetTexture, 0, 0, 1, 0,
			(long window, double percentage) -> {
				arr = Math.lerp(Constants.MIN_ARR, Constants.MAX_ARR, percentage);
			}));
		widgetsSettings.add(new Slider(SLIDER_POSITION_X, SLIDER_POSITION_Y - SLIDER_SPACING * 2.0, false, DAS,
			Utils.inverseLerp(Constants.MIN_DAS, Constants.MAX_DAS, das),
			SLIDER_LENGTH, SLIDER_CLICKER_SIZE, SLIDER_WIDTH, true,
			widgetTexture, 0, 0, 1, 0,
			(long window, double percentage) -> {
				das = Math.lerp(Constants.MIN_DAS, Constants.MAX_DAS, percentage);
			}));
		widgetsSettings.add(new Button(Constants.VIEWPORT_W - BUTTON_WIDTH - 100.0, 100.0, false,
			BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_BORDER_WIDTH, SAVE,
			widgetTexture, 0, 1,
			(long window, int button, int action, int mods) -> {
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
		menuShader.uploadUniformMatrix4fv("uView", false, camera.getView().get(buffer));
		menuShader.uploadUniformMatrix4fv("uTransform", false, transform.get(buffer));

		for (Widget widget : widgetsMain) {
			if (widget.isInteractable()) {
				widgetBatch.addWidget(widget);
			}
		}
		for (Widget widget : widgetsSettings) {
			if (widget.isInteractable()) {
				widgetBatch.addWidget(widget);
			}
		}

		widgetBatch.flush();

		textRenderer.bind();

		for (Widget widget : widgetsSettings) {
			if (!widget.isInteractable()) {
				continue;
			}
			if (widget instanceof Slider) {
				Slider slider = (Slider) widget;
				float fontSize = (float) slider.getClickerSize() * 0.75f;

				String sliderName = slider.getDisplayText();
				float startX = (float) slider.getXPos() - fontSize * (slider.getDisplayText().length() + 1);
				float startY = (float) slider.getYPos();
				textRenderer.addText(sliderName, fontSize, startX, startY, 0.0f, 0.0f, 0.0f);

				startX = (float) (slider.getXPos() + slider.getClickerSize() + (slider.isHorizontal() ? slider.getLength() : 0));
				startY = (float) (slider.getYPos() + (!slider.isHorizontal() ? slider.getLength() : 0));
				String text;
				if (sliderName.equals(SDF)) {
					text = String.valueOf(Math.lerp(Constants.MIN_SDF, Constants.MAX_SDF, slider.getPercentage()));
				}
				else if (sliderName.equals(ARR)) {
					text = String.valueOf(Math.lerp(Constants.MIN_ARR, Constants.MAX_ARR, slider.getPercentage()));
				}
				else if (sliderName.equals(DAS)) {
					text = String.valueOf(Math.lerp(Constants.MIN_DAS, Constants.MAX_DAS, slider.getPercentage()));
				}
				else {
					text = "WTF??? How is this even possible?????";
				}
				textRenderer.addText(text, fontSize, startX, startY, 0.0f, 0.0f, 0.0f);
			}
			if (widget instanceof Button) {
				Button button = (Button) widget;
				float centerX = ((float) (2.0 * button.getXPos() + button.width)) * 0.5f;
				float centerY = ((float) (2.0f * button.getYPos() + button.height)) * 0.5f;
				float fontSize = (float) button.height * 0.5f;
				float startX = centerX - fontSize * button.getDisplayText().length() * 0.5f;
				float startY = centerY - (float) button.height * 0.25f;
				textRenderer.addText(button.getDisplayText(), fontSize, startX, startY, 1.0f, 1.0f, 1.0f);
			}
		}
		for (Widget widget : widgetsMain) {
			if (!widget.isInteractable()) {
				continue;
			}
			if (widget instanceof Button) {
				Button button = (Button) widget;
				float centerX = ((float) (2.0 * button.getXPos() + button.width)) * 0.5f;
				float centerY = ((float) (2.0f * button.getYPos() + button.height)) * 0.5f;
				float fontSize = (float) button.height * 0.5f;
				float startX = centerX - fontSize * button.getDisplayText().length() * 0.5f;
				float startY = centerY - (float) button.height * 0.25f;
				textRenderer.addText(button.getDisplayText(), fontSize, startX, startY, 1.0f, 1.0f, 1.0f);
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
		return new GameScene(windowID);
	}

	@Override
	public void destroy() {
		for (Widget widget : widgetsMain) {
			widget.destroy();
		}
		widgetBatch.destroy();
	}
}
