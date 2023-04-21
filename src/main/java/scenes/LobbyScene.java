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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class LobbyScene extends Scene {
	Shader menuShader;
	WidgetBatch widgetBatch;
	TextureNineSlice widgetTexture;
	TextRenderer textRenderer;

	TopFrame topFrame = new TopFrame(Constants.VIEWPORT_W, Constants.VIEWPORT_H, true);
	Frame lobbyFrame = new Frame(0, 0, Constants.VIEWPORT_W, Constants.VIEWPORT_H, true, false, 0);
	Frame playerListFrame = new Frame(50, 200, 500, Constants.VIEWPORT_H - 2 * 200, true, true, 0);
	List<Player> players = new ArrayList<>();
	Frame postGameFrame = new Frame(0, 0, Constants.VIEWPORT_W, Constants.VIEWPORT_H, false, false, 0);

	boolean isSpectating = false;
	boolean isReady = false;

	boolean prepareForGame = false;
	Scene nextScene;

	OnPrepareGame prepareGameCallback;
	OnLobbyUpdate lobbyUpdateCallback;

	LobbyScene(long windowID, GameClient client) {
		super(windowID, client);
		createUIElements();
		this.client.sendUsername();
	}

	LobbyScene(long windowID, GameClient client, String winner) {
		super(windowID, client);
		createUIElements();
		postGameFrame.addComponent(new TextComponent(Constants.VIEWPORT_W * 0.5, Constants.VIEWPORT_H * 0.5, winner, 36, 0.0f, 0.0f, 0.0f, true));

		postGameFrame.addComponent(new Button(Constants.VIEWPORT_W - 600 - 50, 50, true,
			600, 100, 20, "Next",
			widgetTexture,
			(double mouseX, double mouseY, int button, int action, int mods) -> {
				if (action == GLFW_RELEASE) {
					postGameFrame.setActive(false);
					lobbyFrame.setActive(true);
				}
			}));
		lobbyFrame.setActive(false);
		postGameFrame.setActive(true);
	}

	private void createUIElements() {
		prepareGameCallback = () -> {
			prepareForGame = true;
		};
		lobbyUpdateCallback = (List<Player> players) -> {
			this.players = players;
			this.updatePlayers();
		};
		client.registerOnGamePrepare(prepareGameCallback);
		client.registerOnLobbyUpdate(lobbyUpdateCallback);
		client.triggerLobbyUpdate();

		menuShader = ResourceManager.getShaderByName("shaders/block_vertex.glsl", "shaders/block_fragment.glsl");
		widgetTexture = ResourceManager.getTextureNineSliceByName("images/widgets.png");
		widgetBatch = new WidgetBatch(200);
		textRenderer = TextRenderer.getInstance();

		lobbyFrame.addComponent(playerListFrame);

		lobbyFrame.addComponent(new Button(50, 50, true,
			600, 100, 20, "Disconnect",
			widgetTexture,
			(double mouseX, double mouseY, int button, int action, int mods) -> {
				if (action == GLFW_RELEASE) {
					client.close();
					nextScene = new LobbySearchScene(windowID, client);
					shouldChangeScene = true;
				}
			}));
		lobbyFrame.addComponent(new Button(Constants.VIEWPORT_W - 600 - 50, 170, true,
			600, 100, 20, "Spectate",
			widgetTexture,
			(double mouseX, double mouseY, int button, int action, int mods) -> {
				if (action == GLFW_RELEASE) {
					isSpectating = !isSpectating;
					client.sendReadyState(isSpectating, isReady);
				}
			}));
		lobbyFrame.addComponent(new Button(Constants.VIEWPORT_W - 600 - 50, 50, true,
			600, 100, 20, "Ready Up",
			widgetTexture,
			(double mouseX, double mouseY, int button, int action, int mods) -> {
				if (action == GLFW_RELEASE) {
					isReady = !isReady;
					client.sendReadyState(isSpectating, isReady);
				}
			}));
		topFrame.addComponent(lobbyFrame);
		topFrame.addComponent(postGameFrame);
	}

	private void updatePlayers() {
		synchronized (topFrame) {
			float nameSize = 24.0f;
			double acc = playerListFrame.getHeight() - 2 * nameSize;
			this.playerListFrame.getComponents().clear();
			for (Player player : players) {
				String text = player.getName();
				this.playerListFrame.addComponent(new TextComponent(20, acc, text, nameSize, 0.0f, 0.0f, 0.0f, true));
				if (player.isSpectator()) {
					this.playerListFrame.addComponent(new TextComponent(500 - 20 - nameSize, acc, "S", nameSize, 0.25f, 0.5f, 1.0f, true));
				}
				else if (player.isReady()) {
					this.playerListFrame.addComponent(new TextComponent(500 - 20 - nameSize, acc, "R", nameSize, 0.25f, 0.60f, 0.25f, true));
				}
				acc -= nameSize + 20.0;
			}
		}
	}

	@Override
	public void init() {
		super.init();
	}

	@Override
	public void update(double dt) {
		if (prepareForGame) {
			nextScene = new MultiplayerGameScene(windowID, client, isSpectating);
			shouldChangeScene = true;
		}
	}

	@Override
	public void draw() {
		menuShader.bind();

		menuShader.bindTexture2D("uTexture", widgetTexture);

		float[] buffer = new float[16];
		menuShader.uploadUniformMatrix4fv("uProjection", false, projection.get(buffer));

		synchronized (topFrame) {
			widgetBatch.addComponent(topFrame);

			widgetBatch.flush();

			textRenderer.bind();

			textRenderer.addText(topFrame);

			textRenderer.draw();
		}
	}

	@Override
	public Scene nextScene() {
		return nextScene;
	}

	@Override
	public void destroy() {
		topFrame.destroy();
		client.unregisterOnGamePrepare(prepareGameCallback);
		client.unregisterOnLobbyUpdate(lobbyUpdateCallback);
	}
}
