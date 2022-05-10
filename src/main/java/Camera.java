import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
	private static final Vector3f UP = new Vector3f(0.0f, 1.0f, 0.0f);

	private Matrix4f view;

	public Camera() {
		Vector3f eye = new Vector3f(0.0f, 0.0f, 5.0f);
		Vector3f center = new Vector3f(0.0f, 0.0f, 0.0f);
		view = new Matrix4f().lookAt(eye, center, UP);
	}

	public void translate(Vector3f vec) {
		view.translate(vec);
	}

	public Matrix4f getView() {
		return view;
	}
}
