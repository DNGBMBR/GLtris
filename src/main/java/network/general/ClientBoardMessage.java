package network.general;

import game.pieces.util.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ClientBoardMessage extends MessageSerializer {
	private static final byte IS_TOPPED_OUT_MASK = 0x01;
	public String username;
	public boolean isToppedOut;
	public String hold;
	public String[] queue;
	public int pieceX, pieceY;
	public Orientation pieceOrientation;
	public String pieceName;
	public int[] garbageQueue;
	public TileState[][] board;

	public ClientBoardMessage(byte[] data) {
		super(data);
	}

	public ClientBoardMessage(String username, boolean isToppedOut, String hold, String[] queue, Piece currentPiece, int[] garbageQueue, TileState[][] board) {
		this.username = username;
		this.isToppedOut = isToppedOut;
		this.hold = hold;
		this.queue = queue;
		if (currentPiece != null) {
			this.pieceX = currentPiece.getBottomLeftX();
			this.pieceY = currentPiece.getBottomLeftY();
			this.pieceOrientation = currentPiece.getOrientation();
			this.pieceName = currentPiece.getName();
		}
		this.garbageQueue = garbageQueue;
		this.board = board;
	}

	@Override
	public byte[] serialize() {
		byte flags = (isToppedOut ? IS_TOPPED_OUT_MASK : 0);
		if (isToppedOut) {
			byte[] data = new byte[3];
			data[0] = MessageConstants.CLIENT;
			data[1] = MessageConstants.MESSAGE_CLIENT_BOARD;
			data[2] = flags;
			return data;
		}
		byte[] usernameBytes = this.username.getBytes(StandardCharsets.UTF_8);
		byte[] holdBytes = (this.hold == null ? "" : this.hold).getBytes(StandardCharsets.UTF_8);
		byte[][] queueBytes = new byte[queue.length][];
		int queueByteSize = 0;
		for (int i = 0; i < queue.length; i++) {
			queueBytes[i] = queue[i].getBytes(StandardCharsets.UTF_8);
			queueByteSize += queueBytes[i].length;
		}
		byte[] currentPieceNameBytes = (this.pieceName == null ? "" : this.pieceName).getBytes(StandardCharsets.UTF_8);

		byte[] data = new byte[
			2 + 1 +
			Short.BYTES + usernameBytes.length +
			Short.BYTES + holdBytes.length +
			Byte.BYTES + queueBytes.length * Short.BYTES + queueByteSize +
			2 * Byte.BYTES + Byte.BYTES + Short.BYTES + currentPieceNameBytes.length +
			Short.BYTES + garbageQueue.length * Byte.BYTES +
			2 * Short.BYTES + (board.length * board[0].length + 1) / 2];
		ByteBuffer buffer = ByteBuffer.wrap(data);

		buffer.put(MessageConstants.CLIENT);
		buffer.put(MessageConstants.MESSAGE_CLIENT_BOARD);
		buffer.put(flags);
		//username
		buffer.putShort((short) usernameBytes.length);
		buffer.put(usernameBytes);
		//hold
		buffer.putShort((short) holdBytes.length);
		buffer.put(holdBytes);
		//queue
		buffer.put((byte) queueBytes.length);
		for (byte[] queueByte : queueBytes) {
			buffer.putShort((short) queueByte.length);
			buffer.put(queueByte);
		}
		//current piece
		buffer.put((byte) pieceX);
		buffer.put((byte) pieceY);
		buffer.put((byte) pieceOrientation.getVal());
		buffer.putShort((short) currentPieceNameBytes.length);
		buffer.put(currentPieceNameBytes);
		//garbage queue
		buffer.putShort((short) garbageQueue.length);
		for (int amount : garbageQueue) {
			buffer.put((byte) amount);
		}
		//board
		buffer.putShort((short) board.length);
		buffer.putShort((short) board[0].length);
		for (int x = 0; x < board.length * board[0].length; x += 2) {
			int i1 = x / board[0].length;
			int j1 = x % board[0].length;
			int i2 = (x + 1) / board[0].length;
			int j2 = (x + 1) % board[0].length;
			byte value = (byte) (((board[i1][j1].getVal() << 0) & 0x0F) | ((board[i2][j2].getVal() << 4) & 0xF0));
			buffer.put(value);
		}
		if (((board.length * board[0].length) % 2) == 1) {
			byte value = (byte) ((board[board.length - 1][board[0].length - 1].getVal()) & 0x0F);
			buffer.put(value);
		}

		return data;
	}

	@Override
	public void deserialize(byte[] data) {
		assert data[0] == MessageConstants.CLIENT && data[1] == MessageConstants.MESSAGE_CLIENT_BOARD : "Illegal message type given to deserialize.";

		ByteBuffer buffer = ByteBuffer.wrap(data, 2, data.length - 2);
		int flags = buffer.get();
		this.isToppedOut = (flags & IS_TOPPED_OUT_MASK) != 0;
		if (isToppedOut) {
			return;
		}

		int nameLength = buffer.getShort();
		byte[] usernameBytes = new byte[nameLength];
		buffer.get(usernameBytes);
		this.username = new String(usernameBytes, StandardCharsets.UTF_8);

		int holdLength = buffer.getShort();
		byte[] holdBytes = new byte[holdLength];
		buffer.get(holdBytes);
		this.hold = new String(holdBytes, StandardCharsets.UTF_8);

		int queueNumElements = buffer.get();
		this.queue = new String[queueNumElements];
		for (int i = 0; i < queueNumElements; i++) {
			int queueNameLength = buffer.getShort();
			byte[] queueNameBytes = new byte[queueNameLength];
			buffer.get(queueNameBytes);
			queue[i] = new String(queueNameBytes, StandardCharsets.UTF_8);
		}

		this.pieceX = buffer.get();
		this.pieceY = buffer.get();
		this.pieceOrientation = Orientation.getEnum(buffer.get());
		int pieceNameLength = buffer.getShort();
		byte[] pieceNameBytes = new byte[pieceNameLength];
		buffer.get(pieceNameBytes);
		this.pieceName = new String(pieceNameBytes, StandardCharsets.UTF_8);

		int garbageQueueLength = buffer.getShort();
		this.garbageQueue = new int[garbageQueueLength];
		for (int i = 0; i < garbageQueueLength; i++) {
			this.garbageQueue[i] = buffer.get();
		}

		int boardHeight = buffer.getShort();
		int boardWidth = buffer.getShort();
		board = new TileState[boardHeight][boardWidth];
		for (int x = 0; x < boardHeight * boardWidth / 2; x++) {
			byte tileData = buffer.get();
			int i1 = (2 * x) / boardWidth;
			int j1 = (2 * x) % boardWidth;
			int i2 = (2 * x + 1) / boardWidth;
			int j2 = (2 * x + 1) % boardWidth;
			TileState low = TileState.getEnum(tileData & 0x0F);
			TileState high = TileState.getEnum((tileData & 0xF0) >> 4);
			board[i1][j1] = low;
			board[i2][j2] = high;
		}
		if (((boardHeight * boardWidth) % 2) == 1) {
			byte tileData = buffer.get();
			TileState remaining = TileState.getEnum(tileData & 0x0F);
			board[boardHeight - 1][boardWidth - 1] = remaining;
		}
	}
}
