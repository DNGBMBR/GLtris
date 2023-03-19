package network.general;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ServerLobbyPlayerUpdateMessage extends MessageSerializer{
	protected static final byte DISCONNECTED_MASK = 0x01;
	protected static final byte READY_MASK = 0x02;
	protected static final byte SPECTATING_MASK = 0x04;

	public String username;
	public boolean isDisconnected;
	public boolean isReady;
	public boolean isSpectating;

	public ServerLobbyPlayerUpdateMessage(byte[] data) {
		super(data);
	}

	public ServerLobbyPlayerUpdateMessage(String username, boolean isDisconnected, boolean isReady, boolean isSpectating) {
		this.username = username;
		this.isDisconnected = isDisconnected;
		this.isReady = isReady;
		this.isSpectating = isSpectating;
	}

	@Override
	public byte[] serialize() {
		byte[] usernameBytes = this.username.getBytes(StandardCharsets.UTF_8);
		byte[] data = new byte[2 + 1 + Short.BYTES + usernameBytes.length];
		byte flags = (byte) ((isDisconnected ? DISCONNECTED_MASK : 0) | (isReady ? READY_MASK : 0) | (isSpectating ? SPECTATING_MASK : 0));
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.put(MessageConstants.SERVER);
		buffer.put(MessageConstants.MESSAGE_SERVER_UPDATE_PLAYER);
		buffer.put(flags);
		buffer.putShort((short) usernameBytes.length);
		buffer.put(usernameBytes);
		return data;
	}

	@Override
	public void deserialize(byte[] data) {
		assert data[0] == MessageConstants.SERVER && data[1] == MessageConstants.MESSAGE_SERVER_UPDATE_PLAYER : "Illegal message type given to deserialize.";
		ByteBuffer buffer = ByteBuffer.wrap(data, 2, data.length - 2);
		byte flags = buffer.get();
		isDisconnected = (flags & DISCONNECTED_MASK) != 0;
		isReady = (flags & READY_MASK) != 0;
		isSpectating = (flags & SPECTATING_MASK) != 0;
		int nameLength = buffer.getShort();
		byte[] nameBytes = new byte[nameLength];
		buffer.get(nameBytes);
		username = new String(nameBytes, StandardCharsets.UTF_8);
	}
}
