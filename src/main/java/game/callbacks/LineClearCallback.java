package game.callbacks;

import game.SpinType;

public interface LineClearCallback {
	void run(int rowsCleared, SpinType spinType);
}
