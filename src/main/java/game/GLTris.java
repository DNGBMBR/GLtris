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
import static org.lwjgl.glfw.GLFW.*;

public class GLTris extends GLTrisRender {
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

	private int boardHeight = Constants.BOARD_HEIGHT;
	private int boardWidth = Constants.BOARD_WIDTH;
	private int numPreviews;

	private double accumulatorSD = 0.0;
	private double accumulatorARR = 0.0;
	private double accumulatorDAS = 0.0;

	private boolean isGameOver = false;

	private int linesCleared = 0;
	private SpinType currentSpinType = NONE;
	private SpinDetection spinDetection;

	private int combo = -1;
	private int b2bLevel = 0;

	private boolean isStarted = false;

	public GLTris(GameSettings settings) {

		this.boardHeight = settings.getBoardHeight();
		this.boardWidth = settings.getBoardWidth();
		pieceFactory = settings.getKickTable();
		this.spinDetection = SpinDetector.getDetection(settings.getSpinDetector());

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

		/*
		initGravity = settings.getInitGravity();
		gravityIncrease = settings.getGravityIncrease();
		gravityIncreaseInterval = settings.getGravityIncreaseInterval();
		lockDelay = settings.getLockDelay();
		 */

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

		for (BoardUpdateCallback callback : boardUpdateCallbacks) {
			callback.onBoardUpdate();
		}
	}

	public void update(double dt) {
		if (!isGameOver && isStarted) {
			listenKeys(dt);

			//applyGravity(dt);
			int linesCleared = clearLines();

			if (currentPiece.isPlaced()) {
				int attack;
				int baseAttack = computeAttack(linesCleared, currentSpinType);
				if (linesCleared > 0) {
					attack = cancelGarbageQueue(baseAttack);
				}
				else {
					attack = 0;
					addBoardGarbage();
				}

				for (PiecePlacedCallback callback : piecePlacedCallback) {
					callback.run(linesCleared, currentSpinType, attack);
				}
				setNextPiece();
				for (BoardUpdateCallback callback : boardUpdateCallbacks) {
					callback.onBoardUpdate();
				}
				if (!isGameOver && currentPiece.testCollision(board, 0, 0)) {
					gameOver();
					isGameOver = true;
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

	private void hold() {

		if (heldPiece == null) {
			heldPiece = currentPiece.getName();
			setNextPiece();
		}
		else {
			String temp = heldPiece;
			heldPiece = currentPiece.getName();
			currentPiece = pieceFactory.generatePiece(temp);
		}

		currentSpinType = SpinType.NONE;
		for (BoardUpdateCallback callback : boardUpdateCallbacks) {
			callback.onBoardUpdate();
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
			runnable.run(currentPiece.getPieceColour(), rot, kickIndex);
		}
	}

	private void testSpinDefault(int kickIndex) {
		currentSpinType = spinDetection.detectSpin(currentPiece, board, kickIndex);
	}

	private int clearLines() {
		int linesCleared = checkLineClears();
		this.linesCleared += linesCleared; //TODO: get this out of here
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

	private int computeAttack(int rowsCleared, SpinType spinType) {
		int linesToSend;
		if (rowsCleared > 0) {
			//TODO: add combo table support
			//it's gonna be hardcoded for the time being
			//function is G(b, c, n) = ((c + b) / 4) * n + c + b, where
			//b = b2b level, c = clear type (1 for double/t-mini double, 2 for triple/tss, 4 for quad/tsd, 6 for tst), n is the combo
			int linesBase;
			boolean isB2B;
			switch (spinType) {
				case T_SPIN -> {
					linesBase = 2 * rowsCleared;
					isB2B = true;
				}
				case T_SPIN_MINI -> {
					linesBase = (4 * (rowsCleared - 1)) / 3;
					isB2B = true;
				}
				default -> {
					//evaluates to 0, 1, 2, 4 for inputs 1, 2, 3, 4
					linesBase = (4 * (rowsCleared - 1)) / 3;
					isB2B = rowsCleared >= 4;
					if (!isB2B) {
						b2bLevel = 0;
					}
				}
			}
			combo++;
			if (rowsCleared < 2) {
				if (combo < 2) {
					linesToSend = 0;
				}
				else if (combo < 6) {
					linesToSend = 1;
				}
				else if (combo < 16) {
					linesToSend = 2;
				}
				else if (combo < 43) {
					linesToSend = 3;
				}
				else if (combo < 118) {
					linesToSend = 4;
				}
				else {
					linesToSend = 5;
				}
			}
			else {
				linesToSend = ((linesBase + b2bLevel) / 4) * combo + linesBase + b2bLevel;
			}
			b2bLevel = isB2B ? 1 : 0;
		}
		else {
			combo = -1;
			linesToSend = 0;
		}
		return linesToSend;
	}

	private int cancelGarbageQueue(int attack) {
		int remainingAttack = attack;
		while (remainingAttack > 0 && !garbageQueue.isEmpty()) {
			Garbage garbage = garbageQueue.peek();
			if (remainingAttack >= garbage.amount) {
				garbageQueue.poll();
				remainingAttack -= garbage.amount;
			}
			else {
				garbage.amount -= remainingAttack;
				remainingAttack = 0;
			}
		}
		return remainingAttack;
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
		return pieceFactory.generatePiece(nextPieceName);
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

	private void addQueueGarbage(Garbage garbage) {
		garbageQueue.add(garbage);
	}

	public void addQueueGarbage(List<Garbage> garbageList) {
		for (Garbage garbage : garbageList) {
			addQueueGarbage(garbage);
		}
		for (BoardUpdateCallback callback : boardUpdateCallbacks) {
			callback.onBoardUpdate();
		}
	}

	@Override
	public int getBoardHeight() {
		return boardHeight;
	}

	@Override
	public int getBoardWidth() {
		return boardWidth;
	}

	@Override
	public TileState[][] getBoard() {
		return this.board;
	}

	public Piece getCurrentPiece() {
		return this.currentPiece.copy();
	}

	@Override
	int getPieceX() {
		return currentPiece.getBottomLeftX();
	}

	@Override
	int getPieceY() {
		return currentPiece.getBottomLeftY();
	}

	@Override
	boolean[][] getTileMap() {
		return currentPiece.getTileMap();
	}

	@Override
	PieceColour getPieceColour() {
		return currentPiece.getPieceColour();
	}

	@Override
	public String getHeldPiece() {
		return heldPiece;
	}

	@Override
	public String[] getPieceQueue() {
		String[] ret = {};
		ret = pieceQueue.toArray(ret);
		return ret;
	}

	@Override
	public int[] getGarbageQueue() {
		int[] list = new int[garbageQueue.size()];
		int i = 0;
		for (Garbage garbage : garbageQueue) {
			if (i > list.length) {
				break;
			}
			list[i] = garbage.amount;
			i++;
		}
		return list;
	}

	public PieceFactory getPieceFactory() {
		return pieceFactory;
	}

	@Override
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
