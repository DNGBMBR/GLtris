package network.general.message;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ServerConnectMessage extends Message{

	public List<String> usernames;

	public ServerConnectMessage(List<String> usernames) {
		super(Message.MESSAGE_SERVER_CONNECT);
		this.usernames = usernames;
		this.contents = serialize();
	}

	public ServerConnectMessage(Message message) {
		super(message);
		deserialize(message.contents);
	}

	@Override
	public byte[] serialize() {
		//[int - number of strings] [short - length of string at this index in bytes] [UTF-8 string] ... [short] [UTF-8 string]
		int numBytes = 0;
		byte[][] stringBytes = new byte[usernames.size()][];
		for (int i = 0; i < usernames.size(); i++) {
			stringBytes[i] = usernames.get(i).getBytes(StandardCharsets.UTF_8);
			numBytes += stringBytes[i].length;
		}

		byte[] out = new byte[Integer.BYTES + Short.BYTES * usernames.size() + numBytes];

		for (int i = 0; i < Integer.BYTES; i++) {
			out[i] = (byte) (usernames.size() >> (8 * i));
		}

		int pointer = Integer.BYTES;
		for (int i = 0; i < usernames.size(); i++) {
			for (int j = 0; j < Short.BYTES; j++) {
				out[pointer] = (byte) (stringBytes[i].length >> (8 * j));
				pointer++;
			}
			for (int j = 0; j < stringBytes[i].length; j++) {
				out[pointer] = stringBytes[i][j];
				pointer++;
			}
		}

		return out;
	}

	@Override
	public void deserialize(byte[] contents) {
		int numUsers = 0;
		for (int i = 0; i < Integer.BYTES; i++) {
			numUsers |= (contents[i] & 0xFF) << (8 * i);
		}
		usernames = new ArrayList<>(numUsers);
		int pointer = Integer.BYTES;
		for (int i = 0; i < numUsers; i++) {
			int numBytes = 0;
			for (int j = 0; j < 2; j++) {
				numBytes |= (contents[pointer] & 0xFF) << (8 * i);
				pointer++;
			}
			String s = new String(contents, pointer, numBytes, StandardCharsets.UTF_8);
			pointer++;
			usernames.add(s);
		}
	}
}
