package network.general.message;

import java.nio.charset.StandardCharsets;

public class ServerDisconnectMessage extends Message{

	public String username;
	//disconnect message?

	public ServerDisconnectMessage(String username) {
		super(Message.MESSAGE_SERVER_DISCONNECT);
		this.username = username;
		this.contents = serialize();
	}

	public ServerDisconnectMessage(Message message) {
		super(message);
		deserialize(message.contents);
	}

	@Override
	public byte[] serialize() {
		return username.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public void deserialize(byte[] contents) {
		this.username = new String(contents, StandardCharsets.UTF_8);
	}
}
