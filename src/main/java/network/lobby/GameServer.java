package network.lobby;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import network.Server;
import network.ServerHandler;
import network.general.*;
import org.joml.Random;
import server_interface.ServerPanel;
import settings.*;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.*;

public class GameServer extends Server {
	ServerLobby lobby;
	ServerPanel display;
	Random rng = new Random();
	BiMap<String, SocketAddress> clients = HashBiMap.create();

	public GameServer(ServerSettings settings, ServerPanel display) throws IOException {
		super(settings.getPort());
		this.setHandler(new GLServerHandler(this));
		this.display = display;
		display.addCommandCallback((String s) -> {
			ServerState state = lobby.getState();
			if (state instanceof ServerLobby.LobbyState) {
				switch(s) {
					case "start" -> {
						startGame();
					}
					default -> {
						display.log("Could not parse command: \"" + s + "\" in state: LOBBY");
					}
				}
			}
			else if (state instanceof ServerLobby.InGameState) {
				switch(s) {
					case "end" -> {
						display.log("Ending game.");
						endGame(new ArrayList<>());
					}
					default -> {
						display.log("Could not parse command: \"" + s + "\" in state: IN_GAME");
					}
				}
			}
		});
		GameSettings gameSettings = new GameSettings(settings.getNumPreviews(), settings.getKickTable(), settings.getBoardHeight(), settings.getBoardWidth(), settings.getSpinDetector());
		lobby = new ServerLobby(gameSettings);
	}

	@Override
	public void start() throws IOException {
		super.start();
		this.display.log("Started server.");
	}

	public void startGame() {
		if (lobby.getPlayers().size() <= 0) {
			log("Could not start game; there are no players in the lobby.");
			return;
		}
		for (Player player : lobby.getPlayers()) {
			if (!(player.isSpectator() || player.isReady())) {
				log("Could not start game; one or more players are not ready.");
				return;
			}
		}

		log("Waiting for players to synchronize.");

		lobby.changeState(GameState.IN_GAME);

		ServerCountdownMessage countdownMessage = new ServerCountdownMessage(ServerCountdownMessage.TELL_EVERYONE_TO_PREPARE);
		sendAll(countdownMessage);

		boolean isAllPrepped = false;
		while (!isAllPrepped) {
			isAllPrepped = true;
			synchronized (lobby) {
				for (Player player : lobby.getPlayers()) {
					if (!player.isPrepared) {
						isAllPrepped = false;
						break;
					}
				}
			}
		}

		synchronized (lobby) {
			for (Player player : lobby.getPlayers()) {
				player.setPrepared(false);
				player.setAlive(!player.isSpectator());
			}
		}

		for (byte i = 3; i >= 0; i--) {
			Timer timer = new Timer();
			byte finalI = i;
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					ServerCountdownMessage countdown = new ServerCountdownMessage(finalI);
					sendAll(countdown);
					if (finalI == ServerCountdownMessage.START) {
						log("Game started.");
					}
					else {
						log("Starting game in: " + countdown.state);
					}
				}
			}, 1000L * (3 - i));
		}
	}

	public void endGame(List<Player> livingPlayers) {
		lobby.changeState(GameState.LOBBY);
		String winningPlayer = livingPlayers.size() == 1 ? livingPlayers.get(0).getName() : "Game aborted.";
		if (livingPlayers.size() == 1) {
			log(winningPlayer + " has won.");
		}
		else {
			log("Game ended with no winner.");
		}
		ServerGameEndMessage endMessage = new ServerGameEndMessage(winningPlayer);
		sendAll(endMessage);
		updateUsers();
	}

	public void log(String s) {
		if (display == null) {
			return;
		}
		display.log(s);
	}

	public void updateUsers() {
		if (display == null) {
			return;
		}
		display.updatePlayers(lobby.getPlayers());
	}

	void sendAll(MessageSerializer message) {
		byte[] data = message.serialize();
		super.sendAllReliable(data);
	}

	public List<Player> getLivingPlayers() {
		return lobby.getPlayers().stream()
			.filter((Player player) -> {
				return player.isAlive();
			})
			.toList();
	}
}

class GLServerHandler extends ServerHandler {
	GameServer gameServer;

	public GLServerHandler(GameServer gameServer) {
		this.gameServer = gameServer;
	}

	@Override
	public void onConnect(SocketAddress socketAddress) {

	}

	@Override
	public void onDisconnect(SocketAddress socketAddress) {
		String username = gameServer.clients.inverse().get(socketAddress);
		gameServer.lobby.removePlayer(username);
		gameServer.clients.inverse().remove(socketAddress);
		gameServer.log(username + " has disconnected.");
		gameServer.updateUsers();
		ServerLobbyPlayerUpdateMessage disconnectMessage = new ServerLobbyPlayerUpdateMessage(username, true, false, false);
		gameServer.sendAll(disconnectMessage);
	}

