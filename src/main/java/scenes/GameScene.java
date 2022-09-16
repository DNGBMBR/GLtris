package scenes;

import game.*;
import game.pieces.util.*;
import menu.component.TopFrame;
import menu.widgets.Button;
import network.lobby.Client;
import org.json.simple.parser.ParseException;
import render.*;
import render.batch.TileBatch;
import render.batch.WidgetBatch;
import render.manager.ResourceManager;
import render.manager.TextRenderer;
import render.texture.TextureAtlas;
import render.texture.TextureNineSlice;
import settings.GameSettings;
import util.Constants;

import java.io.IOException;

import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;

public class GameScene extends Scene{

	//TODO: make board and text left align, right align, etc to the window so resizing doesn't break it
	private static final float TILE_SIZE = 30.0f;
	private static final float PROJECTION_WIDTH = Constants.VIEWPORT_W;
	private static final float PROJECTION_HEIGHT = Constants.VIEWPORT_H;

	private static final float X_OFFSET_HELD = 2.0f * TILE_SIZE;
	private static final float Y_OFFSET_HELD = PROJECTION_HEIGHT - 6.0f * TILE_SIZE;
	private static final float HELD_PIECE_BOUND_SIZE = TILE_SIZE * 5.0f;

	private static final float X_OFFSET_BOARD = 9.0f * TILE_SIZE;
	private static final float Y_OFFSET_BOARD = 1.0f * TILE_SIZE;

	private static final float X_OFFSET_QUEUE = 22.0f * TILE_SIZE;
	private static final float Y_OFFSET_QUEUE = PROJECTION_HEIGHT - 6.0f * TILE_SIZE;
	private static final float QUEUE_PIECE_BOUND_SIZE = TILE_SIZE * 5.0f;

	private Shader shaderBlocks;

	private TileBatch batch;
	private WidgetBatch widgetBatch;
	private TextureAtlas pieceTexture;
	private TextureNineSlice backgroundTexture;
	private TextureNineSlice widgetTexture;

	private TextRenderer textRenderer;

	private PieceColour[] currentQueue;

	//private GLTris game;
	private GLTrisBoardComponent gameComponent;
	private GameSettings settings;

	private TopFrame topFrame = new TopFrame(Constants.VIEWPORT_W, Constants.VIEWPORT_H, true);
	private Button backButton;

	public GameScene(long windowID, Client client) {
		super(windowID, client);

		shaderBlocks = ResourceManager.getShaderByName("shaders/block_vertex.glsl", "shaders/block_fragment.glsl");

		//TODO: register pieces from file to factory in a way that's not hardcoded
		try {
			settings = new GameSettings("./game_settings.ini");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Could not find game settings.");
		} catch (ParseException e) {
			throw new IllegalStateException("Could not parse kick table.");
		}
		gameComponent = new GLTrisBoardComponent(30.0, 39.0, 42.0f, true, settings);
		topFrame.addComponent(gameComponent);
		batch = new TileBatch(500);
		widgetBatch = new WidgetBatch(100);

		textRenderer = TextRenderer.getInstance();

		pieceTexture = ResourceManager.getAtlasByName("images/default_skin.png");
		backgroundTexture = ResourceManager.getTextureNineSliceByName("images/game_background.png");
		widgetTexture = ResourceManager.getTextureNineSliceByName("images/widgets.png");
	}

	@Override
	public void init() {
		super.init();

		gameComponent.init();
		gameComponent.setStarted(true);

		backButton = new Button(Constants.VIEWPORT_W - 600 - 50, 50, false, 600, 100, 25,
			"Main Menu", widgetTexture, Constants.BUTTON_PX, Constants.BUTTON_PY,
			(double mouseX, double mouseY, int button, int action, int mods) -> {
				shouldChangeScene = true;
			});
		topFrame.addComponent(backButton);
	}

	@Override
	public void update(double dt) {
		gameComponent.update(dt);
		if (gameComponent.isGameOver()) {
			backButton.setActive(true);
		}
	}

	@Override
	public void draw() {

		float[] buffer = new float[16];

		shaderBlocks.bind();
		shaderBlocks.uploadUniformMatrix4fv("uProjection", false, projection.get(buffer));

		shaderBlocks.bindTexture2D("uTexture", backgroundTexture);

		//draw board, background for piece queue, and hold
		//batchBackground(board);
		batch.addVertices(gameComponent.generateBackgroundVertices());

		batch.flush();

		//draw all elements related to the pieces
		shaderBlocks.bindTexture2D("uTexture", pieceTexture);

		batch.addVertices(gameComponent.generateTileVertices());

		batch.flush();

		shaderBlocks.bindTexture2D("uTexture", widgetTexture);

		widgetBatch.addComponent(topFrame);
		widgetBatch.flush();

		textRenderer.bind();

		textRenderer.addText(topFrame);

		GLTris game = gameComponent.getGame();

		textRenderer.addText("Lines cleared: " + game.getLinesCleared(), 24.0f, 1200, 720, 0, 0, 0);
		if (gameComponent.isGameOver()) {
			textRenderer.addText("GAME OVER", 24.0f, 1200, 650, 1.0f, 0.0f, 0.0f);
		}

		textRenderer.draw();
	}

	@Override
	public Scene nextScene() {
		return new MenuScene(windowID, client);
	}

	@Override
	public void destroy() {
		batch.destroy();
		topFrame.destroy();
		widgetBatch.destroy();
	}
}
