package network.general;

import network.general.message.Message;
import network.general.message.ServerLobbyStateMessage;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

public class SenderThread extends Thread{
	Socket socket;
	private Queue<Message> sendQueue = new ConcurrentLinkedQueue<>(new LinkedList<>());
	ObjectOutputStream oos;
	ReentrantLock lock = new ReentrantLock();

	boolean shouldClose = false;
	Set<UnexpectedCloseCallback> closeCallbacks = new HashSet<>();

	public SenderThread(Socket socket) {
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
		while (socket.isConnected() && !socket.isClosed() && !shouldClose) {
			if (!sendQueue.isEmpty()) {
				Message queuedMessage = sendQueue.poll();
				if (queuedMessage != null) {
					lock.lock();
					try {
						if (queuedMessage instanceof ServerLobbyStateMessage message) {
							oos.writeObject(message);
						}
						else {
							Message message = new Message(queuedMessage);
							oos.writeObject(message);
						}
					} catch (IOException e) {
						System.err.println("Could not send message of type: " + queuedMessage.messageType + ". Closing connection.");
						for (UnexpectedCloseCallback callback : closeCallbacks) {
							callback.onUnexpectedClose(socket);
						}
						break;
					} finally {
						lock.unlock();
					}
				}
			}
		}

		try {
			socket.close();
		} catch (IOException e) {
			System.err.println("Unable to close socket.");
			e.printStackTrace();
		}
	}

	public void sendMessage(Message message) {
		sendQueue.add(message);
	}

	public void setShouldClose() {
		shouldClose = true;
	}

	public void registerUnexpectedCloseCallback(UnexpectedCloseCallback callback) {
		closeCallbacks.add(callback);
	}
}
