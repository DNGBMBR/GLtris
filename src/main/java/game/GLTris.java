package game;

import org.joml.Random;
import pieces.*;
import pieces.util.*;
import util.KeyListener;
import util.LocalSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.lwjgl.glfw.GLFW.*;

public class GLTris {
	public static final int TPS = 60;
	public static final double SPF = 1.0 / TPS;

	public static final int BOARD_HEIGHT = 40;
	public static final int BOARD_WIDTH = 10;

	public static final int NUM_PREVIEWS = 6;

	private List<Runnable> nextPieceCallback;

	private PieceName[] bagRandomizer = {PieceName.I, PieceName.O, PieceName.L, PieceName.J, PieceName.S, PieceName.Z, PieceName.T};
	private Random rng;

	private ConcurrentLinkedQueue<PieceName> pieceQueue;
	private TileState[][] board;
	private Piece currentPiece;
	private PieceName heldPiece;

	private double arr;
	private double das;
	private double sdf;

	private double accumulatorSD;
	private double accumulatorARR;
	private double accumulatorDAS;

	public GLTris() {
		pieceQueue = new ConcurrentLinkedQueue<>();
		board = new TileState[BOARD_HEIGHT][BOARD_WIDTH];
		accumulatorSD = 0.0;
		accumulatorARR = 0.0;
		accumulatorDAS = 0.0;
		rng = new Random();
		nextPieceCallback = new ArrayList<>();

		sdf = LocalSettings.getSDF();
		arr = LocalSettings.getARR();
		das = LocalSettings.getDAS();

		LocalSettings.saveSettings();
	}

	public void init() {
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				board[i][j] = TileState.EMPTY;
			}
		}
		enqueueBag();
		setNextPiece();
		heldPiece = null;

		KeyListener.registerCallback((long window, int key, int scancode, int action, int mods) -> {
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
					case GLFW_KEY_COMMA -> {
						currentPiece.rotate(Rotation.HALF, board);
					}
					case GLFW_KEY_SPACE -> {
						currentPiece.hardDrop(board);
					}
					case GLFW_KEY_F -> {
						hold();
					}
					case GLFW_KEY_G -> {
						LocalSettings.saveSettings();
					}
				}
			}
		});
	}

	public void update(double dt) {
		listenKeys(dt);

		checkLineClears();
		//add garbage(?)

		if (currentPiece.isPlaced()) {
			setNextPiece();
			//check for collision of the new piece, and end the game if it collides
		}

		if (pieceQueue.size() <= 7) {
			enqueueBag();
		}
	}

	private void hold() {
		if (heldPiece == null) {
			heldPiece = currentPiece.getName();
			setNextPiece();
			return;
		}

		PieceName temp = heldPiece;
		heldPiece = currentPiece.getName();
		switch(temp) {
			case I -> {
				currentPiece = new IPiece();
			}
			case O -> {
				currentPiece = new OPiece();
			}
			case L -> {
				currentPiece = new LPiece();
			}
			case J -> {
				currentPiece = new JPiece();
			}
			case S -> {
				currentPiece = new SPiece();
			}
			case Z -> {
				currentPiece = new ZPiece();
			}
			case T -> {
				currentPiece = new TPiece();
			}
		}

	}

	private void listenKeys(double dt) {
		if (KeyListener.isKeyPressed(GLFW_KEY_S)) {
			accumulatorSD += dt;
			if (accumulatorSD >= sdf * SPF) {
				currentPiece.move(Direction.DOWN, board);
				accumulatorSD = 0.0;
			}
		}
		else {
			accumulatorSD = 0;
		}
		if (KeyListener.isKeyPressed(GLFW_KEY_A)) {
			accumulatorDAS += dt;
			if (accumulatorDAS >= das * SPF) {
				if (arr == 0.0f) {
					while (currentPiece.move(Direction.LEFT, board)) {
						//repeat until wall is hit
					}
				}
				else {
					accumulatorARR += dt;
					if (accumulatorARR >= arr * SPF) {
						currentPiece.move(Direction.LEFT, board);
						accumulatorARR = 0.0;
					}
				}
			}
		}
		if (KeyListener.isKeyPressed(GLFW_KEY_D)) {
			accumulatorDAS += dt;
			if (accumulatorDAS >= das * SPF) {
				if (arr == 0.0f) {
					while (currentPiece.move(Direction.RIGHT, board)) {
						//repeat until wall is hit
					}
				}
				else {
					accumulatorARR += dt;
					if (accumulatorARR >= arr * SPF) {
						currentPiece.move(Direction.RIGHT, board);
						accumulatorARR = 0.0;
					}
				}
			}
		}
		if (!KeyListener.isKeyPressed(GLFW_KEY_A) && !KeyListener.isKeyPressed(GLFW_KEY_D)) {
			accumulatorDAS = 0.0;
			accumulatorARR = 0.0;
		}
	}

	private void checkLineClears() {
		ArrayList<Integer> indices = new ArrayList<>();
		for (int i = 0; i < board.length; i++) {
			boolean isClearable = true;
			boolean isEmptyRow = true;
			for (TileState tile : board[i]) {
				if (tile == TileState.EMPTY) {
					isClearable = false;
				}
				else {
					isEmptyRow = false;
				}
			}
			if (isEmptyRow) {
				break;
			}
			if (isClearable) {
				indices.add(i);
			}
		}

		if (indices.isEmpty()) {
			return;
		}

		int index = 0;
		for (int i = indices.get(index) + 1; i < board.length; i++) {
			if (index != indices.size() && i > indices.get(index)) {
				index++;
			}
			else if (index == 0) {
				continue;
			}
			System.arraycopy(board[i], 0, board[i - index], 0, board[i].length);
		}
	}

	private void setNextPiece() {
		currentPiece = nextPieceHelper();
		for (Runnable runnable : nextPieceCallback) {
			runnable.run();
		}
	}

	private Piece nextPieceHelper() {
		PieceName nextPieceName = pieceQueue.poll();
		if (nextPieceName == null) {
			throw new IllegalStateException("Cannot have empty piece queue.");
		}
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

	public TileState[][] getBoard() {
		return this.board;
	}

	public Piece getCurrentPiece() {
		return currentPiece;
	}

	public PieceName getHeldPiece() {
		return heldPiece;
	}

	public PieceName[] getPieceQueue() {
		PieceName[] ret = {};
		ret = pieceQueue.toArray(ret);
		return ret;
	}

	public void registerOnNextPieceListener(Runnable listener) {
		nextPieceCallback.add(listener);
	}
}
