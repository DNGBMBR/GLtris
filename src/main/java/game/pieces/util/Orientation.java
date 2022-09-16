package game.pieces.util;

public enum Orientation {
	//notation: cyclic group of order 4, E identity, R CW rotate
	E(0), R(1), R2(2), R3(3);

	private int val;

	Orientation(int val) {
		this.val = val;
	}

	public int getVal() {
		return this.val;
	}

	public static Orientation getEnum(int val) {
		Orientation orientation;
		switch(val) {
			case 0 -> {
				orientation = E;
			}
			case 1 -> {
				orientation = R;
			}
			case 2 -> {
				orientation = R2;
			}
			case 3 -> {
				orientation = R3;
			}
			default -> {
				throw new IllegalArgumentException("Orientation value is not valid.");
			}
		}
		return orientation;
	}
}
