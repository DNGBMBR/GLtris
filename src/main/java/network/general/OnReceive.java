package network.general;

import java.net.Socket;

public interface OnReceive {
	void onReceive(SenderThread destination, Object gameInfo);
}
