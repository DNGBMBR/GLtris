package settings;

import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;

public class GameSettings {
	private static final String PATH = "./game_settings.ini";

	private static final int NUM_PREVIEWS = 6;

	private static final double INIT_GRAVITY = 0.1; //measured in G, where 1G = 1 tile per tick
	private static final double GRAVITY_INCREASE = 0.008;
	private static final int GRAVITY_INCREASE_INTERVAL = 3600; //measured in frames
	private static final double LOCK_DELAY = 30.0; //measured in frames

	private static String SECTION_GENERAL = "general";
	private static String OPTION_BOARD_HEIGHT = "board_height";
	private static String OPTION_BOARD_WIDTH = "board_width";
	private static String OPTION_NUM_PREVIEWS = "previews";

	private static String SECTION_GRAVITY = "gravity";
	private static String OPTION_INIT_GRAVITY = "initial_gravity";
	private static String OPTION_GRAVITY_INCREASE = "gravity_increase";
	private static String OPTIONS_GRAVITY_INCREASE_INTERVAL = "gravity_increase_interval";
	private static String OPTION_LOCK_DELAY = "lock_delay";

	private static Wini properties;

	private GameSettings() {}

	private static Wini getProperties() {
		if (properties == null) {
			try {
				properties = new Wini(new File(PATH));
			} catch (IOException e) {
				setDefaultProperties();
			}
		}
		return properties;
	}

	private static Object getProperty(String section, String name) {
		Wini p = getProperties();

		String propertyString = p.get(section, name);
		if (propertyString == null) {
			setDefaultProperties();
			return null;
		}

		return propertyString;
	}

	public static void setDefaultProperties() {
		properties = new Wini();
		//properties.put(SECTION_GENERAL, OPTION_BOARD_HEIGHT, BOARD_HEIGHT);
		//properties.put(SECTION_GENERAL, OPTION_BOARD_WIDTH, BOARD_WIDTH);
		properties.put(SECTION_GENERAL, OPTION_NUM_PREVIEWS, NUM_PREVIEWS);

		properties.put(SECTION_GRAVITY, OPTION_INIT_GRAVITY, INIT_GRAVITY);
		properties.put(SECTION_GRAVITY, OPTION_GRAVITY_INCREASE, GRAVITY_INCREASE);
		properties.put(SECTION_GRAVITY, OPTIONS_GRAVITY_INCREASE_INTERVAL, GRAVITY_INCREASE_INTERVAL);
		properties.put(SECTION_GRAVITY, OPTION_LOCK_DELAY, LOCK_DELAY);
	}

	public static void saveSettings() {
		Wini p = getProperties();

		File destination = new File(PATH);

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

	public static int getNumPreviews() {
		Object propertyObject = getProperty(SECTION_GENERAL, OPTION_NUM_PREVIEWS);
		if (!(propertyObject instanceof String)) {
			setDefaultProperties();
			return NUM_PREVIEWS;
		}
		return Integer.parseInt((String) propertyObject);
	}

	public static void setNumPreviews(int numPreviews) {
		Wini p = getProperties();
		p.put(SECTION_GENERAL, OPTION_NUM_PREVIEWS, numPreviews);
	}

	public static double getInitGravity() {
		Object propertyObject = getProperty(SECTION_GRAVITY, OPTION_INIT_GRAVITY);
		if (!(propertyObject instanceof String)) {
			setDefaultProperties();
			return INIT_GRAVITY;
		}
		return Double.parseDouble((String) propertyObject);
	}

	public static void setInitGravity(double initGravity) {
		Wini p = getProperties();
		p.put(SECTION_GRAVITY, OPTION_INIT_GRAVITY, initGravity);
	}

	public static double getGravityIncrease() {
		Object propertyObject = getProperty(SECTION_GRAVITY, OPTION_GRAVITY_INCREASE);
		if (!(propertyObject instanceof String)) {
			setDefaultProperties();
			return GRAVITY_INCREASE;
		}
		return Double.parseDouble((String) propertyObject);
	}

	public static void setGravityIncrease(double gravityIncrease) {
		Wini p = getProperties();
		p.put(SECTION_GRAVITY, OPTION_GRAVITY_INCREASE, gravityIncrease);
	}

	public static int getGravityIncreaseInterval() {
		Object propertyObject = getProperty(SECTION_GRAVITY, OPTIONS_GRAVITY_INCREASE_INTERVAL);
		if (!(propertyObject instanceof String)) {
			setDefaultProperties();
			return GRAVITY_INCREASE_INTERVAL;
		}
		return Integer.parseInt((String) propertyObject);
	}

	public static void setGravityIncreaseInterval(int gravityIncreaseInterval) {
		Wini p = getProperties();
		p.put(SECTION_GRAVITY, OPTIONS_GRAVITY_INCREASE_INTERVAL, gravityIncreaseInterval);
	}

	public static double getLockDelay() {
		Object propertyObject = getProperty(SECTION_GRAVITY, OPTION_LOCK_DELAY);
		if (!(propertyObject instanceof String)) {
			setDefaultProperties();
			return LOCK_DELAY;
		}
		return Double.parseDouble((String) propertyObject);
	}

	public static void setLockDelay(double lockDelay) {
		Wini p = getProperties();
		p.put(SECTION_GRAVITY, OPTION_LOCK_DELAY, lockDelay);
	}
}
