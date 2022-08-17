package game.pieces;

import game.pieces.util.Piece;

import java.util.*;

public class PieceFactory {

	private Map<String, PieceBuilder> map = new HashMap<>();
	private List<String> names = new ArrayList<>();

	public PieceFactory(List<PieceBuilder> info){
		for (PieceBuilder builder : info) {
			map.put(builder.name, builder);
			names.add(builder.name);
		}
	}

	public void registerPiece(String name, PieceBuilder info) {
		map.put(name, info);
	}

	public PieceBuilder getBuilder(String name) {
		PieceBuilder builder = map.get(name);
		return builder;
	}

	public Piece generatePiece(String name) {
		return map.get(name).generate();
	}

	public String[] getNames() {
		String[] out = names.stream().toArray(String[]::new);
		return out;
	}
}
