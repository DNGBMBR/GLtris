package game.pieces.util;

public enum TileState {

	I(2), O(3), L(4), J(5), S(6), Z(7), T(8), EMPTY(0), GARBAGE(1);

	private int val;

	TileState(int val) {
		this.val = val;
	}

	public int getVal() {
		return this.val;
	}

	public static TileState getEnum(int val) {
		TileState tileState;
		switch(val) {
			case 0 -> {
				tileState = EMPTY;
			}
			case 1 -> {
				tileState = GARBAGE;
			}
			case 2 -> {
				tileState = I;
			}
			case 3 -> {
				tileState = O;
			}
			case 4 -> {
				tileState = L;
			}
			case 5 -> {
				tileState = J;
			}
			case 6 -> {
				tileState = S;
			}
			case 7 -> {
				tileState = Z;
			}
			case 8 -> {
				tileState = T;
			}
			default -> {
				throw new IllegalArgumentException("Orientation value is not valid.");
			}
		}
		return tileState;
	}
}
