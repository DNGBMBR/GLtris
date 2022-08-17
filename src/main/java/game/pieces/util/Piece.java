package game.pieces.util;

import static game.pieces.util.Orientation.*;

public abstract class Piece {
	//remember that when you're putting the tile map in an array, index 0 is at the bottom on the board, so the resulting figure is vertically flipped
	protected int bottomLeftX;
	protected int bottomLeftY;
	protected boolean placed;
	protected boolean[][] tileMap;
	protected boolean[][] tileMapE;
	protected boolean[][] tileMapR;
	protected boolean[][] tileMapR2;
	protected boolean[][] tileMapR3;
	protected int[][][] kickTableCW;
	protected int[][][] kickTableCCW;
	protected int[][][] kickTableHalf;
	protected Orientation orientation;
	protected PieceColour pieceColour;
	protected String name;

	public Piece(
				String name, PieceColour pieceColour, int bottomLeftX, int bottomLeftY, Orientation orientation,
				boolean[][] tileMapE, boolean[][] tileMapR, boolean[][] tileMapR2, boolean[][] tileMapR3,
				int[][][] kickTableCW, int[][][] kickTableCCW, int[][][] kickTableHalf
				) {
		this.bottomLeftX = bottomLeftX;
		this.bottomLeftY = bottomLeftY;
		this.placed = false;
		this.tileMapE = tileMapE;
		this.tileMapR = tileMapR;
		this.tileMapR2 = tileMapR2;
		this.tileMapR3 = tileMapR3;
		this.kickTableCW = kickTableCW;
		this.kickTableCCW = kickTableCCW;
		this.kickTableHalf = kickTableHalf;
		this.orientation = orientation;
		switch (orientation) {
			case E -> {
				this.tileMap = tileMapE;
			}
			case R -> {
				this.tileMap = tileMapR;
			}
			case R2 -> {
				this.tileMap = tileMapR2;
			}
			case R3 -> {
				this.tileMap = tileMapR3;
			}
		}
		this.name = name;
		this.pieceColour = pieceColour;
	}

	public Piece(Piece src) {
		this.bottomLeftX = src.bottomLeftX;
		this.bottomLeftY = src.bottomLeftY;
		this.placed = false;
		this.tileMap = src.tileMap;
		this.orientation = src.orientation;
		this.pieceColour = src.pieceColour;

		this.tileMapE = src.tileMapE;
		this.tileMapR = src.tileMapR;
		this.tileMapR2 = src.tileMapR2;
		this.tileMapR3 = src.tileMapR3;

		this.kickTableCW = src.kickTableCW;
		this.kickTableCCW = src.kickTableCCW;
		this.kickTableHalf = src.kickTableHalf;
	}

	//returns index of kick used
	public int rotate(Rotation rot, TileState[][] board) {
		boolean[][] potentialRotation;
		Orientation potentialOrientation;
		int potentialX = bottomLeftX;
		int potentialY = bottomLeftY;

		switch(rot) {
			case CW -> {
				switch (orientation) {
					case E -> {
						potentialRotation = getTileMapR();
						potentialOrientation = R;
					}
					case R -> {
						potentialRotation = getTileMapR2();
						potentialOrientation = R2;
					}
					case R2 -> {
						potentialRotation = getTileMapR3();
						potentialOrientation = R3;
					}
					case R3 -> {
						potentialRotation = getTileMapE();
						potentialOrientation = E;
					}
					default -> {
						return -1;
					}
				}
			}
			case CCW -> {
				switch (orientation) {
					case E -> {
						potentialRotation = getTileMapR3();
						potentialOrientation = R3;
					}
					case R -> {
						potentialRotation = getTileMapE();
						potentialOrientation = E;
					}
					case R2 -> {
						potentialRotation = getTileMapR();
						potentialOrientation = R;
					}
					case R3 -> {
						potentialRotation = getTileMapR2();
						potentialOrientation = R2;
					}
					default -> {
						return -1;
					}
				}
			}
			case HALF -> {
				switch (orientation) {
					case E -> {
						potentialRotation =getTileMapR2();
						potentialOrientation = R2;
					}
					case R -> {
						potentialRotation = getTileMapR3();
						potentialOrientation = R3;
					}
					case R2 -> {
						potentialRotation = getTileMapE();
						potentialOrientation = E;
					}
					case R3 -> {
						potentialRotation = getTileMapR();
						potentialOrientation = R;
					}
					default -> {
						return -1;
					}
				}
			}
			default -> {
				return -1;
			}
		}

		boolean validKick = false;
		int[][][] kickTable;
		switch(rot) {
			case CW -> {
				kickTable = getKickTableCW();
			}
			case CCW -> {
				kickTable = getKickTableCCW();
			}
			case HALF -> {
				kickTable = getKickTableHALF();
			}
			default -> {
				return -1;
			}
		}

		int kickUsed = -1;

		for (int i = 0; i < kickTable[orientation.getVal()].length; i++) {
			potentialX = bottomLeftX + kickTable[orientation.getVal()][i][0];
			potentialY = bottomLeftY + kickTable[orientation.getVal()][i][1];
			if (!isCollision(board, potentialRotation, potentialX, potentialY)) {
				kickUsed = i;
				validKick = true;
				break;
			}
		}

		if (validKick) {
			tileMap = potentialRotation;
			orientation = potentialOrientation;
			bottomLeftX = potentialX;
			bottomLeftY = potentialY;
		}

		return kickUsed;
	}

