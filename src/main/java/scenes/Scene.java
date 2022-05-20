package scenes;

public abstract class Scene {

	protected long windowID;
	protected boolean shouldChangeScene;

	Scene(long windowID) {
		this.windowID = windowID;
		shouldChangeScene = false;
	}

	public abstract void updateProjection(long windowID);

	public abstract void init();
	public abstract void update(double dt);
	public abstract void draw();

	public abstract boolean shouldChangeScene();
	public abstract Scene nextScene();
	public abstract void destroy();
}
