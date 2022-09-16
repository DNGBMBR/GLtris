package network.general.message;

public class ClientDisconnectMessage extends Message{
	public ClientDisconnectMessage(String disconnectMessage) {
		super(Message.MESSAGE_CLIENT_DISCONNECT);
		this.contents = serialize();
	}

	@Override
	public byte[] serialize() {
		return new byte[0];
	}

	@Override
	public void deserialize(byte[] contents) {

	}
}
