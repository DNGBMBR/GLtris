import org.joml.Matrix4f;
import org.joml.Vector3f;
import render.Shader;
import util.KeyListener;

import java.io.IOException;
import java.util.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL45.*;

public class Engine {
	private static final int NUM_BLOCKS = 100;

	private Shader shaderBlocks;
	private Shader shaderTriangle;
	private Camera camera;
	private List<Block> blocks;
	private Matrix4f projection;

	private int vaoID, vboID, eboID;
	private int triangleVaoID, triangleVboID, triangleEboID;

	private Tetris game;

	/*
	* STEPS FOR DRAWING TO SCREEN:
	* - compile shaders, initialize vaos, vbos, and ebos
	* - in window loop:
	* 	- for each shader:
	* 		- bind the shader
	* 		- bind the vao, vbo, and ebo to be used
	* 		- initialize vertex data using glVertexAttribPointer() and glEnableVertexAttribArray()
	* 		- upload uniforms to the shader
	* 		- call glDrawArrays() or glDrawElements() for vbo data or ebo data respectively
	* */

	public Engine() {
		try {
			shaderBlocks = new Shader("src/main/shaders/vertex.glsl", "src/main/shaders/fragment.glsl");
			shaderTriangle = new Shader("src/main/shaders/triangle_vertex.glsl", "src/main/shaders/triangle_fragment.glsl");
		} catch (IOException e) {
			e.printStackTrace();
		}

		camera = new Camera();
		game = new Tetris();
		game.init();
	}

	public void init(long windowID) {
		shaderBlocks.compile();
		shaderTriangle.compile();

		blocks = Collections.synchronizedList(new ArrayList<>());
		for (int i = 0; i < NUM_BLOCKS; i++) {
			blocks.add(new Block(new Vector3f((float) Math.random() * 16.0f, (float) Math.random() * 20.0f, 0.0f)));
		}

		float[] vertexData = {
			0.5f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
			-0.5f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f,
			-0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
			0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f
		};

		int[] indexData = {
			0, 1, 2,
			2, 3, 0
		};

		float[] triangleData = {
			0.0f, 0.86f, 0.0f, 1.0f, 0.5f, 0.0f, 1.0f,
			-1.0f, -0.86f, 0.0f, 1.0f, 0.5f, 0.0f, 1.0f,
			1.0f, -0.86f, 0.0f, 1.0f, 0.5f, 0.0f, 1.0f,
		};

		int[] triangleIndexData = {
			0, 1, 2
		};

		vaoID = glCreateVertexArrays();
		glBindVertexArray(vaoID);

		vboID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);

		eboID = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexData, GL_STATIC_DRAW);

		triangleVaoID = glCreateVertexArrays();
		glBindVertexArray(triangleVaoID);

		triangleVboID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, triangleVboID);
		glBufferData(GL_ARRAY_BUFFER, triangleData, GL_STATIC_DRAW);

		triangleEboID = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, triangleEboID);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, triangleIndexData, GL_STATIC_DRAW);

		updateProjection(windowID);
	}

	public void updateProjection(long windowID) {
		float projectionHeight = 30.0f;
		int[] windowWidth = new int[1];
		int[] windowHeight = new int[1];
		glfwGetWindowSize(windowID, windowWidth, windowHeight);
		float windowAspect = (float) windowWidth[0] / windowHeight[0];
		float projectionWidth = projectionHeight * windowAspect;
		projection = new Matrix4f().identity().ortho(
			0.0f, projectionWidth,
			0.0f, projectionHeight,
			0.001f, 10000.0f);
	}

	public void draw() {
		float[] buffer = new float[16];

		shaderBlocks.bind();
		glBindVertexArray(vaoID);
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);

		glVertexAttribPointer(0, 3, GL_FLOAT, false, 9 * Float.BYTES, 0);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(1, 4, GL_FLOAT, false, 9 * Float.BYTES, 3 * Float.BYTES);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(2, 2, GL_FLOAT, false, 9 * Float.BYTES, 7 * Float.BYTES);
		glEnableVertexAttribArray(2);

		shaderBlocks.uploadUniformMatrix4fv("uView", false, camera.getView().get(buffer));
		shaderBlocks.uploadUniformMatrix4fv("uProjection", false, projection.get(buffer));
		for (int i = 0; i < blocks.size(); i++) {
			shaderBlocks.uploadUniformMatrix4fv("uTransform", false, blocks.get(i).getTransform().get(buffer));
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
		}

		Matrix4f defaultTransform = new Matrix4f().identity();
		shaderTriangle.bind();
		glBindVertexArray(triangleVaoID);
		glBindBuffer(GL_ARRAY_BUFFER, triangleVboID);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, triangleEboID);

		glVertexAttribPointer(0, 3, GL_FLOAT, false, 7 * Float.BYTES, 0);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(1, 4, GL_FLOAT, false, 7 * Float.BYTES, 3 * Float.BYTES);
		glEnableVertexAttribArray(1);
		glDisableVertexAttribArray(2);

		shaderTriangle.uploadUniformMatrix4fv("uView", false, camera.getView().get(buffer));
		shaderTriangle.uploadUniformMatrix4fv("uProjection", false, projection.get(buffer));
		shaderTriangle.uploadUniformMatrix4fv("uTransform", false, defaultTransform.get(buffer));
		glDrawElements(GL_TRIANGLES, 3, GL_UNSIGNED_INT, 0);
	}

	public void update(double dt) {
		for (int i = 0; i < blocks.size(); i++) {
			blocks.get(i).translate(0.0f, -1.0f * (float) dt, 0.0f);
			if (blocks.get(i).getTransform().m31() < 0.0f) {
				blocks.remove(i);
				i--;
			}
		}
		while (blocks.size() < NUM_BLOCKS) {
			blocks.add(new Block(new Vector3f((float) Math.random() * 20.0f, 20.0f, 0.0f)));
		}

		game.update(dt);
	}
}
