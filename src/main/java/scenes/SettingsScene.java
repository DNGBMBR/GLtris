package scenes;

import menu.component.Frame;
import menu.component.TopFrame;
import menu.widgets.*;
import menu.widgets.Button;
import org.joml.Math;
import org.joml.Matrix4f;
import render.Shader;
import render.batch.WidgetBatch;
import render.manager.ResourceManager;
import render.manager.TextRenderer;
import render.texture.TextureNineSlice;
import settings.KeybindingSettings;
import settings.LocalSettings;
import util.*;

import static org.lwjgl.glfw.GLFW.*;

public class SettingsScene extends Scene{

	private static final String BACK = "Back";
	private static final String SAVE = "Save";
	private static final String SDF = "SDF";
	private static final String ARR = "ARR";
	private static final String DAS = "DAS";

	private static final double BUTTON_WIDTH = 600.0;
	private static final double BUTTON_HEIGHT = 100.0;
	private static final double BUTTON_BORDER_WIDTH = 20.0;

	private static final double SLIDER_LENGTH = 600.0;
	private static final double SLIDER_POSITION_X = 150.0;
	private static final double SLIDER_POSITION_Y = 720.0;
	private static final double SLIDER_SPACING = 80.0;
	private static final double SLIDER_CLICKER_SIZE = 30.0;
	private static final double SLIDER_WIDTH = 10.0;

	private static final double CONTROLS_X_POS = 900.0;
	private static final double CONTROLS_Y_POS = 200.0;
	private static final double CONTROLS_WIDTH = Constants.VIEWPORT_W - CONTROLS_X_POS - 50;
	private static final double CONTROLS_HEIGHT = Constants.VIEWPORT_H - CONTROLS_Y_POS - 50;
	private static final double INPUT_MARGIN_LARGE = 280.0;
	private static final double INPUT_MARGIN_SMALL = 50.0;
	private static final double INPUT_WIDTH = 300.0;
	private static final double INPUT_HEIGHT = 50.0;
	private static final double INPUT_SPACING = 100.0;

	Shader menuShader;
	WidgetBatch widgetBatch;
	TextureNineSlice widgetTexture;
	TextRenderer textRenderer;

	Matrix4f projection;

	TopFrame settingsFrame = new TopFrame(Constants.VIEWPORT_W, Constants.VIEWPORT_H, true);
	Frame controlsFrame = new Frame(CONTROLS_X_POS, CONTROLS_Y_POS, CONTROLS_WIDTH, CONTROLS_HEIGHT,
		true, false, 0);

	double sdf = LocalSettings.getSDF();
	double arr = LocalSettings.getARR();
	double das = LocalSettings.getDAS();

	boolean shouldChangeScene = false;
	Scene nextScene;

