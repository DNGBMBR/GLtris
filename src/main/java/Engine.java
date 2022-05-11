import game.GLTris;
import org.joml.Matrix4f;
import pieces.*;
import pieces.util.*;
import render.Shader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL45.*;

public class Engine {
	private static final int NUM_BLOCKS = 100;

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

	/*
	* STEPS FOR DRAWING TO SCREEN:
	* - compile shaders, initialize vaos, vbos, and ebos
	* - in window loop:
	* 	- for each shader:
	* 		- bind the shader
	* 		- bind the vao, vbo, and ebo to be used
	* 		- initialize vertex data using glVertexAttribPointer() and glEnableVertexAttribArray()
	* 		- upload uniforms to the shader
	* 		- call glDrawArrays() or glDrawElements() for vbo data or ebo data respectively
	* */

	public Engine() {
		try {
			shaderBlocks = new Shader("shaders/vertex.glsl", "shaders/fragment.glsl");
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}

		camera = new Camera();
		game = new GLTris();
	}

	public void init(long windowID) {
		shaderBlocks.compile();

		//vertex format: vec3f pos, vec4f col, vec2f uv texture coords
		float[] vertexDataTile = {
			0.5f, 0.5f, 0.0f, 1.0f, 1.0f,
			-0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
			-0.5f, -0.5f, 0.0f, 0.0f, 0.0f,
			0.5f, -0.5f, 0.0f, 1.0f, 0.0f,
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

		updateProjection(windowID);

		game.init();

		currentQueue = game.getPieceQueue();
		game.registerOnNextPieceListener(() -> {
			currentQueue = game.getPieceQueue();
		});
	}

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
			switch(pieceName) {
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

	public void update(double dt) {
		game.update(dt);
	}

	private int[] genIndexData(TileState[][] board) {
		int boardHeight = board.length;
		int boardWidth = board[0].length;
		List<Integer> indices = new ArrayList<>();
		for (int i = 0; i < boardHeight; i++) {
			for (int j = 0; j < boardWidth; j++) {
				switch (board[i][j]) {
					case GARBAGE -> {
						indices.add(0);
						indices.add(1);
						indices.add(2);
						indices.add(2);
						indices.add(3);
						indices.add(0);
					}
					case I -> {
						indices.add(4);
						indices.add(5);
						indices.add(6);
						indices.add(6);
						indices.add(7);
						indices.add(4);
					}
					case O -> {
						indices.add(8);
						indices.add(9);
						indices.add(10);
						indices.add(10);
						indices.add(11);
						indices.add(8);
					}
					case L -> {
						indices.add(12);
						indices.add(13);
						indices.add(14);
						indices.add(14);
						indices.add(15);
						indices.add(12);
					}
					case J -> {
						indices.add(16);
						indices.add(17);
						indices.add(18);
						indices.add(18);
						indices.add(19);
						indices.add(16);
					}
					case S -> {
						indices.add(20);
						indices.add(21);
						indices.add(22);
						indices.add(22);
						indices.add(23);
						indices.add(20);
					}
					case Z -> {
						indices.add(24);
						indices.add(25);
						indices.add(26);
						indices.add(26);
						indices.add(27);
						indices.add(24);
					}
					case T -> {
						indices.add(28);
						indices.add(29);
						indices.add(30);
						indices.add(30);
						indices.add(31);
						indices.add(28);
					}
				}
			}
		}
		int[] ret = new int[indices.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = indices.get(i);
		}
		return ret;
	}
}
