package network.general;

import network.lobby.LobbyState;

import java.io.IOException;
import java.net.*;

public class ServerThread extends Thread{

	int port = 2678;
	OnReceive receiveCallback;
	OnConnect connectCallback;
	ListenerThread listenThread;
	SenderThread senderThread;
	//TODO: Lobby state machine (manage lobby, game, etc.)
	//TODO: Serialize and deserialize data to be sent over network
	//TODO: Create protocol for what to send and when to send it
	ServerThread(OnReceive receiveCallback) {
		this.receiveCallback = receiveCallback;
	}

	ServerThread(int port, OnReceive receiveCallback, OnConnect connectCallback) {
		this.port = port;
		this.receiveCallback = receiveCallback;
		this.connectCallback = connectCallback;
	}

	@Override
	public void run() {
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Unable to initialize at port " + port + ".");
			return;
		}
		while (!serverSocket.isClosed()) {
			Socket socket = null;
			try {
				socket = serverSocket.accept();
				senderThread = new SenderThread(socket);
				senderThread.start();
				listenThread = new ListenerThread(senderThread, socket, receiveCallback);
				listenThread.start();
				connectCallback.onConnect(senderThread);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public void sendMessage(Object message) {
		senderThread.sendMessage(message);
	}
}
