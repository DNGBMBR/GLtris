package network.lobby;

import game.pieces.PieceFactory;
import settings.GameSettings;

import java.util.*;

public class ClientLobby {
	ServerState state;
	Map<String, Player> players = new HashMap<>();
	GameSettings lobbySettings;

	Set<OnStartGame> startGameCallbacks = new HashSet<>();

	int countDown;

	boolean isStarted = false;

	public ClientLobby(List<Player> players, GameSettings lobbySettings, GameState state) {
		this.lobbySettings = lobbySettings;
		for (Player player : players) {
			this.players.put(player.name, player);
		}
		changeState(state);
	}

	public void changeState(GameState state) {
		switch(state) {
			case LOBBY -> {
				changeState(new LobbyState());
			}
			case IN_GAME -> {
				changeState(new InGameState());
			}
		}
	}

	public void changeState(GameState lobby, String winningPlayer) {
		changeState(new LobbyState(winningPlayer));
	}

	public void changeState(ServerState newState) {
		if (this.state != null) {
			this.state.onDestroy();
		}
		this.state = newState;
		this.state.onCreate();
	}

	public GameState getState() {
		return this.state.state;
	}

	public GameSettings getLobbySettings() {
		return lobbySettings;
	}

	public void setLobbySettings(GameSettings lobbySettings) {
		this.lobbySettings = lobbySettings;
	}

	public PieceFactory getKickTable() {
		return lobbySettings.getKickTable();
	}

	public boolean addPlayer(String name) {
		if (players.containsKey(name)) {
			return false;
		}
		players.put(name, new Player(name));
		return true;
	}

	public void removePlayer(String name) {
		players.remove(name);
	}

	public boolean hasPlayer(String name) {
		return players.containsKey(name);
	}

	public Player getPlayer(String name) {
		return players.get(name);
	}

	public List<Player> getPlayerList() {
		return players.values().stream().toList();
	}

	public boolean isStarted() {
		return isStarted;
	}

	public void setStarted(boolean started) {
		isStarted = started;
		if (started) {
			for (OnStartGame callback : startGameCallbacks) {
				callback.onStartGame();
			}
		}
	}

	public int getCountDown() {
		return countDown;
	}

	public void setCountDown(int countDown) {
		this.countDown = countDown;
	}

	public void registerOnGameStart(OnStartGame callback) {
		startGameCallbacks.add(callback);
	}

	public void unregisterOnGameStart(OnStartGame callback) {
		startGameCallbacks.remove(callback);
	}

	public void clearPlayers() {
		this.players.clear();
	}

	protected static class LobbyState extends ServerState {
		LobbyState() {
			//go directly to the lobby
		}

		LobbyState(String winningPlayer) {
			//display winning player before going back to lobby
		}

		@Override
		void onDestroy() {

		}

		@Override
		void onCreate() {

		}
	}

	protected static class InGameState extends ServerState {
		@Override
		void onDestroy() {

		}

		@Override
		void onCreate() {

		}
	}
}
