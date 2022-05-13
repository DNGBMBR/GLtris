package game.callbacks;

import game.pieces.util.PieceName;
import game.pieces.util.Rotation;

public interface RotateCallback {
	void run(PieceName pieceName, Rotation rot, int kickIndex);
}
