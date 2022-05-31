package scenes;

import game.GLTris;
import game.pieces.*;
import game.pieces.util.*;
import org.joml.Matrix4f;
import render.*;
import render.batch.TileBatch;
import render.manager.ResourceManager;
import render.manager.TextRenderer;
import render.texture.TextureAtlas;
import util.Constants;
import util.Utils;

import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;

public class GameScene extends Scene{

	//TODO: make board and text left align, right align, etc to the window so resizing doesn't break it
	private static final float TILE_SIZE = 32.0f;
	private static final float PROJECTION_WIDTH = Constants.VIEWPORT_W;
	private static final float PROJECTION_HEIGHT = Constants.VIEWPORT_H;

	private static final float X_OFFSET_HELD = 4.0f * TILE_SIZE;
	private static final float Y_OFFSET_HELD = PROJECTION_HEIGHT - 6.0f * TILE_SIZE;

	private static final float X_OFFSET_BOARD = 8.0f * TILE_SIZE;
	private static final float Y_OFFSET_BOARD = 1.0f * TILE_SIZE;

	private static final float X_OFFSET_QUEUE = 20.0f * TILE_SIZE;
	private static final float Y_OFFSET_QUEUE = PROJECTION_HEIGHT - 6.0f * TILE_SIZE;
	private static final float QUEUE_PIECE_BOUND_SIZE = TILE_SIZE * 6.0f;

	private Shader shaderBlocks;
	private Matrix4f projection;

	private TileBatch batch;
	private TextureAtlas pieceTexture;

	private TextRenderer textRenderer;

	private PieceName[] currentQueue;

	private GLTris game;

	public GameScene(long windowID) {
		super(windowID);

		shaderBlocks = ResourceManager.getShaderByName("shaders/block_vertex.glsl", "shaders/block_fragment.glsl");

		game = new GLTris();
		batch = new TileBatch(500);

		textRenderer = TextRenderer.getInstance();

		pieceTexture = ResourceManager.getAtlasByName("images/default_skin.png");
	}

	@Override
	public void updateProjection(long windowID) {
		float projectionWidth = PROJECTION_WIDTH;
		float projectionHeight = PROJECTION_HEIGHT;
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

		shaderBlocks.uploadUniformMatrix4fv("uProjection", false, projection.get(buffer));

		//draw all elements related to the pieces
		pieceTexture.bind(shaderBlocks, "uTexture");

		TileState[][] board = game.getBoard();
		Piece currentPiece = game.getCurrentPiece();
		PieceName heldPiece = game.getHeldPiece();

		batchBoard(board);
		batchCurrentPiece(currentPiece);
		batchGhostPiece(currentPiece, board);
		batchHeldPiece(heldPiece);
		batchPieceQueue();

		batch.flush();

		textRenderer.bind();

		textRenderer.addText("Lines cleared: " + game.getLinesCleared(), 24.0f, 1200, 720, 0, 0, 0);

		textRenderer.draw();
	}

