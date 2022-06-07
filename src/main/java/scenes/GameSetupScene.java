package scenes;

import menu.component.*;
import menu.widgets.*;
import org.joml.Matrix4f;
import render.*;
import render.batch.WidgetBatch;
import render.manager.ResourceManager;
import render.manager.TextRenderer;
import render.texture.TextureNineSlice;
import util.Constants;
import settings.GameSettings;

import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;

public class GameSetupScene extends Scene{
	private static final double FRAME_SETTINGS_WIDTH = 1000;
	private static final double FRAME_SETTINGS_HEIGHT = 720;
	private static final double FRAME_SETTINGS_X_POS = (Constants.VIEWPORT_W - FRAME_SETTINGS_WIDTH) * 0.5;
	private static final double FRAME_SETTINGS_Y_POS = (Constants.VIEWPORT_H - FRAME_SETTINGS_HEIGHT) * 0.5;

	public static final String PREVIEWS = "Previews";
	public static final String INITIAL_GRAVITY = "Initial Gravity";
	public static final String GRAVITY_INCREASE = "Gravity Increase";
	public static final String GRAVITY_INCREASE_INTERVAL = "Gravity Increase Interval";
	public static final String LOCK_DELAY = "Lock Delay";

	public static final int FONT_SIZE = 24;
	public static final int X_POS_TEXT_FIELD = 20;

	private Matrix4f projection;
	private Scene nextScene;

	private Shader blockShader = ResourceManager.getShaderByName("shaders/block_vertex.glsl", "shaders/block_fragment.glsl");
	private TextureNineSlice widgetTexture = ResourceManager.getTextureNineSliceByName("images/widgets.png");
	private WidgetBatch batch = new WidgetBatch(80);

	private TextRenderer textRenderer = TextRenderer.getInstance();

	private TopFrame topFrame;
	private Frame settingsFrame;

	TextField textFieldNumPreviews;
	TextField textFieldGravityInit;
	TextField textFieldGravityIncrease;
	TextField textFieldGravityIncreaseInterval;
	TextField textFieldLockDelay;

