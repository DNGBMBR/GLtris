package pieces;

import pieces.util.*;

public class TPiece extends Piece {
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

	public TPiece() {
		topLeftX = 3; //magic numbers yay
		topLeftY = 22;
		tileMap = TILE_MAP_E;
		orientation = Orientation.E;
		name = PieceName.T;
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

	public static boolean[][] getTileMapSpawn() {
		return TILE_MAP_E;
	}
}
