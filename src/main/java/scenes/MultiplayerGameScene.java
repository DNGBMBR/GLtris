package scenes;

import game.*;
import game.callbacks.PiecePlacedCallback;
import game.pieces.util.TileState;
import menu.component.TopFrame;
import menu.widgets.TextComponent;
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
	public static final float GAME_TILE_SIZE = 36.0f;

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
	List<GLTrisDisplayComponent> displayComponents;
	boolean isSpectator;

	Scene nextScene;

	double spectatorXPos = GAME_X_POS + 23 * GAME_TILE_SIZE;
	double spectatorYPos = GAME_Y_POS;
	float spectatorTileSize = 15.0f;
	double spectatorBoardOffsetX = 30 * spectatorTileSize;
	double getSpectatorBoardOffsetY = 25 * spectatorTileSize;

	OnStartGame startGameCallback;
	OnGarbageReceived garbageCallback;
	OnGameFinish finishCallback;
	OnBoardUpdate boardUpdateCallback;

	double nextBoardUpdateAccumulator = 0.0;
	int numLivingPlayers;
	boolean shouldUpdateDisplay;

	Random rng = new Random();

	boolean shouldSetNextScene = false;
	String winner = "";

	MultiplayerGameScene(long windowID, GameClient client, boolean isSpectating) {
		super(windowID, client);

		this.isSpectator = isSpectating;

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
				numLivingPlayers--;
				return;
			}
			player.setBoard(board);
			player.setHeldPiece(hold);
			player.setQueue(queue);
		};
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

		spectatorXPos = isSpectator ? GAME_X_POS : GAME_X_POS + (6 + settings.getBoardWidth() + 6 + 3) * GAME_TILE_SIZE;
		spectatorYPos = GAME_Y_POS;
		spectatorTileSize = 15.0f;
		spectatorBoardOffsetX = (6 + settings.getBoardWidth() + 6 + 3) * spectatorTileSize;
		getSpectatorBoardOffsetY = (settings.getBoardHeight() + 4) * spectatorTileSize;

		List<Player> players = client.getPlayerList();
		otherPlayerComponents = new HashMap<>();
		displayComponents = new ArrayList<>();
		String username = client.getUsername();
		int xOffset = 0;
		int yOffset = 0;

		numLivingPlayers = 0;
		shouldUpdateDisplay = true;
		for (Player player : players) {
			String playerName = player.getName();
			if (!playerName.equals(username) && !player.isSpectator()) {
				GLTrisDisplayComponent playerComponent = new GLTrisDisplayComponent(
					spectatorXPos + xOffset * spectatorBoardOffsetX,
					spectatorYPos + yOffset * getSpectatorBoardOffsetY,
					spectatorTileSize, true, settings);
				otherPlayerComponents.put(player.getName(), playerComponent);
				displayComponents.add(playerComponent);
				TextComponent playerNameText = new TextComponent(
					spectatorXPos + xOffset * spectatorBoardOffsetX + (6 + settings.getBoardWidth() + 6) * spectatorTileSize * 0.5 - playerName.length() * spectatorTileSize,
					spectatorYPos + yOffset * getSpectatorBoardOffsetY - 2 * spectatorTileSize,
					playerName, spectatorTileSize, 0.0f, 0.0f, 0.0f, true);
				topFrame.addComponent(playerNameText);
				xOffset += 1;
				if (xOffset % (isSpectating ? 4 : 2) == 0) {
					xOffset = 0;
					yOffset += 1;
				}

				numLivingPlayers++;
			}
		}

		if (!this.isSpectator) {
			startGameCallback = () -> {
				gameComponent.setStarted(true);
			};
			garbageCallback = (List<Garbage> garbage) -> {
				gameComponent.getGame().addQueueGarbage(garbage);
			};

			client.registerOnGameStart(startGameCallback);
			client.registerOnGarbageReceived(garbageCallback);

			gameComponent = new GLTrisGameComponent(GAME_X_POS, GAME_Y_POS, GAME_TILE_SIZE, true, settings);
			GLTris game = gameComponent.getGame();
			game.registerOnPiecePlacedCallback((int rowsCleared, SpinType spinType, int attack) -> {
				if (attack > 0) {
					List<Garbage> garbage = new ArrayList<>();
					garbage.add(new Garbage(attack, rng.nextInt(10)));
					client.sendGarbage(garbage);
				}
			});

			game.registerOnGameOverListener(() -> {
				client.sendGameOver();
			});
		}
	}

	@Override
	public void init() {
		super.init();
		if (!this.isSpectator) {
			gameComponent.init();
		}
	}

	@Override
	public void update(double dt) {
		if (!this.isSpectator) {
			gameComponent.update(dt);
			nextBoardUpdateAccumulator += dt;
			if (nextBoardUpdateAccumulator >= BOARD_UPDATE_INTERVAL) {
				nextBoardUpdateAccumulator = 0.0;
				GLTris game = this.gameComponent.getGame();
				client.sendBoardUpdate(game.isGameOver(), game.getBoard(), game.getPieceQueue(), game.getHeldPiece());
			}
		}

		if (shouldUpdateDisplay && numLivingPlayers <= 2) {
			List<Player> players = client.getPlayerList();
			for (Player player : players) {
				String playerName = player.getName();
				if (player.isAlive() && !player.isSpectator() && !playerName.equals(client.getUsername())) {
					GLTrisDisplayComponent display = otherPlayerComponents.get(playerName);
					display.setTileSize(GAME_TILE_SIZE);
					break;
				}
			}
			shouldUpdateDisplay = false;
		}

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
		if (!isSpectator) {
			batch.addVertices(gameComponent.generateBackgroundVertices());
		}
		for (GLTrisDisplayComponent component : displayComponents) {
			batch.addVertices(component.generateBackgroundVertices());
		}
		batch.flush();

		shader.bindTexture2D("uTexture", pieceTexture);
		if (!isSpectator) {
			batch.addVertices(gameComponent.generateTileVertices());
		}
		for (GLTrisDisplayComponent component : displayComponents) {
			batch.addVertices(component.generateTileVertices());
		}
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
		topFrame.destroy();
		if (!isSpectator) {
			gameComponent.destroy();
			client.unregisterOnGameStart(startGameCallback);
			client.unregisterOnGarbageReceived(garbageCallback);
		}
		client.unregisterOnGameFinish(finishCallback);
		client.unregisterOnBoardUpdate(boardUpdateCallback);
	}
}
