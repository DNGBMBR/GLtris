package scenes;

import menu.component.Frame;
import menu.component.TopFrame;
import menu.widgets.*;
import network.lobby.GameClient;
import org.json.simple.parser.ParseException;
import render.Shader;
import render.batch.WidgetBatch;
import render.manager.ResourceManager;
import render.manager.TextRenderer;
import render.texture.TextureNineSlice;
import settings.GameSettings;
import util.Constants;

import java.io.*;

public class GameSetupScene extends Scene{
	private static final double FRAME_SETTINGS_WIDTH = 1000;
	private static final double FRAME_SETTINGS_HEIGHT = 720;
	private static final double FRAME_SETTINGS_X_POS = (Constants.VIEWPORT_W - FRAME_SETTINGS_WIDTH) * 0.5;
	private static final double FRAME_SETTINGS_Y_POS = (Constants.VIEWPORT_H - FRAME_SETTINGS_HEIGHT) * 0.5;

	public static final String PREVIEWS = "Previews";
	public static final String BOARD_HEIGHT = "Board Height";
	public static final String BOARD_WIDTH = "Board Width";
	public static final String KICK_TABLE = "Kick Table";

	public static final int FONT_SIZE = 24;
	public static final int X_POS_TEXT_FIELD = 20;

	private Scene nextScene;

	private Shader blockShader = ResourceManager.getShaderByName("shaders/block_vertex.glsl", "shaders/block_fragment.glsl");
	private TextureNineSlice widgetTexture = ResourceManager.getTextureNineSliceByName("images/widgets.png");
	private WidgetBatch batch = new WidgetBatch(600);

	private TextRenderer textRenderer = TextRenderer.getInstance();

	private TopFrame topFrame;
	private Frame settingsFrame;

	GameSettings settings;

	TextField textFieldNumPreviews;
	TextField textFieldBoardHeight;
	TextField textFieldBoardWidth;
	DropDownMenu dropDownKickTable;

	GameSetupScene(long windowID, GameClient client) {
		super(windowID, client);
		topFrame = new TopFrame(Constants.VIEWPORT_W, Constants.VIEWPORT_H, true);
		settingsFrame = new Frame(FRAME_SETTINGS_X_POS, FRAME_SETTINGS_Y_POS,
				FRAME_SETTINGS_WIDTH, FRAME_SETTINGS_HEIGHT,
				true, true, 1000);

		try {
			settings = new GameSettings("./game_settings.ini");
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			System.err.println("Could not find game settings. Using defaults.");
			settings = new GameSettings();
		}

		int numPreviews = settings.getNumPreviews();
		int boardHeight = settings.getBoardHeight();
		int boardWidth = settings.getBoardWidth();
		String kickTableLocation = settings.getKickTableLocation();

		textFieldNumPreviews = new TextField(X_POS_TEXT_FIELD + PREVIEWS.length() * FONT_SIZE, 600, 200, 50, true,
			PREVIEWS, String.valueOf(numPreviews), FONT_SIZE, 0.0f, 0.0f, 0.0f);
		textFieldBoardHeight = new TextField(X_POS_TEXT_FIELD + BOARD_HEIGHT.length() * FONT_SIZE, 500, 200, 50, true,
			BOARD_HEIGHT, String.valueOf(boardHeight), FONT_SIZE, 0.0f, 0.0f, 0.0f);
		textFieldBoardWidth = new TextField(X_POS_TEXT_FIELD + BOARD_WIDTH.length() * FONT_SIZE, 425, 200, 50, true,
			BOARD_WIDTH, String.valueOf(boardWidth), FONT_SIZE, 0.0f, 0.0f, 0.0f);
		dropDownKickTable = new DropDownMenu(X_POS_TEXT_FIELD + KICK_TABLE.length() * FONT_SIZE, 350, 600, 50,
			KICK_TABLE, kickTableLocation, true);

		File kickDirectory = new File("./kicks");
		if (kickDirectory.isDirectory()) {
			File[] files = kickDirectory.listFiles((File dir, String name) -> {
				if (name.length() < ".json".length()) {
					return false;
				}
				return name.endsWith(".json");
			});
			if (files != null) {
				for (File file : files) {
					String fileName = file.getName();
					String kickTableName = fileName.substring(0, fileName.length() - ".json".length());
					dropDownKickTable.addSelection(kickTableName, (double mouseX, double mouseY, int button, int action, int mods) -> {
						dropDownKickTable.setTopText(kickTableName);
					});
				}
			}
		}

		settingsFrame.addComponent(textFieldNumPreviews);
		settingsFrame.addComponent(textFieldBoardHeight);
		settingsFrame.addComponent(textFieldBoardWidth);
		settingsFrame.addComponent(dropDownKickTable);

		topFrame.addComponent(settingsFrame);

		topFrame.addComponent(new Button((Constants.VIEWPORT_W - 300) * 0.5, FRAME_SETTINGS_Y_POS - 100, true, 300, 100, 25,
			"Save", widgetTexture,
			(double mouseX, double mouseY, int button, int action, int mods) -> {
				try {
					int numPreviewsNew = Integer.parseInt(textFieldNumPreviews.getText());
					int boardHeightNew = Integer.parseInt(textFieldBoardHeight.getText());
					int boardWidthNew = Integer.parseInt(textFieldBoardWidth.getText());

					settings.setNumPreviews(numPreviewsNew);
					settings.setBoardHeight(boardHeightNew);
					settings.setBoardWidth(boardWidthNew);
					settings.setKickTableLocation(dropDownKickTable.getTopText());

					settings.saveSettings();
				} catch (NumberFormatException e) {
					//replace with in game error message
					e.printStackTrace();
				} catch (IOException | ParseException e) {
					e.printStackTrace();
				}
			}));

		topFrame.addComponent(
			new Button(Constants.VIEWPORT_W - 650, 50, true,
			600, 100, 25, "Start Game",
				widgetTexture,
				(double mouseX, double mouseY, int button, int action, int mods) -> {
					nextScene = new GameScene(windowID, client);
					shouldChangeScene = true;
				}));
		topFrame.addComponent(
			new Button(50, 50, true,
				600, 100, 25, "Back",
				widgetTexture,
				(double mouseX, double mouseY, int button, int action, int mods) -> {
					nextScene = new MenuScene(windowID, client);
					shouldChangeScene = true;
				}));
	}

	@Override
	public void update(double dt) {

	}

	@Override
	public void draw() {
		blockShader.bind();
		blockShader.bindTexture2D("uTexture", widgetTexture);

		float[] buffer = new float[16];

		blockShader.uploadUniformMatrix4fv("uProjection", false, projection.get(buffer));

		batch.addComponent(topFrame);
		batch.flush();

		textRenderer.bind();

		textRenderer.addText(topFrame);

		textRenderer.draw();
	}

	@Override
	public Scene nextScene() {
		return nextScene;
	}

	@Override
	public void destroy() {
		topFrame.destroy();
		batch.destroy();
	}
}
