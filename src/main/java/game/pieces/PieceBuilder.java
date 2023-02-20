package game.pieces;

import game.pieces.util.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PieceBuilder {
	protected int spawnBottomLeftX;
	protected int spawnBottomLeftY;
	protected boolean[][] tileMapE;
	protected boolean[][] tileMapR;
	protected boolean[][] tileMapR2;
	protected boolean[][] tileMapR3;
	protected int[][][] kickTableCW;
	protected int[][][] kickTableCCW;
	protected int[][][] kickTableHalf;
	protected PieceColour pieceColour;
	protected String name;

	public PieceBuilder(String name, PieceColour pieceColour,
						int spawnBottomLeftX, int spawnBottomLeftY,
						boolean[][] tileMapE, boolean[][] tileMapR, boolean[][] tileMapR2, boolean[][] tileMapR3,
						int[][][] kickTableCW, int[][][] kickTableCCW, int[][][] kickTableHalf) {
		this.spawnBottomLeftX = spawnBottomLeftX;
		this.spawnBottomLeftY = spawnBottomLeftY;
		this.tileMapE = tileMapE;
		this.tileMapR = tileMapR;
		this.tileMapR2 = tileMapR2;
		this.tileMapR3 = tileMapR3;
		this.kickTableCW = kickTableCW;
		this.kickTableCCW = kickTableCCW;
		this.kickTableHalf = kickTableHalf;
		this.pieceColour = pieceColour;
		this.name = name;
	}

	public PieceBuilder(JSONObject json) {
		String pieceName = (String) json.get("pieceName");
		String pieceColour = (String) json.get("colour");
		PieceColour colour = null;
		switch(pieceColour) {
			case "I" -> {
				colour = PieceColour.I;
			}
			case "O" -> {
				colour = PieceColour.O;
			}
			case "L" -> {
				colour = PieceColour.L;
			}
			case "J" -> {
				colour = PieceColour.J;
			}
			case "S" -> {
				colour = PieceColour.S;
			}
			case "Z" -> {
				colour = PieceColour.Z;
			}
			case "T" -> {
				colour = PieceColour.T;
			}
		}

		//double cast???
		int tileMapSize = (int) (long) json.get("size");
		boolean[][] tileMapE = new boolean[tileMapSize][tileMapSize];
		boolean[][] tileMapR = new boolean[tileMapSize][tileMapSize];
		boolean[][] tileMapR2 = new boolean[tileMapSize][tileMapSize];
		boolean[][] tileMapR3 = new boolean[tileMapSize][tileMapSize];

		JSONArray tileMapEJson = (JSONArray) json.get("E");
		JSONArray tileMapRJson = (JSONArray) json.get("R");
		JSONArray tileMapR2Json = (JSONArray) json.get("R2");
		JSONArray tileMapR3Json = (JSONArray) json.get("R3");

		//vertically flip the pieces
		for (int i = 0; i < tileMapEJson.size(); i++) {
			tileMapE[tileMapSize - 1 - i / tileMapSize][i % tileMapSize] = (int) (long) tileMapEJson.get(i) != 0;
		}
		for (int i = 0; i < tileMapRJson.size(); i++) {
			tileMapR[tileMapSize - 1 - i / tileMapSize][i % tileMapSize] = (int) (long) tileMapRJson.get(i) != 0;
		}
		for (int i = 0; i < tileMapR2Json.size(); i++) {
			tileMapR2[tileMapSize - 1 - i / tileMapSize][i % tileMapSize] = (int) (long) tileMapR2Json.get(i) != 0;
		}
		for (int i = 0; i < tileMapR3Json.size(); i++) {
			tileMapR3[tileMapSize - 1 - i / tileMapSize][i % tileMapSize] = (int) (long) tileMapR3Json.get(i) != 0;
		}

		JSONArray kickCWJson = (JSONArray) json.get("kickCW");
		JSONArray kickCCWJson = (JSONArray) json.get("kickCCW");
		JSONArray kickHalfJson = (JSONArray) json.get("kickHalf");

		//this looks quite bad
		int[][][] kickCW = new int[4][][];
		int[][][] kickCCW = new int[4][][];
		int[][][] kickHalf = new int[4][][];

		for (int i = 0; i < kickCWJson.size(); i++) {
			JSONArray array1 = (JSONArray) kickCWJson.get(i);
			kickCW[i] = new int[array1.size()][];
			for (int j = 0; j < array1.size(); j++) {
				JSONArray array2 = (JSONArray) array1.get(j);
				kickCW[i][j] = new int[2];
				for (int k = 0; k < array2.size(); k++) {
					kickCW[i][j][k] = (int) (long) array2.get(k);
				}
			}
		}
		for (int i = 0; i < kickCCWJson.size(); i++) {
			JSONArray array1 = (JSONArray) kickCCWJson.get(i);
			kickCCW[i] = new int[((JSONArray) kickCCWJson.get(i)).size()][];
			for (int j = 0; j < array1.size(); j++) {
				JSONArray array2 = (JSONArray) array1.get(j);
				kickCCW[i][j] = new int[2];
				for (int k = 0; k < array2.size(); k++) {
					kickCCW[i][j][k] = (int) (long) array2.get(k);
				}
			}
		}
		for (int i = 0; i < kickHalfJson.size(); i++) {
			JSONArray array1 = (JSONArray) kickHalfJson.get(i);
			kickHalf[i] = new int[((JSONArray) kickHalfJson.get(i)).size()][];
			for (int j = 0; j < array1.size(); j++) {
				JSONArray array2 = (JSONArray) array1.get(j);
				kickHalf[i][j] = new int[2];
				for (int k = 0; k < array2.size(); k++) {
					kickHalf[i][j][k] = (int) (long) array2.get(k);
				}
			}
		}

		this.name = pieceName;
		this.pieceColour = colour;
		this.tileMapE = tileMapE;
		this.tileMapR = tileMapR;
		this.tileMapR2 = tileMapR2;
		this.tileMapR3 = tileMapR3;
		this.kickTableCW = kickCW;
		this.kickTableCCW = kickCCW;
		this.kickTableHalf = kickHalf;
	}

	public static List<PieceBuilder> getPieces(File path) throws IOException, ParseException {
		String jsonString = Files.readString(Path.of(String.valueOf(path)));
		return parseJSON(jsonString);
	}

	public static List<PieceBuilder> parseJSON(String jsonString) throws ParseException{
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
		JSONArray pieces = (JSONArray) jsonObject.get("Pieces");
		List<PieceBuilder> pieceBuilders = new ArrayList<>();
		for (Object object : pieces) {
			if (object instanceof JSONObject json) {
				PieceBuilder builder = new PieceBuilder(json);
				pieceBuilders.add(builder);
			}
		}
		return pieceBuilders;
	}

	public Piece generate() {
		return new Piece(name, pieceColour, spawnBottomLeftX, spawnBottomLeftY, Orientation.E,
		tileMapE, tileMapR, tileMapR2, tileMapR3,
		kickTableCW, kickTableCCW, kickTableHalf) {};
	}

	public String getName() {
		return name;
	}

	public PieceColour getPieceColour() {
		return this.pieceColour;
	}

	public int getSpawnBottomLeftX() {
		return spawnBottomLeftX;
	}

	public int getSpawnBottomLeftY() {
		return spawnBottomLeftY;
	}

	public boolean[][] getTileMapE() {
		return tileMapE;
	}

	public boolean[][] getTileMapR() {
		return tileMapR;
	}

	public boolean[][] getTileMapR2() {
		return tileMapR2;
	}

	public boolean[][] getTileMapR3() {
		return tileMapR3;
	}

	public int[][][] getKickTableCW() {
		return kickTableCW;
	}

	public int[][][] getKickTableCCW() {
		return kickTableCCW;
	}

	public int[][][] getKickTableHalf() {
		return kickTableHalf;
	}

	public void setSpawnBottomLeftX(int spawnBottomLeftX) {
		this.spawnBottomLeftX = spawnBottomLeftX;
	}

	public void setSpawnBottomLeftY(int spawnBottomLeftY) {
		this.spawnBottomLeftY = spawnBottomLeftY;
	}
}
