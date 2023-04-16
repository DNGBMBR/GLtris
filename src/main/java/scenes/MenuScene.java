package scenes;

import menu.component.TopFrame;
import menu.widgets.*;
import network.lobby.GameClient;
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

	TopFrame mainFrame = new TopFrame(Constants.VIEWPORT_W, Constants.VIEWPORT_H, true);

	Scene nextScene;

	public MenuScene(long windowID, GameClient client) {
		super(windowID, client);

		menuShader = ResourceManager.getShaderByName("shaders/block_vertex.glsl", "shaders/block_fragment.glsl");
		widgetTexture = ResourceManager.getTextureNineSliceByName("images/widgets.png");

		mainFrame.addComponent(
			new Button((Constants.VIEWPORT_W - BUTTON_WIDTH) * 0.5, Constants.VIEWPORT_H * 0.4, true,
				BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_BORDER_WIDTH, START_GAME,
				widgetTexture,
				(double mouseX, double mouseY, int button, int action, int mods) -> {
				if (action == GLFW_RELEASE) {
					nextScene = new GameSetupScene(windowID, client);
					shouldChangeScene = true;
				}
			}));
		mainFrame.addComponent(new Button((Constants.VIEWPORT_W - BUTTON_WIDTH) * 0.5, Constants.VIEWPORT_H * 0.4 - (BUTTON_HEIGHT + 50.0), true,
			BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_BORDER_WIDTH, SETTINGS,
			widgetTexture,
			(double mouseX, double mouseY, int button, int action, int mods) -> {
				if (action == GLFW_RELEASE) {
					nextScene = new SettingsScene(windowID, client);
					shouldChangeScene = true;
				}
			}));
		mainFrame.addComponent(new Button((Constants.VIEWPORT_W - BUTTON_WIDTH) * 0.5, Constants.VIEWPORT_H * 0.4 - 2 * (BUTTON_HEIGHT + 50.0), true,
			BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_BORDER_WIDTH, "Multiplayer",
			widgetTexture,
			(double mouseX, double mouseY, int button, int action, int mods) -> {
				if (action == GLFW_RELEASE) {
					nextScene = new LobbySearchScene(windowID, client);
					shouldChangeScene = true;
				}
			}));

		widgetBatch = new WidgetBatch(40);

		textRenderer = TextRenderer.getInstance();
	}

	@Override
	public void update(double dt) {

	}

	@Override
	public void draw() {
		menuShader.bind();

		//bind texture for button here
		menuShader.bindTexture2D("uTexture", widgetTexture);

		float[] buffer = new float[16];
		menuShader.uploadUniformMatrix4fv("uProjection", false, projection.get(buffer));

		widgetBatch.addComponent(mainFrame);

		widgetBatch.flush();

		textRenderer.bind();

		textRenderer.addText(mainFrame);

		textRenderer.draw();
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
