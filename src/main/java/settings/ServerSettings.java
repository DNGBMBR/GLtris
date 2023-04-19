package settings;

import game.SpinDetector;
import game.pieces.PieceFactory;
import org.json.simple.parser.ParseException;
import util.Utils;

import java.io.*;
import java.util.Properties;

/**
 * This class is meant for using a .properties file with the server, the other one is for the game itself
 */
public class ServerSettings {
	public static final String PORT = "port";
	public static final String NUM_PREVIEWS = "num_previews";
	public static final String KICK_TABLE = "kick_table";
	public static final String BOARD_HEIGHT = "board_height";
	public static final String BOARD_WIDTH = "board_width";
	public static final String SPIN_DETECTOR = "spin_detector";
	protected int port;
	protected int numPreviews;
	protected String kickTableLocation;
	protected PieceFactory kickTable;
	protected int boardWidth;
	protected int boardHeight;
	protected SpinDetector spinDetector;

	public ServerSettings() {
		this.port = 2678;
		this.numPreviews = 5;
		this.kickTableLocation = "TETR";
		try {
			File kickTableFile = new File(Utils.getKickTableLocation(this.kickTableLocation));
			this.kickTable = new PieceFactory(kickTableFile);
		} catch (IOException | ParseException e) {
			throw new IllegalStateException("Critical Error: Could not read default kick table \"TETR\".");
		}
		this.boardHeight = 20;
		this.boardWidth = 10;
		this.spinDetector = SpinDetector.NONE;
	}

	public ServerSettings(File file) throws IOException, ParseException {
		Properties properties = new Properties();
		FileInputStream in = new FileInputStream(file);
		properties.load(in);
		this.port = Integer.parseInt(properties.getProperty(PORT));
		this.numPreviews = Integer.parseInt(properties.getProperty(NUM_PREVIEWS));
		this.kickTableLocation = properties.getProperty(KICK_TABLE);
		File kickTableFile = new File(Utils.getKickTableLocation(this.kickTableLocation));
		this.kickTable = new PieceFactory(kickTableFile);
		this.boardWidth = Integer.parseInt(properties.getProperty(BOARD_WIDTH));
		this.boardHeight = Integer.parseInt(properties.getProperty(BOARD_HEIGHT));
		this.spinDetector = SpinDetector.getEnum(properties.getProperty(SPIN_DETECTOR));
		in.close();
	}

	public void writeSettings(File file) throws IOException {
		Properties properties = new Properties();
		FileOutputStream out = new FileOutputStream(file);
		properties.setProperty(PORT, String.valueOf(this.port));
		properties.setProperty(NUM_PREVIEWS, String.valueOf(this.numPreviews));
		properties.setProperty(KICK_TABLE, this.kickTableLocation);
		properties.setProperty(BOARD_HEIGHT, String.valueOf(this.boardHeight));
		properties.setProperty(BOARD_WIDTH, String.valueOf(this.boardWidth));
		properties.setProperty(SPIN_DETECTOR, this.spinDetector.name());
		properties.store(out, "");
		out.close();
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getNumPreviews() {
		return numPreviews;
	}

	public void setNumPreviews(int numPreviews) {
		this.numPreviews = numPreviews;
	}

	public PieceFactory getKickTable() {
		return kickTable;
	}

	public void setKickTable(PieceFactory kickTable) {
		this.kickTable = kickTable;
	}

	public int getBoardWidth() {
		return boardWidth;
	}

	public void setBoardWidth(int boardWidth) {
		this.boardWidth = boardWidth;
	}

	public int getBoardHeight() {
		return boardHeight;
	}

	public void setBoardHeight(int boardHeight) {
		this.boardHeight = boardHeight;
	}

	public SpinDetector getSpinDetector() {
		return spinDetector;
	}

	public void setSpinDetector(SpinDetector spinDetector) {
		this.spinDetector = spinDetector;
	}
}
