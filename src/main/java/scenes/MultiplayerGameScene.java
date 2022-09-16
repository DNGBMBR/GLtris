package scenes;

import game.*;
import game.callbacks.GameOverCallback;
import game.callbacks.PiecePlacedCallback;
import menu.component.TopFrame;
import network.lobby.*;
import org.joml.Random;
import render.Shader;
import render.batch.TileBatch;
import render.batch.WidgetBatch;
import render.manager.ResourceManager;
import render.manager.TextRenderer;
import render.texture.TextureAtlas;
import render.texture.TextureNineSlice;
import util.Constants;

import java.util.ArrayList;
import java.util.List;

public class MultiplayerGameScene extends Scene{

	private Shader shader;

	private TileBatch batch;
	private WidgetBatch widgetBatch;
	private TextureAtlas pieceTexture;
	private TextureNineSlice backgroundTexture;
	private TextureNineSlice widgetTexture;

	private TextRenderer textRenderer;

	TopFrame topFrame = new TopFrame(Constants.VIEWPORT_W, Constants.VIEWPORT_H, true);
	GLTrisBoardComponent gameComponent;

	Scene nextScene;

	OnStartGame startGameCallback;
	OnGarbageReceived garbageCallback;
	OnGameFinish finishCallback;

	Random rng = new Random();

	boolean shouldSetNextScene = false;
	String winner = "";

	MultiplayerGameScene(long windowID, Client client) {
		//TODO: listener for garbage, topping out, etc.
		super(windowID, client);

		startGameCallback = () -> {
			gameComponent.setStarted(true);
		};
		garbageCallback = (List<Garbage> garbage) -> {
			gameComponent.getGame().addQueueGarbage(garbage);
		};
		finishCallback = (String winner) -> {
			shouldSetNextScene = true;
			this.winner = winner;
		};
		client.registerOnGameStart(startGameCallback);
		client.registerOnGarbageReceived(garbageCallback);
		client.registerOnGameFinish(finishCallback);

		shader = ResourceManager.getShaderByName("shaders/block_vertex.glsl", "shaders/block_fragment.glsl");
		widgetTexture = ResourceManager.getTextureNineSliceByName("images/widgets.png");
		pieceTexture = ResourceManager.getAtlasByName("images/default_skin.png");
		backgroundTexture = ResourceManager.getTextureNineSliceByName("images/game_background.png");
		widgetTexture = ResourceManager.getTextureNineSliceByName("images/widgets.png");
		batch = new TileBatch(500);
		widgetBatch = new WidgetBatch(100);
		textRenderer = TextRenderer.getInstance();

		gameComponent = new GLTrisBoardComponent(30.0, 39.0, 42.0f, true, client.getLobbySettings());
		GLTris game = gameComponent.getGame();
		game.registerOnPiecePlacedCallback(new PiecePlacedCallback() {
			@Override
			public void run(int rowsCleared, SpinType spinType) {
				if (rowsCleared > 0) {
					List<Garbage> garbage = new ArrayList<>();
					//TODO: add combo table support
					garbage.add(new Garbage(rowsCleared, rng.nextInt(10)));
					client.sendGarbage(garbage);
				}
			}
		});
		game.registerOnGameOverListener(new GameOverCallback() {
			@Override
			public void onGameOver() {
				client.sendGameOver();
			}
		});
	}

	@Override
	public void init() {
		super.init();
		gameComponent.init();
	}

	@Override
	public void update(double dt) {
		gameComponent.update(dt);
		if (shouldSetNextScene) {
			nextScene = new LobbyScene(windowID, client, winner);
			shouldChangeScene = true;
		}
	}

	@Override
	public void draw() {
		shader.bind();

		float[] buffer = new float[16];
		shader.uploadUniformMatrix4fv("uProjection", false, projection.get(buffer));

		shader.bindTexture2D("uTexture", backgroundTexture);
		batch.addVertices(gameComponent.generateBackgroundVertices());
		batch.flush();

		shader.bindTexture2D("uTexture", pieceTexture);
		batch.addVertices(gameComponent.generateTileVertices());
		batch.flush();

		shader.bindTexture2D("uTexture", widgetTexture);

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
		gameComponent.destroy();
		topFrame.destroy();
		client.unregisterOnGameStart(startGameCallback);
		client.unregisterOnGarbageReceived(garbageCallback);
		client.unregisterOnGameFinish(finishCallback);
	}
}