	public boolean move(Direction dir, TileState[][] board) {
		int potentialX;
		int potentialY;
		switch(dir) {
			case LEFT -> {
				potentialX = bottomLeftX - 1;
				potentialY = bottomLeftY;
			}
			case RIGHT -> {
				potentialX = bottomLeftX + 1;
				potentialY = bottomLeftY;
			}
			case DOWN -> {
				potentialX = bottomLeftX;
				potentialY = bottomLeftY - 1;
			}
			default -> {
				return false;
			}
		}
		if (isCollision(board, this.tileMap, potentialX, potentialY)) {
			return false;
		}
		bottomLeftX = potentialX;
		bottomLeftY = potentialY;
		return true;
	}

	//returns true if successfully placed, false if there was something in the way
	public boolean place(TileState[][] board) {
		if (isCollision(board, tileMap, bottomLeftX, bottomLeftY)) {
			return false;
		}

		TileState placedTileType;
		switch(pieceColour) {
			case I -> {
				placedTileType = TileState.I;
			}
			case O -> {
				placedTileType = TileState.O;
			}
			case L -> {
				placedTileType = TileState.L;
			}
			case J -> {
				placedTileType = TileState.J;
			}
			case S -> {
				placedTileType = TileState.S;
			}
			case Z -> {
				placedTileType = TileState.Z;
			}
			case T -> {
				placedTileType = TileState.T;
			}
			default -> {
				placedTileType = TileState.GARBAGE;
			}
		}
		for (int i = 0; i < tileMap.length; i++) {
			for (int j = 0; j < tileMap[i].length; j++) {
				if (tileMap[i][j]) {
					int xIndex = bottomLeftX + j;
					int yIndex = bottomLeftY + i;
					board[yIndex][xIndex] = placedTileType;
				}
			}
		}
		placed = true;
		return true;
	}

	public void hardDrop(TileState[][] board) {
		while (move(Direction.DOWN, board)) {
			//move down as far as possible
		}
		place(board);
	}

	public boolean gravity(TileState[][] board) {
		return move(Direction.DOWN, board);
	}

	public boolean[][] getTileMapE() {
		return this.tileMapE;
	}
	public boolean[][] getTileMapR() {
		return this.tileMapR;
	}
	public boolean[][] getTileMapR2() {
		return this.tileMapR2;
	}
	public boolean[][] getTileMapR3() {
		return this.tileMapR3;
	}

	public int[][][] getKickTableCW() {
		return this.kickTableCW;
	}
	public int[][][] getKickTableCCW() {
		return this.kickTableCCW;
	}
	public int[][][] getKickTableHALF() {
		return this.kickTableHalf;
	}

	public Piece copy() {
		return new Piece(this) {};
	}

	protected boolean isCollision(TileState[][] board, boolean[][] potentialMap, int potentialX, int potentialY) {
		for (int i = 0; i < potentialMap.length; i++) {
			for (int j = 0; j < potentialMap[i].length; j++) {
				int testX = potentialX + j;
				int testY = potentialY + i;
				if (testX < 0 || testY < 0 || testX >= board[0].length || testY >= board.length) {
					if (potentialMap[i][j]) {
						return true;
					}
					else {
						continue;
					}
				}
				if (potentialMap[i][j] && board[testY][testX] != TileState.EMPTY) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean testCollision(TileState[][] board, int directionX, int directionY) {
		return isCollision(board, this.tileMap, this.bottomLeftX + directionX, this.bottomLeftY + directionY);
	}

	public int getBottomLeftX() {
		return bottomLeftX;
	}

	public int getBottomLeftY() {
		return bottomLeftY;
	}

	public boolean[][] getTileMap() {
		return tileMap;
	}

	public boolean isPlaced() {
		return placed;
	}

	public PieceColour getPieceColour() {
		return pieceColour;
	}

	public String getName() {
		return name;
	}

	public Orientation getOrientation() {
		return orientation;
	}
}
