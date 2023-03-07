package network.general;

public class ClientReadyMessage extends MessageSerializer {
	protected static final byte IS_SPECTATING_MASK = 0x01;
	protected static final byte IS_READY_MASK = 0x02;

	public boolean isSpectating;
	public boolean isReady;

	public ClientReadyMessage(byte[] data) {
		super(data);
	}

	public ClientReadyMessage(boolean isSpectating, boolean isReady) {
		this.isSpectating = isSpectating;
		this.isReady = isReady;
	}

	@Override
	public byte[] serialize() {
		byte flags = (byte) ((isSpectating ? IS_SPECTATING_MASK : 0) | (isReady ? IS_READY_MASK : 0));
		byte[] data = new byte[3];
		data[0] = MessageConstants.CLIENT;
		data[1] = MessageConstants.MESSAGE_CLIENT_READY;
		data[2] = flags;
		return data;
	}

	@Override
	public void deserialize(byte[] data) {
		assert data[0] == MessageConstants.CLIENT && data[1] == MessageConstants.MESSAGE_CLIENT_READY : "Illegal message type given to deserialize.";
		this.isSpectating = (data[2] & IS_SPECTATING_MASK) != 0;
		this.isReady = (data[2] & IS_READY_MASK) != 0;
	}
}
