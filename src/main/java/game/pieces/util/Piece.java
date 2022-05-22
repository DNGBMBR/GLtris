package game.pieces.util;

import static game.pieces.util.Orientation.*;

public abstract class Piece {
	//TODO: IMPLEMENT 180 KICKS FOR ALL PIECES

	//remember that when you're putting the tile map in an array, index 0 is at the bottom on the board, so the resulting figure is vertically flipped
	protected int topLeftX;
	protected int topLeftY;
	protected boolean placed;
	protected boolean[][] tileMap;
	protected Orientation orientation;
	protected PieceName name;

	//returns index of kick used
	public int rotate(Rotation rot, TileState[][] board) {
		boolean[][] potentialRotation;
		Orientation potentialOrientation;
		int potentialX = topLeftX;
		int potentialY = topLeftY;

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
			potentialX = topLeftX + kickTable[orientation.getVal()][i][0];
			potentialY = topLeftY + kickTable[orientation.getVal()][i][1];
			if (!isCollision(board, potentialRotation, potentialX, potentialY)) {
				kickUsed = i;
				validKick = true;
				break;
			}
		}

		if (validKick) {
			tileMap = potentialRotation;
			orientation = potentialOrientation;
			topLeftX = potentialX;
			topLeftY = potentialY;
		}

		return kickUsed;
	}

	public boolean move(Direction dir, TileState[][] board) {
		int potentialX;
		int potentialY;
		switch(dir) {
			case LEFT -> {
				potentialX = topLeftX - 1;
				potentialY = topLeftY;
			}
			case RIGHT -> {
				potentialX = topLeftX + 1;
				potentialY = topLeftY;
			}
			case DOWN -> {
				potentialX = topLeftX;
				potentialY = topLeftY - 1;
			}
			default -> {
				return false;
			}
		}
		if (isCollision(board, this.tileMap, potentialX, potentialY)) {
			return false;
		}
		topLeftX = potentialX;
		topLeftY = potentialY;
		return true;
	}

	//returns true if successfully placed, false if there was something in the way
	public boolean place(TileState[][] board) {
		if (isCollision(board, tileMap, topLeftX, topLeftY)) {
			return false;
		}

		TileState placedTileType;
		switch(name) {
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
					int xIndex = topLeftX + j;
					int yIndex = topLeftY + i;
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

	public abstract boolean[][] getTileMapE();
	public abstract boolean[][] getTileMapR();
	public abstract boolean[][] getTileMapR2();
	public abstract boolean[][] getTileMapR3();

	public abstract int[][][] getKickTableCW();
	public abstract int[][][] getKickTableCCW();
	public abstract int[][][] getKickTableHALF();

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
		return isCollision(board, this.tileMap, this.topLeftX + directionX, this.topLeftY + directionY);
	}

	public int getTopLeftX() {
		return topLeftX;
	}

	public int getTopLeftY() {
		return topLeftY;
	}

	public boolean[][] getTileMap() {
		return tileMap;
	}

	public boolean isPlaced() {
		return placed;
	}

	public PieceName getName() {
		return name;
	}

	public Orientation getOrientation() {
		return orientation;
	}
}
