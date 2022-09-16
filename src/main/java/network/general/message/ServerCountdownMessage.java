package network.general.message;

public class ServerCountdownMessage extends Message{
	public static final byte TELL_EVERYONE_TO_PREPARE = 5;
	public static final byte START = 0;

	public byte state;

	public ServerCountdownMessage(byte state) {
		super(Message.MESSAGE_SERVER_COUNTDOWN);
		this.state = state;
		this.contents = serialize();
	}

	public ServerCountdownMessage(Message message) {
		super(message);
		deserialize(message.contents);
	}

	@Override
	public byte[] serialize() {
		return new byte[]{state};
	}

	@Override
	public void deserialize(byte[] contents) {
		this.state = contents[0];
	}
}
