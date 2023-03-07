package network.general;

import game.Garbage;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ServerGarbageMessage extends MessageSerializer {
	public List<Garbage> garbage;

	public ServerGarbageMessage(byte[] data) {
		super(data);
	}

	public ServerGarbageMessage(List<Garbage> garbage) {
		this.garbage = garbage;
	}

	@Override
	public byte[] serialize() {
		byte[] data = new byte[2 + Short.BYTES + 2 * Short.BYTES * this.garbage.size()];
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.put(MessageConstants.SERVER);
		buffer.put(MessageConstants.MESSAGE_SERVER_GARBAGE);
		buffer.putShort((short) this.garbage.size());
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
		int garbageLength = buffer.getShort();
		this.garbage = new ArrayList<>(garbageLength);
		for (int i = 0; i < garbageLength; i++) {
			int amount = buffer.getShort();
			int column = buffer.getShort();
			this.garbage.add(new Garbage(amount, column));
		}
	}

}
