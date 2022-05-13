package scenes;

import game.GLTris;
import org.joml.Matrix4f;
import game.pieces.*;
import game.pieces.util.*;
import render.Camera;
import render.Shader;
import util.KeyListener;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL45.glCreateVertexArrays;

public class GameScene extends Scene{
	private static final float[] COLOUR_GARBAGE = {0.5f, 0.5f, 0.5f, 1.0f};
	private static final float[] COLOUR_I = {0.0f, 1.0f, 1.0f, 1.0f};
	private static final float[] COLOUR_O = {1.0f, 1.0f, 0.0f, 1.0f};
	private static final float[] COLOUR_L = {1.0f, 0.5f, 0.0f, 1.0f};
	private static final float[] COLOUR_J = {0.0f, 0.0f, 1.0f, 1.0f};
	private static final float[] COLOUR_S = {0.0f, 1.0f, 0.0f, 1.0f};
	private static final float[] COLOUR_Z = {1.0f, 0.0f, 0.0f, 1.0f};
	private static final float[] COLOUR_T = {0.5f, 0.0f, 0.5f, 1.0f};

	private static final float TILE_SIZE = 1.0f;
	private static final float PROJECTION_HEIGHT = 30.0f * TILE_SIZE;

	private static final float X_OFFSET_HELD = 4.0f * TILE_SIZE;
	private static final float Y_OFFSET_HELD = PROJECTION_HEIGHT - 6.0f * TILE_SIZE;

	private static final float X_OFFSET_BOARD = 8.0f * TILE_SIZE;
	private static final float Y_OFFSET_BOARD = 1.0f * TILE_SIZE;

	private static final float X_OFFSET_QUEUE = 20.0f * TILE_SIZE;
	private static final float Y_OFFSET_QUEUE = PROJECTION_HEIGHT - 3.0f * TILE_SIZE;

	private Shader shaderBlocks;
	private Camera camera;
	private Matrix4f projection;

	private int tileVaoID, tileVboID, tileEboID;

	private PieceName[] currentQueue;

	private GLTris game;

	public GameScene(long windowID) {
		super(windowID);
		try {
			shaderBlocks = new Shader("shaders/vertex.glsl", "shaders/fragment.glsl");
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}

		camera = new Camera();
		game = new GLTris();
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

		updateProjection(windowID);

		//vertex format: vec3f pos, vec4f col, vec2f uv texture coords
		float[] vertexDataTile = {
			1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
			0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
			0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
			1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
		};

		int[] indexDataTile = {
			0, 1, 2,
			2, 3, 0
		};

		tileVaoID = glCreateVertexArrays();
		glBindVertexArray(tileVaoID);

		tileVboID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, tileVboID);
		glBufferData(GL_ARRAY_BUFFER, vertexDataTile, GL_STATIC_DRAW);

