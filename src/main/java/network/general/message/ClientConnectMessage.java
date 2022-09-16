package network.general.message;

import java.nio.charset.StandardCharsets;

public class ClientConnectMessage extends Message{
	public String username;

	public ClientConnectMessage(String username) {
		super(Message.MESSAGE_CLIENT_CONNECT);
		this.username = username;
		this.contents = serialize();
	}

	public ClientConnectMessage(Message message) {
		super(message);
		deserialize(message.contents);
	}

	@Override
	public byte[] serialize() {
		return username.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public void deserialize(byte[] contents) {
		username = new String(contents, StandardCharsets.UTF_8);
	}
}
