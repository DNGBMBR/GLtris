import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Block {
	public static final float BLOCK_SIZE = 1.0f;

	Matrix4f transform;

	public Block(Vector3f initPos) {
		transform = new Matrix4f().translation(initPos);
	}

	public void translate(Vector3f vec) {
		translate(vec.x, vec.y, vec.z);
	}

	public void translate(float dx, float dy, float dz) {
		transform.translate(dx, dy, dz);
	}

	public Matrix4f getTransform() {
		return transform;
	}
}
