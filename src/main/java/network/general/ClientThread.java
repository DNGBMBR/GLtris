package network.general;

import java.io.IOException;
import java.net.*;

public class ClientThread extends Thread{
	Socket socket;
	InetAddress dstAddress;
	int dstPort;
	OnReceive receiveCallback;
	OnConnect connectCallback;
	ListenerThread listenThread;
	SenderThread senderThread;

	public ClientThread(InetAddress dstAddress, int dstPort, OnReceive receiveCallback, OnConnect connectCallback) {
		this.dstAddress = dstAddress;
		this.dstPort = dstPort;
		this.receiveCallback = receiveCallback;
		this.connectCallback = connectCallback;
	}

	@Override
	public void run() {
		try {
			socket = new Socket(dstAddress, dstPort);
			senderThread = new SenderThread(socket);
			senderThread.start();
			listenThread = new ListenerThread(senderThread, socket, receiveCallback);
			listenThread.start();
			connectCallback.onConnect(senderThread);
		} catch (ConnectException e) {
			System.out.println("Unable to connect to host " + dstAddress + " at port " + dstPort + ".");
		} catch (IOException e) {
			//TODO: handle unable to connect gracefully
			e.printStackTrace();
		}
	}
}