	GameSetupScene(long windowID) {
		super(windowID);
		topFrame = new TopFrame(Constants.VIEWPORT_W, Constants.VIEWPORT_H, true);
		settingsFrame = new Frame(FRAME_SETTINGS_X_POS, FRAME_SETTINGS_Y_POS,
				FRAME_SETTINGS_WIDTH, FRAME_SETTINGS_HEIGHT,
				true, true, 1000);

		int numPreviews = GameSettings.getNumPreviews();
		double gravityInit = GameSettings.getInitGravity();
		double gravityIncrease = GameSettings.getGravityIncrease();
		int gravityIncreaseInterval = GameSettings.getGravityIncreaseInterval();
		double lockDelay = GameSettings.getLockDelay();

		textFieldNumPreviews = new TextField(X_POS_TEXT_FIELD + PREVIEWS.length() * FONT_SIZE, 600, 200, 50, true,
			PREVIEWS, String.valueOf(numPreviews), FONT_SIZE, 0.0f, 0.0f, 0.0f);
		textFieldGravityInit = new TextField(X_POS_TEXT_FIELD + INITIAL_GRAVITY.length() * FONT_SIZE, 500, 200, 50, true,
			INITIAL_GRAVITY, String.valueOf(gravityInit), FONT_SIZE, 0.0f, 0.0f, 0.0f);
		textFieldGravityIncrease = new TextField(X_POS_TEXT_FIELD + GRAVITY_INCREASE.length() * FONT_SIZE, 425, 200, 50, true,
			GRAVITY_INCREASE, String.valueOf(gravityIncrease), FONT_SIZE, 0.0f, 0.0f, 0.0f);
		textFieldGravityIncreaseInterval = new TextField(X_POS_TEXT_FIELD + GRAVITY_INCREASE_INTERVAL.length() * FONT_SIZE, 350, 200, 50, true,
			GRAVITY_INCREASE_INTERVAL, String.valueOf(gravityIncreaseInterval), FONT_SIZE, 0.0f, 0.0f, 0.0f);
		textFieldLockDelay = new TextField(X_POS_TEXT_FIELD + LOCK_DELAY.length() * FONT_SIZE, 275, 200, 50, true,
			LOCK_DELAY, String.valueOf(lockDelay), FONT_SIZE, 0.0f, 0.0f, 0.0f);

		settingsFrame.addComponent(textFieldNumPreviews);
		settingsFrame.addComponent(textFieldGravityInit);
		settingsFrame.addComponent(textFieldGravityIncrease);
		settingsFrame.addComponent(textFieldGravityIncreaseInterval);
		settingsFrame.addComponent(textFieldLockDelay);

		topFrame.addComponent(settingsFrame);

		topFrame.addComponent(new Button((Constants.VIEWPORT_W - 300) * 0.5, FRAME_SETTINGS_Y_POS - 100, true, 300, 100, 25,
			"Save", widgetTexture, Constants.BUTTON_PX, Constants.BUTTON_PY,
			(double mouseX, double mouseY, int button, int action, int mods) -> {
				try {
					int numPreviewsNew = Integer.parseInt(textFieldNumPreviews.getText());
					double gravityInitNew = Double.parseDouble(textFieldGravityInit.getText());
					double gravityIncreaseNew = Double.parseDouble(textFieldGravityIncrease.getText());
					int gravityIncreaseIntervalNew = Integer.parseInt(textFieldGravityIncreaseInterval.getText());
					double lockDelayNew = Double.parseDouble(textFieldLockDelay.getText());

					GameSettings.setNumPreviews(numPreviewsNew);
					GameSettings.setInitGravity(gravityInitNew);
					GameSettings.setGravityIncrease(gravityIncreaseNew);
					GameSettings.setGravityIncreaseInterval(gravityIncreaseIntervalNew);
					GameSettings.setLockDelay(lockDelayNew);

					GameSettings.saveSettings();
				} catch (NumberFormatException e) {
					//replace with in game error message
					e.printStackTrace();
				}
			}));

		topFrame.addComponent(
			new Button(Constants.VIEWPORT_W - 650, 50, true,
			600, 100, 25, "Start Game",
				widgetTexture, Constants.BUTTON_PX, Constants.BUTTON_PY,
				(double mouseX, double mouseY, int button, int action, int mods) -> {
					nextScene = new GameScene(windowID);
					shouldChangeScene = true;
				}));
		topFrame.addComponent(
			new Button(50, 50, true,
				600, 100, 25, "Back",
				widgetTexture, Constants.BUTTON_PX, Constants.BUTTON_PY,
				(double mouseX, double mouseY, int button, int action, int mods) -> {
					nextScene = new MenuScene(windowID);
					shouldChangeScene = true;
				}));
	}

	@Override
	public void updateProjection(long windowID) {
		float projectionWidth = Constants.VIEWPORT_W;
		float projectionHeight = Constants.VIEWPORT_H;
		int[] windowWidth = new int[1];
		int[] windowHeight = new int[1];
		glfwGetWindowSize(windowID, windowWidth, windowHeight);
		float windowAspect = (float) windowWidth[0] / windowHeight[0];
		if (windowAspect >= 16.0f / 9.0f) {
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
		blockShader.bind();
		blockShader.bindTexture2D("uTexture", widgetTexture);

		float[] buffer = new float[16];

		blockShader.uploadUniformMatrix4fv("uProjection", false, projection.get(buffer));

		batch.addComponent(topFrame);
		batch.flush();

		textRenderer.bind();

		textRenderer.addText(topFrame);

		textRenderer.draw();
	}

	@Override
	public Scene nextScene() {
		return nextScene;
	}

	@Override
	public void destroy() {
		topFrame.destroy();
		batch.destroy();
	}
}
