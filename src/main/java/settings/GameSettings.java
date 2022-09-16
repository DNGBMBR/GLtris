package settings;

import game.pieces.PieceBuilder;
import game.pieces.PieceFactory;
import org.ini4j.Wini;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class GameSettings {
	//SINGLETONS ARE EVIL
	protected String filePath = "./game_settings.ini";

	protected static final int NUM_PREVIEWS = 6;
	protected static final String SRS_KICK_TABLE = "./kicks/SRS.json"; //to be changed

	protected static final double INIT_GRAVITY = 0.1; //measured in G, where 1G = 1 tile per tick
	protected static final double GRAVITY_INCREASE = 0.008;
	protected static final int GRAVITY_INCREASE_INTERVAL = 3600; //measured in frames
	protected static final double LOCK_DELAY = 30.0; //measured in frames

	protected static final String SECTION_GENERAL = "general";
	protected static final String OPTION_BOARD_HEIGHT = "board_height";
	protected static final String OPTION_BOARD_WIDTH = "board_width";
	protected static final String OPTION_NUM_PREVIEWS = "previews";
	protected static final String OPTION_KICK_TABLE = "kick_table";

	protected static final String SECTION_GRAVITY = "gravity";
	protected static final String OPTION_INIT_GRAVITY = "initial_gravity";
	protected static final String OPTION_GRAVITY_INCREASE = "gravity_increase";
	protected static final String OPTIONS_GRAVITY_INCREASE_INTERVAL = "gravity_increase_interval";
	protected static final String OPTION_LOCK_DELAY = "lock_delay";

	protected transient Wini properties;

	protected int numPreviews = NUM_PREVIEWS;
	protected PieceFactory kickTable;

	protected double initGravity = INIT_GRAVITY;
	protected double gravityIncrease = GRAVITY_INCREASE;
	protected int gravityIncreaseInterval = GRAVITY_INCREASE_INTERVAL;
	protected double lockDelay = LOCK_DELAY;

	public GameSettings() {
		getProperties();
		try {
			this.kickTable = new PieceFactory(PieceBuilder.getPieces(new File(getKickTableLocationINI())));
		} catch (IOException | ParseException e) {
			throw new IllegalStateException("Could not find default kick table.");
		}
	}

	public GameSettings(int numPreviews, PieceFactory kickTable, double initGravity, double gravityIncrease, int gravityIncreaseInterval, double lockDelay) {
		this.numPreviews = numPreviews;
		this.kickTable = kickTable;
		this.initGravity = initGravity;
		this.gravityIncrease = gravityIncrease;
		this.gravityIncreaseInterval = gravityIncreaseInterval;
		this.lockDelay = lockDelay;

		properties = getProperties();
	}

	public GameSettings(String filePath) throws IOException, ParseException {
		this.filePath = filePath;
		getProperties();
		this.numPreviews = getNumPreviewsINI();
		this.kickTable = new PieceFactory(PieceBuilder.getPieces(new File(getKickTableLocationINI())));

		this.initGravity = getInitGravityINI();
		this.gravityIncrease = getGravityIncreaseINI();
		this.gravityIncreaseInterval = getGravityIncreaseIntervalINI();
		this.lockDelay = getLockDelayINI();
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
		//properties.put(SECTION_GENERAL, OPTION_BOARD_HEIGHT, BOARD_HEIGHT);
		//properties.put(SECTION_GENERAL, OPTION_BOARD_WIDTH, BOARD_WIDTH);
		properties.put(SECTION_GENERAL, OPTION_NUM_PREVIEWS, NUM_PREVIEWS);
		properties.put(SECTION_GENERAL, OPTION_KICK_TABLE, SRS_KICK_TABLE);

		properties.put(SECTION_GRAVITY, OPTION_INIT_GRAVITY, INIT_GRAVITY);
		properties.put(SECTION_GRAVITY, OPTION_GRAVITY_INCREASE, GRAVITY_INCREASE);
		properties.put(SECTION_GRAVITY, OPTIONS_GRAVITY_INCREASE_INTERVAL, GRAVITY_INCREASE_INTERVAL);
		properties.put(SECTION_GRAVITY, OPTION_LOCK_DELAY, LOCK_DELAY);
	}

	public void saveSettings() {
		Wini p = getProperties();
		setNumPreviewsINI(this.numPreviews);
		setInitGravityINI(this.initGravity);
		setGravityIncreaseINI(this.gravityIncrease);
		setGravityIncreaseIntervalINI(this.gravityIncreaseInterval);
		setLockDelayINI(this.lockDelay);

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

	private double getInitGravityINI() {
		Object propertyObject = getProperty(SECTION_GRAVITY, OPTION_INIT_GRAVITY);
		if (!(propertyObject instanceof String)) {
			setDefaultProperties();
			return INIT_GRAVITY;
		}
		return Double.parseDouble((String) propertyObject);
	}

	private void setInitGravityINI(double initGravity) {
		Wini p = getProperties();
		p.put(SECTION_GRAVITY, OPTION_INIT_GRAVITY, initGravity);
	}

	private double getGravityIncreaseINI() {
		Object propertyObject = getProperty(SECTION_GRAVITY, OPTION_GRAVITY_INCREASE);
		if (!(propertyObject instanceof String)) {
			setDefaultProperties();
			return GRAVITY_INCREASE;
		}
		return Double.parseDouble((String) propertyObject);
	}

	private void setGravityIncreaseINI(double gravityIncrease) {
		Wini p = getProperties();
		p.put(SECTION_GRAVITY, OPTION_GRAVITY_INCREASE, gravityIncrease);
	}

	private int getGravityIncreaseIntervalINI() {
		Object propertyObject = getProperty(SECTION_GRAVITY, OPTIONS_GRAVITY_INCREASE_INTERVAL);
		if (!(propertyObject instanceof String)) {
			setDefaultProperties();
			return GRAVITY_INCREASE_INTERVAL;
		}
		return Integer.parseInt((String) propertyObject);
	}

	private void setGravityIncreaseIntervalINI(int gravityIncreaseInterval) {
		Wini p = getProperties();
		p.put(SECTION_GRAVITY, OPTIONS_GRAVITY_INCREASE_INTERVAL, gravityIncreaseInterval);
	}

	private double getLockDelayINI() {
		Object propertyObject = getProperty(SECTION_GRAVITY, OPTION_LOCK_DELAY);
		if (!(propertyObject instanceof String)) {
			setDefaultProperties();
			return LOCK_DELAY;
		}
		return Double.parseDouble((String) propertyObject);
	}

	private void setLockDelayINI(double lockDelay) {
		Wini p = getProperties();
		p.put(SECTION_GRAVITY, OPTION_LOCK_DELAY, lockDelay);
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

	/*
	public static int getBoardHeight() {
		Object propertyObject = getProperty(SECTION_GENERAL, OPTION_BOARD_HEIGHT);
		if (!(propertyObject instanceof String)) {
			setDefaultProperties();
			return BOARD_HEIGHT;
		}
		return Integer.parseInt((String) propertyObject);
	}

	public static void setBoardHeight(int boardHeight) {
		Wini p = getProperties();
		p.put(SECTION_GENERAL, OPTION_BOARD_HEIGHT, boardHeight);
	}

	public static int getBoardWidth() {
		Object propertyObject = getProperty(SECTION_GENERAL, OPTION_BOARD_WIDTH);
		if (!(propertyObject instanceof String)) {
			setDefaultProperties();
			return BOARD_WIDTH;
		}
		return Integer.parseInt((String) propertyObject);
	}

	public static void setBoardWidth(int boardWidth) {
		Wini p = getProperties();
		p.put(SECTION_GENERAL, OPTION_BOARD_WIDTH, boardWidth);
	}
	 */

	public int getNumPreviews() {
		return numPreviews;
	}

	public void setNumPreviews(int numPreviews) {
		this.numPreviews = numPreviews;
	}

	public double getInitGravity() {
		return initGravity;
	}

	public void setInitGravity(double initGravity) {
		this.initGravity = initGravity;
	}

	public double getGravityIncrease() {
		return gravityIncrease;
	}

	public void setGravityIncrease(double gravityIncrease) {
		this.gravityIncrease = gravityIncrease;
	}

	public int getGravityIncreaseInterval() {
		return gravityIncreaseInterval;
	}

	public void setGravityIncreaseInterval(int gravityIncreaseInterval) {
		this.gravityIncreaseInterval = gravityIncreaseInterval;
	}

	public double getLockDelay() {
		return lockDelay;
	}

	public void setLockDelay(double lockDelay) {
		this.lockDelay = lockDelay;
	}

	public PieceFactory getKickTable() {
		return kickTable;
	}

	public void setKickTableLocation(String location) throws IOException, ParseException {
		this.kickTable = new PieceFactory(PieceBuilder.getPieces(new File(location)));
		Wini p = getProperties();
		p.put(SECTION_GENERAL, OPTION_KICK_TABLE, location);
	}
}
