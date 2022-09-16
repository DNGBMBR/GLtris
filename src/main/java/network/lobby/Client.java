package network.lobby;

import game.Garbage;
import network.general.*;
import network.general.message.*;
import settings.LobbySettings;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class Client {
	String username;
	ClientLobby lobby;
	OnReceive onReceive;
	OnConnect onConnect;

	Socket socket;
	InetAddress dstAddress;
	int dstPort;

	ListenerThread listenThread;
	SenderThread senderThread;

	Set<OnPrepareGame> prepareCallbacks = new HashSet<>();
	Set<OnGarbageReceived> garbageReceivedCallbacks = new HashSet<>();
	Set<OnGameFinish> finishCallbacks = new HashSet<>();

	public Client(InetAddress dstAddress, int dstPort, String username) {
		this.dstAddress = dstAddress;
		this.dstPort = dstPort;
		this.username = username;

		this.lobby = new ClientLobby(new ArrayList<>(), new LobbySettings(), LobbyState.LOBBY);

		this.onConnect = (Socket receivedFrom, SenderThread returnAddress) -> {
			Message message = new ClientConnectMessage(username);
			returnAddress.sendMessage(message);
		};

		this.onReceive = (Socket receivedFrom, SenderThread returnAddress, Message message) -> {
			switch(message.messageType) {
				case Message.MESSAGE_SERVER_CONNECT -> {
					ServerConnectMessage serverMessage = new ServerConnectMessage(message);
					for (String username1 : serverMessage.usernames) {
						lobby.addPlayer(username1);
					}
				}
				case Message.MESSAGE_SERVER_DISCONNECT -> {
					ServerDisconnectMessage serverMessage = new ServerDisconnectMessage(message);
					lobby.removePlayer(serverMessage.username);
					if (serverMessage.username.equals(username)) {
						close();
					}
				}
				case Message.MESSAGE_SERVER_LOBBY_STATE -> {
					ServerLobbyStateMessage serverMessage = new ServerLobbyStateMessage(message);
					lobby.setLobbySettings(serverMessage.settings);
					if (serverMessage.state != lobby.getState()) {
						lobby.changeState(serverMessage.state);
					}
				}
				case Message.MESSAGE_SERVER_UPDATE_PLAYER -> {
					ServerUpdatePlayerMessage serverMessage = new ServerUpdatePlayerMessage(message);
					if (lobby.getState() == serverMessage.updateType) {
						Player player = lobby.getPlayer(serverMessage.playerName);
						switch (serverMessage.updateType) {
							case LOBBY -> {
								player.setSpectator(serverMessage.isSpectating);
								player.setReady(serverMessage.isReady);
							}
							case IN_GAME -> {
								player.setAlive(serverMessage.isAlive);
								if (serverMessage.isAlive) {
									player.setBoard(serverMessage.board);
								}
							}
						}
					}
				}
				case Message.MESSAGE_SERVER_COUNTDOWN -> {
					ServerCountdownMessage serverMessage = new ServerCountdownMessage(message);
					if (serverMessage.state == ServerCountdownMessage.TELL_EVERYONE_TO_PREPARE) {
						for (OnPrepareGame callback : prepareCallbacks) {
							callback.onPrepareGame();
						}
						senderThread.sendMessage(new ClientConfirmStartMessage());
					}
					lobby.setCountDown(serverMessage.state);

					if (serverMessage.state == ServerCountdownMessage.START) {
						lobby.setStarted(true);
						for (OnStartGame callback : lobby.startGameCallbacks) {
							callback.onStartGame();
						}
					}
				}
				case Message.MESSAGE_SERVER_GARBAGE -> {
					ServerGarbageMessage serverMessage = new ServerGarbageMessage(message);
					for (OnGarbageReceived callback : garbageReceivedCallbacks) {
						callback.onGarbageReceived(serverMessage.garbage);
					}
				}
				case Message.MESSAGE_SERVER_GAME_END -> {
					ServerGameEndMessage serverMessage = new ServerGameEndMessage(message);
					lobby.changeState(LobbyState.LOBBY, serverMessage.winningPlayer);
					for (OnGameFinish callback : finishCallbacks) {
						callback.onGameFinish(serverMessage.winningPlayer);
					}
				}
			}
		};
	}

	public void setAddress(InetAddress address, int port, String username) {
		this.dstAddress = address;
		this.dstPort = port;
		this.username = username;
		this.onConnect = (Socket receivedFrom, SenderThread returnAddress) -> {
			Message message = new ClientConnectMessage(username);
			returnAddress.sendMessage(message);
		};
	}

	public boolean start() {
		try {
			socket = new Socket(dstAddress, dstPort);
			senderThread = new SenderThread(socket);
			senderThread.start();
			listenThread = new ListenerThread(senderThread, socket, onReceive);
			listenThread.start();
			onConnect.onConnect(socket, senderThread);
		} catch (ConnectException e) {
			System.out.println("Unable to connect to host " + dstAddress + " at port " + dstPort + ".");
			return false;
		} catch (IOException e) {
			//TODO: handle unable to connect gracefully
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void close() {
		senderThread.sendMessage(new Message(new ClientDisconnectMessage("Disconnecting")));
		senderThread.setShouldClose();
		listenThread.setShouldClose();
	}

	public void sendReadyState(boolean isSpectating, boolean isReady) {
		ClientReadyMessage message = new ClientReadyMessage(isSpectating, isReady);
		senderThread.sendMessage(new Message(message));
	}

	public void sendGarbage(List<Garbage> garbage) {
		ClientGarbageMessage garbageMessage = new ClientGarbageMessage(username, garbage);
		senderThread.sendMessage(new Message(garbageMessage));
	}

	public void sendGameOver() {
		ClientTopOutMessage topOutMessage = new ClientTopOutMessage();
		senderThread.sendMessage(new Message(topOutMessage));
	}

	public LobbySettings getLobbySettings() {
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

	public void unregisterOnGarbageReceived(OnGarbageReceived callback) {
		garbageReceivedCallbacks.remove(callback);
	}

	public void registerOnGameFinish(OnGameFinish callback) {
		finishCallbacks.add(callback);
	}

	public void unregisterOnGameFinish(OnGameFinish callback) {
		finishCallbacks.remove(callback);
	}
}
