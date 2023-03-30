package network.general;

public class MessageConstants {
	public static final byte MESSAGE_CLIENT_CONNECT = 0; //send username of client connecting
	public static final byte MESSAGE_CLIENT_READY = 1; //send spectating/ready update
	public static final byte MESSAGE_CLIENT_CONFIRM_START = 2; //respond to server that everything on client's end is prepared to start
	public static final byte MESSAGE_CLIENT_GARBAGE = 3; //sends garbage to server to be redirected
	public static final byte MESSAGE_CLIENT_BOARD = 4; //send board, queue, and hold piece, as well as other info like top out and garbage queue(?)

	public static final byte MESSAGE_SERVER_LOBBY_STATE = 0; //sends what state the client should be in currently (includes lobby settings, in game, etc.)
	public static final byte MESSAGE_SERVER_LOBBY_UPDATE_PLAYER = 1; //updates a player's state (lobby, in game board etc.) (to be added: queue, hold piece in game)
	public static final byte MESSAGE_SERVER_COUNTDOWN = 2; //sends countdown to game start so clients are synced. if countdown is 0, game starts
	public static final byte MESSAGE_SERVER_GARBAGE = 3; //sends garbage to client to be added to their board
	public static final byte MESSAGE_SERVER_GAME_END = 4; //tells clients the game has ended, includes winner (possibly stats)
	public static final byte MESSAGE_SERVER_BOARD = 5; //updates an opponent's board

	public static final byte SERVER = 0;
	public static final byte CLIENT = 1;

	private MessageConstants() {}
}
