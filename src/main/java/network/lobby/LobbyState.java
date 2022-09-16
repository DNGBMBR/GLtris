package network.lobby;

public enum LobbyState {
	LOBBY(0), IN_GAME(1);

	private int val;

	LobbyState(int val) {
		this.val = val;
	}

	public int getVal() {
		return this.val;
	}

	public static LobbyState getEnum(int val) {
		switch(val) {
			case 0 -> {
				return LOBBY;
			}
			case 1 -> {
				return IN_GAME;
			}
			default -> {
				throw new IllegalArgumentException("Invalid enum value for value " + val + ".");
			}
		}
	}
}
