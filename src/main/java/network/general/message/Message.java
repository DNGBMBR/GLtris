package network.general.message;

import java.io.Serializable;

public class Message implements Serializable {
	public static final byte MESSAGE_CLIENT_CONNECT = 0; //send username of client connecting
	public static final byte MESSAGE_CLIENT_DISCONNECT = 1;
	public static final byte MESSAGE_CLIENT_READY = 2; //send spectating/ready update
	public static final byte MESSAGE_CLIENT_CONFIRM_START = 3; //respond to server that everything on client's end is prepared to start
	public static final byte MESSAGE_CLIENT_GARBAGE = 4; //sends garbage to server to be redirected
	public static final byte MESSAGE_CLIENT_TOP_OUT = 5; //sends when client tops out
	public static final byte MESSAGE_CLIENT_BOARD = 6; //send board, queue, and hold piece

	public static final byte MESSAGE_SERVER_CONNECT = 0; //send information about other players in lobby
	public static final byte MESSAGE_SERVER_DISCONNECT = 1; //notify all players when another player has disconnected
	public static final byte MESSAGE_SERVER_LOBBY_STATE = 2; //sends what state the client should be in currently (includes lobby settings, in game, etc.)
	public static final byte MESSAGE_SERVER_UPDATE_PLAYER = 3; //updates a player's state (lobby, in game board etc.) (to be added: queue, hold piece ofr in game)
	public static final byte MESSAGE_SERVER_COUNTDOWN = 4; //sends countdown to game start so clients are synced. if countdown is 0, game starts
	public static final byte MESSAGE_SERVER_GARBAGE = 5; //sends garbage to client to be added to their board
	public static final byte MESSAGE_SERVER_GAME_END = 6; //tells clients the game has ended, includes winner (possibly stats)

	public byte messageType;
	public byte[] contents; //represents serialized version of message object

	public Message(byte messageType) {
		this.messageType = messageType;
	}

	public Message(byte messageType, byte[] contents) {
		this.messageType = messageType;
		this.contents = contents;
	}

	public Message(Message message) {
		this(message.messageType, message.contents);
	}

	public byte getMessageType() {
		return messageType;
	}

	public byte[] getContents() {
		return contents;
	}

	public void setContents(byte[] contents) {
		this.contents = contents;
	}

	//these are meant to be overridden
	public byte[] serialize() {
		return new byte[0];
	}
	public void deserialize(byte[] contents) {}
}
