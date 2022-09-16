package network.general.message;

import game.pieces.PieceBuilder;
import game.pieces.PieceFactory;
import network.lobby.LobbyState;
import org.json.simple.parser.ParseException;
import settings.LobbySettings;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class ServerLobbyStateMessage extends Message{
	//this is probably the only message class that uses the ObjectOutputStream as it was intended, simply because serializing this info manually is a massive pain
	public LobbySettings settings;
	public LobbyState state;

	public ServerLobbyStateMessage(LobbySettings settings, LobbyState state) {
		super(Message.MESSAGE_SERVER_LOBBY_STATE);
		this.settings = settings;
		this.state = state;
		this.contents = serialize();
	}

	public ServerLobbyStateMessage(Message message) {
		super(message);
		deserialize(message.contents);
	}

	//TODO: make everything backed by a ByteBuffer instead of manually copying data

	@Override
	public byte[] serialize() {
		//1 byte for lobby state
		int numPreviews = settings.getNumPreviews(); // 1 byte
		int gravityIncreaseInterval = settings.getGravityIncreaseInterval(); //4 bytes
		int firstTo = settings.getFirstTo(); //4 bytes
		double initGravity = settings.getInitGravity(); //8 bytes
		double gravityIncrease = settings.getGravityIncrease(); //8 bytes
		double lockDelay = settings.getLockDelay(); //8 bytes
		PieceFactory kickTable = settings.getKickTable();
		String kickTableString = kickTable.getJson();
		byte[] kickTableBytes = kickTableString.getBytes(StandardCharsets.UTF_8); //4 bytes + variable length

		byte[] out = new byte[2 * Byte.BYTES + 2 * Integer.BYTES + 3 * Double.BYTES + Integer.BYTES + kickTableBytes.length];
		out[0] = (byte) state.getVal();
		out[1] = (byte) ((numPreviews) & 0xFF);

		//write all int values first, then doubles, then the kick table
		for (int i = 0; i < Integer.BYTES; i++) {
			out[2 * Byte.BYTES+ i] = (byte) ((gravityIncreaseInterval >> (8 * i)) & 0xFF);
			out[2 * Byte.BYTES + Integer.BYTES + i] = (byte) ((firstTo >> (8 * i)) & 0xFF);
		}

		byte[] doubleBytes = new byte[3 * Double.BYTES];
		ByteBuffer.wrap(doubleBytes).order(ByteOrder.LITTLE_ENDIAN).putDouble(initGravity).putDouble(gravityIncrease).putDouble(lockDelay);
		System.arraycopy(doubleBytes, 0, out, 2 * Byte.BYTES + 2 * Integer.BYTES, doubleBytes.length);

		for (int i = 0; i < Integer.BYTES; i++) {
			out[2 * Byte.BYTES + 2 * Integer.BYTES + 3 * Double.BYTES + i] = (byte) ((kickTableBytes.length >> (8 * i)) & 0xFF);
		}

		int kickTableSize = 0;
		for (int i = 0; i < Integer.BYTES; i++) {
			kickTableSize |= ((int) out[2 * Byte.BYTES + 2 * Integer.BYTES + 3 * Double.BYTES + i] & 0xFF) << (8 * i);
		}

		System.arraycopy(kickTableBytes, 0, out, 2 * Byte.BYTES + 2 * Integer.BYTES + 3 * Double.BYTES + Integer.BYTES, kickTableBytes.length);

		return out;
	}

	@Override
	public void deserialize(byte[] contents) {
		state = LobbyState.getEnum(contents[0]);
		int numPreviews = contents[1];
		int gravityIncreaseInterval = 0;
		int firstTo = 0;

		for (int i = 0; i < Integer.BYTES; i++) {
			gravityIncreaseInterval |= (contents[2 * Byte.BYTES + Integer.BYTES + i] & 0xFF) << (8 * i);
			firstTo |= (contents[2 * Byte.BYTES + 2 * Integer.BYTES + i] & 0xFF) << (8 * i);
		}

		byte[] doubleBytes = new byte[3 * Double.BYTES];
		System.arraycopy(contents, 2 * Byte.BYTES + 2 * Integer.BYTES, doubleBytes, 0, doubleBytes.length);
		ByteBuffer buffer = ByteBuffer.wrap(doubleBytes).order(ByteOrder.LITTLE_ENDIAN);
		double initGravity = buffer.getDouble();
		double gravityIncrease = buffer.getDouble();
		double lockDelay = buffer.getDouble();

		int kickTableSize = 0;
		for (int i = 0; i < Integer.BYTES; i++) {
			//TODO: do this fix for every message
			kickTableSize |= (contents[2 * Byte.BYTES + 2 * Integer.BYTES + 3 * Double.BYTES + i] & 0xFF) << (8 * i);
		}

		PieceFactory kickTable = null;
		String s = new String(contents, 2 * Byte.BYTES + 2 * Integer.BYTES + 3 * Double.BYTES + Integer.BYTES, kickTableSize, StandardCharsets.UTF_8);
		try {
			kickTable = new PieceFactory(PieceBuilder.parseJSON(s));
		} catch (ParseException e) {
			System.err.println("Could not parse kick table given in message.");
			e.printStackTrace();
		}

		settings = new LobbySettings(numPreviews, kickTable, initGravity, gravityIncrease, gravityIncreaseInterval, lockDelay, firstTo);
	}
}
