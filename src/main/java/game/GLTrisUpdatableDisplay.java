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
		board = new TileState[2 * Constants.BOARD_HEIGHT][2 * Constants.BOARD_WIDTH];
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				board[i][j] = TileState.EMPTY;
			}
		}
		queue = new String[0];
		heldPiece = null;
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
		//TODO: add the current piece in message instead of baking it into the board
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
		for (BoardUpdateCallback callback : this.boardUpdateCallbacks) {
			callback.onBoardUpdate();
		}
	}
}
