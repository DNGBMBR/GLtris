package network.general;

import java.io.*;
import java.net.Socket;

public class ListenerThread extends Thread{
	Socket socket;
	OnReceive callback;
	ObjectInputStream ois;
	SenderThread senderThread;

	public ListenerThread(SenderThread senderThread, Socket socket, OnReceive callback) {
		this.senderThread = senderThread;
		this.socket = socket;
		this.callback = callback;
		try {
			InputStream is = socket.getInputStream();
			ois = new ObjectInputStream(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (socket.isConnected()) {
			try {
				Object gameInfo = ois.readObject();
				if (gameInfo != null) {
					callback.onReceive(senderThread, gameInfo);
				}
			} catch (ClassNotFoundException e) {
				System.err.println("Malformed object received from socket " + socket + ".");
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("Unable to connect to socket.");
				e.printStackTrace();
				return;
			}
		}
	}

	public void closeConnection() throws IOException {
		socket.close();
	}
}
