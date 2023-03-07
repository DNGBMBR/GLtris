package network.general;

import game.pieces.PieceFactory;
import org.json.simple.parser.ParseException;
import settings.GameSettings;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ServerLobbyStateMessage extends MessageSerializer{
	private static final byte IS_STARTING_MASK = 0x01;

	public GameSettings settings;
	public boolean isStarting;

	public ServerLobbyStateMessage(byte[] data) {
		super(data);
	}

	public ServerLobbyStateMessage(GameSettings settings, boolean isStarting) {
		this.settings = settings;
		this.isStarting = isStarting;
	}

	@Override
	public byte[] serialize() {
		String kickTable = this.settings.getKickTable().getJson();
		byte flags = isStarting ? IS_STARTING_MASK : 0;
		byte[] kickTableBytes = kickTable.getBytes(StandardCharsets.UTF_8);
		byte[] data = new byte[3 + 3 * Double.BYTES + 2 * Integer.BYTES + Integer.BYTES + kickTableBytes.length];
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.put(MessageConstants.SERVER);
		buffer.put(MessageConstants.MESSAGE_SERVER_LOBBY_STATE);
		buffer.put(flags);

		buffer.putDouble(settings.getInitGravity());
		buffer.putDouble(settings.getGravityIncrease());
		buffer.putDouble(settings.getLockDelay());

		buffer.putInt(settings.getGravityIncreaseInterval());
		buffer.putInt(settings.getNumPreviews());

		buffer.putInt(kickTableBytes.length);
		buffer.put(kickTableBytes);
		return data;
	}

	@Override
	public void deserialize(byte[] data) {
		assert data[0] == MessageConstants.SERVER && data[1] == MessageConstants.MESSAGE_SERVER_LOBBY_STATE : "Illegal message type given to deserialize.";
		ByteBuffer buffer = ByteBuffer.wrap(data, 2, data.length - 2);
		byte flags = buffer.get();
		this.isStarting = (flags & IS_STARTING_MASK) != 0;
		double initGravity = buffer.getDouble();
		double gravityIncrease = buffer.getDouble();
		double lockDelay = buffer.getDouble();

		int gravityIncreaseInterval = buffer.getInt();
		int numPreviews = buffer.getInt();

		int kickTableLength = buffer.getInt();
		byte[] kickTableBytes = new byte[kickTableLength];
		buffer.get(kickTableBytes);
		String kickTableJSON = new String(kickTableBytes, StandardCharsets.UTF_8);
		PieceFactory kickTable;
		try {
			kickTable = new PieceFactory(kickTableJSON);
		} catch (ParseException e) {
			//there should probably be a new exception for this
			throw new IllegalArgumentException("Could not parse kick table.");
		}
		this.settings = new GameSettings(numPreviews, kickTable, initGravity, gravityIncrease, gravityIncreaseInterval, lockDelay);
	}
}
