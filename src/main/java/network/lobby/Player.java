package network.lobby;

import game.pieces.util.TileState;

public class Player {
	String name;
	boolean isReady = false;
	boolean isSpectator = false;
	boolean isAlive = true;
	TileState[][] board;

	Player(String name) {
		this.name = name;
	}

	public void initialize(TileState[][] board) {
		this.board = new TileState[board.length][board[0].length];
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				this.board[i][j] = board[i][j];
			}
		}
		if (!this.isSpectator) {
			this.isAlive = true;
		}
	}

	public boolean isReady() {
		return isReady;
	}

	public void setReady(boolean ready) {
		isReady = ready;
	}

	public boolean isSpectator() {
		return isSpectator;
	}

	public void setSpectator(boolean spectator) {
		isSpectator = spectator;
	}

	public boolean isAlive() {
		return isAlive;
	}

	public void setAlive(boolean alive) {
		isAlive = alive;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TileState[][] getBoard() {
		return board;
	}

	public void setBoard(TileState[][] board) {
		this.board = board;
	}
}