	private void batchBoard(TileState[][] board) {
		float[] vertices = new float[board.length * board[0].length * Constants.BLOCK_ATTRIBUTES_PER_VERTEX * Constants.BLOCK_ELEMENTS_PER_QUAD];

		int px, py;
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				if (board[i][j] != TileState.EMPTY) {
					float p0x = j * TILE_SIZE + X_OFFSET_BOARD;
					float p0y = i * TILE_SIZE + Y_OFFSET_BOARD;
					float p1x = j * TILE_SIZE + X_OFFSET_BOARD + TILE_SIZE;
					float p1y = i * TILE_SIZE + Y_OFFSET_BOARD + TILE_SIZE;

					switch(board[i][j]) {
						case GARBAGE -> {
							px = 0; py = 0;
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
						default -> {
							px = 0; py = 0;
						}
					}

					float[] uvs = pieceTexture.getElementUVs(px, py, 1, 1);
					float p0u = uvs[0];
					float p0v = uvs[1];
					float p1u = uvs[2];
					float p1v = uvs[3];

					Utils.addBlockVertices(vertices,
						i * board[j].length * Constants.BLOCK_ELEMENTS_PER_QUAD * Constants.BLOCK_ATTRIBUTES_PER_VERTEX +
						j * Constants.BLOCK_ELEMENTS_PER_QUAD * Constants.BLOCK_ATTRIBUTES_PER_VERTEX,
						p0x, p0y, p0u, p0v,
						p1x, p1y, p1u, p1v);
				}
			}
		}
		batch.addVertices(vertices);
	}

	private void batchCurrentPiece(Piece currentPiece) {
		float[] vertices = new float[Constants.BLOCK_ATTRIBUTES_PER_VERTEX * Constants.BLOCK_ELEMENTS_PER_QUAD];

		boolean[][] tileMap = currentPiece.getTileMap();
		int px, py;
		for (int i = 0; i < tileMap.length; i++) {
			for (int j = 0; j < tileMap[i].length; j++) {
				if (tileMap[i][j]) {
					float p0x = (currentPiece.getTopLeftX() + j) * TILE_SIZE + X_OFFSET_BOARD;
					float p0y = (currentPiece.getTopLeftY() + i) * TILE_SIZE + Y_OFFSET_BOARD;
					float p1x = (currentPiece.getTopLeftX() + j) * TILE_SIZE + X_OFFSET_BOARD + TILE_SIZE;
					float p1y = (currentPiece.getTopLeftY() + i) * TILE_SIZE + Y_OFFSET_BOARD + TILE_SIZE;

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
						default -> {
							px = 0; py = 0;
						}
					}

					float[] uvs = pieceTexture.getElementUVs(px, py, 1, 1);

					Utils.addBlockVertices(vertices, 0,
						p0x, p0y, uvs[0], uvs[1],
						p1x, p1y, uvs[2], uvs[3]);

					batch.addVertices(vertices);
				}
			}
		}
	}

	private void batchGhostPiece(Piece piece, TileState[][] board) {
		Piece ghostPiece = piece.copy();
		while (ghostPiece.move(Direction.DOWN, board)) {}

		boolean[][] tileMap = ghostPiece.getTileMap();

		float[] vertices = new float[Constants.BLOCK_ATTRIBUTES_PER_VERTEX * Constants.BLOCK_ELEMENTS_PER_QUAD];
		int px, py;
		switch(ghostPiece.getName()) {
			case I -> {
				px = 1; py = 2;
			}
			case O -> {
				px = 2; py = 2;
			}
			case L -> {
				px = 3; py = 2;
			}
			case J -> {
				px = 0; py = 3;
			}
			case S -> {
				px = 1; py = 3;
			}
			case Z -> {
				px = 2; py = 3;
			}
			case T -> {
				px = 3; py = 3;
			}
			default -> {
				px = 0; py = 2;
			}
		}

		float[] uvs = pieceTexture.getElementUVs(px, py, 1, 1);

		for (int i = 0; i < tileMap.length; i++) {
			for (int j = 0; j < tileMap[i].length; j++) {
				if (tileMap[i][j]) {
					float p0x = (ghostPiece.getTopLeftX() + j) * TILE_SIZE + X_OFFSET_BOARD;
					float p0y = (ghostPiece.getTopLeftY() + i) * TILE_SIZE + Y_OFFSET_BOARD;
					float p1x = (ghostPiece.getTopLeftX() + j) * TILE_SIZE + X_OFFSET_BOARD + TILE_SIZE;
					float p1y = (ghostPiece.getTopLeftY() + i) * TILE_SIZE + Y_OFFSET_BOARD + TILE_SIZE;

					Utils.addBlockVertices(vertices, 0,
						p0x, p0y, uvs[0], uvs[1],
						p1x, p1y, uvs[2], uvs[3]);

					batch.addVertices(vertices);
				}
			}
		}
	}

	private void batchHeldPiece(PieceName heldPiece) {
		float[] vertices = new float[Constants.BLOCK_ATTRIBUTES_PER_VERTEX * Constants.BLOCK_ELEMENTS_PER_QUAD];

		boolean[][] tileMap = {};
		int px, py;

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
				default -> {
					px = 0; py = 0;
				}
			}

			float[] uvs = pieceTexture.getElementUVs(px, py, 1, 1);

			for (int i = 0; i < tileMap.length; i++) {
				for (int j = 0; j < tileMap[i].length; j++) {
					if (tileMap[i][j]) {
						float p0x = j * TILE_SIZE + X_OFFSET_HELD;
						float p0y = i * TILE_SIZE + Y_OFFSET_HELD;
						float p1x = j * TILE_SIZE+ X_OFFSET_HELD + TILE_SIZE;
						float p1y = i  * TILE_SIZE+ Y_OFFSET_HELD + TILE_SIZE;

						Utils.addBlockVertices(vertices, 0,
							p0x, p0y, uvs[0], uvs[1],
							p1x, p1y, uvs[2], uvs[3]);

						batch.addVertices(vertices);
					}
				}
			}
		}
	}

	private void batchPieceQueue() {
		//TODO: make all pieces in queue centered in their respective boxes
		float[] vertices = new float[Constants.BLOCK_ATTRIBUTES_PER_VERTEX * Constants.BLOCK_ELEMENTS_PER_QUAD];

		boolean[][] tileMap = {};
		int px = 0, py = 0;

		//draw the piece queue
		for (int index = 0; index < game.getNumPreviews(); index++) {
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

			for (int i = 0; i < tileMap.length; i++) {
				for (int j = 0; j < tileMap[i].length; j++) {
					if (tileMap[i][j]) {
						float p0x = j * TILE_SIZE + (X_OFFSET_QUEUE + 0.5f * QUEUE_PIECE_BOUND_SIZE) - tileMap.length * 0.5f * TILE_SIZE;
						float p0y = i * TILE_SIZE + Y_OFFSET_QUEUE - index * 5.0f * TILE_SIZE;
						float p1x = j * TILE_SIZE + (X_OFFSET_QUEUE + 0.5f * QUEUE_PIECE_BOUND_SIZE) - tileMap.length * 0.5f * TILE_SIZE + TILE_SIZE;
						float p1y = i * TILE_SIZE + Y_OFFSET_QUEUE - index * 5.0f * TILE_SIZE + TILE_SIZE;

						Utils.addBlockVertices(vertices, 0,
							p0x, p0y, uvs[0], uvs[1],
							p1x, p1y, uvs[2], uvs[3]);

						batch.addVertices(vertices);
					}
				}
			}
		}
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
		batch.destroy();
	}
}
