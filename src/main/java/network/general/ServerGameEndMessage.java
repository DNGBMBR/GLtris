package network.general;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ServerGameEndMessage extends MessageSerializer{
	public String winningPlayer;

	public ServerGameEndMessage(byte[] data) {
		super(data);
	}

	public ServerGameEndMessage(String winningPlayer) {
		this.winningPlayer = winningPlayer;
	}

	@Override
	public byte[] serialize() {
		byte[] nameBytes = this.winningPlayer.getBytes(StandardCharsets.UTF_8);
		byte[] data = new byte[2 + Short.BYTES + nameBytes.length];
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.put(MessageConstants.SERVER);
		buffer.put(MessageConstants.MESSAGE_SERVER_GAME_END);
		buffer.putShort((short) nameBytes.length);
		buffer.put(nameBytes);
		return data;
	}

	@Override
	public void deserialize(byte[] data) {
		assert data[0] == MessageConstants.SERVER && data[1] == MessageConstants.MESSAGE_SERVER_GAME_END : "Illegal message type given to deserialize.";
		ByteBuffer buffer = ByteBuffer.wrap(data, 2, data.length - 2);
		int nameLength = buffer.getShort();
		byte[] nameBytes = new byte[nameLength];
		this.winningPlayer = new String(nameBytes, StandardCharsets.UTF_8);
	}
}
