package util;

import org.ini4j.Wini;

import java.io.*;

public class LocalSettings {
	//units are in seconds
	private static final double DEFAULT_ARR = 0.0;
	private static final double DEFAULT_DAS = 8.0;
	private static final double DEFAULT_SDF = 1.0;

	private static final String PATH = "./settings.ini";

	private static final String SECTION_HANDLING = "handling";
	private static final String OPTION_SDF = "sdf";
	private static final String OPTION_ARR = "arr";
	private static final String OPTION_DAS = "das";

	//this is gonna have to go into a different config setting, or is labelled in the .ini as "do no touch".
	private static final String SECTION_KEY_BINDINGS = "key_bindings";
	private static final String OPTION_LEFT = "left";
	private static final String OPTION_RIGHT = "right";
	private static final String OPTION_SOFT_DROP = "soft_drop";
	private static final String OPTION_HARD_DROP = "hard_drop";
	private static final String OPTION_CW = "rotate_cw";
	private static final String OPTION_CCW = "rotate_ccw";
	private static final String OPTION_180 = "rotate_180";

	private LocalSettings() {}

	private static Wini properties;

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
		//thanks, https://stackoverflow.com/questions/8775303/read-properties-file-outside-jar-file
		Wini p = getProperties();

		String propertyString = p.get(section, name);
		if (propertyString == null) {
			setDefaultProperties();
			return null;
		}

		return propertyString;
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

	public static void setDefaultProperties() {
		properties = new Wini();
		properties.put(SECTION_HANDLING, OPTION_SDF, DEFAULT_SDF);
		properties.put(SECTION_HANDLING, OPTION_ARR, DEFAULT_ARR);
		properties.put(SECTION_HANDLING, OPTION_DAS, DEFAULT_DAS);
	}

	public static double getSDF() {
		Object propertyObject = getProperty(SECTION_HANDLING, OPTION_SDF);
		if (!(propertyObject instanceof String)) {
			setDefaultProperties();
			return DEFAULT_SDF;
		}
		return Double.parseDouble((String) propertyObject);
	}

	public static void setSDF(double sdf) {
		Wini p = getProperties();
		p.put(SECTION_HANDLING, OPTION_SDF, sdf);
	}

	public static double getARR() {
		Object propertyObject = getProperty(SECTION_HANDLING, OPTION_ARR);
		if (!(propertyObject instanceof String)) {
			System.out.println("did not store it correctly");
			properties.put(SECTION_HANDLING, OPTION_ARR, DEFAULT_ARR);
			return DEFAULT_ARR;
		}
		return Double.parseDouble((String) propertyObject);
	}

	public static void setARR(double arr) {
		Wini p = getProperties();
		p.put(SECTION_HANDLING, OPTION_ARR, arr);
	}

	public static double getDAS() {
		Object propertyObject = getProperty(SECTION_HANDLING, OPTION_DAS);
		if (!(propertyObject instanceof String)) {
			properties.put(SECTION_HANDLING, OPTION_DAS, DEFAULT_DAS);
			return DEFAULT_DAS;
		}
		return Double.parseDouble((String) propertyObject);
	}

	public static void setDAS(double das) {
		Wini p = getProperties();
		p.put(SECTION_HANDLING, OPTION_DAS, das);
	}
}
