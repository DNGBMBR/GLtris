package game;

import game.callbacks.*;
import game.pieces.*;
import game.pieces.util.*;
import org.joml.Math;
import org.joml.Random;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import settings.*;
import util.*;

import java.util.*;

import static game.SpinType.*;
import static game.pieces.util.Orientation.*;
import static org.lwjgl.glfw.GLFW.*;

public class GLTris {
	//TODO: add garbage queue cancelling
	public static final int TPS = 60;
	public static final double SPF = 1.0 / TPS;

	private Set<Runnable> nextPieceCallback;
	private Set<MoveCallback> pieceMoveCallback;
	private Set<RotateCallback> pieceRotateCallback;
	private Set<PiecePlacedCallback> piecePlacedCallback;
	private Set<GameOverCallback> gameOverCallbacks;

	private String[] bagRandomizer;
	private Random rng;

	private Queue<Garbage> garbageQueue;

	private GLFWKeyCallbackI keyCallback;

	private PieceFactory pieceFactory;
	private Queue<String> pieceQueue;
	private TileState[][] board;
	private Piece currentPiece;
	private String heldPiece;

	//TODO: make SDF like tetr.io's scaling
	private double arr = LocalSettings.getARR();
	private double das = LocalSettings.getDAS();
	private double sdf = LocalSettings.getSDF(); //sdf units in tiles/frame, 1sdf is 1 tile/frame, 40sdf is 40 tiles/frame
	private boolean isDASCancel = LocalSettings.getDASCancel();

	private Direction lastMovedDirection;

	private int[] leftKeys = KeybindingSettings.getMoveLeftKeys();
	private int[] rightKeys = KeybindingSettings.getMoveRightKeys();
	private int[] softDropKeys = KeybindingSettings.getSoftDropKeys();
	private int[] rotateCWKeys = KeybindingSettings.getRotateCWKeys();
	private int[] rotateCCWKeys = KeybindingSettings.getRotateCCWKeys();
	private int[] rotate180Keys = KeybindingSettings.getRotate180Keys();
	private int[] holdKeys = KeybindingSettings.getHoldKeys();
	private int[] hardDropKeys = KeybindingSettings.getHardDropKeys();

	private boolean[] isLeftPressed;
	private boolean[] isRightPressed;
	private boolean[] isSoftDropPressed;
	/*
	private boolean[] isRotateCWPressed;
	private boolean[] isRotateCCWPressed;
	private boolean[] isRotate180Pressed;
	private boolean[] isHoldPressed;
	private boolean[] isHardDropPressed;
	 */

	private int boardHeight = Constants.BOARD_HEIGHT;
	private int boardWidth = Constants.BOARD_WIDTH;
	private int numPreviews;

	private double initGravity; //measured in G, where 1G = 1 tile per tick
	private double gravityIncrease;
	private double gravityIncreaseInterval;
	private double lockDelay; //measured in seconds

	private double accumulatorSD = 0.0;
	private double accumulatorARR = 0.0;
	private double accumulatorDAS = 0.0;

	private double accumulatorGravityIncrease = 0.0;
	private double accumulatorGravity = 0.0;
	private double accumulatorLock = 0.0;
	private double currentGravity;

	private boolean isGameOver = false;

	private int linesCleared = 0;
	private SpinType currentSpinType = NONE;

	private boolean isStarted = false;

