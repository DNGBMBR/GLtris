package render;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class Batch {
	private static final int FLOATS_PER_VERTEX = 4;
	private static final int VERTEX_SIZE = FLOATS_PER_VERTEX * Float.BYTES;

	private int maxVertices;
	private int usedVertices;
	private int vaoID;
	private int vboID;
	private BatchConfig config;
	private Shader shader;

	public Batch(Shader shader, int maxVertices, BatchConfig config) {
		if (maxVertices <= 0) {
			throw new IllegalArgumentException("Cannot have less than or equal to 0 vertices in the batch.");
		}

		shader.compile();
		this.shader = shader;
		this.maxVertices = maxVertices;
		this.config = config;
		usedVertices = 0;

		vaoID = glGenVertexArrays();
		glBindVertexArray(vaoID);

		vboID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, (long) maxVertices * VERTEX_SIZE, GL_STREAM_DRAW);

		glVertexAttribPointer(0, 2, GL_FLOAT, false, VERTEX_SIZE, 0);
		glVertexAttribPointer(0, 2, GL_FLOAT, false, VERTEX_SIZE, 2 * Float.BYTES);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
	}

	public void setConfig(BatchConfig config) {
		this.config = config;
	}

	public boolean isEmpty() {
		return usedVertices == 0;
	}

	public boolean isEnoughRoom(int numVerticesToAdd) {
		return usedVertices + numVerticesToAdd <= maxVertices;
	}

	public void addVertices(float[] vertexData) {
		if (vertexData.length % FLOATS_PER_VERTEX != 0) {
			throw new IllegalArgumentException("Vertex data given does not conform to the given format.");
		}
		int numVertices = vertexData.length / FLOATS_PER_VERTEX;
		if (numVertices > maxVertices) {
			throw new IllegalArgumentException("Number of vertices passed in exceeds the maximum capacity of the batch.");
		}
		if(!isEnoughRoom(numVertices)) {
			flush();
		}

		glBindVertexArray(vaoID);
		glBindBuffer(GL_ARRAY_BUFFER, vboID);

		glBufferSubData(GL_ARRAY_BUFFER, (long) usedVertices * VERTEX_SIZE, vertexData);
		usedVertices += numVertices;
	}

	public void flush(){
		if (usedVertices <= 0) {
			return;
		}

		glVertexAttribPointer(0, 2, GL_FLOAT, false, VERTEX_SIZE, 0);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, VERTEX_SIZE, 2 * Float.BYTES);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);

		glBindVertexArray(vaoID);
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glDrawArrays(config.renderType, 0, usedVertices);

		usedVertices = 0;
	}
}
