package scenes;

import game.*;
import game.callbacks.PiecePlacedCallback;
import game.pieces.util.TileState;
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
import settings.GameSettings;
import util.Constants;

import java.util.*;

public class MultiplayerGameScene extends Scene{

	public static final double GAME_X_POS = 30.0;
	public static final double GAME_Y_POS = 39.0;
	public static final float GAME_TILE_SIZE = 42.0f;

	public static final double OTHER_X_POS = GAME_X_POS + 20 * GAME_TILE_SIZE;
	public static final double OTHER_Y_POS = GAME_Y_POS;
	public static final float OTHER_TILE_SIZE = 5.0f;
	public static final double OTHER_BOARD_OFFSET = 20 * OTHER_TILE_SIZE;

	public static final double BOARD_UPDATE_INTERVAL = 1.0;

	private Shader shader;

	private TileBatch batch;
	private WidgetBatch widgetBatch;
	private TextureAtlas pieceTexture;
	private TextureNineSlice backgroundTexture;
	private TextureNineSlice widgetTexture;

	private TextRenderer textRenderer;

	TopFrame topFrame = new TopFrame(Constants.VIEWPORT_W, Constants.VIEWPORT_H, true);
	GLTrisGameComponent gameComponent;
	Map<String, GLTrisDisplayComponent> otherPlayerComponents;

	Scene nextScene;

	OnStartGame startGameCallback;
	OnGarbageReceived garbageCallback;
	OnGameFinish finishCallback;
	OnBoardUpdate boardUpdateCallback;

	double nextBoardUpdateAccumulator = 0.0;

	Random rng = new Random();

	boolean shouldSetNextScene = false;
	String winner = "";

	MultiplayerGameScene(long windowID, GameClient client) {
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
		boardUpdateCallback = (String username, boolean isToppedOut, TileState[][] board, String[] queue, String hold) -> {
			GLTrisDisplayComponent player = otherPlayerComponents.get(username);
			if (player == null) {
				return;
			}
			if (isToppedOut) {
				player.setActive(false);
				return;
			}
			player.setBoard(board);
			player.setHeldPiece(hold);
			player.setQueue(queue);
		};
		client.registerOnGameStart(startGameCallback);
		client.registerOnGarbageReceived(garbageCallback);
		client.registerOnGameFinish(finishCallback);
		client.registerOnBoardUpdate(boardUpdateCallback);

		shader = ResourceManager.getShaderByName("shaders/block_vertex.glsl", "shaders/block_fragment.glsl");
		widgetTexture = ResourceManager.getTextureNineSliceByName("images/widgets.png");
		pieceTexture = ResourceManager.getAtlasByName("images/default_skin.png");
		backgroundTexture = ResourceManager.getTextureNineSliceByName("images/game_background.png");
		widgetTexture = ResourceManager.getTextureNineSliceByName("images/widgets.png");
		batch = new TileBatch(500);
		widgetBatch = new WidgetBatch(100);
		textRenderer = TextRenderer.getInstance();

		GameSettings settings = client.getLobbySettings();
		List<Player> players = client.getPlayers();
		otherPlayerComponents = new HashMap<>();
		String username = client.getUsername();
		int offset = 0;
		for (Player player : players) {
			if (!player.getName().equals(username)) {
				otherPlayerComponents.put(player.getName(), new GLTrisDisplayComponent(OTHER_X_POS + offset, OTHER_Y_POS, OTHER_TILE_SIZE, true, settings));
				offset += OTHER_BOARD_OFFSET;
			}
		}
		gameComponent = new GLTrisGameComponent(GAME_X_POS, GAME_Y_POS, GAME_TILE_SIZE, true, settings);
		GLTris game = gameComponent.getGame();
		game.registerOnPiecePlacedCallback(new PiecePlacedCallback() {
			@Override
			public void run(int rowsCleared, SpinType spinType, int attack) {
				if (attack > 0) {
					List<Garbage> garbage = new ArrayList<>();
					garbage.add(new Garbage(attack, rng.nextInt(10)));
					client.sendGarbage(garbage);
				}
			}
		});

		game.registerOnGameOverListener(() -> {
			client.sendGameOver();
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
		nextBoardUpdateAccumulator += dt;
		if (nextBoardUpdateAccumulator >= BOARD_UPDATE_INTERVAL) {
			nextBoardUpdateAccumulator = 0.0;
			GLTris game = this.gameComponent.getGame();
			client.sendBoardUpdate(game.isGameOver(), game.getBoard(), game.getPieceQueue(), game.getHeldPiece());
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
		client.unregisterOnBoardUpdate(boardUpdateCallback);
	}
}
