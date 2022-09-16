package network.lobby;

public abstract class GameState {
	public LobbyState state;

	abstract void onDestroy();
	abstract void onCreate();
}
