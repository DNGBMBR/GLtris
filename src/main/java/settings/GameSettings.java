package settings;

import game.SpinDetector;
import game.pieces.PieceBuilder;
import game.pieces.PieceFactory;
import org.ini4j.Wini;
import org.json.simple.parser.ParseException;
import util.Constants;
import util.Utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class GameSettings {
	//SINGLETONS ARE EVIL
	protected String filePath = "./game_settings.ini";

	protected static final int NUM_PREVIEWS = 6;
	protected static final String SRS_KICK_TABLE = "SRS"; //to be changed

	/*
	protected static final double INIT_GRAVITY = 0.1; //measured in G, where 1G = 1 tile per tick
	protected static final double GRAVITY_INCREASE = 0.008;
	protected static final int GRAVITY_INCREASE_INTERVAL = 3600; //measured in frames
	protected static final double LOCK_DELAY = 30.0; //measured in frames
	 */

	protected static final String SECTION_GENERAL = "general";
	protected static final String OPTION_BOARD_HEIGHT = "board_height";
	protected static final String OPTION_BOARD_WIDTH = "board_width";
	protected static final String OPTION_NUM_PREVIEWS = "previews";
	protected static final String OPTION_KICK_TABLE = "kick_table";
	protected static final String OPTION_SPIN_DETECTOR = "spin_detector";

	protected transient Wini properties;

	protected int numPreviews = NUM_PREVIEWS;
	protected PieceFactory kickTable;

	protected int boardHeight = Constants.BOARD_HEIGHT;
	protected int boardWidth = Constants.BOARD_WIDTH;
	protected SpinDetector spinDetector = SpinDetector.NONE;

	public GameSettings() {
		getProperties();
		try {
			File location = new File(Utils.getKickTableLocation(getKickTableLocationINI()));
			this.kickTable = new PieceFactory(PieceBuilder.getPieces(location));
		} catch (IOException | ParseException e) {
			throw new IllegalStateException("Could not find default kick table.");
		}
	}

	public GameSettings(int numPreviews, PieceFactory kickTable, int boardHeight, int boardWidth, SpinDetector spinDetector) {
		this.numPreviews = numPreviews;
		this.kickTable = kickTable;
		this.boardHeight = boardHeight;
		this.boardWidth = boardWidth;
		this.spinDetector = spinDetector;

		properties = getProperties();
	}

	public GameSettings(String filePath) throws IOException, ParseException {
		this.filePath = filePath;
		getProperties();
		this.numPreviews = getNumPreviewsINI();
		File kickTableLocation = new File(Utils.getKickTableLocation(getKickTableLocationINI()));
		this.kickTable = new PieceFactory(PieceBuilder.getPieces(kickTableLocation));
		this.boardHeight = getBoardHeightINI();
		this.boardWidth = getBoardWidthINI();
		this.spinDetector = getSpinDetectorINI();
	}

	protected Wini getProperties() {
		if (properties == null) {
			try {
				properties = new Wini(new File(filePath));
			} catch (IOException e) {
				setDefaultProperties();
			}
		}
		return properties;
	}

	protected Object getProperty(String section, String name) {
		Wini p = getProperties();

		String propertyString = p.get(section, name);
		if (propertyString == null) {
			setDefaultProperties();
			return null;
		}

		return propertyString;
	}

	public void setDefaultProperties() {
		properties = new Wini();
		properties.put(SECTION_GENERAL, OPTION_NUM_PREVIEWS, NUM_PREVIEWS);
		properties.put(SECTION_GENERAL, OPTION_KICK_TABLE, SRS_KICK_TABLE);

		properties.put(SECTION_GENERAL, OPTION_BOARD_HEIGHT, Constants.BOARD_HEIGHT);
		properties.put(SECTION_GENERAL, OPTION_BOARD_WIDTH, Constants.BOARD_WIDTH);

		properties.put(SECTION_GENERAL, OPTION_SPIN_DETECTOR, "T_SPIN");
	}

	public void saveSettings() {
		Wini p = getProperties();
		File destination = new File(filePath);

		try {
			if(!destination.exists()) {
				if(!destination.createNewFile()) {
					return;
				}
			}
			p.store(destination);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int getNumPreviewsINI() {
		Object propertyObject = getProperty(SECTION_GENERAL, OPTION_NUM_PREVIEWS);
		if (!(propertyObject instanceof String)) {
			setDefaultProperties();
			return NUM_PREVIEWS;
		}
		return Integer.parseInt((String) propertyObject);
	}

	private void setNumPreviewsINI(int numPreviews) {
		Wini p = getProperties();
		p.put(SECTION_GENERAL, OPTION_NUM_PREVIEWS, numPreviews);
	}

	private String getKickTableLocationINI() {
		Object propertyObject = getProperty(SECTION_GENERAL, OPTION_KICK_TABLE);
		if (!(propertyObject instanceof String)) {
			setDefaultProperties();
			return SRS_KICK_TABLE;
		}
		return (String) propertyObject;
	}

	private void setKickTableLocationINI(String location) {
		Wini p = getProperties();
		p.put(SECTION_GENERAL, OPTION_KICK_TABLE, location);
	}

	private int getBoardHeightINI() {
		Object propertyObject = getProperty(SECTION_GENERAL, OPTION_BOARD_HEIGHT);
		if (!(propertyObject instanceof String)) {
			setDefaultProperties();
			return Constants.BOARD_HEIGHT;
		}
		return Integer.parseInt((String) propertyObject);
	}

	private void setBoardHeightINI(int boardHeight) {
		Wini p = getProperties();
		p.put(SECTION_GENERAL, OPTION_BOARD_HEIGHT, boardHeight);
	}

	private int getBoardWidthINI() {
		Object propertyObject = getProperty(SECTION_GENERAL, OPTION_BOARD_WIDTH);
		if (!(propertyObject instanceof String)) {
			setDefaultProperties();
			return Constants.BOARD_WIDTH;
		}
		return Integer.parseInt((String) propertyObject);
	}

	private void setBoardWidthINI(int boardWidth) {
		Wini p = getProperties();
		p.put(SECTION_GENERAL, OPTION_BOARD_WIDTH, boardWidth);
	}

	private SpinDetector getSpinDetectorINI() {
		Object propertyObject = getProperty(SECTION_GENERAL, OPTION_SPIN_DETECTOR);
		if (!(propertyObject instanceof String)) {
			setDefaultProperties();
			return SpinDetector.T_SPIN;
		}
		return SpinDetector.getEnum((String) propertyObject);
	}

	private void setSpinDetectorINI(SpinDetector detector) {
		Wini p = getProperties();
		p.put(SECTION_GENERAL, OPTION_SPIN_DETECTOR, detector.name());
	}

	public int getNumPreviews() {
		return numPreviews;
	}

	public void setNumPreviews(int numPreviews) {
		if (this.numPreviews < 0) {
			throw new IllegalArgumentException("Number of previews must be greater than or equal to 0.");
		}
		this.numPreviews = numPreviews;
		setNumPreviewsINI(this.numPreviews);
	}

	public PieceFactory getKickTable() {
		return kickTable;
	}

	public void setKickTableLocation(String location) throws IOException, ParseException {
		File kickTableLocation = new File(Utils.getKickTableLocation(location));
		this.kickTable = new PieceFactory(PieceBuilder.getPieces(kickTableLocation));
		setKickTableLocationINI(location);
	}

	public int getBoardHeight() {
		return boardHeight;
	}

	public void setBoardHeight(int boardHeight) {
		if (boardHeight <= 0) {
			throw new IllegalArgumentException("Board height must be greater than 0.");
		}
		this.boardHeight = boardHeight;
		setBoardHeightINI(this.boardHeight);
	}

	public int getBoardWidth() {
		return boardWidth;
	}

	public void setBoardWidth(int boardWidth) {
		if (boardWidth <= 0) {
			throw new IllegalArgumentException("Board width must be greater than 0.");
		}
		this.boardWidth = boardWidth;
		setBoardWidthINI(this.boardWidth);
	}

	public String getKickTableLocation() {
		return getKickTableLocationINI();
	}

	public SpinDetector getSpinDetector() {
		return spinDetector;
	}

	public void setSpinDetector(SpinDetector spinDetector) {
		this.spinDetector = spinDetector;
		setSpinDetectorINI(spinDetector);
	}
}
