package network.lobby;

import fr.slaynash.communication.rudp.RUDPClient;
import game.Garbage;
import game.pieces.util.TileState;
import network.general.*;
import settings.GameSettings;
import settings.LobbySettings;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

public class GameClient extends RUDPClient {
	//TODO: handle server going down when it doesn't send a disconnect message (send client back to connection menu/main menu)
	String username;
	ClientLobby lobby;

	Set<OnPrepareGame> prepareCallbacks = new HashSet<>();
	Set<OnGarbageReceived> garbageReceivedCallbacks = new HashSet<>();
	Set<OnGameFinish> finishCallbacks = new HashSet<>();
	Set<OnLobbyUpdate> lobbyUpdateCallbacks = new HashSet<>();
	Set<OnBoardUpdate> boardUpdateCallbacks = new HashSet<>();

	public GameClient(InetAddress dstAddress, int dstPort, String username) throws IOException {
		super(dstAddress, dstPort);
		this.username = username;

		setPacketHandler(ClientHandler.class);

		this.lobby = new ClientLobby(new ArrayList<>(), new LobbySettings(), GameState.LOBBY);
	}

	public void setAddress(InetAddress address, int port, String username) {
		super.setAddress(address);
		super.setPort(port);
		this.username = username;
	}

	public boolean start() {
		try {
			this.connect();
		} catch (IOException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void close() {
		this.disconnect();
	}

	public String getUsername() {
		return username;
	}

	public void sendUsername() {
		ClientConnectMessage message = new ClientConnectMessage(this.username);
		this.sendReliablePacket(message.serialize());
	}

	public void sendReadyState(boolean isSpectating, boolean isReady) {
		ClientReadyMessage message = new ClientReadyMessage(isSpectating, isReady);
		this.sendReliablePacket(message.serialize());
	}

	public void sendGarbage(List<Garbage> garbage) {
		ClientGarbageMessage message = new ClientGarbageMessage(username, garbage);
		this.sendReliablePacket(message.serialize());
	}

	public void sendGameOver() {
		ClientBoardMessage topOutMessage = new ClientBoardMessage(username, true, null, null, null);
		this.sendReliablePacket(topOutMessage.serialize());
	}

	public void sendBoardUpdate(boolean gameOver, TileState[][] board, String[] queue, String hold) {
		ClientBoardMessage message = new ClientBoardMessage(this.username, gameOver, board, queue, hold);
		this.sendPacket(message.serialize());
	}

	public GameSettings getLobbySettings() {
		return lobby.getLobbySettings();
	}

	public void registerOnGamePrepare(OnPrepareGame callback) {
		prepareCallbacks.add(callback);
	}

	public void unregisterOnGamePrepare(OnPrepareGame callback) {
		prepareCallbacks.remove(callback);
	}

	public void registerOnGameStart(OnStartGame callback) {
		lobby.registerOnGameStart(callback);
	}

	public void unregisterOnGameStart(OnStartGame callback) {
		lobby.unregisterOnGameStart(callback);
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
		List<Player> players = this.lobby.getPlayerList();
		for (OnLobbyUpdate callback : this.lobbyUpdateCallbacks) {
			callback.onLobbyUpdate(players);
		}
	}

	public void updatePlayer(String username, boolean isToppedOut, TileState[][] board, String[] queue, String hold) {
		Player player = lobby.getPlayer(username);
		if (player != null) {
			if (isToppedOut) {
				player.setAlive(false);
			}
			for (OnBoardUpdate callback : this.boardUpdateCallbacks) {
				callback.onBoardUpdate(username, isToppedOut, board, queue, hold);
			}
		}
	}

	public List<Player> getPlayers() {
		return lobby.getPlayerList();
	}
}

