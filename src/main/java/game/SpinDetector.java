package game;

import game.pieces.util.*;

import static game.SpinType.*;
import static game.pieces.util.Orientation.*;
import static game.pieces.util.Orientation.R;

public enum SpinDetector {
	NONE,
	T_SPIN,
	ALL_SPIN;

	public static SpinDetector getEnum(String name) {
		switch (name) {
			case "T_SPIN" -> {
				return T_SPIN;
			}
			case "ALL_SPIN" -> {
				return ALL_SPIN;
			}
			default -> {
				return NONE;
			}
		}
	}

	public static SpinDetection getDetection(SpinDetector type) {
		SpinDetection ret;
		switch (type) {
			case T_SPIN -> {
				ret = (Piece piece, TileState[][] board, int kickIndex) -> {
					int boardHeight = board.length;
					int boardWidth = board[0].length;
					int forwardCornerCount = 0;
					int cornerCount = 0;
					int tCenterX = piece.getBottomLeftX() + 1;
					int tCenterY = piece.getBottomLeftY() + 1;
					Orientation orientation = piece.getOrientation();
					if (tCenterX - 1 < 0 || tCenterY - 1 < 0 || tCenterX - 1 >= boardWidth || tCenterY - 1 >= boardHeight || board[tCenterY - 1][tCenterX - 1] != TileState.EMPTY) {
						if (orientation == R2 || orientation == R3) {
							forwardCornerCount++;
						}
						cornerCount++;
					}
					if (tCenterX - 1 < 0 || tCenterY + 1 >= boardHeight || tCenterX - 1 >= boardWidth || tCenterY + 1 < 0 || board[tCenterY + 1][tCenterX - 1] != TileState.EMPTY) {
						if (orientation == E || orientation == R3) {
							forwardCornerCount++;
						}
						cornerCount++;
					}
					if (tCenterX + 1 >= boardWidth || tCenterY - 1 < 0 || tCenterX + 1 < 0 || tCenterY - 1 >= boardHeight || board[tCenterY - 1][tCenterX + 1] != TileState.EMPTY) {
						if (orientation == R || orientation == R2) {
							forwardCornerCount++;
						}
						cornerCount++;
					}
					if (tCenterX + 1 >= boardWidth || tCenterY + 1 >= boardHeight || tCenterX + 1 < 0 || tCenterY + 1 < 0 || board[tCenterY + 1][tCenterX + 1] != TileState.EMPTY) {
						if (orientation == E || orientation == R) {
							forwardCornerCount++;
						}
						cornerCount++;
					}

					if (cornerCount >= 3) {
						//4 is the magic index in SRS that uses the t-spin triple kick
						if (kickIndex >= 4) {
							return SpinType.T_SPIN;
						}
						else if (forwardCornerCount < 2) {
							return SpinType.T_SPIN_MINI;
						}
						else {
							return SpinType.T_SPIN;
						}
					}
					return SpinType.NONE;
				};
			}
			case ALL_SPIN -> {
				ret = getAllSpinDetector();
			}
			default -> {
				ret = (Piece piece, TileState[][] board, int kickIndex) -> {
					return SpinType.NONE;
				};
			}
		}
		return ret;
	}

	private static SpinDetection getAllSpinDetector() {
		return (Piece piece, TileState[][] board, int kickIndex) -> {
			SpinType currentSpinType = SpinType.NONE;
			switch(piece.getPieceColour()) {
				case I -> {
					if (piece.testCollision(board, 1, 0) &&
						piece.testCollision(board, -1, 0) &&
						piece.testCollision(board, 0, -1)) {
						currentSpinType = SpinType.I_SPIN;
					}
				}
				case O -> {
					if (piece.testCollision(board, 1, 0) &&
						piece.testCollision(board, -1, 0) &&
						piece.testCollision(board, 0, -1)) {
						currentSpinType = SpinType.O_SPIN;
					}
				}
				case L -> {
					if (piece.testCollision(board, 1, 0) &&
						piece.testCollision(board, -1, 0) &&
						piece.testCollision(board, 0, -1)) {
						currentSpinType = SpinType.L_SPIN;
					}
				}
				case J -> {
					if (piece.testCollision(board, 1, 0) &&
						piece.testCollision(board, -1, 0) &&
						piece.testCollision(board, 0, -1)) {
						currentSpinType = SpinType.J_SPIN;
					}
				}
				case S -> {
					if (piece.testCollision(board, 1, 0) &&
						piece.testCollision(board, -1, 0) &&
						piece.testCollision(board, 0, -1)) {
						currentSpinType = SpinType.S_SPIN;
					}
				}
				case Z -> {
					if (piece.testCollision(board, 1, 0) &&
						piece.testCollision(board, -1, 0) &&
						piece.testCollision(board, 0, -1)) {
						currentSpinType = SpinType.Z_SPIN;
					}
				}
				case T -> {
					int boardHeight = board.length;
					int boardWidth = board[0].length;
					int forwardCornerCount = 0;
					int cornerCount = 0;
					int tCenterX = piece.getBottomLeftX() + 1;
					int tCenterY = piece.getBottomLeftY() + 1;
					Orientation orientation = piece.getOrientation();
					if (tCenterX - 1 < 0 || tCenterY - 1 < 0 || tCenterX - 1 >= boardWidth || tCenterY - 1 >= boardHeight || board[tCenterY - 1][tCenterX - 1] != TileState.EMPTY) {
						if (orientation == R2 || orientation == R3) {
							forwardCornerCount++;
						}
						cornerCount++;
					}
					if (tCenterX - 1 < 0 || tCenterY + 1 >= boardHeight || tCenterX - 1 >= boardWidth || tCenterY + 1 < 0 || board[tCenterY + 1][tCenterX - 1] != TileState.EMPTY) {
						if (orientation == E || orientation == R3) {
							forwardCornerCount++;
						}
						cornerCount++;
					}
					if (tCenterX + 1 >= boardWidth || tCenterY - 1 < 0 || tCenterX + 1 < 0 || tCenterY - 1 >= boardHeight || board[tCenterY - 1][tCenterX + 1] != TileState.EMPTY) {
						if (orientation == R || orientation == R2) {
							forwardCornerCount++;
						}
						cornerCount++;
					}
					if (tCenterX + 1 >= boardWidth || tCenterY + 1 >= boardHeight || tCenterX + 1 < 0 || tCenterY + 1 < 0 || board[tCenterY + 1][tCenterX + 1] != TileState.EMPTY) {
						if (orientation == E || orientation == R) {
							forwardCornerCount++;
						}
						cornerCount++;
					}

					if (cornerCount >= 3) {
						//4 is the magic index in SRS that uses the t-spin triple kick
						if (kickIndex >= 4) {
							currentSpinType = SpinType.T_SPIN;
						}
						else if (forwardCornerCount < 2) {
							currentSpinType = SpinType.T_SPIN_MINI;
						}
						else {
							currentSpinType = SpinType.T_SPIN;
						}
					}
				}
				default -> {
					currentSpinType = SpinType.NONE;
				}
			}
			return currentSpinType;
		};
	}
}