	SettingsScene(long windowID) {
		super(windowID);

		menuShader = ResourceManager.getShaderByName("shaders/widget_vertex.glsl", "shaders/widget_fragment.glsl");
		widgetTexture = ResourceManager.getTextureNineSliceByName("images/widgets.png");

		settingsFrame.addComponent(new Button(50.0, 50.0, true,
			BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_BORDER_WIDTH, BACK,
			widgetTexture, Constants.BUTTON_PX, Constants.BUTTON_PY,
			(double mouseX, double mouseY, int button, int action, int mods) -> {
				shouldChangeScene = true;
				nextScene = new MenuScene(windowID);
			}));
		settingsFrame.addComponent(new Slider(SLIDER_POSITION_X, SLIDER_POSITION_Y,true, SDF,
			Utils.inverseLerp(Constants.MIN_SDF, Constants.MAX_SDF, sdf),
			SLIDER_LENGTH, Constants.MIN_SDF, Constants.MAX_SDF, SLIDER_CLICKER_SIZE, SLIDER_WIDTH, true,
			widgetTexture, Constants.SLIDER_PX, Constants.SLIDER_PY,
			(double percentage) -> {
				sdf = Math.lerp(Constants.MIN_SDF, Constants.MAX_SDF, percentage);
			}));
		settingsFrame.addComponent(new Slider(SLIDER_POSITION_X, SLIDER_POSITION_Y - SLIDER_SPACING,true, ARR,
			Utils.inverseLerp(Constants.MIN_ARR, Constants.MAX_ARR, arr),
			SLIDER_LENGTH, Constants.MIN_ARR, Constants.MAX_ARR, SLIDER_CLICKER_SIZE, SLIDER_WIDTH, true,
			widgetTexture, Constants.SLIDER_PX, Constants.SLIDER_PY,
			(double percentage) -> {
				arr = Math.lerp(Constants.MIN_ARR, Constants.MAX_ARR, percentage);
			}));
		settingsFrame.addComponent(new Slider(SLIDER_POSITION_X, SLIDER_POSITION_Y - SLIDER_SPACING * 2.0, true, DAS,
			Utils.inverseLerp(Constants.MIN_DAS, Constants.MAX_DAS, das),
			SLIDER_LENGTH, Constants.MIN_DAS, Constants.MAX_DAS, SLIDER_CLICKER_SIZE, SLIDER_WIDTH, true,
			widgetTexture, Constants.SLIDER_PX, Constants.SLIDER_PY,
			(double percentage) -> {
				das = Math.lerp(Constants.MIN_DAS, Constants.MAX_DAS, percentage);
			}));
		settingsFrame.addComponent(new Button(Constants.VIEWPORT_W - BUTTON_WIDTH - 50, 50.0, true,
			BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_BORDER_WIDTH, SAVE,
			widgetTexture, Constants.BUTTON_PX, Constants.BUTTON_PY,
			(double mouseX, double mouseY, int button, int action, int mods) -> {
				LocalSettings.setARR(arr);
				LocalSettings.setDAS(das);
				LocalSettings.setSDF(sdf);
				LocalSettings.saveSettings();

				KeybindingSettings.saveSettings();
			}));

		int[] moveLeftKeys = KeybindingSettings.getMoveLeftKeys();
		int[] moveRightKeys = KeybindingSettings.getMoveRightKeys();
		int[] softDropKeys = KeybindingSettings.getSoftDropKeys();
		int[] rotateCWKeys = KeybindingSettings.getRotateCWKeys();
		int[] rotateCCWKeys = KeybindingSettings.getRotateCCWKeys();
		int[] rotate180Keys = KeybindingSettings.getRotate180Keys();
		int[] holdKeys = KeybindingSettings.getHoldKeys();
		int[] hardDropKeys = KeybindingSettings.getHardDropKeys();

		controlsFrame.addComponent(new KeyBindingComponent(
			INPUT_MARGIN_LARGE, CONTROLS_HEIGHT - INPUT_MARGIN_SMALL - INPUT_HEIGHT, INPUT_WIDTH, INPUT_HEIGHT,
			"move left 1", true, moveLeftKeys[0],
			widgetTexture, 0, 3,
			(int scancode) -> {
				KeybindingSettings.setMoveLeftKey(scancode, 0);
			}));
		controlsFrame.addComponent(new KeyBindingComponent(
			CONTROLS_WIDTH - INPUT_WIDTH - INPUT_MARGIN_SMALL, CONTROLS_HEIGHT - INPUT_MARGIN_SMALL - INPUT_HEIGHT, INPUT_WIDTH, INPUT_HEIGHT,
			"move left 2", true, moveLeftKeys[1],
			widgetTexture, 0, 3,
			(int scancode) -> {
				KeybindingSettings.setMoveLeftKey(scancode, 1);
			}));

		controlsFrame.addComponent(new KeyBindingComponent(
			INPUT_MARGIN_LARGE, CONTROLS_HEIGHT - INPUT_MARGIN_SMALL - INPUT_HEIGHT - 1 * INPUT_SPACING, INPUT_WIDTH, INPUT_HEIGHT,
			"move right 1", true, moveRightKeys[0],
			widgetTexture, 0, 3,
			(int scancode) -> {
				KeybindingSettings.setMoveRightKey(scancode, 0);
			}));
		controlsFrame.addComponent(new KeyBindingComponent(
			CONTROLS_WIDTH - INPUT_WIDTH - INPUT_MARGIN_SMALL, CONTROLS_HEIGHT - INPUT_MARGIN_SMALL - INPUT_HEIGHT - 1 * INPUT_SPACING, INPUT_WIDTH, INPUT_HEIGHT,
			"move right 2", true, moveRightKeys[1],
			widgetTexture, 0, 3,
			(int scancode) -> {
				KeybindingSettings.setMoveRightKey(scancode, 1);
			}));

		controlsFrame.addComponent(new KeyBindingComponent(
			INPUT_MARGIN_LARGE, CONTROLS_HEIGHT - INPUT_MARGIN_SMALL - INPUT_HEIGHT - 2 * INPUT_SPACING, INPUT_WIDTH, INPUT_HEIGHT,
			"soft drop 1", true, softDropKeys[0],
			widgetTexture, 0, 3,
			(int scancode) -> {
				KeybindingSettings.setSoftDropKey(scancode, 0);
			}));
		controlsFrame.addComponent(new KeyBindingComponent(
			CONTROLS_WIDTH - INPUT_WIDTH - INPUT_MARGIN_SMALL, CONTROLS_HEIGHT - INPUT_MARGIN_SMALL - INPUT_HEIGHT - 2 * INPUT_SPACING, INPUT_WIDTH, INPUT_HEIGHT,
			"soft drop 2", true, softDropKeys[1],
			widgetTexture, 0, 3,
			(int scancode) -> {
				KeybindingSettings.setSoftDropKey(scancode, 1);
			}));

		controlsFrame.addComponent(new KeyBindingComponent(
			INPUT_MARGIN_LARGE, CONTROLS_HEIGHT - INPUT_MARGIN_SMALL - INPUT_HEIGHT - 3 * INPUT_SPACING, INPUT_WIDTH, INPUT_HEIGHT,
			"rotate cw 1", true, rotateCWKeys[0],
			widgetTexture, 0, 3,
			(int scancode) -> {
				KeybindingSettings.setRotateCWKey(scancode, 0);
			}));
		controlsFrame.addComponent(new KeyBindingComponent(
			CONTROLS_WIDTH - INPUT_WIDTH - INPUT_MARGIN_SMALL, CONTROLS_HEIGHT - INPUT_MARGIN_SMALL - INPUT_HEIGHT - 3 * INPUT_SPACING, INPUT_WIDTH, INPUT_HEIGHT,
			"rotate cw 2", true, rotateCWKeys[1],
			widgetTexture, 0, 3,
			(int scancode) -> {
				KeybindingSettings.setRotateCWKey(scancode, 1);
			}));

		controlsFrame.addComponent(new KeyBindingComponent(
			INPUT_MARGIN_LARGE, CONTROLS_HEIGHT - INPUT_MARGIN_SMALL - INPUT_HEIGHT - 4 * INPUT_SPACING, INPUT_WIDTH, INPUT_HEIGHT,
			"rotate ccw 1", true, rotateCCWKeys[0],
			widgetTexture, 0, 3,
			(int scancode) -> {
				KeybindingSettings.setRotateCCWKey(scancode, 0);
			}));
		controlsFrame.addComponent(new KeyBindingComponent(
			CONTROLS_WIDTH - INPUT_WIDTH - INPUT_MARGIN_SMALL, CONTROLS_HEIGHT - INPUT_MARGIN_SMALL - INPUT_HEIGHT - 4 * INPUT_SPACING, INPUT_WIDTH, INPUT_HEIGHT,
			"rotate ccw 2", true, rotateCCWKeys[1],
			widgetTexture, 0, 3,
			(int scancode) -> {
				KeybindingSettings.setRotateCCWKey(scancode, 1);
			}));

		controlsFrame.addComponent(new KeyBindingComponent(
			INPUT_MARGIN_LARGE, CONTROLS_HEIGHT - INPUT_MARGIN_SMALL - INPUT_HEIGHT - 5 * INPUT_SPACING, INPUT_WIDTH, INPUT_HEIGHT,
			"rotate 180 1", true, rotate180Keys[0],
			widgetTexture, 0, 3,
			(int scancode) -> {
				KeybindingSettings.setRotate180Key(scancode, 0);
			}));
		controlsFrame.addComponent(new KeyBindingComponent(
			CONTROLS_WIDTH - INPUT_WIDTH - INPUT_MARGIN_SMALL, CONTROLS_HEIGHT - INPUT_MARGIN_SMALL - INPUT_HEIGHT - 5 * INPUT_SPACING, INPUT_WIDTH, INPUT_HEIGHT,
			"rotate 180 2", true, rotate180Keys[1],
			widgetTexture, 0, 3,
			(int scancode) -> {
				KeybindingSettings.setRotate180Key(scancode, 1);
			}));

		controlsFrame.addComponent(new KeyBindingComponent(
			INPUT_MARGIN_LARGE, CONTROLS_HEIGHT - INPUT_MARGIN_SMALL - INPUT_HEIGHT - 6 * INPUT_SPACING, INPUT_WIDTH, INPUT_HEIGHT,
			"hold 1", true, holdKeys[0],
			widgetTexture, 0, 3,
			(int scancode) -> {
				KeybindingSettings.setHoldKey(scancode, 0);
			}));
		controlsFrame.addComponent(new KeyBindingComponent(
			CONTROLS_WIDTH - INPUT_WIDTH - INPUT_MARGIN_SMALL, CONTROLS_HEIGHT - INPUT_MARGIN_SMALL - INPUT_HEIGHT - 6 * INPUT_SPACING, INPUT_WIDTH, INPUT_HEIGHT,
			"hold 2", true, holdKeys[1],
			widgetTexture, 0, 3,
			(int scancode) -> {
				KeybindingSettings.setHoldKey(scancode, 1);
			}));

		controlsFrame.addComponent(new KeyBindingComponent(
			INPUT_MARGIN_LARGE, CONTROLS_HEIGHT - INPUT_MARGIN_SMALL - INPUT_HEIGHT - 7 * INPUT_SPACING, INPUT_WIDTH, INPUT_HEIGHT,
			"hard drop 1", true, hardDropKeys[0],
			widgetTexture, 0, 3,
			(int scancode) -> {
				KeybindingSettings.setHardDropKey(scancode, 0);
			}));
		controlsFrame.addComponent(new KeyBindingComponent(
			CONTROLS_WIDTH - INPUT_WIDTH - INPUT_MARGIN_SMALL, CONTROLS_HEIGHT - INPUT_MARGIN_SMALL - INPUT_HEIGHT - 7 * INPUT_SPACING, INPUT_WIDTH, INPUT_HEIGHT,
			"hard drop 2", true, hardDropKeys[1],
			widgetTexture, 0, 3,
			(int scancode) -> {
				KeybindingSettings.setHardDropKey(scancode, 1);
			}));

		settingsFrame.addComponent(controlsFrame);

		widgetBatch = new WidgetBatch(100);

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

		widgetTexture.bind(menuShader, "uTexture");

		float[] buffer = new float[16];
		menuShader.uploadUniformMatrix4fv("uProjection", false, projection.get(buffer));

		widgetBatch.addComponent(settingsFrame);

		widgetBatch.flush();

		textRenderer.bind();

		textRenderer.addText(settingsFrame);

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
		settingsFrame.destroy();
	}
}
