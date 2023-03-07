package network.general;

public abstract class MessageSerializer {
	public MessageSerializer() {

	}

	public MessageSerializer(byte[] data) {
		deserialize(data);
	}

	public abstract byte[] serialize();
	public abstract void deserialize(byte[] data);
}
