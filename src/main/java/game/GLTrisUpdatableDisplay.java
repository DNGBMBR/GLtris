package game;

import game.callbacks.BoardUpdateCallback;
import game.pieces.util.Piece;
import game.pieces.util.TileState;
import util.Constants;

import java.util.ArrayList;
import java.util.List;

public class GLTrisUpdatableDisplay extends GLTrisRender{
	TileState[][] board;
	String[] queue;
	String heldPiece;
	int numPreviews;

	List<Garbage> emptyGarbageList = new ArrayList<>();

	public GLTrisUpdatableDisplay(int numPreviews) {
		this.numPreviews = numPreviews;
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
	public Piece getCurrentPiece() {
		//TODO: add in message the current piece instead of baking it into the board
		return null;
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
	public List<Garbage> getGarbageQueue() {
		return emptyGarbageList;
	}

	public void setBoard(TileState[][] board) {
		this.board = board;
	}

	public void setHeldPiece(String heldPiece) {
		this.heldPiece = heldPiece;
	}

	public void setQueue(String[] queue) {
		this.queue = queue;
	}
}
