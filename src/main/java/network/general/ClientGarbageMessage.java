package network.general;

import game.Garbage;
import util.Utils;

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
		byte[] garbageBytes = new byte[2 * Short.BYTES * garbage.size()];
		for (int i = 0; i < this.garbage.size(); i++) {
			Utils.copyShort(garbageBytes, 2 * i + 0, this.garbage.get(i).amount);
			Utils.copyShort(garbageBytes, 2 * i + 1, this.garbage.get(i).column);
		}
		byte[] data = new byte[2 + Short.BYTES + usernameBytes.length + Short.BYTES + garbageBytes.length];
		data[0] = MessageConstants.CLIENT;
		data[1] = MessageConstants.MESSAGE_CLIENT_GARBAGE;
		Utils.copyShort(data, 2, usernameBytes.length);
		System.arraycopy(usernameBytes, 0, data, 2 + Short.BYTES, usernameBytes.length);
		Utils.copyShort(data, 2 + Short.BYTES + usernameBytes.length, this.garbage.size());
		System.arraycopy(garbageBytes, 0, data, 2 + Short.BYTES + usernameBytes.length + Short.BYTES, garbageBytes.length);
		return data;
	}

	@Override
	public void deserialize(byte[] data) {
		if (data[0] != MessageConstants.CLIENT || data[1] != MessageConstants.MESSAGE_CLIENT_GARBAGE) {
			throw new IllegalArgumentException("Illegal message type given to deserialize.");
		}
		int nameLength = Utils.readShort(data, 2);
		this.username = new String(data, 2 + Short.BYTES, nameLength, StandardCharsets.UTF_8);
		int garbageLength = Utils.readShort(data, 2 + Short.BYTES + nameLength);
		this.garbage = new ArrayList<>(garbageLength);
		for (int i = 0; i < garbageLength; i++) {
			int amount = Utils.readShort(data, 2 + Short.BYTES + nameLength + Short.BYTES + 2 * i);
			int column = Utils.readShort(data, 2 + Short.BYTES + nameLength + Short.BYTES + 2 * i + 1);
			this.garbage.add(new Garbage(amount, column));
		}
	}
}
