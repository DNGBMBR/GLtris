package network.lobby;

import game.pieces.PieceFactory;
import settings.LobbySettings;

import java.util.*;

public class ClientLobby {
	GameState state;
	Map<String, Player> players = new HashMap<>();
	LobbySettings lobbySettings;

	Set<OnStartGame> startGameCallbacks = new HashSet<>();

	int countDown;

	boolean isStarted = false;

	public ClientLobby(List<Player> players, LobbySettings lobbySettings, network.lobby.LobbyState state) {
		this.lobbySettings = lobbySettings;
		for (Player player : players) {
			this.players.put(player.name, player);
		}
		changeState(state);
	}

	public void changeState(network.lobby.LobbyState state) {
		switch(state) {
			case LOBBY -> {
				changeState(new LobbyState());
			}
			case IN_GAME -> {
				changeState(new InGameState());
			}
		}
	}

	public void changeState(network.lobby.LobbyState lobby, String winningPlayer) {
		changeState(new LobbyState(winningPlayer));
	}

	public void changeState(GameState newState) {
		if (this.state != null) {
			this.state.onDestroy();
		}
		this.state = newState;
		this.state.onCreate();
	}

	public network.lobby.LobbyState getState() {
		return this.state.state;
	}

	public LobbySettings getLobbySettings() {
		return lobbySettings;
	}

	public void setLobbySettings(LobbySettings lobbySettings) {
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

	protected static class LobbyState extends GameState {
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

	protected static class InGameState extends GameState {
		@Override
		void onDestroy() {

		}

		@Override
		void onCreate() {

		}
	}
}
