package scenes;

import game.GLTris;
import game.pieces.*;
import game.pieces.util.*;
import org.joml.Matrix4f;
import render.*;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;

public class GameScene extends Scene{

	private static final float TILE_SIZE = 1.0f;
	private static final float PROJECTION_HEIGHT = 30.0f * TILE_SIZE;

	private static final float X_OFFSET_HELD = 4.0f * TILE_SIZE;
	private static final float Y_OFFSET_HELD = PROJECTION_HEIGHT - 6.0f * TILE_SIZE;

	private static final float X_OFFSET_BOARD = 8.0f * TILE_SIZE;
	private static final float Y_OFFSET_BOARD = 1.0f * TILE_SIZE;

	private static final float X_OFFSET_QUEUE = 20.0f * TILE_SIZE;
	private static final float Y_OFFSET_QUEUE = PROJECTION_HEIGHT - 3.0f * TILE_SIZE;

	private Shader shaderBlocks;
	private Shader shaderText;
	private Camera camera;
	private Matrix4f projection;

	private BatchTiles batch;
	private TextureAtlas pieceTexture;
	private TextureAtlas fontTexture;
	private TextRenderer textRenderer;

	private PieceName[] currentQueue;

	private GLTris game;

	public GameScene(long windowID) {
		super(windowID);
		try {
			shaderBlocks = new Shader("shaders/vertex.glsl", "shaders/fragment.glsl");
			shaderText = new Shader("shaders/text_vertex.glsl", "shaders/text_fragment.glsl");
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}

		camera = new Camera();
		game = new GLTris();
		batch = new BatchTiles(24);
		try {
			fontTexture = new TextureAtlas("fonts/font.png", 0, 8, 8);
			pieceTexture = new TextureAtlas("images/default_skin.png", 1, 32, 32);
			textRenderer = new TextRenderer(fontTexture, 60);
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	@Override
	public void updateProjection(long windowID) {
		float projectionHeight = PROJECTION_HEIGHT;
		int[] windowWidth = new int[1];
		int[] windowHeight = new int[1];
		glfwGetWindowSize(windowID, windowWidth, windowHeight);
		float windowAspect = (float) windowWidth[0] / windowHeight[0];
		float projectionWidth = projectionHeight * windowAspect;
		projection = new Matrix4f().identity().ortho(
			0.0f, projectionWidth,
			0.0f, projectionHeight,
			0.001f, 10000.0f);
	}

	@Override
	public void init() {
		shaderBlocks.compile();
		shaderText.compile();

		updateProjection(windowID);

		game.init();

		currentQueue = game.getPieceQueue();
		game.registerOnNextPieceListener(() -> {
			currentQueue = game.getPieceQueue();
		});
	}

	@Override
	public void update(double dt) {
		game.update(dt);
	}

	@Override
	public void draw() {
		float[] buffer = new float[16];

		shaderBlocks.bind();
		pieceTexture.bind(shaderBlocks, "uTexture");

		shaderBlocks.uploadUniformMatrix4fv("uView", false, camera.getView().get(buffer));
		shaderBlocks.uploadUniformMatrix4fv("uProjection", false, projection.get(buffer));

		float[] transformMatrix = {
			1.0f, 0.0f, 0.0f, 0.0f,
			0.0f, 1.0f, 0.0f, 0.0f,
			0.0f, 0.0f, 1.0f, 0.0f,
			0.0f, 0.0f, 0.0f, 1.0f
		};

		shaderBlocks.uploadUniformMatrix4fv("uTransform", true, transformMatrix);

		float[] bottomLeft = new float[4];
		float[] bottomRight = new float[4];
		float[] topRight = new float[4];
		float[] topLeft = new float[4];

		TileState[][] board = game.getBoard();

		int px = 0, py = 0;
		//draw the board
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				if (board[i][j] != TileState.EMPTY) {
					bottomLeft[0] = j + X_OFFSET_BOARD; bottomLeft[1] = i + Y_OFFSET_BOARD;
					bottomRight[0] = j + X_OFFSET_BOARD + 1.0f; bottomRight[1] = i + Y_OFFSET_BOARD;
					topRight[0] = j + X_OFFSET_BOARD + 1.0f; topRight[1] = i + Y_OFFSET_BOARD + 1.0f;
					topLeft[0] = j + X_OFFSET_BOARD; topLeft[1] = i + Y_OFFSET_BOARD + 1.0f;

					switch(board[i][j]) {
						case GARBAGE -> {
							px = 0; py = 2;
						}
						case I -> {
							px = 1; py = 0;
						}
						case O -> {
							px = 2; py = 0;
						}
						case L -> {
							px = 3; py = 0;
						}
						case J -> {
							px = 0; py = 1;
						}
						case S -> {
							px = 1; py = 1;
						}
						case Z -> {
							px = 2; py = 1;
						}
						case T -> {
							px = 3; py = 1;
						}
					}

					float[] uvs = pieceTexture.getElementUVs(px, py, 1, 1);

					bottomLeft[2] = uvs[0]; bottomLeft[3] = uvs[1];
					bottomRight[2] = uvs[2]; bottomRight[3] = uvs[1];
					topRight[2] = uvs[2]; topRight[3] = uvs[3];
					topLeft[2] = uvs[0]; topLeft[3] = uvs[3];

					batch.addVertices(bottomLeft);
					batch.addVertices(bottomRight);
					batch.addVertices(topRight);
					batch.addVertices(topRight);
					batch.addVertices(topLeft);
					batch.addVertices(bottomLeft);
				}
			}
		}

		//draw the current piece the player is placing
		Piece currentPiece = game.getCurrentPiece();
		boolean[][] tileMap = currentPiece.getTileMap();
		for (int i = 0; i < tileMap.length; i++) {
			for (int j = 0; j < tileMap[i].length; j++) {
				if (tileMap[i][j]) {
					bottomLeft[0] = currentPiece.getTopLeftX() + j + X_OFFSET_BOARD;         bottomLeft[1] = currentPiece.getTopLeftY() + i + Y_OFFSET_BOARD;
					bottomRight[0] = currentPiece.getTopLeftX() + j + X_OFFSET_BOARD + 1.0f;  bottomRight[1] = currentPiece.getTopLeftY() + i + Y_OFFSET_BOARD;
					topRight[0] = currentPiece.getTopLeftX() + j + X_OFFSET_BOARD + 1.0f;  topRight[1] = currentPiece.getTopLeftY() + i + Y_OFFSET_BOARD + 1.0f;
					topLeft[0] = currentPiece.getTopLeftX() + j + X_OFFSET_BOARD;        topLeft[1] = currentPiece.getTopLeftY() + i + Y_OFFSET_BOARD + 1.0f;

					switch(currentPiece.getName()) {
						case I -> {
							px = 1; py = 0;
						}
						case O -> {
							px = 2; py = 0;
						}
						case L -> {
							px = 3; py = 0;
						}
						case J -> {
							px = 0; py = 1;
						}
						case S -> {
							px = 1; py = 1;
						}
						case Z -> {
							px = 2; py = 1;
						}
						case T -> {
							px = 3; py = 1;
						}
					}

					float[] uvs = pieceTexture.getElementUVs(px, py, 1, 1);

					bottomLeft[2] = uvs[0]; bottomLeft[3] = uvs[1];
					bottomRight[2] = uvs[2]; bottomRight[3] = uvs[1];
					topRight[2] = uvs[2]; topRight[3] = uvs[3];
					topLeft[2] = uvs[0]; topLeft[3] = uvs[3];

					batch.addVertices(bottomLeft);
					batch.addVertices(bottomRight);
					batch.addVertices(topRight);
					batch.addVertices(topRight);
					batch.addVertices(topLeft);
					batch.addVertices(bottomLeft);
				}
			}
		}

		//draw the held piece
		PieceName heldPiece = game.getHeldPiece();

		if (heldPiece != null) {
			switch(heldPiece) {
				case I -> {
					px = 1; py = 0;
					tileMap = IPiece.getTileMapSpawn();
				}
				case O -> {
					px = 2; py = 0;
					tileMap = OPiece.getTileMapSpawn();
				}
				case L -> {
					px = 3; py = 0;
					tileMap = LPiece.getTileMapSpawn();
				}
				case J -> {
					px = 0; py = 1;
					tileMap = JPiece.getTileMapSpawn();
				}
				case S -> {
					px = 1; py = 1;
					tileMap = SPiece.getTileMapSpawn();
				}
				case Z -> {
					px = 2; py = 1;
					tileMap = ZPiece.getTileMapSpawn();
				}
				case T -> {
					px = 3; py = 1;
					tileMap = TPiece.getTileMapSpawn();
				}
			}

			float[] uvs = pieceTexture.getElementUVs(px, py, 1, 1);

			bottomLeft[2] = uvs[0]; bottomLeft[3] = uvs[1];
			bottomRight[2] = uvs[2]; bottomRight[3] = uvs[1];
			topRight[2] = uvs[2]; topRight[3] = uvs[3];
			topLeft[2] = uvs[0]; topLeft[3] = uvs[3];

			for (int i = 0; i < tileMap.length; i++) {
				for (int j = 0; j < tileMap[i].length; j++) {
					if (tileMap[i][j]) {
						bottomLeft[0] = j + X_OFFSET_HELD;         bottomLeft[1] = i + Y_OFFSET_HELD;
						bottomRight[0] = j + X_OFFSET_HELD + 1.0f;  bottomRight[1] = i + Y_OFFSET_HELD;
						topRight[0] = j + X_OFFSET_HELD + 1.0f;  topRight[1] = i + Y_OFFSET_HELD + 1.0f;
						topLeft[0] = j + X_OFFSET_HELD;        topLeft[1] = i + Y_OFFSET_HELD + 1.0f;

						batch.addVertices(bottomLeft);
						batch.addVertices(bottomRight);
						batch.addVertices(topRight);
						batch.addVertices(topRight);
						batch.addVertices(topLeft);
						batch.addVertices(bottomLeft);
					}
				}
			}
		}

		//draw the piece queue
		for (int index = 0; index < GLTris.NUM_PREVIEWS; index++) {
			PieceName pieceName = currentQueue[index];
			switch (pieceName) {
				case I -> {
					px = 1; py = 0;
					tileMap = IPiece.getTileMapSpawn();
				}
				case O -> {
					px = 2; py = 0;
					tileMap = OPiece.getTileMapSpawn();
				}
				case L -> {
					px = 3; py = 0;
					tileMap = LPiece.getTileMapSpawn();
				}
				case J -> {
					px = 0; py = 1;
					tileMap = JPiece.getTileMapSpawn();
				}
				case S -> {
					px = 1; py = 1;
					tileMap = SPiece.getTileMapSpawn();
				}
				case Z -> {
					px = 2; py = 1;
					tileMap = ZPiece.getTileMapSpawn();
				}
				case T -> {
					px = 3; py = 1;
					tileMap = TPiece.getTileMapSpawn();
				}
			}

			float[] uvs = pieceTexture.getElementUVs(px, py, 1, 1);

			bottomLeft[2] = uvs[0]; bottomLeft[3] = uvs[1];
			bottomRight[2] = uvs[2]; bottomRight[3] = uvs[1];
			topRight[2] = uvs[2]; topRight[3] = uvs[3];
			topLeft[2] = uvs[0]; topLeft[3] = uvs[3];

			for (int i = 0; i < tileMap.length; i++) {
				for (int j = 0; j < tileMap[i].length; j++) {
					if (tileMap[i][j]) {
						bottomLeft[0] = j + X_OFFSET_QUEUE;         bottomLeft[1] = i + Y_OFFSET_QUEUE - index * 5.0f;
						bottomRight[0] = j + X_OFFSET_QUEUE + 1.0f;  bottomRight[1] = i + Y_OFFSET_QUEUE - index * 5.0f;
						topRight[0] = j + X_OFFSET_QUEUE + 1.0f;  topRight[1] = i + Y_OFFSET_QUEUE - index * 5.0f + 1.0f;
						topLeft[0] = j + X_OFFSET_QUEUE;        topLeft[1] = i + Y_OFFSET_QUEUE - index * 5.0f + 1.0f;

						batch.addVertices(bottomLeft);
						batch.addVertices(bottomRight);
						batch.addVertices(topRight);
						batch.addVertices(topRight);
						batch.addVertices(topLeft);
						batch.addVertices(bottomLeft);
					}
				}
			}
		}

		batch.flush();

		shaderText.bind();

		fontTexture.bind(shaderText, "uFontTexture");

		shaderText.uploadUniformMatrix4fv("uProjection", false, projection.get(buffer));
		shaderText.uploadUniformMatrix4fv("uView", false, camera.getView().get(buffer));
		shaderText.uploadUniformMatrix4fv("uTransform", true, transformMatrix);

		textRenderer.addText("Lines cleared: " + game.getLinesCleared(), 1.0f, 27, 18, 0, 0, 0);
		textRenderer.draw();
	}

	@Override
	public boolean shouldChangeScene() {
		return false;
	}

	@Override
	public Scene nextScene() {
		return null;
	}

	@Override
	public void destroy() {
		game.destroy();
	}
}
