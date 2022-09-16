package network.general.message;

import java.nio.charset.StandardCharsets;

public class ServerGameEndMessage extends Message{

	public String winningPlayer;

	public ServerGameEndMessage(String winningPlayer) {
		super(Message.MESSAGE_SERVER_GAME_END);
		this.winningPlayer = winningPlayer;
		this.contents = serialize();
	}

	public ServerGameEndMessage(Message message) {
		super(message);
		deserialize(message.contents);
	}

	@Override
	public byte[] serialize() {
		return winningPlayer.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public void deserialize(byte[] contents) {
		this.winningPlayer = new String(contents, StandardCharsets.UTF_8);
	}
}
