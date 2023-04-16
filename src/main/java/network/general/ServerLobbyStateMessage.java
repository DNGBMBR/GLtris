package network.general;

import game.SpinDetector;
import game.pieces.PieceFactory;
import network.lobby.Player;
import org.json.simple.parser.ParseException;
import settings.GameSettings;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ServerLobbyStateMessage extends MessageSerializer{
	private static final byte IS_STARTING_MASK = 0x01;

	private static final byte PLAYER_READY_MASK = 0x01;
	private static final byte PLAYER_SPECTATING_MASK = 0x02;

	public GameSettings settings;
	public List<Player> players;
	public boolean isStarting;

	public ServerLobbyStateMessage(byte[] data) {
		super(data);
	}

	public ServerLobbyStateMessage(GameSettings settings, List<Player> players, boolean isStarting) {
		this.settings = settings;
		this.players = players;
		this.isStarting = isStarting;
	}

	@Override
	public byte[] serialize() {
		String kickTable = this.settings.getKickTable().getJson();
		byte flags = isStarting ? IS_STARTING_MASK : 0;
		byte[] kickTableBytes = kickTable.getBytes(StandardCharsets.UTF_8);
		int playerByteSize = 0;
		for (Player player : players) {
			String name = player.getName();
			byte[] bytes = name.getBytes(StandardCharsets.UTF_8);
			playerByteSize += Byte.BYTES + Short.BYTES + bytes.length;
		}
		byte[] spinTypeBytes = this.settings.getSpinDetector().name().getBytes(StandardCharsets.UTF_8);
		byte[] data = new byte[3 + 3 * Short.BYTES + Short.BYTES + playerByteSize + Short.BYTES + spinTypeBytes.length + Integer.BYTES + kickTableBytes.length];
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.put(MessageConstants.SERVER);
		buffer.put(MessageConstants.MESSAGE_SERVER_LOBBY_STATE);
		buffer.put(flags);

		buffer.putShort((short) players.size());
		for (Player player : players) {
			byte playerFlags = (byte) ((player.isReady() ? PLAYER_READY_MASK : 0) | (player.isSpectator() ? PLAYER_SPECTATING_MASK : 0));
			String name = player.getName();
			byte[] bytes = name.getBytes(StandardCharsets.UTF_8);
			buffer.put(playerFlags);
			buffer.putShort((short) bytes.length);
			buffer.put(bytes);
		}

		buffer.putShort((short) settings.getBoardHeight());
		buffer.putShort((short) settings.getBoardWidth());
		buffer.putShort((short) settings.getNumPreviews());

		buffer.putShort((short) spinTypeBytes.length);
		buffer.put(spinTypeBytes);

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

		int numPlayers = buffer.getShort();
		this.players = new ArrayList<>(numPlayers);
		for (int i = 0; i < numPlayers; i++) {
			byte playerFlags = buffer.get();
			boolean isReady = (playerFlags & PLAYER_READY_MASK) != 0;
			boolean isSpectating = (playerFlags & PLAYER_SPECTATING_MASK) != 0;
			int playerNameLength = buffer.getShort();
			byte[] playerNameBytes = new byte[playerNameLength];
			buffer.get(playerNameBytes);
			String playerName = new String(playerNameBytes, StandardCharsets.UTF_8);
			this.players.add(new Player(playerName, isReady, isSpectating));
		}

		int boardHeight = buffer.getShort();
		int boardWidth = buffer.getShort();
		int numPreviews = buffer.getShort();

		int spinDetectorNameLength = buffer.getShort();
		byte[] spinDetectorNameBytes = new byte[spinDetectorNameLength];
		buffer.get(spinDetectorNameBytes);
		String spinDetectorName = new String(spinDetectorNameBytes, StandardCharsets.UTF_8);
		SpinDetector detector = SpinDetector.getEnum(spinDetectorName);

		int kickTableLength = buffer.getInt();
		byte[] kickTableBytes = new byte[kickTableLength];
		buffer.get(kickTableBytes);
		String kickTableJSON = new String(kickTableBytes, StandardCharsets.UTF_8);
		PieceFactory kickTable;
		try {
			kickTable = new PieceFactory(kickTableJSON);
		} catch (ParseException e) {
			//there should probably be a new exception for this
			throw new IllegalStateException("Could not parse kick table.");
		}
		this.settings = new GameSettings(numPreviews, kickTable, boardHeight, boardWidth, detector);
	}
}
