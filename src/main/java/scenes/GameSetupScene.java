package scenes;

import menu.component.*;
import menu.widgets.Button;
import menu.widgets.Slider;
import org.joml.Matrix4f;
import render.*;
import render.manager.ResourceManager;
import util.Constants;

import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;

public class GameSetupScene extends Scene{
	private Matrix4f projection;
	private Scene nextScene;

	private Shader widgetShader = ResourceManager.getShaderByName("shaders/widget_vertex.glsl", "shaders/widget_fragment.glsl");
	private TextureNineSlice widgetTexture = ResourceManager.getTextureNineSliceByName("images/widgets.png");
	private WidgetBatch batch = new WidgetBatch(80);

	private TextRenderer textRenderer = TextRenderer.getInstance();

	private TopFrame frame;

	GameSetupScene(long windowID) {
		super(windowID);
		frame = new TopFrame(Constants.VIEWPORT_W, Constants.VIEWPORT_H, true);

		frame.addComponent(
			new Button(200, 100, true,
			600, 100, 25, "Start Game",
				widgetTexture, 0, 1,
				(double mouseX, double mouseY, int button, int action, int mods) -> {
					nextScene = new GameScene(windowID);
					shouldChangeScene = true;
				}));
	}

	@Override
	public void updateProjection(long windowID) {
		float projectionWidth = Constants.VIEWPORT_W;
		float projectionHeight = Constants.VIEWPORT_H;
		int[] windowWidth = new int[1];
		int[] windowHeight = new int[1];
		glfwGetWindowSize(windowID, windowWidth, windowHeight);
		float windowAspect = (float) windowWidth[0] / windowHeight[0];
		if (windowAspect >= 16.0f / 9.0f) {
			projectionWidth = projectionHeight * windowAspect;
		}
		else {
			projectionHeight = projectionWidth / windowAspect;
		}
		projection = new Matrix4f().identity().ortho(
			0.0f, projectionWidth,
			0.0f, projectionHeight,
			0.0f, 100.0f);
	}

	@Override
	public void init() {
		updateProjection(windowID);
	}

	@Override
	public void update(double dt) {

	}

	@Override
	public void draw() {
		widgetShader.bind();
		widgetTexture.bind(widgetShader, "uTexture");

		float[] buffer = new float[16];

		widgetShader.uploadUniformMatrix4fv("uProjection", false, projection.get(buffer));

		batch.addVertices(frame.generateVertices());
		batch.flush();

		textRenderer.bind();

		for (Component component : frame.getComponents()) {
			if (component instanceof Button) {
				Button button = (Button) component;
				textRenderer.addText(button, 1.0f, 1.0f, 1.0f);
			}
		}

		textRenderer.draw();
	}

	@Override
	public boolean shouldChangeScene() {
		return shouldChangeScene;
	}

	@Override
	public Scene nextScene() {
		return nextScene;
	}

	@Override
	public void destroy() {
		frame.destroy();
	}
}
