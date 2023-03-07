package network.general;

import util.Utils;

import java.nio.charset.StandardCharsets;

public class ClientConnectMessage extends MessageSerializer{
	public String username;

	public ClientConnectMessage(byte[] data) {
		super(data);
	}

	public ClientConnectMessage(String username) {
		this.username = username;
	}

	@Override
	public byte[] serialize() {
		byte[] usernameBytes = this.username.getBytes(StandardCharsets.UTF_8);
		byte[] data = new byte[2 + Short.BYTES + usernameBytes.length];
		data[0] = MessageConstants.CLIENT;
		data[1] = MessageConstants.MESSAGE_CLIENT_CONNECT;
		Utils.copyShort(data, 2, usernameBytes.length);
		System.arraycopy(usernameBytes, 0, data, 2 + Short.BYTES, usernameBytes.length);
		return data;
	}

	@Override
	public void deserialize(byte[] data) {
		if (data[0] != MessageConstants.CLIENT || data[1] != MessageConstants.MESSAGE_CLIENT_CONNECT) {
			throw new IllegalArgumentException("Illegal message type given to deserialize.");
		}
		int nameLength = Utils.readShort(data, 2);
		this.username = new String(data, 2 + Short.BYTES, nameLength, StandardCharsets.UTF_8);
	}
}
