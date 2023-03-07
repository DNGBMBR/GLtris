package network.general;

public class ClientConfirmStartMessage extends MessageSerializer{
	public ClientConfirmStartMessage(byte[] data) {
		super(data);
	}

	public ClientConfirmStartMessage() {

	}

	@Override
	public byte[] serialize() {
		byte[] data = new byte[2];
		data[0] = MessageConstants.CLIENT;
		data[1] = MessageConstants.MESSAGE_CLIENT_CONFIRM_START;
		return data;
	}

	@Override
	public void deserialize(byte[] data) {
		assert data[0] == MessageConstants.CLIENT && data[1] == MessageConstants.MESSAGE_CLIENT_CONFIRM_START : "Illegal message type given to deserialize.";
	}
}
