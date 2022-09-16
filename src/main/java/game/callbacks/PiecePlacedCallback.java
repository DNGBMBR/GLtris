package game.callbacks;

import game.SpinType;

public interface PiecePlacedCallback {
	void run(int rowsCleared, SpinType spinType);
}
