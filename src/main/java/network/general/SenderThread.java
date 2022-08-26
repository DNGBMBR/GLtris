package network.general;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SenderThread extends Thread{
	Socket socket;
	private Queue<Object> sendQueue = new ConcurrentLinkedQueue<>(new LinkedList<>());
	ObjectOutputStream oos;

	SenderThread(Socket socket) {
		this.socket = socket;
		try {
			OutputStream os = socket.getOutputStream();
			oos = new ObjectOutputStream(os);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (socket.isConnected()) {
			if (!sendQueue.isEmpty()) {
				try {

					Object message = sendQueue.poll();
					oos.writeObject(message);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

	public void sendMessage(Object message) {
		sendQueue.add(message);
	}

	public void closeConnection() throws IOException {
		socket.close();
	}
}
