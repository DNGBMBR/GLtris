package game;

import game.callbacks.*;
import org.joml.Random;
import game.pieces.*;
import game.pieces.util.*;
import util.*;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static game.SpinType.*;
import static game.pieces.util.Orientation.*;
import static org.lwjgl.glfw.GLFW.*;

public class GLTris {
	public static final int TPS = 60;
	public static final double SPF = 1.0 / TPS;

	public static final int BOARD_HEIGHT = 40;
	public static final int BOARD_WIDTH = 10;

	public static final int NUM_PREVIEWS = 6;

	private Set<Runnable> nextPieceCallback;
	private Set<MoveCallback> pieceMoveCallback;
	private Set<RotateCallback> pieceRotateCallback;
	private Set<LineClearCallback> lineClearCallback;

	private PieceName[] bagRandomizer = {PieceName.I, PieceName.O, PieceName.L, PieceName.J, PieceName.S, PieceName.Z, PieceName.T};
	private Random rng;

	private KeyCallback keyCallback;

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

	private int linesCleared;
	private SpinType currentSpinType;

	public GLTris() {
		pieceQueue = new ConcurrentLinkedQueue<>();
		board = new TileState[BOARD_HEIGHT][BOARD_WIDTH];
		accumulatorSD = 0.0;
		accumulatorARR = 0.0;
		accumulatorDAS = 0.0;
		rng = new Random();
		nextPieceCallback = Collections.synchronizedSet(new HashSet<>());
		pieceRotateCallback = Collections.synchronizedSet(new HashSet<>());
		lineClearCallback = Collections.synchronizedSet(new HashSet<>());

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

		keyCallback = (long window, int key, int scancode, int action, int mods) -> {
			if (action == GLFW_PRESS) {
				switch(key) {
					case GLFW_KEY_S -> {
						movePiece(Direction.DOWN);
					}
					case GLFW_KEY_A -> {
						movePiece(Direction.LEFT);
					}
					case GLFW_KEY_D -> {
						movePiece(Direction.RIGHT);
					}
					case GLFW_KEY_PERIOD -> {
						rotatePiece(Rotation.CCW);
					}
					case GLFW_KEY_SLASH -> {
						rotatePiece(Rotation.CW);
					}
					case GLFW_KEY_COMMA -> {
						rotatePiece(Rotation.HALF);
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
		};
		KeyListener.registerCallback(keyCallback);

		registerOnLineClearListener((int rowsCleared, SpinType spinType) -> {
			System.out.println("rows cleared: " + rowsCleared + " spin type: " + spinType);
		});
	}

	public void update(double dt) {
		listenKeys(dt);

		clearLines();
		//add garbage(???)

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

		currentSpinType = SpinType.NONE;
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

	private void movePiece(Direction dir) {
		boolean hasMoved = currentPiece.move(dir, board);
		if (hasMoved) {
			for (MoveCallback callback : pieceMoveCallback) {
				callback.run(dir);
			}
		}
	}

	private void rotatePiece(Rotation rot) {
		int kickIndex = currentPiece.rotate(rot, board);
		//detect spin, if any
		//can have support for different spin detection methods
		testSpinDefault(kickIndex);

		for (RotateCallback runnable : pieceRotateCallback) {
			runnable.run(currentPiece.getName(), rot, kickIndex);
		}
	}

	private void testSpinDefault(int kickIndex) {
		//spins for all pieces except T follow stupid spin rules
		switch(currentPiece.getName()) {
			case I -> {
				if (currentPiece.testCollision(board, 1, 0) &&
					currentPiece.testCollision(board, -1, 0) &&
					currentPiece.testCollision(board, 0, -1)) {
					currentSpinType = I_SPIN;
				}
			}
			case O -> {
				if (currentPiece.testCollision(board, 1, 0) &&
					currentPiece.testCollision(board, -1, 0) &&
					currentPiece.testCollision(board, 0, -1)) {
					currentSpinType = O_SPIN;
				}
			}
			case L -> {
				if (currentPiece.testCollision(board, 1, 0) &&
					currentPiece.testCollision(board, -1, 0) &&
					currentPiece.testCollision(board, 0, -1)) {
					currentSpinType = L_SPIN;
				}
			}
			case J -> {
				if (currentPiece.testCollision(board, 1, 0) &&
					currentPiece.testCollision(board, -1, 0) &&
					currentPiece.testCollision(board, 0, -1)) {
					currentSpinType = J_SPIN;
				}
			}
			case S -> {
				if (currentPiece.testCollision(board, 1, 0) &&
					currentPiece.testCollision(board, -1, 0) &&
					currentPiece.testCollision(board, 0, -1)) {
					currentSpinType = S_SPIN;
				}
			}
			case Z -> {
				if (currentPiece.testCollision(board, 1, 0) &&
					currentPiece.testCollision(board, -1, 0) &&
					currentPiece.testCollision(board, 0, -1)) {
					currentSpinType = Z_SPIN;
				}
			}
			case T -> {
				int forwardCornerCount = 0;
				int cornerCount = 0;
				int tCenterX = currentPiece.getTopLeftX() + 1;
				int tCenterY = currentPiece.getTopLeftY() + 1;
				Orientation orientation = currentPiece.getOrientation();
				if (tCenterX - 1 < 0 || tCenterY - 1 < 0 || board[tCenterY - 1][tCenterX - 1] != TileState.EMPTY) {
					if (orientation == R2 || orientation == R3) {
						forwardCornerCount++;
					}
					cornerCount++;
				}
				if (tCenterX - 1 < 0 || tCenterY + 1 >= board.length || board[tCenterY + 1][tCenterX - 1] != TileState.EMPTY) {
					if (orientation == E || orientation == R3) {
						forwardCornerCount++;
					}
					cornerCount++;
				}
				if (tCenterX + 1 >= board[0].length || tCenterY - 1 < 0 || board[tCenterY - 1][tCenterX + 1] != TileState.EMPTY) {
					if (orientation == R || orientation == R2) {
						forwardCornerCount++;
					}
					cornerCount++;
				}
				if (tCenterX + 1 >= board[0].length || tCenterY + 1 >= board.length || board[tCenterY + 1][tCenterX + 1] != TileState.EMPTY) {
					if (orientation == E || orientation == R) {
						forwardCornerCount++;
					}
					cornerCount++;
				}

				if (cornerCount >= 3) {
					//4 is the magic index in SRS that uses the t-spin triple kick
					if (kickIndex >= 4) {
						currentSpinType = T_SPIN;
					}
					else if (forwardCornerCount < 2) {
						currentSpinType = T_SPIN_MINI;
					}
					else {
						currentSpinType = T_SPIN;
					}
				}
			}
			default -> {
				currentSpinType = NONE;
			}
		}
	}

	private void clearLines() {
		int linesCleared = checkLineClears();
		if (linesCleared > 0) {
			for (LineClearCallback callback : lineClearCallback) {
				callback.run(linesCleared, currentSpinType);
			}
		}
	}

	private int checkLineClears() {
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
			return 0;
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

		return indices.size();
	}

	private void setNextPiece() {
		currentPiece = nextPieceHelper();
		currentSpinType = SpinType.NONE;
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
				throw new IllegalStateException("Bag has piece that is not one of the standard game pieces.");
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

	public void registerOnRotateListener(RotateCallback callback) {
		pieceRotateCallback.add(callback);
	}

	public void registerOnLineClearListener(LineClearCallback callback) {
		lineClearCallback.add(callback);
	}

	public void destroy() {
		KeyListener.unregisterCallback(keyCallback);
	}
}
