package game.callbacks;

import game.pieces.util.Direction;

public interface MoveCallback {
	void run(Direction dir);
}
