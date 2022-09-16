package network.general;

import java.net.Socket;

public class ServerConnection {
	public Socket socket;
	public SenderThread send;
	public ListenerThread receive;
	public boolean isPrepared = false;

	public ServerConnection(Socket socket, SenderThread send, ListenerThread receive) {
		this.socket = socket;
		this.send = send;
		this.receive = receive;
	}
}