	public GLTris(GameSettings settings) {
		currentGravity = initGravity;

		pieceFactory = settings.getKickTable();

		List<PieceBuilder> pieceInfo = pieceFactory.getBuilders();

		for (PieceBuilder builder : pieceInfo) {
			//TODO: improve the centering for xSpawn
			int xSpawn = (this.boardWidth - builder.getTileMapE()[0].length) / 2;
			boolean[][] currentTileMap = builder.getTileMapE();
			int yOffset = 0;
			for (int i = 0; i < currentTileMap.length; i++) {
				boolean isRowOccupied = false;
				for (int j = 0; j < currentTileMap[i].length; j++) {
					if (currentTileMap[i][j]) {
						yOffset = -i;
						isRowOccupied = true;
						break;
					}
				}
				if (isRowOccupied) {
					break;
				}
			}
			int ySpawn = this.boardHeight + yOffset + 1;
			builder.setSpawnBottomLeftX(xSpawn);
			builder.setSpawnBottomLeftY(ySpawn);
		}

		numPreviews = settings.getNumPreviews();

		initGravity = settings.getInitGravity();
		gravityIncrease = settings.getGravityIncrease();
		gravityIncreaseInterval = settings.getGravityIncreaseInterval();
		lockDelay = settings.getLockDelay();

		garbageQueue = new LinkedList<>();

		bagRandomizer = pieceFactory.getNames();
		pieceQueue = new LinkedList<>();
		board = new TileState[2 * boardHeight][boardWidth];
		rng = new Random();
		nextPieceCallback = Collections.synchronizedSet(new HashSet<>());
		pieceMoveCallback = Collections.synchronizedSet(new HashSet<>());
		pieceRotateCallback = Collections.synchronizedSet(new HashSet<>());
		piecePlacedCallback = Collections.synchronizedSet(new HashSet<>());
		gameOverCallbacks = Collections.synchronizedSet(new HashSet<>());

		isLeftPressed = new boolean[leftKeys.length];
		isRightPressed = new boolean[rightKeys.length];
		isSoftDropPressed = new boolean[softDropKeys.length];
		/*
		isRotateCWPressed = new boolean[rotateCWKeys.length];
		isRotateCCWPressed = new boolean[rotateCCWKeys.length];
		isRotate180Pressed = new boolean[rotate180Keys.length];
		isHoldPressed = new boolean[holdKeys.length];
		isHardDropPressed = new boolean[hardDropKeys.length];
		 */
	}

