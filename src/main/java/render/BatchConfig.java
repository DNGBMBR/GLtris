package render;

import org.joml.Matrix4f;

public class BatchConfig {
	public int renderType;
	public Matrix4f transform;

	public BatchConfig(int renderType, boolean usingTexture, int textureID) {
		this.renderType = renderType;
		this.transform = new Matrix4f().identity();
	}
}
