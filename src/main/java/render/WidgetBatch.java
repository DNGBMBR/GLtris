package render;

import menu.component.Component;
import menu.widgets.*;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class WidgetBatch extends Batch{
	//They're all quads?
	//Always has been.
	private static final int FLOATS_PER_VERTEX = 4;
	private static final int VERTICES_PER_QUAD = 6;
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

	public void addComponent(Component component) {
		float[] vertices = component.generateVertices();
		addVertices(vertices);
	}

	public void addButton(Button button) {
		float[] buttonVertices = button.generateVertices();
		addVertices(buttonVertices);
	}

	public void addSlider(Slider slider, TextureAtlas texture, int px, int py) {
		float[] uvs = texture.getElementUVs(px, py, 1, 1);

		float p0xClicker = (float) (slider.getXPos() + (slider.isHorizontal() ? slider.getPercentage() * slider.getLength() : 0));
		float p0yClicker = (float) (slider.getYPos() + (!slider.isHorizontal() ? slider.getPercentage() * slider.getLength() : 0));
		float p1xClicker = p0xClicker + (float) slider.getClickerSize();
		float p1yClicker = p0yClicker + (float) slider.getClickerSize();

		float[][] vertices = {
			{p0xClicker, p0yClicker, uvs[0], uvs[1]},
			{p1xClicker, p0yClicker, uvs[2], uvs[1]},
			{p1xClicker, p1yClicker, uvs[2], uvs[3]},
			{p0xClicker, p1yClicker, uvs[0], uvs[3]},
		};

		addVertices(vertices[0]);
		addVertices(vertices[1]);
		addVertices(vertices[2]);
		addVertices(vertices[2]);
		addVertices(vertices[3]);
		addVertices(vertices[0]);
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

	@Override
	public void destroy() {
		glDeleteBuffers(vboID);
		glDeleteVertexArrays(vaoID);
	}
}