		tileEboID = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, tileEboID);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexDataTile, GL_STATIC_DRAW);

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
		glBindVertexArray(tileVaoID);
		glBindBuffer(GL_ARRAY_BUFFER, tileVboID);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, tileEboID);

		glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
		glEnableVertexAttribArray(1);

		shaderBlocks.uploadUniformMatrix4fv("uView", false, camera.getView().get(buffer));
		shaderBlocks.uploadUniformMatrix4fv("uProjection", false, projection.get(buffer));

		float[] transformMatrix = {
			1.0f, 0.0f, 0.0f, 0.0f,
			0.0f, 1.0f, 0.0f, 0.0f,
			0.0f, 0.0f, 1.0f, 0.0f,
			0.0f, 0.0f, 0.0f, 1.0f
		};

		float[] currentColour = {0.0f, 0.0f, 0.0f, 1.0f};

		TileState[][] board = game.getBoard();

		//draw the board
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				if (board[i][j] != TileState.EMPTY) {
					transformMatrix[3] = j + X_OFFSET_BOARD;
					transformMatrix[7] = i + Y_OFFSET_BOARD;
					switch(board[i][j]) {
						case GARBAGE -> {
							currentColour = COLOUR_GARBAGE;
						}
						case I -> {
							currentColour = COLOUR_I;
						}
						case O -> {
							currentColour = COLOUR_O;
						}
						case L -> {
							currentColour = COLOUR_L;
						}
						case J -> {
							currentColour = COLOUR_J;
						}
						case S -> {
							currentColour = COLOUR_S;
						}
						case Z -> {
							currentColour = COLOUR_Z;
						}
						case T -> {
							currentColour = COLOUR_T;
						}
					}
					//probably have to transpose but w/e
					shaderBlocks.uploadUniformMatrix4fv("uTransform", true, transformMatrix);
					shaderBlocks.uploadUniform4fv("uColour", currentColour);
					glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
				}
			}
		}

		//draw the current piece the player is placing
		Piece currentPiece = game.getCurrentPiece();
		boolean[][] tileMap = currentPiece.getTileMap();
		for (int i = 0; i < tileMap.length; i++) {
			for (int j = 0; j < tileMap[i].length; j++) {
				if (tileMap[i][j]) {
					transformMatrix[3] = currentPiece.getTopLeftX() + j + X_OFFSET_BOARD;
					transformMatrix[7] = currentPiece.getTopLeftY() + i + Y_OFFSET_BOARD;
					switch(currentPiece.getName()) {
						case I -> {
							currentColour = COLOUR_I;
						}
						case O -> {
							currentColour = COLOUR_O;
						}
						case L -> {
							currentColour = COLOUR_L;
						}
						case J -> {
							currentColour = COLOUR_J;
						}
						case S -> {
							currentColour = COLOUR_S;
						}
						case Z -> {
							currentColour = COLOUR_Z;
						}
						case T -> {
							currentColour = COLOUR_T;
						}
					}
					shaderBlocks.uploadUniformMatrix4fv("uTransform", true, transformMatrix);
					shaderBlocks.uploadUniform4fv("uColour", currentColour);
					glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
				}
			}
		}

		transformMatrix[3] = currentPiece.getTopLeftX() + X_OFFSET_BOARD + 1;
		transformMatrix[7] = currentPiece.getTopLeftY() + Y_OFFSET_BOARD + 1;
		shaderBlocks.uploadUniformMatrix4fv("uTransform", true, transformMatrix);
		shaderBlocks.uploadUniform4f("uColour", 0.0f, 0.0f, 0.0f, 1.0f);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

		//draw the held piece
		PieceName heldPiece = game.getHeldPiece();

		if (heldPiece != null) {
			switch(heldPiece) {
				case I -> {
					currentColour = COLOUR_I;
					tileMap = IPiece.getTileMapSpawn();
				}
				case O -> {
					currentColour = COLOUR_O;
					tileMap = OPiece.getTileMapSpawn();
				}
				case L -> {
					currentColour = COLOUR_L;
					tileMap = LPiece.getTileMapSpawn();
				}
				case J -> {
					currentColour = COLOUR_J;
					tileMap = JPiece.getTileMapSpawn();
				}
				case S -> {
					currentColour = COLOUR_S;
					tileMap = SPiece.getTileMapSpawn();
				}
				case Z -> {
					currentColour = COLOUR_Z;
					tileMap = ZPiece.getTileMapSpawn();
				}
				case T -> {
					currentColour = COLOUR_T;
					tileMap = TPiece.getTileMapSpawn();
				}
			}

			shaderBlocks.uploadUniform4fv("uColour", currentColour);
			for (int i = 0; i < tileMap.length; i++) {
				for (int j = 0; j < tileMap[i].length; j++) {
					if (tileMap[i][j]) {
						transformMatrix[3] = j + X_OFFSET_HELD;
						transformMatrix[7] = i + Y_OFFSET_HELD;
						shaderBlocks.uploadUniformMatrix4fv("uTransform", true, transformMatrix);
						glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
					}
				}
			}
		}


		//draw the piece queue

		for (int index = 0; index < GLTris.NUM_PREVIEWS; index++) {
			PieceName pieceName = currentQueue[index];
			switch (pieceName) {
				case I -> {
					currentColour = COLOUR_I;
					tileMap = IPiece.getTileMapSpawn();
				}
				case O -> {
					currentColour = COLOUR_O;
					tileMap = OPiece.getTileMapSpawn();
				}
				case L -> {
					currentColour = COLOUR_L;
					tileMap = LPiece.getTileMapSpawn();
				}
				case J -> {
					currentColour = COLOUR_J;
					tileMap = JPiece.getTileMapSpawn();
				}
				case S -> {
					currentColour = COLOUR_S;
					tileMap = SPiece.getTileMapSpawn();
				}
				case Z -> {
					currentColour = COLOUR_Z;
					tileMap = ZPiece.getTileMapSpawn();
				}
				case T -> {
					currentColour = COLOUR_T;
					tileMap = TPiece.getTileMapSpawn();
				}
			}
			shaderBlocks.uploadUniform4fv("uColour", currentColour);
			for (int i = 0; i < tileMap.length; i++) {
				for (int j = 0; j < tileMap[i].length; j++) {
					if (tileMap[i][j]) {
						transformMatrix[3] = j + X_OFFSET_QUEUE;
						transformMatrix[7] = i + Y_OFFSET_QUEUE - index * 5.0f;
						shaderBlocks.uploadUniformMatrix4fv("uTransform", true, transformMatrix);
						glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
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
	}
}
