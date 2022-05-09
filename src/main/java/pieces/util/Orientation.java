package pieces.util;

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
}
