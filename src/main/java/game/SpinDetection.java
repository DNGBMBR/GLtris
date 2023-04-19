package game;

import game.pieces.util.Piece;
import game.pieces.util.TileState;

public interface SpinDetection {
	SpinType detectSpin(Piece piece, TileState[][] board, int kickIndex);
}
