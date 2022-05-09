import org.joml.Random;
import pieces.*;
import pieces.util.*;
import util.KeyListener;

import java.util.LinkedList;
import java.util.Queue;

import static org.lwjgl.glfw.GLFW.*;

public class Tetris {
	private static final int TPS = 60;
	private static final double SPF = 1.0 / TPS;
	private static final double ARR = 1.0 * SPF; //20 ticks/move
	private static final double DAS = 8.0 * SPF; //200 ticks before DAS kicks in
	private static final double SDF = 20.0 * SPF; //20 ticks/move down

	private PieceName[] bagRandomizer = {PieceName.I, PieceName.O, PieceName.L, PieceName.J, PieceName.S, PieceName.Z, PieceName.T};
	Random rng;

	Queue<PieceName> pieceQueue;
	TileState[][] board;
	Piece currentPiece;
	PieceName heldPiece;

	double accumulatorSD;
	double accumulatorARR;
	double accumulatorDAS;

	public Tetris() {
		pieceQueue = new LinkedList<>();
		board = new TileState[40][10];
		accumulatorSD = 0.0;
		accumulatorARR = 0.0;
		accumulatorDAS = 0.0;
		rng = new Random();
	}

	public void init() {
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				board[i][j] = TileState.EMPTY;
			}
		}
		enqueueBag();
		currentPiece = nextPiece();
		KeyListener.registerCallback((window, key, scancode, action, mods) -> {
			if (action == GLFW_PRESS) {
				switch(key) {
					case GLFW_KEY_S -> {
						currentPiece.move(Direction.DOWN, board);
					}
					case GLFW_KEY_A -> {
						currentPiece.move(Direction.LEFT, board);
					}
					case GLFW_KEY_D -> {
						currentPiece.move(Direction.RIGHT, board);
					}
					case GLFW_KEY_PERIOD -> {
						currentPiece.rotate(Rotation.CCW, board);
					}
					case GLFW_KEY_SLASH -> {
						currentPiece.rotate(Rotation.CW, board);
					}
					case GLFW_KEY_SPACE -> {
						currentPiece.hardDrop(board);
					}
					case GLFW_KEY_G -> {
						printBoard();
					}
				}
			}
		});
	}

	public void update(double dt) {
		if (KeyListener.isKeyPressed(GLFW_KEY_S)) {
			accumulatorSD += dt;
			if (accumulatorSD >= SDF) {
				currentPiece.move(Direction.DOWN, board);
				accumulatorSD = 0.0;
			}
		}
		else {
			accumulatorSD = 0;
		}
		if (KeyListener.isKeyPressed(GLFW_KEY_A)) {
			accumulatorDAS += dt;
			if (accumulatorDAS >= DAS) {
				accumulatorARR += dt;
				if (accumulatorARR >= ARR) {
					currentPiece.move(Direction.LEFT, board);
					accumulatorARR = 0.0;
				}
			}
		}
		if (KeyListener.isKeyPressed(GLFW_KEY_D)) {
			accumulatorDAS += dt;
			if (accumulatorDAS >= DAS) {
				accumulatorARR += dt;
				if (accumulatorARR >= ARR) {
					currentPiece.move(Direction.RIGHT, board);
					accumulatorARR = 0.0;
				}
			}
		}
		if (!KeyListener.isKeyPressed(GLFW_KEY_A) && !KeyListener.isKeyPressed(GLFW_KEY_D)) {
			accumulatorDAS = 0.0;
			accumulatorARR = 0.0;
		}

		if (currentPiece.isPlaced()) {
			currentPiece = nextPiece();
		}

		if (pieceQueue.size() <= 7) {
			enqueueBag();
		}
	}

	private Piece nextPiece() {
		PieceName nextPieceName = pieceQueue.poll();
		Piece nextPiece;
		switch (nextPieceName) {
			case I -> {
				nextPiece = new IPiece();
			}
			case O -> {
				nextPiece = new OPiece();
			}
			case L -> {
				nextPiece = new LPiece();
			}
			case J -> {
				nextPiece = new JPiece();
			}
			case S -> {
				nextPiece = new SPiece();
			}
			case Z -> {
				nextPiece = new ZPiece();
			}
			case T -> {
				nextPiece = new TPiece();
			}
			default -> {
				throw new IllegalStateException("Bag has piece that is not one of the standard pieces.");
			}
		}
		return nextPiece;
	}

	private void enqueueBag() {
		shufflePieces();
		for (int i = 0; i < bagRandomizer.length; i++) {
			pieceQueue.offer(bagRandomizer[i]);
		}
	}

	private void shufflePieces() {
		//Fisher-Yates shuffle
		for (int i = bagRandomizer.length - 1; i >= 1; i--) {
			int j = rng.nextInt(i);
			PieceName temp = bagRandomizer[i];
			bagRandomizer[i] = bagRandomizer[j];
			bagRandomizer[j] = temp;
		}
	}

	void printBoard() {
		boolean[][] tileMap = currentPiece.getTileMap();
		int x = currentPiece.getTopLeftX();
		int y = currentPiece.getTopLeftY();
		for (int i = 27; i >= 0; i--) {
			for (int j = 0; j < board[i].length; j++) {
				char printable = '.';
				if ((i - y >= 0 && i - y < tileMap.length && j - x >= 0 && j - x < tileMap[0].length) && tileMap[i - y][j - x]) {
					printable = 'X';
				}
				else {
					switch(board[i][j]) {
						case GARBAGE -> {
							printable = 'R';
						}
						case I -> {
							printable = 'I';
						}
						case O -> {
							printable = 'O';
						}
						case L -> {
							printable = 'L';
						}
						case J -> {
							printable = 'J';
						}
						case S -> {
							printable = 'S';
						}
						case Z -> {
							printable = 'Z';
						}
						case T -> {
							printable = 'T';
						}
					}
				}

				System.out.print(printable);
			}
			System.out.println();
		}
		System.out.println("__________");
	}
}
