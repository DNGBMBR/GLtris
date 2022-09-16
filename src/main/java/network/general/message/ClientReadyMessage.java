package network.general.message;

public class ClientReadyMessage extends Message{

	public boolean isSpectating;
	public boolean isReady;

	public ClientReadyMessage(boolean isSpectating, boolean isReady) {
		super(MESSAGE_CLIENT_READY);
		this.isSpectating = isSpectating;
		this.isReady = isReady;
		contents = serialize();
	}

	public ClientReadyMessage(Message message) {
		super(message);
		deserialize(message.contents);
	}

	@Override
	public byte[] serialize() {
		return new byte[]{(byte) (isSpectating ? 1 : 0), (byte) (isReady ? 1 : 0)};
	}

	@Override
	public void deserialize(byte[] contents) {
		isSpectating = contents[0] != 0;
		isReady = contents[1] != 0;
	}
}
