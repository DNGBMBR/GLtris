package network.general.message;

import game.pieces.util.TileState;
import network.lobby.LobbyState;

import java.nio.charset.StandardCharsets;

public class ServerUpdatePlayerMessage extends Message {

	private static final byte SPECTATING_MASK = 0x01;
	private static final byte READY_MASK = 0x02;

	public LobbyState updateType;
	public String playerName;

	public boolean isSpectating;
	public boolean isReady;

	public boolean isAlive;
	public TileState[][] board;

	public ServerUpdatePlayerMessage(String playerName, boolean isSpectating, boolean isReady) {
		super(Message.MESSAGE_SERVER_UPDATE_PLAYER);
		this.playerName = playerName;
		this.updateType = LobbyState.LOBBY;
		this.isSpectating = isSpectating;
		this.isReady = isReady;
		this.contents = serialize();
	}

	//board is null is isAlive is false
	public ServerUpdatePlayerMessage(String playerName, boolean isAlive, TileState[][] board) {
		super(Message.MESSAGE_SERVER_UPDATE_PLAYER);
		this.playerName = playerName;
		this.updateType = LobbyState.IN_GAME;
		this.isAlive = isAlive;
		this.board = board;
		this.contents = serialize();
	}

	public ServerUpdatePlayerMessage(Message message) {
		super(message);
		deserialize(message.contents);
	}

	@Override
	public byte[] serialize() {
		byte[] out;
		byte[] nameBytes = playerName.getBytes(StandardCharsets.UTF_8);
		switch(updateType) {
			case LOBBY -> {
				out = new byte[nameBytes.length + 2];
				for (int i = 0; i < nameBytes.length; i++) {
					out[i + 1] = nameBytes[i];
				}
				out[out.length - 1] = (byte) ((this.isSpectating ? SPECTATING_MASK : 0) | (this.isReady ? READY_MASK : 0));
			}
			case IN_GAME -> {
				if (isAlive) {
					out = new byte[1 + 1 + Integer.BYTES + nameBytes.length + 2 * Integer.BYTES + (board.length * board[0].length + 1) / 2];
					//write board
					for (int i = 0; i < Integer.BYTES; i++) {
						out[2 + nameBytes.length + i] = (byte) (board.length >> (8 * i));
						out[2 + nameBytes.length + Integer.BYTES + i] = (byte) (board[0].length >> (8 * i));
					}

					for (int index = 0; index < board.length * board[0].length; index++) {
						int i = index / board[0].length;
						int j = index % board[0].length;

						out[2 + Integer.BYTES + nameBytes.length + 2 * Integer.BYTES + index] = (byte) (board[i][j].getVal() << (index % 2 != 0 ? 4 : 0));
					}
				}
				else {
					out = new byte[2 + Integer.BYTES + nameBytes.length];
				}

				out[1] = (byte) (this.isAlive ? 1 : 0);

				//write username
				for (int i = 0; i < Integer.BYTES; i++) {
					out[2 + i] = (byte) (nameBytes.length >> (8 * i));
				}
				for (int i = 0; i < nameBytes.length; i++) {
					out[2 + Integer.BYTES + i] = nameBytes[i];
				}
			}
			default -> {
				throw new IllegalStateException("Update type not valid.");
			}
		}
		out[0] = (byte) this.updateType.getVal();

		return out;
	}

	@Override
	public void deserialize(byte[] contents) {
		this.updateType = LobbyState.getEnum(contents[0]);
		switch(updateType) {
			case LOBBY -> {
				this.playerName = new String(contents, 1, contents.length - 2, StandardCharsets.UTF_8);
				this.isSpectating = (contents[contents.length - 1] & SPECTATING_MASK) != 0;
				this.isReady = (contents[contents.length - 1] & READY_MASK) != 0;
			}
			case IN_GAME -> {
				isAlive = contents[1] != 0;
				int nameNumBytes = 0;
				for (int i = 0; i < Integer.BYTES; i++) {
					nameNumBytes |= ((int) contents[2 + i] & 0xFF) << (8 * i);
				}
				this.playerName = new String(contents, 2 + Integer.BYTES, nameNumBytes, StandardCharsets.UTF_8);

				if (isAlive) {
					int boardHeight = 0;
					int boardWidth = 0;
					for (int i = 0; i < Integer.BYTES; i++) {
						boardHeight |= ((int) contents[2 + Integer.BYTES + nameNumBytes + i] & 0xFF) << (8 * i);
						boardWidth |= ((int) contents[2 + Integer.BYTES + nameNumBytes + Integer.BYTES + i] & 0xFF) << (8 * i);
					}
					board = new TileState[boardHeight][boardWidth];
					for (int index = 0; index < (boardHeight * boardWidth + 1) / 2; index++) {
						int i = index / boardWidth;
						int j = index % boardWidth;
						board[i][j] = TileState.getEnum(contents[2 + Integer.BYTES + nameNumBytes + 2 * Integer.BYTES + index] >> ((index & 1) != 0 ? 4 : 0) & 0x0F);
					}
				}
			}
		}
	}
}
