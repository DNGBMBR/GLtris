package network.general;

import java.net.InetAddress;

public class Client {
	ClientThread clientThread;

	public Client(InetAddress dstAddress, int dstPort, OnReceive receiveCallback, OnConnect connectCallback) {
		clientThread = new ClientThread(dstAddress, dstPort, receiveCallback, connectCallback);
	}

	public void start() {
		clientThread.start();
	}
}
