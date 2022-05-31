package game.pieces;

import game.pieces.util.*;

public class JPiece extends Piece {
	private static final int[][][] KICK_CW = {
		{{0, 0}, {-1, 0}, {-1, 1}, {0, -2}, {-1, -2}},
		{{0, 0}, {1, 0}, {1, -1}, {0, 2}, {1, 2}},
		{{0, 0}, {1, 0}, {1, 1}, {0, -2}, {1, -2}},
		{{0, 0}, {-1, 0}, {-1, -1}, {0, 2}, {-1, 2}}
	};

	private static final int[][][] KICK_CCW = {
		{{0, 0}, {1, 0}, {1, 1}, {0, -2}, {1, -2}},
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
		{true, false, false}
	};

	private static final boolean[][] TILE_MAP_R = {
		{false, true, false},
		{false, true, false},
		{false, true, true}
	};

	private static final boolean[][] TILE_MAP_R2 = {
		{false, false, true},
		{true, true, true},
		{false, false, false}
	};

	private static final boolean[][] TILE_MAP_R3 = {
		{true, true, false},
		{false, true, false},
		{false, true, false}
	};

	public JPiece(int topLeftX, int topLeftY) {
		super(topLeftX, topLeftY, TILE_MAP_E, Orientation.E, PieceName.J);
	}

	private JPiece(JPiece piece) {
		super(piece.topLeftX, piece.topLeftY, piece.tileMap, piece.orientation, PieceName.J);
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
		return new JPiece(this);
	}

	public static boolean[][] getTileMapSpawn() {
		return TILE_MAP_E;
	}
}
