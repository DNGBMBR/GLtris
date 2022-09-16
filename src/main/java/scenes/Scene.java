package scenes;

import network.lobby.Client;
import org.joml.Matrix4f;
import util.Constants;

import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;

public abstract class Scene {

	protected long windowID;
	protected boolean shouldChangeScene = false;
	protected Matrix4f projection;
	protected Client client;

	Scene(long windowID, Client client) {
		this.windowID = windowID;
		this.client = client;
	}

	public void updateProjection(long windowID) {
		float projectionWidth = Constants.VIEWPORT_W;
		float projectionHeight = Constants.VIEWPORT_H;
		int[] windowWidth = new int[1];
		int[] windowHeight = new int[1];
		glfwGetWindowSize(windowID, windowWidth, windowHeight);
		float windowAspect = (float) windowWidth[0] / windowHeight[0];
		if (windowAspect < 16.0f / 9.0f) {
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

	public void init() {
		updateProjection(windowID);
	}

	public abstract void update(double dt);
	public abstract void draw();

	public boolean shouldChangeScene() {
		return this.shouldChangeScene;
	}
	public abstract Scene nextScene();
	public abstract void destroy();
}
