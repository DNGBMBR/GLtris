package network.general;

public class ServerCountdownMessage extends MessageSerializer {
	public static final byte TELL_EVERYONE_TO_PREPARE = (byte) 0xFF;
	public static final byte START = 0;

	public byte state;

	public ServerCountdownMessage(byte[] data) {
		deserialize(data);
	}

	public ServerCountdownMessage(byte state) {
		this.state = state;
	}

	@Override
	public byte[] serialize() {
		return new byte[] {MessageConstants.SERVER, MessageConstants.MESSAGE_SERVER_COUNTDOWN, state};
	}

	@Override
	public void deserialize(byte[] data) {
		if (data[0] != MessageConstants.SERVER || data[1] != MessageConstants.MESSAGE_SERVER_COUNTDOWN) {
			throw new IllegalArgumentException("Illegal message type given to deserialize.");
		}
		this.state = data[2];
	}
}
