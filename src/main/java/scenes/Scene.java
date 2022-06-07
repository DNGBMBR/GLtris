package scenes;

public abstract class Scene {

	protected long windowID;
	protected boolean shouldChangeScene = false;

	Scene(long windowID) {
		this.windowID = windowID;
		shouldChangeScene = false;
	}

	public abstract void updateProjection(long windowID);

	public abstract void init();
	public abstract void update(double dt);
	public abstract void draw();

	public boolean shouldChangeScene() {
		return this.shouldChangeScene;
	}
	public abstract Scene nextScene();
	public abstract void destroy();
}
