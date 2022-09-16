package network.general;

import network.general.message.Message;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

public class ListenerThread extends Thread{
	Socket socket;
	OnReceive callback;
	ObjectInputStream ois;
	SenderThread senderThread;

	boolean shouldClose = false;

	Set<UnexpectedCloseCallback> closeCallbacks = new HashSet<>();

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
		while (socket.isConnected() && !socket.isClosed() && !shouldClose) {
			try {
				Object gameInfo = ois.readObject();
				if (gameInfo instanceof Message message) {
					callback.onReceive(socket, senderThread, message);
				}
			} catch (ClassNotFoundException e) {
				System.err.println("Malformed object received from socket " + socket + ".");
				e.printStackTrace();
			} catch (SocketException e) {
				//I really need to handle disconnections better huh
			} catch (IOException e) {
				System.err.println("Cannot listen on this connection. Closing connection.");
				for (UnexpectedCloseCallback callback : closeCallbacks) {
					callback.onUnexpectedClose(socket);
				}
				break;
			}
		}

		try {
			socket.close();
		} catch (IOException e) {
			System.err.println("An error occurred while trying to close this connection.");
			e.printStackTrace();
		}
	}

	public void setShouldClose() {
		shouldClose = true;
	}

	public void registerUnexpectedCloseCallback(UnexpectedCloseCallback callback) {
		closeCallbacks.add(callback);
	}
}
