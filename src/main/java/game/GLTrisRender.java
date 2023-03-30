package game;

import game.callbacks.BoardUpdateCallback;
import game.callbacks.PiecePlacedCallback;
import game.pieces.util.Piece;
import game.pieces.util.TileState;

import java.util.*;

public abstract class GLTrisRender {
	protected Set<BoardUpdateCallback> boardUpdateCallbacks = Collections.synchronizedSet(new HashSet<>());

	abstract int getBoardWidth();
	abstract int getBoardHeight();
	abstract TileState[][] getBoard();
	abstract Piece getCurrentPiece();
	abstract String getHeldPiece();
	abstract String[] getPieceQueue();
	abstract int getNumPreviews();
	abstract List<Garbage> getGarbageQueue();

	public void registerOnBoardUpdate(BoardUpdateCallback callback) {
		boardUpdateCallbacks.add(callback);
		callback.onBoardUpdate();
	}

	public void unregisterOnBoardUpdate(BoardUpdateCallback callback) {
		boardUpdateCallbacks.remove(callback);
	}
}
