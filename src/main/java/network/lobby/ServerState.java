package network.lobby;

public abstract class ServerState {
	public GameState state;

	abstract void onDestroy();
	abstract void onCreate();
}
