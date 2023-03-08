package network.general;

import game.Garbage;
import util.Utils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ClientGarbageMessage extends MessageSerializer {
	public String username;
	public List<Garbage> garbage;

	public ClientGarbageMessage(byte[] data) {
		super(data);
	}

	public ClientGarbageMessage(String username, List<Garbage> garbage) {
		this.username = username;
		this.garbage = garbage;
	}

	@Override
	public byte[] serialize() {
		byte[] usernameBytes = this.username.getBytes(StandardCharsets.UTF_8);
		byte[] data = new byte[2 + Short.BYTES + usernameBytes.length + Short.BYTES + 2 * Short.BYTES * this.garbage.size()];
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.put(MessageConstants.CLIENT);
		buffer.put(MessageConstants.MESSAGE_CLIENT_GARBAGE);

		buffer.putShort((short) usernameBytes.length);
		buffer.put(usernameBytes);

		buffer.putShort((short) garbage.size());
		for (Garbage garbage : this.garbage) {
			buffer.putShort((short) garbage.amount);
			buffer.putShort((short) garbage.column);
		}
		return data;
	}

	@Override
	public void deserialize(byte[] data) {
		if (data[0] != MessageConstants.CLIENT || data[1] != MessageConstants.MESSAGE_CLIENT_GARBAGE) {
			throw new IllegalArgumentException("Illegal message type given to deserialize.");
		}
		ByteBuffer buffer = ByteBuffer.wrap(data, 2, data.length - 2);
		int nameLength = buffer.getShort();
		byte[] nameBytes = new byte[nameLength];
		buffer.get(nameBytes);
		this.username = new String(nameBytes, 0, nameLength, StandardCharsets.UTF_8);
		int garbageLength = buffer.getShort();
		this.garbage = new ArrayList<>(garbageLength);
		for (int i = 0; i < garbageLength; i++) {
			int amount = buffer.getShort();
			int column = buffer.getShort();
			this.garbage.add(new Garbage(amount, column));
		}
	}
}