	@Override
	public void onReceive(SocketAddress socketAddress, byte[] bytes) {
		if (bytes[0] == MessageConstants.CLIENT) {
			switch (bytes[1]) {
				case MessageConstants.MESSAGE_CLIENT_CONNECT -> {
					ClientConnectMessage msg = new ClientConnectMessage(bytes);
					if (!gameServer.lobby.addPlayer(msg.username)) {
						disconnect(socketAddress);
						gameServer.log(msg.username + " is already connected.");
					}
					else {
						String username = msg.username;
						gameServer.clients.put(username, socketAddress);
						gameServer.log(msg.username + " has connected.");
						ServerLobbyStateMessage response = new ServerLobbyStateMessage(gameServer.lobby.getLobbySettings(), gameServer.lobby.getPlayers().stream().toList(), false);
						byte[] serializedResponse = response.serialize();
						gameServer.sendReliable(socketAddress, serializedResponse);
						ServerLobbyPlayerUpdateMessage updatePlayer = new ServerLobbyPlayerUpdateMessage(msg.username, false, false, false);
						gameServer.sendAll(updatePlayer);
					}
					gameServer.updateUsers();
				}
				case MessageConstants.MESSAGE_CLIENT_READY -> {
					ClientReadyMessage msg = new ClientReadyMessage(bytes);
					String username = gameServer.clients.inverse().get(socketAddress);
					Player player = gameServer.lobby.getPlayer(username);
					player.setReady(msg.isReady);
					player.setSpectator(msg.isSpectating);
					ServerLobbyPlayerUpdateMessage updatePlayer = new ServerLobbyPlayerUpdateMessage(username, false, msg.isReady, msg.isSpectating);
					gameServer.sendAll(updatePlayer);
					gameServer.log(username + " is " + (msg.isSpectating ? "spectating" : (msg.isReady ? "ready" : "not ready")) + ".");
					gameServer.updateUsers();

				}
				case MessageConstants.MESSAGE_CLIENT_CONFIRM_START -> {
					//ClientConfirmStartMessage msg = new ClientConfirmStartMessage(bytes);
					String username = gameServer.clients.inverse().get(socketAddress);
					gameServer.lobby.getPlayer(username).setPrepared(true);
					gameServer.log(username + " is prepared");
				}
				case MessageConstants.MESSAGE_CLIENT_GARBAGE -> {
					ClientGarbageMessage msg = new ClientGarbageMessage(bytes);
					//redirect garbage to a player that's not the sender
					List<Player> livingPlayers = gameServer.getLivingPlayers();
					if (livingPlayers.size() <= 1) {
						gameServer.endGame(livingPlayers);
						return;
					}
					Player[] playerList = new Player[livingPlayers.size()];
					livingPlayers.toArray(playerList);
					for (int i = 0; i < playerList.length; i++) {
						if (playerList[i].getName().equals(msg.username)) {
							Player temp = playerList[playerList.length - 1];
							playerList[playerList.length - 1] = playerList[i];
							playerList[i] = temp;
						}
					}
					int index = gameServer.rng.nextInt(playerList.length - 1);
					String destinationName = playerList[index].name;
					SocketAddress destination = gameServer.clients.get(destinationName);
					ServerGarbageMessage send = new ServerGarbageMessage(msg.garbage);
					sendReliable(destination, send.serialize());
				}
				case MessageConstants.MESSAGE_CLIENT_BOARD -> {
					ClientBoardMessage msg = new ClientBoardMessage(bytes);
					String username = gameServer.clients.inverse().get(socketAddress);
					if (msg.isToppedOut) {
						gameServer.lobby.getPlayer(username).setAlive(!msg.isToppedOut);
						List<Player> livingPlayers = gameServer.getLivingPlayers();
						if (livingPlayers.size() <= 1) {
							gameServer.endGame(livingPlayers);
						}
					}
					ServerBoardMessage boardMessage = new ServerBoardMessage(
						msg.username, msg.isToppedOut, msg.hold, msg.queue,
						msg.pieceX, msg.pieceY, msg.pieceOrientation, msg.pieceName,
						msg.garbageQueue, msg.board);
					gameServer.sendAll(boardMessage);
				}
			}

			gameServer.updateUsers();
		}
	}



