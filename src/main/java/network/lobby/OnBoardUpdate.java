package network.lobby;

import game.pieces.util.PieceColour;
import game.pieces.util.TileState;

public interface OnBoardUpdate {
	void onBoardUpdate(String username, boolean isToppedOut, String hold, String[] queue, int pieceX, int pieceY, boolean[][] tileMap, PieceColour pieceColour, int[] garbageQueue, TileState[][] board);
}
