package network.lobby;

import game.Garbage;
import game.pieces.PieceBuilder;
import game.pieces.util.*;
import network.Client;
import network.ClientHandler;
import network.general.*;
import settings.GameSettings;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.*;

public class GameClient extends Client {
	//TODO: handle server going down when it doesn't send a disconnect message (send client back to connection menu/main menu)
	String username;

	GameState state;
	Map<String, Player> players = new HashMap<>();
	GameSettings lobbySettings;

	Set<OnStartGame> startGameCallbacks = new HashSet<>();
	Set<OnPrepareGame> prepareCallbacks = new HashSet<>();
	Set<OnGarbageReceived> garbageReceivedCallbacks = new HashSet<>();
	Set<OnGameFinish> finishCallbacks = new HashSet<>();
	Set<OnLobbyUpdate> lobbyUpdateCallbacks = new HashSet<>();
	Set<OnBoardUpdate> boardUpdateCallbacks = new HashSet<>();

	public GameClient(InetAddress dstAddress, int dstPort, String username) {
		super(dstAddress, dstPort);
		this.setHandler(new GLClientHandler(this));
		this.username = username;

		this.state = GameState.LOBBY;
		//this.lobbySettings = new GameSettings();
	}

	public void setAddress(InetAddress address, int port, String username) {
		super.setAddress(address, port);
		this.username = username;
	}