	public void init() {
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				board[i][j] = TileState.EMPTY;
			}
		}
		while (pieceQueue.size() < numPreviews + 1) {
			enqueueBag();
		}
		setNextPiece();
		heldPiece = null;

		keyCallback = (long window, int key, int scancode, int action, int mods) -> {
			if (!isStarted || isGameOver) {
				return;
			}
			if (action == GLFW_PRESS) {
				for (int i = 0; i < leftKeys.length; i++) {
					if (scancode == leftKeys[i]) {
						isLeftPressed[i] = true;
						lastMovedDirection = Direction.LEFT;
						movePiece(Direction.LEFT);
						if (isDASCancel) {
							accumulatorDAS = 0.0;
						}
						return;
					}
				}
				for (int i = 0; i < rightKeys.length; i++) {
					if (scancode == rightKeys[i]) {
						isRightPressed[i] = true;
						lastMovedDirection = Direction.RIGHT;
						movePiece(Direction.RIGHT);
						if (isDASCancel) {
							accumulatorDAS = 0.0;
						}
						return;
					}
				}
				for (int i = 0; i < softDropKeys.length; i++) {
					if (scancode == softDropKeys[i]) {
						isSoftDropPressed[i] = true;
						movePiece(Direction.DOWN);
						return;
					}
				}
				for (int code : rotateCWKeys) {
					if (scancode == code) {
						rotatePiece(Rotation.CW);
						return;
					}
				}
				for (int code : rotateCCWKeys) {
					if (scancode == code) {
						rotatePiece(Rotation.CCW);
						return;
					}
				}
				for (int code : rotate180Keys) {
					if (scancode == code) {
						rotatePiece(Rotation.HALF);
						return;
					}
				}
				for (int code : holdKeys) {
					if (scancode == code) {
						hold();
						return;
					}
				}
				for (int code : hardDropKeys) {
					if (scancode == code) {
						currentPiece.hardDrop(board);
						return;
					}
				}
			}
			else if (action == GLFW_RELEASE) {
				for (int i = 0; i < leftKeys.length; i++) {
					if (scancode == leftKeys[i]) {
						isLeftPressed[i] = false;
						return;
					}
				}
				for (int i = 0; i < rightKeys.length; i++) {
					if (scancode == rightKeys[i]) {
						isRightPressed[i] = false;
						return;
					}
				}
				for (int i = 0; i < softDropKeys.length; i++) {
					if (scancode == softDropKeys[i]) {
						isSoftDropPressed[i] = false;
						return;
					}
				}
			}
		};
		KeyListener.registerKeyCallback(keyCallback);
	}

	public void update(double dt) {
		if (!isGameOver && isStarted) {
			listenKeys(dt);

			applyGravity(dt);
			int linesCleared = clearLines();

			if (currentPiece.isPlaced()) {
				for (PiecePlacedCallback callback : piecePlacedCallback) {
					callback.run(linesCleared, currentSpinType);
				}
				addBoardGarbage();
				setNextPiece();
				if (currentPiece.testCollision(board, 0, 0)) {
					gameOver();
				}
			}

			if (pieceQueue.size() <= 2 * numPreviews) {
				enqueueBag();
			}
		}
	}

	private void listenKeys(double dt) {
		boolean isLeft = false;
		boolean isRight = false;
		boolean isDropping = false;

		for (boolean isPressed : isLeftPressed) {
			if (isPressed) {
				isLeft = true;
				break;
			}
		}
		for (boolean isPressed : isRightPressed) {
			if (isPressed) {
				isRight = true;
				break;
			}
		}

		if (isLeft) {
			if (!isRight || lastMovedDirection == Direction.LEFT) {
				moveKeyLeft(dt);
			}
		}
		if (isRight) {
			if (!isLeft || lastMovedDirection == Direction.RIGHT) {
				moveKeyRight(dt);
			}
		}


		for (boolean isPressed : isSoftDropPressed) {
			if (isPressed) {
				if (sdf > Constants.MAX_SDF) {
					while (currentPiece.move(Direction.DOWN, board));
					break;
				}
				accumulatorSD += dt;
				if (accumulatorSD >= 0.13 / sdf) {
					currentPiece.move(Direction.DOWN, board);
					accumulatorSD = 0.0;
				}
				isDropping = true;
				break;
			}
		}

		if (!isLeft && !isRight) {
			accumulatorDAS = 0.0;
			accumulatorARR = 0.0;
		}
		if (!isDropping) {
			accumulatorSD = 0.0;
		}
	}

	private void moveKeyLeft(double dt) {
		for (boolean isPressed : isLeftPressed) {
			if (isPressed) {
				accumulatorDAS += dt;
				if (accumulatorDAS >= das * SPF) {
					if (arr <= 0.0f) {
						while (currentPiece.move(Direction.LEFT, board));
					}
					else {
						accumulatorARR += dt;
						if (accumulatorARR >= arr * SPF) {
							while (accumulatorARR >= 0.0) {
								currentPiece.move(Direction.LEFT, board);
								accumulatorARR -= SPF;
							}
							accumulatorARR = 0.0;
						}
					}
				}
				break;
			}
		}
	}

	private void moveKeyRight(double dt) {
		for (boolean isPressed : isRightPressed) {
			if (isPressed) {
				accumulatorDAS += dt;
				if (accumulatorDAS >= das * SPF) {
					if (arr <= 0.0f) {
						while (currentPiece.move(Direction.RIGHT, board));
					}
					else {
						accumulatorARR += dt;
						if (accumulatorARR >= arr * SPF) {
							while (accumulatorARR >= 0.0) {
								currentPiece.move(Direction.RIGHT, board);
								accumulatorARR -= SPF;
							}
							accumulatorARR = 0.0;
						}
					}
				}
				break;
			}
		}
	}

	private void applyGravity(double dt) {
		accumulatorGravity += dt;
		accumulatorGravityIncrease += dt;
		if (accumulatorGravity >= SPF / currentGravity) {
			boolean onFloor = !currentPiece.gravity(board);
			if (onFloor) {
				accumulatorLock += dt;
				if (accumulatorLock >= SPF * lockDelay) {
					currentPiece.place(board);
					accumulatorLock = 0.0;
					accumulatorGravity = 0.0;
				}
			}
			else {
				accumulatorGravity = 0.0;
				accumulatorLock = 0.0;
			}
		}
		if (accumulatorGravityIncrease >= gravityIncreaseInterval * SPF) {
			currentGravity += gravityIncrease;
			accumulatorGravityIncrease = 0.0;
		}
	}

	private void hold() {
		accumulatorLock = 0.0;

		if (heldPiece == null) {
			heldPiece = currentPiece.getName();
			setNextPiece();
			return;
		}

		String temp = heldPiece;
		heldPiece = currentPiece.getName();
		currentPiece = pieceFactory.generatePiece(temp);

		currentSpinType = SpinType.NONE;
	}

	private void movePiece(Direction dir) {
		accumulatorLock = 0.0;
		boolean hasMoved = currentPiece.move(dir, board);
		if (hasMoved) {
			for (MoveCallback callback : pieceMoveCallback) {
				callback.run(dir);
			}
		}
	}

	private void rotatePiece(Rotation rot) {
		accumulatorLock = 0.0;
		int kickIndex = currentPiece.rotate(rot, board);
		//detect spin, if any
		//can have support for different spin detection methods
		testSpinDefault(kickIndex);

		for (RotateCallback runnable : pieceRotateCallback) {
			runnable.run(currentPiece.getPieceColour(), rot, kickIndex);
		}
	}

	private void testSpinDefault(int kickIndex) {
		//spins for all pieces except T follow stupid spin rules
		//TODO: rip this out for a listener system
		switch(currentPiece.getPieceColour()) {
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
				int tCenterX = currentPiece.getBottomLeftX() + 1;
				int tCenterY = currentPiece.getBottomLeftY() + 1;
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

	private int clearLines() {
		int linesCleared = checkLineClears();
		this.linesCleared += linesCleared; //TODO: get this out of here
		if (linesCleared > 0) {
			for (PiecePlacedCallback callback : piecePlacedCallback) {
				callback.run(linesCleared, currentSpinType);
			}
		}
		return linesCleared;
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

	private void addBoardGarbage() {
		if (garbageQueue.isEmpty()) {
			return;
		}
		//TODO: add garbage cap
		while (!garbageQueue.isEmpty()) {
			Garbage garbage = garbageQueue.poll();

			//copy current board upwards
			for (int i = board.length - 1; i >= 0; i--) {
				if (i + garbage.amount >= board.length) {
					//check if row is empty. If not, and the row is about to be copied out of bounds, end the game
					for (int j = 0; j < boardWidth; j++) {
						if (board[i][j] != TileState.EMPTY) {
							gameOver();
							break;
						}
					}
				}
				else {
					System.arraycopy(board[i], 0, board[i + garbage.amount], 0, board[i].length);
				}
			}

			//add garbage to the now empty part of the board
			for (int i = 0; i < Math.min(garbage.amount, board.length); i++) {
				for (int j = 0; j < boardWidth; j++) {
					board[i][j] = j == garbage.column ? TileState.EMPTY : TileState.GARBAGE;
				}
			}
		}
	}

	private void setNextPiece() {
		currentPiece = nextPieceHelper();
		currentSpinType = SpinType.NONE;
		for (Runnable runnable : nextPieceCallback) {
			runnable.run();
		}
	}

	private Piece nextPieceHelper() {
		String nextPieceName = pieceQueue.poll();
		if (nextPieceName == null) {
			throw new IllegalStateException("Cannot have empty piece queue.");
		}
		Piece nextPiece = pieceFactory.generatePiece(nextPieceName);
		return nextPiece;
	}

	private void gameOver() {
		isGameOver = true;
		for (GameOverCallback callback : gameOverCallbacks) {
			callback.onGameOver();
		}
	}

	private void enqueueBag() {
		shufflePieces();
		for (String s : bagRandomizer) {
			pieceQueue.offer(s);
		}
	}

	private void shufflePieces() {
		//Fisher-Yates shuffle
		for (int i = bagRandomizer.length - 1; i >= 1; i--) {
			int j = rng.nextInt(i);
			String temp = bagRandomizer[i];
			bagRandomizer[i] = bagRandomizer[j];
			bagRandomizer[j] = temp;
		}
	}

	public void addQueueGarbage(Garbage garbage) {
		garbageQueue.add(garbage);
	}

	public void addQueueGarbage(List<Garbage> garbageList) {
		for (Garbage garbage : garbageList) {
			addQueueGarbage(garbage);
		}
	}

	public int getBoardHeight() {
		return boardHeight;
	}

	public int getBoardWidth() {
		return boardWidth;
	}

	public TileState[][] getBoard() {
		return this.board;
	}

	public Piece getCurrentPiece() {
		return currentPiece;
	}

	public String getHeldPiece() {
		return heldPiece;
	}

	public String[] getPieceQueue() {
		String[] ret = {};
		ret = pieceQueue.toArray(ret);
		return ret;
	}

	public PieceFactory getPieceFactory() {
		return pieceFactory;
	}

	public int getNumPreviews() {
		return numPreviews;
	}

	public int getLinesCleared() {
		return linesCleared;
	}

	public boolean isGameOver() {
		return isGameOver;
	}

	public void setStarted(boolean started) {
		isStarted = started;
	}

	public void registerOnPiecePlacedCallback(PiecePlacedCallback callback) {
		piecePlacedCallback.add(callback);
	}

	public void registerOnNextPieceListener(Runnable listener) {
		nextPieceCallback.add(listener);
	}

	public void registerOnRotateListener(RotateCallback callback) {
		pieceRotateCallback.add(callback);
	}

	public void registerOnGameOverListener(GameOverCallback callback) {
		gameOverCallbacks.add(callback);
	}

	public void destroy() {
		KeyListener.unregisterKeyCallback(keyCallback);
	}
}
