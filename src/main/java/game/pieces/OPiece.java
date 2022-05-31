package game.pieces;

import game.pieces.util.*;

public class OPiece extends Piece {
	private static final int[][][] KICK_CW = {
		{{0, 0}},
		{{0, 0}},
		{{0, 0}},
		{{0, 0}}
	};

	private static final int[][][] KICK_CCW = {
		{{0, 0}},
		{{0, 0}},
		{{0, 0}},
		{{0, 0}}
	};

	private static final int[][][] KICK_HALF = {
		{{0, 0}},
		{{0, 0}},
		{{0, 0}},
		{{0, 0}}
	};

	//the only tile map we need for O piece, until we want to get some custom spins going hahahahaha
	private static final boolean[][] TILE_MAP_E = {
		{true, true},
		{true, true}
	};

	public OPiece(int topLeftX, int topLeftY) {
		super(topLeftX, topLeftY, TILE_MAP_E, Orientation.E, PieceName.O);
	}

	private OPiece(OPiece piece) {
		super(piece.topLeftX, piece.topLeftY, piece.tileMap, piece.orientation, PieceName.O);
	}

	private static final boolean[][] TILE_MAP_R = TILE_MAP_E;
	private static final boolean[][] TILE_MAP_R2 = TILE_MAP_E;
	private static final boolean[][] TILE_MAP_R3 = TILE_MAP_E;

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
		return new OPiece(this);
	}

	public static boolean[][] getTileMapSpawn() {
		return TILE_MAP_E;
	}
}
