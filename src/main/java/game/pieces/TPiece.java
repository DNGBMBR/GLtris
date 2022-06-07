package game.pieces;

import game.pieces.util.*;

public class TPiece extends Piece {
	//order is:
	//[0][][] - E
	//[1][][] - R
	//[2][][] - R2
	//[3][][] - R3
	//kicks are prioritized from 0 to n - 1
	private static final int[][][] KICK_CW = {
		//{1, -3} is a meme kick
		{{0, 0}, {-1, 0}, {-1, 1}, {0, -2}, {-1, -2}, {1, -3}},
		{{0, 0}, {1, 0}, {1, -1}, {0, 2}, {1, 2}},
		{{0, 0}, {1, 0}, {1, 1}, {0, -2}, {1, -2}},
		{{0, 0}, {-1, 0}, {-1, -1}, {0, 2}, {-1, 2}}
	};

	private static final int[][][] KICK_CCW = {
		{{0, 0}, {1, 0}, {1, 1}, {0, -2}, {1, -2}, {-1, -3}},
		{{0, 0}, {1, 0}, {1, -1}, {0, 2}, {1, 2}},
		{{0, 0}, {-1, 0}, {-1, 1}, {0, -2}, {-1, -2}},
		{{0, 0}, {-1, 0}, {-1, -1}, {0, 2}, {-1, 2}}
	};

	private static final int[][][] KICK_HALF = {
		{{0, 0}},
		{{0, 0}},
		{{0, 0}},
		{{0, 0}}
	};

	private static final boolean[][] TILE_MAP_E = {
		{false, false, false},
		{true, true, true},
		{false, true, false}
	};

	private static final boolean[][] TILE_MAP_R = {
		{false, true, false},
		{false, true, true},
		{false, true, false}
	};

	private static final boolean[][] TILE_MAP_R2 = {
		{false, true, false},
		{true, true, true},
		{false, false, false},
	};

	private static final boolean[][] TILE_MAP_R3 = {
		{false, true, false},
		{true, true, false},
		{false, true, false}
	};

	public TPiece(int bottomLeftX, int bottomLeftY) {
		super(bottomLeftX, bottomLeftY, TILE_MAP_E, Orientation.E, PieceName.T);
	}

	private TPiece(TPiece piece) {
		super(piece.bottomLeftX, piece.bottomLeftY, piece.tileMap, piece.orientation, PieceName.T);
	}

	@Override
	public boolean[][] getTileMapE() {
		return TILE_MAP_E;
	}

	@Override
	public boolean[][] getTileMapR() {
		return TILE_MAP_R;
	}

	@Override
	public boolean[][] getTileMapR2() {
		return TILE_MAP_R2;
	}

	@Override
	public boolean[][] getTileMapR3() {
		return TILE_MAP_R3;
	}

	@Override
	public int[][][] getKickTableCW() {
		return KICK_CW;
	}

	@Override
	public int[][][] getKickTableCCW() {
		return KICK_CCW;
	}

	@Override
	public int[][][] getKickTableHALF() {
		return KICK_HALF;
	}

	@Override
	public Piece copy() {
		return new TPiece(this);
	}

	public static boolean[][] getTileMapSpawn() {
		return TILE_MAP_E;
	}
}
