package network.general.message;

import game.Garbage;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class ClientGarbageMessage extends Message{

	public String sender;
	public List<Garbage> garbage;

	public ClientGarbageMessage(String sender, List<Garbage> garbage) {
		super(MESSAGE_CLIENT_GARBAGE);
		this.sender = sender;
		this.garbage = garbage;
		this.contents = this.serialize();
	}

	public ClientGarbageMessage(byte[] contents) {
		super(MESSAGE_CLIENT_GARBAGE, contents);
		deserialize(contents);
	}

	public ClientGarbageMessage(Message message) {
		super(message);
		deserialize(message.contents);
	}

	@Override
	public byte[] serialize() {
		//each garbage instance is defined by two ints
		//format looks like:
		//[garbage][garbage]...[garbage]
		//garbage: [4 bytes column][4 bytes amount]
		byte[] nameBytes = sender.getBytes(StandardCharsets.UTF_8);
		byte[] ret = new byte[Integer.BYTES + nameBytes.length + Short.BYTES + garbage.size() * 2 * Integer.BYTES];
		for (int i = 0; i < Integer.BYTES; i++) {
			ret[i] = (byte) ((nameBytes.length >> (8 * i)) & 0xFF);
		}
		System.arraycopy(nameBytes, 0, ret, Integer.BYTES, nameBytes.length);
		ret[Integer.BYTES + nameBytes.length] = (byte) ((garbage.size() >> 0) & 0xFF);
		ret[Integer.BYTES + nameBytes.length + 1] = (byte) ((garbage.size() >> 8) & 0xFF);

		for (int i = 0; i < garbage.size(); i++) {
			Garbage g = garbage.get(i);
			ret[Integer.BYTES + nameBytes.length + Short.BYTES + 2 * Integer.BYTES * i + 0] = (byte) ((g.column >> 0) & 0xFF);
			ret[Integer.BYTES + nameBytes.length + Short.BYTES + 2 * Integer.BYTES * i + 1] = (byte) ((g.column >> 8) & 0xFF);
			ret[Integer.BYTES + nameBytes.length + Short.BYTES + 2 * Integer.BYTES * i + 2] = (byte) ((g.column >> 16) & 0xFF);
			ret[Integer.BYTES + nameBytes.length + Short.BYTES + 2 * Integer.BYTES * i + 3] = (byte) ((g.column >> 24) & 0xFF);

			ret[Integer.BYTES + nameBytes.length + Short.BYTES + 2 * Integer.BYTES * i + 4] = (byte) ((g.amount >> 0) & 0xFF);
			ret[Integer.BYTES + nameBytes.length + Short.BYTES + 2 * Integer.BYTES * i + 5] = (byte) ((g.amount >> 8) & 0xFF);
			ret[Integer.BYTES + nameBytes.length + Short.BYTES + 2 * Integer.BYTES * i + 6] = (byte) ((g.amount >> 16) & 0xFF);
			ret[Integer.BYTES + nameBytes.length + Short.BYTES + 2 * Integer.BYTES * i + 7] = (byte) ((g.amount >> 24) & 0xFF);
		}
		return ret;
	}

	@Override
	public void deserialize(byte[] contents) {
		int senderNameLength = 0;
		for (int i = 0; i < Integer.BYTES; i++) {
			senderNameLength |= (contents[i] & 0xFF) << (8 * i);
		}
		sender = new String(contents, Integer.BYTES, senderNameLength, StandardCharsets.UTF_8);

		int garbageLength = 0;
		for (int i = 0; i < Short.BYTES; i++) {
			garbageLength |= (contents[Integer.BYTES + senderNameLength + i] & 0xFF) << (8 * i);
		}
		garbage = new Vector<>(garbageLength);
		for (int i = 0; i < garbageLength; i++) {
			int column = 0;
			int amount = 0;
			for (int j = 0; j < 4; j++) {
				column |= (contents[Integer.BYTES + senderNameLength + Short.BYTES + 2 * Integer.BYTES * i + j] & 0xFF) << (8 * j);
				amount |= (contents[Integer.BYTES + senderNameLength + Short.BYTES + 2 * Integer.BYTES * i + Integer.BYTES + j] & 0xFF) << (8 * j);
			}
			Garbage g = new Garbage(column, amount);
			garbage.add(g);
		}
	}
}
