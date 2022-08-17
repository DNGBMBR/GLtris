package game.callbacks;

import game.pieces.util.PieceColour;
import game.pieces.util.Rotation;

public interface RotateCallback {
	void run(PieceColour pieceName, Rotation rot, int kickIndex);
}
