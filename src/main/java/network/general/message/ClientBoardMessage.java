package network.general.message;

import game.pieces.PieceFactory;
import game.pieces.util.*;

import java.util.List;
import java.util.Vector;

public class ClientBoardMessage extends Message{

	PieceFactory factory;
	int currentPieceX, currentPieceY;
	Orientation orientation;
	String currentPieceName;
	TileState[][] board;
	int queueLength;
	List<String> queue;
	String hold;

	public ClientBoardMessage(PieceFactory factory, Piece currentPiece, TileState[][] board, int queueLength, List<String> queue, String hold) {
		super(MESSAGE_CLIENT_BOARD);
		this.factory = factory;
		this.currentPieceX = currentPiece.getBottomLeftX();
		this.currentPieceY = currentPiece.getBottomLeftY();
		this.orientation = currentPiece.getOrientation();
		this.currentPieceName = currentPiece.getName();
		this.board = board;
		this.queueLength = queueLength;
		this.queue = queue;
		this.hold = hold;
		this.contents = this.serialize();
	}

	public ClientBoardMessage(PieceFactory factory, int queueLength, byte[] contents) {
		super(MESSAGE_CLIENT_BOARD, contents);
		this.factory = factory;
		this.queueLength = queueLength;
		deserialize(contents);
	}

	public ClientBoardMessage(PieceFactory factory, int queueLength, Message message) {
		super(message);
		this.factory = factory;
		this.queueLength = queueLength;
		deserialize(message.contents);
	}

	@Override
	public byte[] serialize() {
		//format: current piece ID (1 byte), orientation (2 bits/ 1 byte), x, y (2 ints, LSB)
		//hold ID (1 byte)
		//queue (number of previews should be the same as server's, so no need to add it here) (just take the first queueLength elements)
		//board dimensions (2 ints) (width * height + 1) / 2 bytes of board data (board size should be synced across client and server, so no need to send it)
		//
		//board is stored as a contiguous array of half bytes (4 bits) from left to right, bottom to top, least significant 4 bits first
		//tile states, as defined in enum:
		//0 - empty
		//1 - garbage
		//2 - I
		//3 - O
		//4 - L
		//5 - J
		//6 - S
		//7 - Z
		//8 - T
		//
		//orientation: E - 0, R - 1, R2 - 2, R3 - 3 according to enum definition

		//current piece
		byte[] buffer = new byte[1 + 1 + 2 * Integer.BYTES + 1 + queueLength + 2 * Integer.BYTES + (board.length * board[0].length + 1) / 2];
		buffer[0] = (byte) (factory.getIndex(currentPieceName) & 0xFF);
		buffer[1] = (byte) (orientation.getVal());
		for (int i = 0; i < 4; i++) {
			buffer[2 + i] = (byte) ((currentPieceX << (8 * i)) & 0xFF);
			buffer[2 + Integer.BYTES + i] = (byte) ((currentPieceY << (8 * i)) & 0xFF);
		}

		//hold
		buffer[2 + 2 * Integer.BYTES] = (byte) (factory.getIndex(hold) & 0xFF);

		//queue
		for (int i = 0; i < queueLength; i++) {
			buffer[2 + 2 * Integer.BYTES + 1 + i] = (byte) (factory.getIndex(queue.get(i)) & 0xFF);
		}

		//board
		for (int i = 0; i < 4; i++) {
			buffer[2 + 2 * Integer.BYTES + 1 + queueLength + i] = (byte) ((board.length << (8 * i)) & 0xFF);
			buffer[2 + 2 * Integer.BYTES + 1 + queueLength + Integer.BYTES + i] = (byte) ((board[0].length << (8 * i)) & 0xFF);
		}

		for (int index = 0; index < board.length * board[0].length; index++) {
			int i = index / board[0].length;
			int j = index % board[0].length;
			buffer[2 + 2 * Integer.BYTES + 1 + queueLength + 2 * Integer.BYTES + index] = (byte) ((board[i][j].getVal() << ((index & 1) != 0 ? 4 : 0)) & 0xFF);
		}

		return buffer;
	}

	@Override
	public void deserialize(byte[] contents) {
		this.currentPieceName = factory.getName(contents[0]);
		this.orientation = Orientation.getEnum(contents[1]);

		for (int i = 0; i < 4; i++) {
			currentPieceX |= (contents[2 + i] & 0xFF) << (8 * i);
			currentPieceY |= (contents[2 + Integer.BYTES + i] & 0xFF) << (8 * i);
		}
		this.hold = factory.getName(contents[2 + 2 * Integer.BYTES]);

		queue = new Vector<>();
		for (int i = 0; i < queueLength; i++) {
			queue.add(factory.getName(contents[2 + 2 * Integer.BYTES + 1 + i]));
		}

		int boardWidth = 0;
		int boardHeight = 0;

		for (int i = 0; i < Integer.SIZE; i++) {
			boardHeight |= (contents[2 + 2 * Integer.BYTES + 1 + queueLength + i] & 0xFF) << (8 * i);
			boardWidth |= (contents[2 + 2 * Integer.BYTES + 1 + queueLength + Integer.BYTES + i] & 0xFF) << (8 * i);
		}
		board = new TileState[boardHeight][boardWidth];

		for (int index = 0; index < boardHeight * boardWidth; index++) {
			TileState tile = TileState.getEnum(contents[2 + 2 * Integer.BYTES + 1 + queueLength + 2 * Integer.BYTES + index / 2] >> ((index & 1) != 0 ? 4 : 0));
			int i = index / boardWidth;
			int j = index % boardWidth;
			board[i][j] = tile;
		}
	}
}
