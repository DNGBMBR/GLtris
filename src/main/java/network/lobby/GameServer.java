package network.lobby;

import fr.slaynash.communication.rudp.RUDPClient;
import fr.slaynash.communication.rudp.RUDPServer;
import network.general.*;
import org.joml.Random;
import org.json.simple.parser.ParseException;
import server_interface.ServerPanel;
import settings.LobbySettings;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

public class GameServer extends RUDPServer implements AutoCloseable{
	ServerLobby lobby;

	int port = 2678;
	ServerPanel display;
	Random rng = new Random();
	Map<String, RUDPClient> clients = new HashMap<>();

	public GameServer(int port) throws IOException {
		super(port);
		this.setPacketHandler(ServerHandler.class);
		this.port = port;
	}

	public GameServer(int port, ServerPanel display) throws IOException {
		super(port);
		this.port = port;
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

	@Override
	public void start() {
		super.start();
		this.display.log("Started server.");
	}

	@Override
	public void stop(){
		super.stop();
	}

	@Override
	protected void handlePacket(byte[] bytes, InetAddress inetAddress, int i) {
		//this is a really jank workaround for a less than ideal interface
		super.handlePacket(bytes, inetAddress, i);
		for (RUDPClient client : getConnectedClients()) {
			((ServerHandler) (client.getPacketHandler())).gameServer = this;
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
		for (RUDPClient client : GameServer.this.getConnectedClients()) {
			client.sendReliablePacket(message.serialize());
		}
	}

	@Override
	public void close() {
		this.stop();
	}
}

