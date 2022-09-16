package settings;

import game.pieces.PieceBuilder;
import game.pieces.PieceFactory;
import org.ini4j.Wini;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class LobbySettings extends GameSettings {
	private static final int FIRST_TO = 3;

	private static final String SECTION_LOBBY = "lobby";
	private static final String OPTION_FIRST_TO = "first_to";

	protected int firstTo = FIRST_TO;

	public LobbySettings() {
		super();
	}

	public LobbySettings(int numPreviews, PieceFactory kickTable, double initGravity, double gravityIncrease, int gravityIncreaseInterval, double lockDelay, int firstTo) {
		super(numPreviews, kickTable, initGravity, gravityIncrease, gravityIncreaseInterval, lockDelay);
		this.firstTo = firstTo;

		properties = getProperties();
	}

	public LobbySettings(String filePath) throws IOException, ParseException {
		super(filePath);
		this.firstTo = getFirstToINI();
	}

	@Override
	public void setDefaultProperties() {
		super.setDefaultProperties();
		properties.put(SECTION_LOBBY, OPTION_FIRST_TO, FIRST_TO);
	}

	private int getFirstToINI() {
		Object propertyObject = super.getProperty(SECTION_LOBBY, OPTION_FIRST_TO);
		if (!(propertyObject instanceof String)) {
			setDefaultProperties();
			return FIRST_TO;
		}
		return Integer.parseInt((String) propertyObject);
	}

	private void setFirstToINI(int firstTo) {
		Wini p = getProperties();
		p.put(SECTION_LOBBY, OPTION_FIRST_TO, firstTo);
	}

	public int getFirstTo() {
		return firstTo;
	}

	public void setFirstTo(int firstTo) {
		this.firstTo = firstTo;
	}
}
