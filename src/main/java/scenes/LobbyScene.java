package scenes;

import menu.component.Frame;
import menu.component.TopFrame;
import menu.widgets.Button;
import menu.widgets.TextComponent;
import network.lobby.*;
import render.Shader;
import render.batch.WidgetBatch;
import render.manager.ResourceManager;
import render.manager.TextRenderer;
import render.texture.TextureNineSlice;
import util.Constants;

import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class LobbyScene extends Scene {
	Shader menuShader;
	WidgetBatch widgetBatch;
	TextureNineSlice widgetTexture;
	TextRenderer textRenderer;

	TopFrame topFrame = new TopFrame(Constants.VIEWPORT_W, Constants.VIEWPORT_H, true);
	Frame lobbyFrame = new Frame(0, 0, Constants.VIEWPORT_W, Constants.VIEWPORT_H, true, false, 0);
	Frame postGameFrame = new Frame(0, 0, Constants.VIEWPORT_W, Constants.VIEWPORT_H, false, false, 0);

	boolean isSpectating = false;
	boolean isReady = false;

	boolean prepareForGame = false;
	Scene nextScene;

	OnPrepareGame prepareGameCallback;

	LobbyScene(long windowID, Client client) {
		super(windowID, client);
		prepareGameCallback = () -> {
			prepareForGame = true;
		};
		client.registerOnGamePrepare(prepareGameCallback);

		menuShader = ResourceManager.getShaderByName("shaders/block_vertex.glsl", "shaders/block_fragment.glsl");
		widgetTexture = ResourceManager.getTextureNineSliceByName("images/widgets.png");
		widgetBatch = new WidgetBatch(200);
		textRenderer = TextRenderer.getInstance();

		lobbyFrame.addComponent(new Button(50, 50, true,
			600, 100, 20, "Disconnect",
			widgetTexture, Constants.BUTTON_PX, Constants.BUTTON_PY,
			(double mouseX, double mouseY, int button, int action, int mods) -> {
				if (action == GLFW_RELEASE) {
					client.close();
					nextScene = new LobbySearchScene(windowID, client);
					shouldChangeScene = true;
				}
			}));
		lobbyFrame.addComponent(new Button(Constants.VIEWPORT_W - 600 - 50, 170, true,
			600, 100, 20, "Spectate",
			widgetTexture, Constants.BUTTON_PX, Constants.BUTTON_PY,
			(double mouseX, double mouseY, int button, int action, int mods) -> {
				if (action == GLFW_RELEASE) {
					isSpectating = !isSpectating;
					client.sendReadyState(isSpectating, isReady);
				}
			}));
		lobbyFrame.addComponent(new Button(Constants.VIEWPORT_W - 600 - 50, 50, true,
			600, 100, 20, "Ready Up",
			widgetTexture, Constants.BUTTON_PX, Constants.BUTTON_PY,
			(double mouseX, double mouseY, int button, int action, int mods) -> {
				if (action == GLFW_RELEASE) {
					isReady = !isReady;
					client.sendReadyState(isSpectating, isReady);
				}
			}));
		topFrame.addComponent(lobbyFrame);
		topFrame.addComponent(postGameFrame);
	}

	public LobbyScene(long windowID, Client client, String winner) {
		this(windowID, client);
		postGameFrame.addComponent(new TextComponent(Constants.VIEWPORT_W * 0.5, Constants.VIEWPORT_H * 0.5, winner, 36, 0.0f, 0.0f, 0.0f, true));
		//TODO: interface for post game
		postGameFrame.addComponent(new Button(Constants.VIEWPORT_W - 600 - 50, 50, true,
			600, 100, 20, "Next",
			widgetTexture, Constants.BUTTON_PX, Constants.BUTTON_PY,
			(double mouseX, double mouseY, int button, int action, int mods) -> {
				if (action == GLFW_RELEASE) {
					postGameFrame.setActive(false);
					lobbyFrame.setActive(true);
				}
			}));
		lobbyFrame.setActive(false);
		postGameFrame.setActive(true);
	}

	@Override
	public void update(double dt) {
		if (prepareForGame) {
			nextScene = new MultiplayerGameScene(windowID, client);
			shouldChangeScene = true;
		}
	}

	@Override
	public void draw() {
		menuShader.bind();

		menuShader.bindTexture2D("uTexture", widgetTexture);

		float[] buffer = new float[16];
		menuShader.uploadUniformMatrix4fv("uProjection", false, projection.get(buffer));

		widgetBatch.addComponent(topFrame);

		widgetBatch.flush();

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
		client.unregisterOnGamePrepare(prepareGameCallback);
	}
}
