package network.general;

import java.net.Socket;

public interface UnexpectedCloseCallback {
	void onUnexpectedClose(Socket socket);
}
