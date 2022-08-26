package network.general;

import network.general.OnReceive;
import network.general.ServerThread;
import network.lobby.Lobby;

import java.net.Socket;

public class Server {
	ServerThread serverThread;

	public Server(int port, OnReceive receiveCallback, OnConnect connectCallback) {
		serverThread = new ServerThread(port, receiveCallback, connectCallback);
	}

	public void start() {
		serverThread.start();
	}
}