	public boolean start() {
		try {
			this.connect();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public String getUsername() {
		return username;
	}

	public void sendUsername() {
		ClientConnectMessage message = new ClientConnectMessage(this.username);
		sendReliable(message.serialize());
	}

	public void sendReadyState(boolean isSpectating, boolean isReady) {
		ClientReadyMessage message = new ClientReadyMessage(isSpectating, isReady);
		sendReliable(message.serialize());
	}

	public void sendGarbage(List<Garbage> garbage) {
		ClientGarbageMessage message = new ClientGarbageMessage(username, garbage);
		sendReliable(message.serialize());
	}

	public void sendGameOver() {
		ClientBoardMessage topOutMessage = new ClientBoardMessage(username, true, null, null, null, null, null);
		sendReliable(topOutMessage.serialize());
	}

	public void sendBoardUpdate(boolean gameOver, String hold, String[] queue, Piece currentPiece, int[] garbageQueue, TileState[][] board) {
		ClientBoardMessage message = new ClientBoardMessage(this.username, gameOver, hold, queue, currentPiece, garbageQueue, board);
		sendRaw(message.serialize());
	}

	public GameSettings getLobbySettings() {
		return lobbySettings;
	}

	public void setLobbySettings(GameSettings lobbySettings) {
		this.lobbySettings = lobbySettings;
	}

	public void registerOnGamePrepare(OnPrepareGame callback) {
		prepareCallbacks.add(callback);
	}

	public void unregisterOnGamePrepare(OnPrepareGame callback) {
		prepareCallbacks.remove(callback);
	}

	public void registerOnGameStart(OnStartGame callback) {
		startGameCallbacks.add(callback);
	}

	public void unregisterOnGameStart(OnStartGame callback) {
		startGameCallbacks.remove(callback);
	}

	public void registerOnGarbageReceived(OnGarbageReceived callback) {
		garbageReceivedCallbacks.add(callback);
	}

	public void registerOnLobbyUpdate(OnLobbyUpdate callback) {
		lobbyUpdateCallbacks.add(callback);
	}

	public void unregisterOnGarbageReceived(OnGarbageReceived callback) {
		garbageReceivedCallbacks.remove(callback);
	}

	public void registerOnGameFinish(OnGameFinish callback) {
		finishCallbacks.add(callback);
	}

	public void unregisterOnGameFinish(OnGameFinish callback) {
		finishCallbacks.remove(callback);
	}

	public void unregisterOnLobbyUpdate(OnLobbyUpdate callback) {
		lobbyUpdateCallbacks.remove(callback);
	}

	public void registerOnBoardUpdate(OnBoardUpdate callback) {
		boardUpdateCallbacks.add(callback);
	}

	public void unregisterOnBoardUpdate(OnBoardUpdate callback) {
		boardUpdateCallbacks.remove(callback);
	}

	public void triggerLobbyUpdate() {
		List<Player> players = getPlayerList();
		for (OnLobbyUpdate callback : this.lobbyUpdateCallbacks) {
			callback.onLobbyUpdate(players);
		}
	}

	public void updatePlayer(String username, boolean isToppedOut, String hold, String[] queue,
							 int pieceX, int pieceY, Orientation orientation, String pieceName,
							 int[] garbageQueue, TileState[][] board) {
		Player player = this.getPlayer(username);
		if (player != null) {
			if (isToppedOut) {
				player.setAlive(false);
			}
		}
		PieceBuilder pieceBuilder = this.lobbySettings.getKickTable().getBuilder(pieceName);
		if (pieceBuilder != null) {
			PieceColour colour = pieceBuilder.getPieceColour();
			boolean[][] tileMap = null;
			switch(orientation) {
				case E -> {
					tileMap = pieceBuilder.getTileMapE();
				}
				case R -> {
					tileMap = pieceBuilder.getTileMapR();
				}
				case R2 -> {
					tileMap = pieceBuilder.getTileMapR2();
				}
				case R3 -> {
					tileMap = pieceBuilder.getTileMapR3();
				}
			}
			for (OnBoardUpdate callback : this.boardUpdateCallbacks) {
				callback.onBoardUpdate(
					username, isToppedOut, hold, queue,
					pieceX, pieceY, tileMap, colour,
					garbageQueue, board);
			}
		}
	}

	public boolean addPlayer(String name) {
		if (players.containsKey(name)) {
			return false;
		}
		players.put(name, new Player(name));

		return true;
	}

	public Player getPlayer(String name) {
		return players.get(name);
	}

	public void removePlayer(String name) {
		players.remove(name);
	}

	public void clearPlayers() {
		this.players.clear();
	}

	public List<Player> getPlayerList() {
		return players.values().stream().toList();
	}

	public void changeState(GameState state) {
		this.state = state;
	}

	public void changeState(GameState state, String winningPlayer) {
		changeState(state);
		for (OnGameFinish callback : finishCallbacks) {
			callback.onGameFinish(winningPlayer);
		}
	}

	public void callStartGame() {
		for (OnStartGame callback : this.startGameCallbacks) {
			callback.onStartGame();
		}
	}
}

class GLClientHandler extends ClientHandler {
	GameClient gameClient;

	public GLClientHandler(GameClient gameClient) {
		this.gameClient = gameClient;
	}

	@Override
	public void onConnect(SocketAddress socketAddress) {

	}

	@Override
	public void onDisconnect(SocketAddress socketAddress) {

	}

	@Override
	public void onReceive(SocketAddress socketAddress, byte[] bytes) {
		if (bytes[0] == MessageConstants.SERVER) {
			switch (bytes[1]) {
				case MessageConstants.MESSAGE_SERVER_LOBBY_STATE -> {
					ServerLobbyStateMessage msg = new ServerLobbyStateMessage(bytes);
					this.gameClient.setLobbySettings(msg.settings);
					for (Player player : msg.players) {
						this.gameClient.addPlayer(player.getName());
						Player currentPlayer = this.gameClient.getPlayer(player.getName());
						currentPlayer.setReady(player.isReady());
						currentPlayer.setSpectator(player.isSpectator());
					}
					for (OnLobbyUpdate callback : this.gameClient.lobbyUpdateCallbacks) {
						callback.onLobbyUpdate(msg.players);
					}
				}
				case MessageConstants.MESSAGE_SERVER_LOBBY_UPDATE_PLAYER -> {
					ServerLobbyPlayerUpdateMessage msg = new ServerLobbyPlayerUpdateMessage(bytes);
					if (this.gameClient.getPlayer(msg.username) == null) {
						this.gameClient.addPlayer(msg.username);
					}
					else if (msg.isDisconnected) {
						this.gameClient.removePlayer(msg.username);
					}
					else {
						Player player = this.gameClient.getPlayer(msg.username);
						player.setSpectator(msg.isSpectating);
						player.setReady(msg.isReady);
					}
					List<Player> players = this.gameClient.getPlayerList();
					for (OnLobbyUpdate callback : this.gameClient.lobbyUpdateCallbacks) {
						callback.onLobbyUpdate(players);
					}
				}
				case MessageConstants.MESSAGE_SERVER_COUNTDOWN -> {
					ServerCountdownMessage msg = new ServerCountdownMessage(bytes);
					if (msg.state == ServerCountdownMessage.TELL_EVERYONE_TO_PREPARE) {
						for (Player player : this.gameClient.players.values()) {
							player.setReady(false);
						}
						this.gameClient.changeState(GameState.IN_GAME);
						for (OnPrepareGame callback : this.gameClient.prepareCallbacks) {
							callback.onPrepareGame();
						}
						ClientConfirmStartMessage prepMsg = new ClientConfirmStartMessage();
						sendReliable(prepMsg.serialize());
					}
					if (msg.state == ServerCountdownMessage.START) {
						this.gameClient.callStartGame();
					}
				}
				case MessageConstants.MESSAGE_SERVER_GARBAGE -> {
					ServerGarbageMessage msg = new ServerGarbageMessage(bytes);
					for (OnGarbageReceived callback : this.gameClient.garbageReceivedCallbacks) {
						callback.onGarbageReceived(msg.garbage);
					}
				}
				case MessageConstants.MESSAGE_SERVER_GAME_END -> {
					ServerGameEndMessage msg = new ServerGameEndMessage(bytes);
					this.gameClient.changeState(GameState.LOBBY, msg.winningPlayer);
					for (OnGameFinish callback : this.gameClient.finishCallbacks) {
						callback.onGameFinish(msg.winningPlayer);
					}
				}
				case MessageConstants.MESSAGE_SERVER_BOARD -> {
					ServerBoardMessage msg = new ServerBoardMessage(bytes);
					this.gameClient.updatePlayer(
						msg.username, msg.isToppedOut, msg.hold, msg.queue,
						msg.pieceX, msg.pieceY, msg.pieceOrientation, msg.pieceName,
						msg.garbageQueue, msg.board);
				}
			}
		}
	}
}