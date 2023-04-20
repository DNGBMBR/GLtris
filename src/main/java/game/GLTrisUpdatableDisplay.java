package game;

import game.callbacks.BoardUpdateCallback;
import game.pieces.util.*;
import util.Constants;

public class GLTrisUpdatableDisplay extends GLTrisRender{

	TileState[][] board;
	String[] queue;
	String heldPiece;
	int numPreviews;

	int[] garbageQueue;
	int pieceX, pieceY;
	boolean[][] tileMap;
	PieceColour pieceColour;

	public GLTrisUpdatableDisplay(int numPreviews) {
		this.numPreviews = numPreviews;
		board = new TileState[2 * Constants.BOARD_HEIGHT][2 * Constants.BOARD_WIDTH];
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				board[i][j] = TileState.EMPTY;
			}
		}
		queue = new String[0];
		heldPiece = null;
		garbageQueue = new int[0];
	}

	@Override
	public int getBoardWidth() {
		return Constants.BOARD_WIDTH;
	}

	@Override
	public int getBoardHeight() {
		return Constants.BOARD_HEIGHT;
	}

	@Override
	public TileState[][] getBoard() {
		return board;
	}

	@Override
	int getPieceX() {
		return pieceX;
	}

	@Override
	int getPieceY() {
		return pieceY;
	}

	@Override
	boolean[][] getTileMap() {
		return tileMap;
	}

	@Override
	PieceColour getPieceColour() {
		return pieceColour;
	}

	@Override
	public String getHeldPiece() {
		return heldPiece;
	}

	@Override
	public String[] getPieceQueue() {
		return queue;
	}

	@Override
	public int getNumPreviews() {
		return numPreviews;
	}

	@Override
	public int[] getGarbageQueue() {
		return garbageQueue;
	}

	public void setBoard(TileState[][] board) {
		this.board = board;
	}

	public void setHeldPiece(String heldPiece) {
		this.heldPiece = heldPiece;
	}

	public void setQueue(String[] queue) {
		this.queue = queue;
		for (BoardUpdateCallback callback : this.boardUpdateCallbacks) {
			callback.onBoardUpdate();
		}
	}

	public void setGarbageQueue(int[] garbageQueue) {
		this.garbageQueue = garbageQueue;
	}

	public void setCurrentPieceInfo(int pieceX, int pieceY, boolean[][] tileMap, PieceColour pieceColour) {
		this.pieceX = pieceX;
		this.pieceY = pieceY;
		this.tileMap = tileMap;
		this.pieceColour = pieceColour;
	}
}
