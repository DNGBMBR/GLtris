package network.lobby;

import game.Garbage;

import java.util.List;

public interface OnGarbageReceived {
	void onGarbageReceived(List<Garbage> garbage);
}