	/*
	@Override
	public void onConnection() {

	}

	@Override
	public void onDisconnectedByLocal(String s) {
		onDisconnect(s);
	}

	@Override
	public void onDisconnectedByRemote(String s) {
		onDisconnect(s);
	}

	private void onDisconnect(String s) {
		gameServer.lobby.removePlayer(this.username);
		gameServer.clients.remove(this.username);
		gameServer.log(this.username + " has disconnected.");
		gameServer.updateUsers();
		ServerLobbyPlayerUpdateMessage disconnectMessage = new ServerLobbyPlayerUpdateMessage(this.username, true, false, false);
		gameServer.sendAll(disconnectMessage);
	}

	@Override
	public void onPacketReceived(byte[] bytes) {
		byte[] data = new byte[bytes.length - 3];
		System.arraycopy(bytes, 3, data, 0, data.length);
		decodePacket(data);
	}

	@Override
	public void onReliablePacketReceived(byte[] bytes) {
		byte[] data = new byte[bytes.length - 3];
		System.arraycopy(bytes, 3, data, 0, data.length);
		decodePacket(data);
	}

	private void decodePacket(byte[] bytes) {
		if (bytes[0] == MessageConstants.CLIENT) {
			switch (bytes[1]) {
				case MessageConstants.MESSAGE_CLIENT_CONNECT -> {
					ClientConnectMessage msg = new ClientConnectMessage(bytes);
					if (!gameServer.lobby.addPlayer(msg.username)) {
						this.rudp.disconnect(msg.username + " is already connected.");
						gameServer.log(msg.username + " is already connected.");
					}
					else {
						this.username = msg.username;
						gameServer.clients.put(this.username, this.rudp);
						gameServer.log(msg.username + " has connected.");
						ServerLobbyStateMessage response = new ServerLobbyStateMessage(gameServer.lobby.getLobbySettings(), gameServer.lobby.getPlayers().stream().toList(), false);
						byte[][] serializedResponse = response.serialize();
						for (int i = 0; i < serializedResponse.length; i++) {
							this.rudp.sendReliablePacket(serializedResponse[i]);
						}
						ServerLobbyPlayerUpdateMessage updatePlayer = new ServerLobbyPlayerUpdateMessage(msg.username, false, false, false);
						gameServer.sendAll(updatePlayer);
					}
					gameServer.updateUsers();
				}
				case MessageConstants.MESSAGE_CLIENT_READY -> {
					ClientReadyMessage msg = new ClientReadyMessage(bytes);
					Player player = gameServer.lobby.getPlayer(this.username);
					player.setReady(msg.isReady);
					player.setSpectator(msg.isSpectating);
					ServerLobbyPlayerUpdateMessage updatePlayer = new ServerLobbyPlayerUpdateMessage(this.username, false, msg.isReady, msg.isSpectating);
					gameServer.sendAll(updatePlayer);
					gameServer.log(this.username + " is " + (msg.isSpectating ? "spectating" : (msg.isReady ? "ready" : "not ready")) + ".");
					gameServer.updateUsers();

				}
				case MessageConstants.MESSAGE_CLIENT_CONFIRM_START -> {
					//ClientConfirmStartMessage msg = new ClientConfirmStartMessage(bytes);
					synchronized (gameServer.lobby) {
						gameServer.lobby.getPlayer(this.username).setPrepared(true);
					}
					gameServer.log(this.username + " is prepared");
				}
				case MessageConstants.MESSAGE_CLIENT_GARBAGE -> {
					ClientGarbageMessage msg = new ClientGarbageMessage(bytes);
					//redirect garbage to a player that's not the sender
					List<Player> livingPlayers = getLivingPlayers();
					if (livingPlayers.size() <= 1) {
						gameServer.endGame(livingPlayers);
						return;
					}
					Player[] playerList = new Player[livingPlayers.size()];
					livingPlayers.toArray(playerList);
					for (int i = 0; i < playerList.length; i++) {
						if (playerList[i].getName().equals(msg.username)) {
							Player temp = playerList[playerList.length - 1];
							playerList[playerList.length - 1] = playerList[i];
							playerList[i] = temp;
						}
					}
					int index = gameServer.rng.nextInt(playerList.length - 1);
					String destinationName = playerList[index].name;
					RUDPClient destination = gameServer.clients.get(destinationName);
					ServerGarbageMessage send = new ServerGarbageMessage(msg.garbage);
					destination.sendReliablePacket(send.serialize());
				}
				case MessageConstants.MESSAGE_CLIENT_BOARD -> {
					ClientBoardMessage msg = new ClientBoardMessage(bytes);
					if (msg.isToppedOut) {
						this.gameServer.lobby.getPlayer(username).setAlive(!msg.isToppedOut);
						List<Player> livingPlayers = getLivingPlayers();
						if (livingPlayers.size() <= 1) {
							gameServer.endGame(livingPlayers);
						}
					}
					ServerBoardMessage boardMessage = new ServerBoardMessage(
						msg.username, msg.isToppedOut, msg.hold, msg.queue,
						msg.pieceX, msg.pieceY, msg.pieceOrientation, msg.pieceName,
						msg.garbageQueue, msg.board);
					gameServer.sendAll(boardMessage);
				}
			}
		}
	}

	private List<Player> getLivingPlayers() {
		Collection<Player> players = gameServer.lobby.getPlayers();
		List<Player> livingPlayers = new ArrayList<>();
		for (Player player : players) {
			if (player.isAlive()) {
				livingPlayers.add(player);
			}
		}
		return livingPlayers;
	}

	@Override
	public void onRemoteStatsReturned(int sentRemote, int sentRemoteR, int receivedRemote, int receivedRemoteR) {
		gameServer.log("sentRemote: " + sentRemote + ", sentRemoteR: " + sentRemote + ", receivedRemote: " + receivedRemote + ", receivedRemoteR: " + receivedRemoteR);
	}
	*/
}
