package game;

import game.pieces.PieceBuilder;
import menu.component.Component;

import java.util.List;

public class GLTrisBoardComponent extends Component {
	GLTris game;
	GLTrisBoardRenderer renderer;

	public GLTrisBoardComponent(double xPos, double yPos, float tileSize, boolean isActive, List<PieceBuilder> pieceInfo) {
		super(xPos, yPos, 0.0, 0.0, "", true);
		game = new GLTris(pieceInfo);
		renderer = new GLTrisBoardRenderer(this.xPos, this.yPos, tileSize, isActive, game);
		this.width = tileSize * 5.0f + (game.getBoardWidth() + 1) * tileSize + tileSize * 5.0f;
		this.height = game.getBoardHeight() * tileSize;
	}

	public void init() {
		game.init();
	}

	public void update(double dt) {
		game.update(dt);
	}

	public boolean isGameOver() {
		return game.isGameOver();
	}

	public float[] generateBackgroundVertices() {
		return renderer.generateBackgroundVertices();
	}

	public float[] generateTileVertices() {
		return renderer.generateTileVertices();
	}

	public GLTris getGame() {
		return game;
	}

	@Override
	public float[] generateVertices() {
		return renderer.generateVertices();
	}

	@Override
	public void destroy() {
		game.destroy();
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
}
