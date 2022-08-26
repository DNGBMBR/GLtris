package network;

import network.general.*;

import java.net.Socket;

public class ServerMain {
	public static void main(String[] args) {
		//TODO: build an interface for the server
		Server server = new Server(2678, new OnReceive() {
			@Override
			public void onReceive(SenderThread destination, Object gameInfo) {
				System.out.println("Server has received: " + gameInfo);
			}
		}, new OnConnect() {
			@Override
			public void onConnect(SenderThread destination) {
				System.out.println("CONNECTED");
				destination.sendMessage("aaaad");
			}
		});
		System.out.println("Initialized server");
		server.start();
		System.out.println("Server started");
	}
}
