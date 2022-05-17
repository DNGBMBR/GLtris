package scenes;

import game.GLTris;
import org.joml.Matrix4f;
import game.pieces.*;
import game.pieces.util.*;
import org.lwjgl.BufferUtils;
import render.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.opengl.GL15.*;

public class GameScene extends Scene{

	private static final float TILE_SIZE = 1.0f;
	private static final float PROJECTION_HEIGHT = 30.0f * TILE_SIZE;

	private static final float X_OFFSET_HELD = 4.0f * TILE_SIZE;
	private static final float Y_OFFSET_HELD = PROJECTION_HEIGHT - 6.0f * TILE_SIZE;

	private static final float X_OFFSET_BOARD = 8.0f * TILE_SIZE;
	private static final float Y_OFFSET_BOARD = 1.0f * TILE_SIZE;

	private static final float X_OFFSET_QUEUE = 20.0f * TILE_SIZE;
	private static final float Y_OFFSET_QUEUE = PROJECTION_HEIGHT - 3.0f * TILE_SIZE;

	private static final int TEXTURE_SIZE = 128; //image to be sampled is 128x128px
	private static final int TEXTURE_TILE_SIZE = 32; //each tile in atlas is 32px

	private Shader shaderBlocks;
	private Camera camera;
	private Matrix4f projection;

	private Batch batch;

	private int textureID;

	private PieceName[] currentQueue;

	private GLTris game;

	public GameScene(long windowID) {
		super(windowID);
		try {
			shaderBlocks = new Shader("shaders/vertex.glsl", "shaders/fragment.glsl");
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}

		InputStream is = getClass().getClassLoader().getResourceAsStream("images/default_skin.png");
		if (is == null) {
			System.out.println("not poggers");
		}
		BufferedImage image;
		try {
			image = ImageIO.read(is);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not find skin.");
		}

		int[] pixels = new int[image.getWidth() * image.getHeight()];
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0 , image.getWidth());
		ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);
		for (int i = image.getHeight() - 1; i >= 0; i--) {
			for (int j = 0; j < image.getWidth(); j++) {
				int pixel = pixels[i * image.getWidth() + j];
				buffer.put((byte) ((pixel >> 16) & 0xFF));
				buffer.put((byte) ((pixel >> 8) & 0xFF));
				buffer.put((byte) ((pixel >> 0) & 0xFF));
				buffer.put((byte) ((pixel >> 24) & 0xFF));
			}
		}
		buffer.flip();

		camera = new Camera();
		game = new GLTris();
		BatchConfig config = new BatchConfig(GL_TRIANGLES, false, 0);
		batch = new Batch(shaderBlocks, 600, config);

		textureID = glGenTextures();
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, textureID);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
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

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, textureID);
		shaderBlocks.uploadUniform1i("uTexture", 0);

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

					float p0x = (px * TEXTURE_TILE_SIZE + 0.5f) / TEXTURE_SIZE;
					float p0y = (py * TEXTURE_TILE_SIZE + 0.5f) / TEXTURE_SIZE;
					float p1x = ((px + 1) * TEXTURE_TILE_SIZE - 0.5f) / TEXTURE_SIZE;
					float p1y = ((py + 1) * TEXTURE_TILE_SIZE - 0.5f) / TEXTURE_SIZE;

					bottomLeft[2] = p0x; bottomLeft[3] = p0y;
					bottomRight[2] = p1x; bottomRight[3] = p0y;
					topRight[2] = p1x; topRight[3] = p1y;
					topLeft[2] = p0x; topLeft[3] = p1y;

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

					float p0x = (px * TEXTURE_TILE_SIZE + 0.5f) / TEXTURE_SIZE;
					float p0y = (py * TEXTURE_TILE_SIZE + 0.5f) / TEXTURE_SIZE;
					float p1x = ((px + 1) * TEXTURE_TILE_SIZE - 0.5f) / TEXTURE_SIZE;
					float p1y = ((py + 1) * TEXTURE_TILE_SIZE - 0.5f) / TEXTURE_SIZE;

					bottomLeft[2] = p0x; bottomLeft[3] = p0y;
					bottomRight[2] = p1x; bottomRight[3] = p0y;
					topRight[2] = p1x; topRight[3] = p1y;
					topLeft[2] = p0x; topLeft[3] = p1y;

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

			float p0x = (px * TEXTURE_TILE_SIZE + 0.5f) / TEXTURE_SIZE;
			float p0y = (py * TEXTURE_TILE_SIZE + 0.5f) / TEXTURE_SIZE;
			float p1x = ((px + 1) * TEXTURE_TILE_SIZE - 0.5f) / TEXTURE_SIZE;
			float p1y = ((py + 1) * TEXTURE_TILE_SIZE - 0.5f) / TEXTURE_SIZE;

			bottomLeft[2] = p0x; bottomLeft[3] = p0y;
			bottomRight[2] = p1x; bottomRight[3] = p0y;
			topRight[2] = p1x; topRight[3] = p1y;
			topLeft[2] = p0x; topLeft[3] = p1y;

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

			float p0x = (px * TEXTURE_TILE_SIZE + 0.5f) / TEXTURE_SIZE;
			float p0y = (py * TEXTURE_TILE_SIZE + 0.5f) / TEXTURE_SIZE;
			float p1x = ((px + 1) * TEXTURE_TILE_SIZE - 0.5f) / TEXTURE_SIZE;
			float p1y = ((py + 1) * TEXTURE_TILE_SIZE - 0.5f) / TEXTURE_SIZE;

			bottomLeft[2] = p0x; bottomLeft[3] = p0y;
			bottomRight[2] = p1x; bottomRight[3] = p0y;
			topRight[2] = p1x; topRight[3] = p1y;
			topLeft[2] = p0x; topLeft[3] = p1y;

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
