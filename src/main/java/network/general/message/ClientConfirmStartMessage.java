package network.general.message;

public class ClientConfirmStartMessage extends Message{
	public ClientConfirmStartMessage() {
		super(Message.MESSAGE_CLIENT_CONFIRM_START);
		this.contents = serialize();
	}

	public ClientConfirmStartMessage(Message message) {
		super(message);
		deserialize(message.contents);
	}
}
