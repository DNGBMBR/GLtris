package render;

import menu.widgets.Button;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class WidgetBatch extends Batch{
	//They're all quads?
	//Always has been.
	private static final int FLOATS_PER_VERTEX = 4;
	private static final int VERTICES_PER_QUAD = 4;
	private static final int VERTEX_SIZE = FLOATS_PER_VERTEX * Float.BYTES;

	private int maxVertices;
	private int usedVertices;
	private int vaoID;
	private int vboID;

	public WidgetBatch(int maxElements) {
		if (maxElements <= 0) {
			throw new IllegalArgumentException("Cannot have less than or equal to 0 vertices in the batch.");
		}

		this.maxVertices = maxElements * VERTICES_PER_QUAD;
		usedVertices = 0;

		vaoID = glGenVertexArrays();
		glBindVertexArray(vaoID);

		vboID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboID);

		glVertexAttribPointer(0, 2, GL_FLOAT, false, VERTEX_SIZE, 0);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, VERTEX_SIZE, 2 * Float.BYTES);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);

		glBufferData(GL_ARRAY_BUFFER, (long) maxVertices * VERTEX_SIZE, GL_STREAM_DRAW);
	}

	@Override
	public boolean isEmpty() {
		return usedVertices == 0;
	}

	@Override
	public boolean isEnoughRoom(int numVerticesToAdd) {
		return usedVertices + numVerticesToAdd <= maxVertices;
	}

	public void addVertices(float[] vertexData) {
		if (vertexData.length % FLOATS_PER_VERTEX != 0) {
			throw new IllegalArgumentException("Vertex data given does not conform to the given format.");
		}
		int numVertices = vertexData.length / FLOATS_PER_VERTEX;
		if (numVertices >= maxVertices) {
			throw new IllegalArgumentException("Number of vertices passed in exceeds the maximum capacity of the batch.");
		}
		if(!isEnoughRoom(numVertices)) {
			flush();
		}

		glBindVertexArray(vaoID);
		glBindBuffer(GL_ARRAY_BUFFER, vboID);

		glBufferSubData(GL_ARRAY_BUFFER, usedVertices * VERTEX_SIZE, vertexData);
		usedVertices += numVertices;
	}

	public void addButton(Button button, TextureNineSlice texture, int px, int py) {
		float[][] buttonVertices = button.generateVertices(texture, px, py, 1, 1);

		//we should probably add an index buffer somewhere in the batcher
		addVertices(buttonVertices[0]);
		addVertices(buttonVertices[1]);
		addVertices(buttonVertices[5]);
		addVertices(buttonVertices[5]);
		addVertices(buttonVertices[4]);
		addVertices(buttonVertices[0]);

		addVertices(buttonVertices[1]);
		addVertices(buttonVertices[2]);
		addVertices(buttonVertices[6]);
		addVertices(buttonVertices[6]);
		addVertices(buttonVertices[5]);
		addVertices(buttonVertices[1]);

		addVertices(buttonVertices[2]);
		addVertices(buttonVertices[3]);
		addVertices(buttonVertices[7]);
		addVertices(buttonVertices[7]);
		addVertices(buttonVertices[6]);
		addVertices(buttonVertices[2]);

		addVertices(buttonVertices[4]);
		addVertices(buttonVertices[5]);
		addVertices(buttonVertices[9]);
		addVertices(buttonVertices[9]);
		addVertices(buttonVertices[8]);
		addVertices(buttonVertices[4]);

		addVertices(buttonVertices[5]);
		addVertices(buttonVertices[6]);
		addVertices(buttonVertices[10]);
		addVertices(buttonVertices[10]);
		addVertices(buttonVertices[9]);
		addVertices(buttonVertices[5]);

		addVertices(buttonVertices[6]);
		addVertices(buttonVertices[7]);
		addVertices(buttonVertices[11]);
		addVertices(buttonVertices[11]);
		addVertices(buttonVertices[10]);
		addVertices(buttonVertices[6]);

		addVertices(buttonVertices[8]);
		addVertices(buttonVertices[9]);
		addVertices(buttonVertices[13]);
		addVertices(buttonVertices[13]);
		addVertices(buttonVertices[12]);
		addVertices(buttonVertices[8]);

		addVertices(buttonVertices[9]);
		addVertices(buttonVertices[10]);
		addVertices(buttonVertices[14]);
		addVertices(buttonVertices[14]);
		addVertices(buttonVertices[13]);
		addVertices(buttonVertices[9]);

		addVertices(buttonVertices[10]);
		addVertices(buttonVertices[11]);
		addVertices(buttonVertices[15]);
		addVertices(buttonVertices[15]);
		addVertices(buttonVertices[14]);
		addVertices(buttonVertices[10]);
	}

	@Override
	public void flush(){
		if (usedVertices <= 0) {
			return;
		}

		glBindVertexArray(vaoID);
		glDrawArrays(GL_TRIANGLES, 0, usedVertices);

		usedVertices = 0;
	}
}
