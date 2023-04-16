package network.lobby;

import game.pieces.PieceFactory;
import settings.GameSettings;

import java.util.*;

public class ServerLobby {
	/*
	* store:
	* - lobby state (lobby, playing, post-game)
	* LOBBY
	* 	NECESSARY
	* 	- list of connected players (names?)
	* 	- kick table (load in at start of game)
	* 	OPTIONAL
	* 	- number of wins per player in lobby
	* GAME
	* 	NECESSARY
	* 	- countdown, buffer at start of game (make sure everyone's connected)
	* 	- list of players that are alive/dead/spectating
	* 	- way to send garbage to other players (do pure random per garbage package for now)
	* 	- display other player's boards
	* 	- spectate (after death or at start)
	* 	OPTIONAL
	* 	- best of n points
	* POSTGAME (just lobby again)
	*	NECESSARY
	* 	- display winning player
	* 	OPTIONAL
	* 	- stats?
	* */
	ServerState state;
	Map<String, Player> players = new HashMap<>();
	GameSettings gameSettings;

	public ServerLobby(GameSettings settings) {
		this.gameSettings = settings;
		state = new LobbyState();
	}

	public void changeState(GameState state) {
		ServerState newState;
		switch(state) {
			case LOBBY -> {
				newState = new LobbyState();
			}
			case IN_GAME -> {
				newState = new InGameState();
			}
			default -> {
				throw new IllegalArgumentException();
			}
		}
		changeState(newState);
	}

	public void changeState(ServerState newState) {
		this.state.onDestroy();
		this.state = newState;
		this.state.onCreate();
	}

	public ServerState getState() {
		return this.state;
	}

	public GameSettings getLobbySettings() {
		return this.gameSettings;
	}

	public void setLobbySettings(GameSettings settings) {
		this.gameSettings = settings;
	}

	public int getNumPreviews() {
		return gameSettings.getNumPreviews();
	}

	public PieceFactory getKickTable() {
		return this.gameSettings.getKickTable();
	}

	public boolean addPlayer(String name) {
		if (players.containsKey(name)) {
			return false;
		}
		players.put(name, new Player(name));
		return true;
	}

	public Player removePlayer(String name) {
		return players.remove(name);
	}

	public boolean hasPlayer(String name) {
		return players.containsKey(name);
	}

	public Player getPlayer(String name) {
		return players.get(name);
	}

	public Collection<Player> getPlayers() {
		return this.players.values();
	}

	public class LobbyState extends ServerState {

		LobbyState() {
			this.state = GameState.LOBBY;
		}

		@Override
		void onDestroy() {

		}

		@Override
		void onCreate() {
			for (Player player : players.values()) {
				player.setReady(false);
			}
		}
	}

	public class InGameState extends ServerState {

		InGameState() {
			this.state = GameState.IN_GAME;
		}

		@Override
		void onDestroy() {

		}

		@Override
		void onCreate() {
			
		}
	}
}
