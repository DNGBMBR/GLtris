package network.general;

import network.general.message.Message;

import java.net.Socket;

public interface OnReceive {
	void onReceive(Socket receivedFrom, SenderThread returnAddress, Message message);
}
