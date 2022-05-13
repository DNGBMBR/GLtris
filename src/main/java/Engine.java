import scenes.*;
import util.KeyListener;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Engine {


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
	private Scene currentScene;
	private long windowID;

	public Engine(long windowID) {
		this.windowID = windowID;
	}

	public void changeScene(Scene newScene) {
		currentScene.destroy();
		currentScene = newScene;
		newScene.init();
	}

	public void init() {
		glfwSetWindowSizeCallback(windowID, (long window, int width, int height) -> {
			currentScene.updateProjection(windowID);
		});
		KeyListener.registerCallback((long window, int key, int scancode, int action, int mods) -> {
			//debug for switching scenes
			if (action == GLFW_PRESS) {
				if (key == GLFW_KEY_M) {
					changeScene(new MenuScene(windowID));
				}
				if (key == GLFW_KEY_N) {
					changeScene(new GameScene(windowID));
				}
			}
		});
		currentScene = new GameScene(windowID);
		currentScene.init();
	}

	public void run() {
		double startTime = glfwGetTime();
		double endTime = glfwGetTime();
		double dt = endTime - startTime;
		while (!glfwWindowShouldClose(windowID)) {
			if (dt >= 0.0) {
				currentScene.update(dt);
			}

			glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
			glClear(GL_COLOR_BUFFER_BIT);

			currentScene.draw();

			glfwSwapBuffers(windowID);
			glfwPollEvents();

			endTime = glfwGetTime();
			dt = endTime - startTime;
			startTime = glfwGetTime();
		}
	}
}
