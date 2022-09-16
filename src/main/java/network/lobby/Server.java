package network.lobby;

import network.general.*;
import network.general.message.*;
import network.lobby.ServerLobby.InGameState;
import org.joml.Random;
import org.json.simple.parser.ParseException;
import server_interface.ServerPanel;
import settings.LobbySettings;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class Server {

	ServerThread serverThread;
	ServerLobby lobby;

	int port = 2678;
	ServerPanel display;
	Random rng = new Random();

	public Server(int port) {
		this.serverThread = new ServerThread(port);
		this.port = port;
	}

	public Server(int port, ServerPanel display) {
		this.serverThread = new ServerThread(port);
		this.display = display;
		display.addCommandCallback((String s) -> {
			GameState state = lobby.getState();
			if (state instanceof ServerLobby.LobbyState) {
				switch(s) {
					case "start" -> {
						serverThread.startGame();
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
						//TODO: end game
					}
					default -> {
						display.log("Could not parse command: \"" + s + "\" in state: IN_GAME");
					}
				}
			}
		});

		try {
			lobby = new ServerLobby(new LobbySettings("./lobby_settings.ini"));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		serverThread.start();
		display.log("Started server.");
	}

	public void close(){
		serverThread.setShouldClose();
		System.out.println("Closing server");
		try {
			serverThread.serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (ServerConnection connection : serverThread.connections.values()) {
			connection.send.setShouldClose();
			connection.receive.setShouldClose();
		}
	}

	class ServerThread extends Thread{
		Map<Socket, ServerConnection> connections = new HashMap<>();
		Map<String, ServerConnection> namedConnections = new HashMap<>();
		ServerSocket serverSocket;
		boolean isRunning = true;

		OnConnect connectCallback;
		OnReceive receiveCallback;

		ServerThread(int port) {
			try {
				serverSocket = new ServerSocket(port);
			} catch (IOException e) {
				System.out.println("Unable to initialize at port " + port + ".");
				return;
			}

			this.connectCallback = (Socket receivedFrom, SenderThread returnAddress) -> {
				ServerLobbyStateMessage message = new ServerLobbyStateMessage(lobby.getLobbySettings(), LobbyState.LOBBY);
				returnAddress.sendMessage(new Message(message));
			};

			this.receiveCallback = (Socket receivedFrom, SenderThread returnAddress, Message message) -> {
				switch(message.getMessageType()) {
					case Message.MESSAGE_CLIENT_CONNECT -> {
						ClientConnectMessage clientMessage = new ClientConnectMessage(message);
						if (!lobby.hasPlayer(receivedFrom)) {
							//send rejection message to client and dc them
						}
						lobby.addPlayer(receivedFrom, clientMessage.username);
						namedConnections.put(clientMessage.username, connections.get(receivedFrom));

						log(clientMessage.username + " has connected.");
						updateUsers();
						//notify other clients that this one is now connected
					}
					case Message.MESSAGE_CLIENT_DISCONNECT -> {
						disconnectPlayer(receivedFrom, true);
						//notify all clients this one dc'ed
						//may need to do additional things based on lobby state
					}
					case Message.MESSAGE_CLIENT_READY -> {
						ClientReadyMessage clientMessage = new ClientReadyMessage(message);
						Player player = lobby.getPlayer(receivedFrom);
						player.setSpectator(clientMessage.isSpectating);
						player.setReady(clientMessage.isReady);
						updateUsers();
						//notify other players that this player is ready/not ready
					}
					case Message.MESSAGE_CLIENT_CONFIRM_START -> {
						//have list of players who are truly ready
						//when all players are truly ready, begin countdown to game start
						connections.get(receivedFrom).isPrepared = true;
					}
					case Message.MESSAGE_CLIENT_GARBAGE -> {
						ClientGarbageMessage clientMessage = new ClientGarbageMessage(message);
						ServerGarbageMessage redirectGarbage = new ServerGarbageMessage(clientMessage.sender, clientMessage.garbage);
						Collection<Player> players = lobby.getPlayers();
						List<Player> livingPlayers = players.stream().filter((Player player) -> {
							return player.isAlive();
						}).collect(Collectors.toList());
						if (livingPlayers.size() <= 1) {
							endGame(livingPlayers);
							return;
						}
						Player[] playerList = new Player[livingPlayers.size()];
						livingPlayers.toArray(playerList);
						for (int i = 0; i < playerList.length; i++) {
							if (playerList[i].getName().equals(clientMessage.sender)) {
								Player temp = playerList[playerList.length - 1];
								playerList[playerList.length - 1] = playerList[i];
								playerList[i] = temp;
							}
						}
						int index = rng.nextInt(playerList.length - 1);
						sendMessage(playerList[index].getName(), redirectGarbage);
						//redirect garbage to a client
					}
					case Message.MESSAGE_CLIENT_TOP_OUT -> {
						if (lobby.getState() instanceof InGameState) {
							Player deadPlayer = lobby.getPlayer(receivedFrom);
							deadPlayer.setAlive(false);

							ServerUpdatePlayerMessage playerDiedMessage = new ServerUpdatePlayerMessage(deadPlayer.getName(), false, null);
							sendAll(playerDiedMessage);
							List<Player> livingPlayers = new ArrayList<>();
							for (Player player : lobby.getPlayers()) {
								if (player.isAlive()) {
									livingPlayers.add(player);
								}
							}
							if (livingPlayers.size() <= 1) {
								endGame(livingPlayers);
							}
						}
					}
					case Message.MESSAGE_CLIENT_BOARD -> {
						ClientBoardMessage clientMessage = new ClientBoardMessage(lobby.getKickTable(), lobby.getNumPreviews(), message);
						//update this player's board state for everyone (send client information as well)
						//can probably redirect a lot of byte data directly instead of having to deserialize it then re-serialize later
					}
				}
			};
		}

		@Override
		public void run() {
			while (!serverSocket.isClosed() && isRunning) {
				Socket newSocket;
				SenderThread senderThread;
				ListenerThread listenThread;
				try {
					newSocket = serverSocket.accept();
					UnexpectedCloseCallback callback = (Socket socket) -> {
						try {
							socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					};
					senderThread = new SenderThread(newSocket);
					listenThread = new ListenerThread(senderThread, newSocket, receiveCallback);

					senderThread.registerUnexpectedCloseCallback(callback);
					listenThread.registerUnexpectedCloseCallback(callback);

					senderThread.start();
					listenThread.start();
					connections.put(newSocket, new ServerConnection(newSocket, senderThread, listenThread));
					connectCallback.onConnect(newSocket, senderThread);
				} catch (SocketException e) {
					//should be closing server
					//this is a terrible solution
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		public void disconnectPlayer(Socket receivedFrom, boolean isGraceful) {
			Player removedPlayer = lobby.removePlayer(receivedFrom);
			ServerConnection connection = connections.remove(receivedFrom);
			namedConnections.remove(removedPlayer.getName());
			connection.send.setShouldClose();
			connection.receive.setShouldClose();
			try {
				receivedFrom.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (isGraceful) {
				log(removedPlayer.getName() + " has logged out.");
			}
			else {
				log(removedPlayer.getName() + " has disconnected.");
			}
			updateUsers();
		}

		public void sendMessage(String name, Message message) {
			SenderThread send = namedConnections.get(name).send;
			send.sendMessage(new Message(message));
		}

		public void sendMessage(Socket socket, Message message) {
			SenderThread send = connections.get(socket).send;
			send.sendMessage(new Message(message));
		}

		public void sendAll(Message message) {
			for (ServerConnection connection : connections.values()) {
				sendMessage(connection.socket, message);
			}
		}

		public void startGame() {
			if (lobby.getPlayers().size() <= 0) {
				log("Could not start game; there are no players in the lobby.");
				return;
			}
			for (Player player : lobby.getPlayers()) {
				if (!player.isSpectator() && !player.isReady()) {
					log("Could not start game; one or more players are not ready.");
					return;
				}
			}

			lobby.changeState(LobbyState.IN_GAME);

			ServerCountdownMessage countdownMessage = new ServerCountdownMessage(ServerCountdownMessage.TELL_EVERYONE_TO_PREPARE);
			sendAll(countdownMessage);

			boolean isAllPrepped = false;
			while (!isAllPrepped) {
				isAllPrepped = true;
				for (ServerConnection connection : connections.values()) {
					if (!connection.isPrepared) {
						isAllPrepped = false;
						break;
					}
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
			lobby.changeState(LobbyState.LOBBY);
			String winningPlayer = livingPlayers.size() == 1 ? livingPlayers.get(0).getName() : "Everyone died lmao this shouldn't happen";
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

		public void setShouldClose() {
			this.isRunning = false;
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
	}
}
