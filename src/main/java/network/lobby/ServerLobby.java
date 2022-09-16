package network.lobby;

import game.pieces.PieceFactory;
import settings.LobbySettings;

import java.net.Socket;
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
	* 	- first to n points
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
	GameState state;
	Map<Socket, Player> players = new HashMap<>();
	LobbySettings lobbySettings;

	public ServerLobby(LobbySettings settings) {
		setLobbySettings(settings);
		state = new LobbyState();
	}

	public void changeState(network.lobby.LobbyState state) {
		GameState newState;
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

	public void changeState(GameState newState) {
		this.state.onDestroy();
		this.state = newState;
		this.state.onCreate();
	}

	public GameState getState() {
		return this.state;
	}

	public LobbySettings getLobbySettings() {
		return this.lobbySettings;
	}

	public void setLobbySettings(LobbySettings settings) {
		this.lobbySettings = settings;
	}

	public int getNumPreviews() {
		return lobbySettings.getNumPreviews();
	}

	public PieceFactory getKickTable() {
		return this.lobbySettings.getKickTable();
	}

	public boolean addPlayer(Socket socket, String name) {
		if (players.containsKey(socket)) {
			return false;
		}
		players.put(socket, new Player(name));
		return true;
	}

	public Player removePlayer(Socket socket) {
		return players.remove(socket);
	}

	public boolean hasPlayer(Socket socket) {
		return players.containsKey(socket);
	}

	public Player getPlayer(Socket socket) {
		return players.get(socket);
	}

	public Collection<Player> getPlayers() {
		return this.players.values();
	}

	public class LobbyState extends GameState {

		LobbyState() {
			this.state = network.lobby.LobbyState.LOBBY;
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

	public class InGameState extends GameState {

		InGameState() {
			this.state = network.lobby.LobbyState.IN_GAME;
		}

		@Override
		void onDestroy() {

		}

		@Override
		void onCreate() {
			for (Player player : players.values()) {
				if (!player.isSpectator()) {
					player.setAlive(true);
				}
			}
		}
	}
}
