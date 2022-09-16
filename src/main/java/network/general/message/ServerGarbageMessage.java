package network.general.message;

import game.Garbage;

import java.util.ArrayList;
import java.util.List;

public class ServerGarbageMessage extends Message{

	public String sender;
	public List<Garbage> garbage;

	public ServerGarbageMessage(String sender, List<Garbage> garbage) {
		super(MESSAGE_SERVER_GARBAGE);
		this.sender = sender;
		this.garbage = garbage;
		this.contents = serialize();
	}

	public ServerGarbageMessage(Message message) {
		super(message);
		deserialize(message.contents);
	}

	@Override
	public byte[] serialize() {
		byte[] out = new byte[2 * Integer.BYTES * garbage.size()];
		for (int i = 0; i < this.garbage.size(); i++) {
			for (int j = 0; j < Integer.BYTES; j++){
				out[2 * Integer.BYTES * i + j] = (byte) ((garbage.get(i).amount >> (8 * j)) & 0xFF);
				out[2 * Integer.BYTES * i + Integer.BYTES + j] = (byte) ((garbage.get(i).column >> (8 * j)) & 0xFF);
			}
		}
		return out;
	}

	@Override
	public void deserialize(byte[] contents) {
		this.garbage = new ArrayList<>();
		for (int i = 0; i < contents.length / (2 * Integer.BYTES); i++) {
			int amount = 0;
			int column = 0;
			for (int j = 0; j < Integer.BYTES; j++){
				amount |= (contents[2 * Integer.BYTES * i + j] & 0xFF) << (8 * j);
				column |= (contents[2 * Integer.BYTES * i + Integer.BYTES + j] & 0xFF) << (8 * j);
			}
			garbage.add(new Garbage(amount, column));
		}
	}
}
