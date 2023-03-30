package network.lobby;

import fr.slaynash.communication.handlers.PacketHandler;
import fr.slaynash.communication.rudp.RUDPClient;
import network.general.*;

import java.util.*;

public class ServerHandler extends PacketHandler {
	String username;
	GameServer gameServer;

	public ServerHandler() {
	}

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
		for (int i = 0; i < bytes.length; i++) {
			System.out.printf("%2x ", bytes[i]);
			if (i % 16 == 7) {
				System.out.print("| ");
			}
			if (i % 16 == 15) {
				System.out.println();
			}
		}
		System.out.println();
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
						this.rudp.sendReliablePacket(response.serialize());
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
					//TODO: make other player's boards visible
					ClientBoardMessage msg = new ClientBoardMessage(bytes);
					if (msg.isToppedOut) {
						this.gameServer.lobby.getPlayer(username).setAlive(!msg.isToppedOut);
						List<Player> livingPlayers = getLivingPlayers();
						if (livingPlayers.size() <= 1) {
							gameServer.endGame(livingPlayers);
						}
					}
					ServerBoardMessage boardMessage = new ServerBoardMessage(msg.username, msg.isToppedOut, msg.board, msg.queue, msg.hold);
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
}
