package game.pieces;

import game.pieces.util.Piece;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PieceFactory {

	private Map<String, PieceBuilder> map = new HashMap<>();
	private List<String> names = new ArrayList<>();
	private Map<String, Integer> nameIndices = new HashMap<>();

	public PieceFactory(List<PieceBuilder> info){
		for (PieceBuilder builder : info) {
			registerPiece(builder);
		}
	}

	public PieceFactory(String json) throws ParseException {
		this(PieceBuilder.parseJSON(json));
	}

	public PieceFactory(File file) throws IOException, ParseException {
		this(PieceBuilder.getPieces(file));
	}

	public void registerPiece(PieceBuilder info) {
		map.put(info.name, info);
		names.add(info.name);
		nameIndices.put(info.name, names.size() - 1);
	}

	public PieceBuilder getBuilder(String name) {
		return map.get(name);
	}

	public List<PieceBuilder> getBuilders() {
		return new ArrayList<>(map.values());
	}

	public Piece generatePiece(String name) {
		return map.get(name).generate();
	}

	public String[] getNames() {
		return names.stream().toArray((int value) -> {
			return new String[value];
		});
	}

	public int getIndex(String name) {
		return nameIndices.get(name);
	}

	public String getName(int index) {
		return names.get(index);
	}

	@SuppressWarnings(value = "unchecked")
	public String getJson() {
		JSONObject top = new JSONObject();
		JSONArray arr = new JSONArray();
		for (String name : names) {
			JSONObject obj = new JSONObject();
			PieceBuilder builder = map.get(name);
			obj.put("pieceName", builder.name);
			String colour;
			switch (builder.pieceColour) {
				case I -> {
					colour = "I";
				}
				case O -> {
					colour = "O";
				}
				case L -> {
					colour = "L";
				}
				case J -> {
					colour = "J";
				}
				case S -> {
					colour = "S";
				}
				case Z -> {
					colour = "Z";
				}
				case T -> {
					colour = "T";
				}
				default -> {
					throw new IllegalStateException("Illegal colour for piece.");
				}
			}
			obj.put("colour", colour);
			obj.put("size", builder.tileMapE.length);
			JSONArray arrayE = new JSONArray();
			JSONArray arrayR = new JSONArray();
			JSONArray arrayR2 = new JSONArray();
			JSONArray arrayR3 = new JSONArray();
			for (int index = 0; index < builder.tileMapE.length * builder.tileMapE[0].length; index++) {
				int i = builder.tileMapE.length - 1 - index / builder.tileMapE[0].length;
				int j = index % builder.tileMapE[0].length;
				arrayE.add(builder.tileMapE[i][j] ? 1 : 0);
				arrayR.add(builder.tileMapR[i][j] ? 1 : 0);
				arrayR2.add(builder.tileMapR2[i][j] ? 1 : 0);
				arrayR3.add(builder.tileMapR3[i][j] ? 1 : 0);
			}
			obj.put("E", arrayE);
			obj.put("R", arrayR);
			obj.put("R2", arrayR2);
			obj.put("R3", arrayR3);

			JSONArray kickCW = new JSONArray();
			JSONArray kickCCW = new JSONArray();
			JSONArray kickHalf = new JSONArray();

			for (int i = 0; i < builder.kickTableCW.length; i++) {
				JSONArray rotationKicks = new JSONArray();
				for (int j = 0; j < builder.kickTableCW[i].length; j++) {
					JSONArray kick = new JSONArray();
					for (int k = 0; k < builder.kickTableCW[i][j].length; k++) {
						kick.add(builder.kickTableCW[i][j][k]);
					}
					rotationKicks.add(kick);
				}
				kickCW.add(rotationKicks);
			}
			for (int i = 0; i < builder.kickTableCCW.length; i++) {
				JSONArray rotationKicks = new JSONArray();
				for (int j = 0; j < builder.kickTableCCW[i].length; j++) {
					JSONArray kick = new JSONArray();
					for (int k = 0; k < builder.kickTableCCW[i][j].length; k++) {
						kick.add(builder.kickTableCCW[i][j][k]);
					}
					rotationKicks.add(kick);
				}
				kickCCW.add(rotationKicks);
			}
			for (int i = 0; i < builder.kickTableHalf.length; i++) {
				JSONArray rotationKicks = new JSONArray();
				for (int j = 0; j < builder.kickTableHalf[i].length; j++) {
					JSONArray kick = new JSONArray();
					for (int k = 0; k < builder.kickTableHalf[i][j].length; k++) {
						kick.add(builder.kickTableHalf[i][j][k]);
					}
					rotationKicks.add(kick);
				}
				kickHalf.add(rotationKicks);
			}

			obj.put("kickCW", kickCW);
			obj.put("kickCCW", kickCCW);
			obj.put("kickHalf", kickHalf);

			arr.add(obj);
		}
		top.put("Pieces", arr);
		return top.toJSONString();
	}
}
