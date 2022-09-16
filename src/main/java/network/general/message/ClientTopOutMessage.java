package network.general.message;

public class ClientTopOutMessage extends Message{
	public ClientTopOutMessage() {
		super(Message.MESSAGE_CLIENT_TOP_OUT);
		this.contents = this.serialize();
	}

	@Override
	public byte[] serialize() {
		return new byte[0];
	}

	@Override
	public void deserialize(byte[] contents) {

	}
}
