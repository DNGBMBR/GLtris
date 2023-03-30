package network.lobby;

import game.pieces.util.TileState;

public interface OnBoardUpdate {
	void onBoardUpdate(String username, boolean isToppedOut, TileState[][] board, String[] queue, String hold);
}
