package game;

import game.callbacks.BoardUpdateCallback;
import game.pieces.util.*;

import java.util.*;

public abstract class GLTrisRender {
	protected Set<BoardUpdateCallback> boardUpdateCallbacks = Collections.synchronizedSet(new HashSet<>());

	abstract int getBoardWidth();
	abstract int getBoardHeight();
	abstract TileState[][] getBoard();
	abstract int getPieceX();
	abstract int getPieceY();
	abstract boolean[][] getTileMap();
	abstract PieceColour getPieceColour();
	abstract String getHeldPiece();
	abstract String[] getPieceQueue();
	abstract int getNumPreviews();
	abstract int[] getGarbageQueue();

	public void registerOnBoardUpdate(BoardUpdateCallback callback) {
		boardUpdateCallbacks.add(callback);
		callback.onBoardUpdate();
	}

	public void unregisterOnBoardUpdate(BoardUpdateCallback callback) {
		boardUpdateCallbacks.remove(callback);
	}
}
