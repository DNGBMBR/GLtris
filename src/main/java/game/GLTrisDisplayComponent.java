package game;

import game.pieces.util.TileState;
import menu.component.Component;
import settings.GameSettings;

public class GLTrisDisplayComponent extends Component {
	GLTrisUpdatableDisplay display;
	GLTrisBoardRenderer renderer;

	public GLTrisDisplayComponent(double xPos, double yPos, float tileSize, boolean isActive, GameSettings settings) {
		super(xPos, yPos, 0.0, 0.0, "", isActive);
		display = new GLTrisUpdatableDisplay(settings.getNumPreviews());
		renderer = new GLTrisBoardRenderer(this.xPos, this.yPos, tileSize, isActive, display, settings.getKickTable());
		this.width = tileSize * 5.0f + (display.getBoardWidth() + 1) * tileSize + tileSize * 5.0f;
		this.height = display.getBoardHeight() * tileSize;
	}

	@Override
	public float[] generateVertices() {
		return renderer.generateVertices();
	}

	public float[] generateBackgroundVertices() {
		return renderer.generateBackgroundVertices();
	}

	public float[] generateTileVertices() {
		return renderer.generateTileVertices();
	}

	@Override
	public void destroy() {
		renderer.destroy();
	}

	@Override
	public void onClick(double mouseX, double mouseY, int button, int action, int mods) {
		renderer.onClick(mouseX, mouseY, button, action, mods);
	}

	@Override
	public void onHover(double mouseX, double mouseY, boolean isInFrame) {
		renderer.onHover(mouseX, mouseY, isInFrame);
	}

	@Override
	public void onScroll(double mouseX, double mouseY, double xOffset, double yOffset) {
		renderer.onScroll(mouseX, mouseY, xOffset, yOffset);
	}

	public void setBoard(TileState[][] board) {
		display.setBoard(board);
	}

	public void setHeldPiece(String heldPiece) {
		display.setHeldPiece(heldPiece);
	}

	public void setQueue(String[] queue) {
		display.setQueue(queue);
	}

	public void setTileSize(float tileSize) {
		renderer.setTileSize(tileSize);
		this.width = tileSize * 5.0f + (display.getBoardWidth() + 1) * tileSize + tileSize * 5.0f;
		this.height = display.getBoardHeight() * tileSize;
	}
}
