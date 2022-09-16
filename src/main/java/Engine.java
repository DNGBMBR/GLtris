import network.lobby.Client;
import render.manager.TextRenderer;
import scenes.*;
import util.KeyListener;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Engine {
	/*
	* STEPS FOR DRAWING TO SCREEN:
	* - compile shaders, initialize vaos, vbos, and ebos
	* - initialize vertex data using glVertexAttribPointer() and glEnableVertexAttribArray(), which binds the vbo to the vao on calling them
	* - in window loop:
	* 	- for each shader:
	* 		- bind the shader
	* 		- upload uniforms to the shader
	* 		- bind the vao to be used
	* 		- call glDrawArrays() or glDrawElements() for vbo data or ebo data respectively
	* */
	private Scene currentScene;
	private long windowID;
	private Client client;

	public Engine(long windowID) {
		this.windowID = windowID;
	}

	public void changeScene(Scene newScene) {
		if (currentScene != null) {
			currentScene.destroy();
		}
		currentScene = newScene;
		newScene.init();
	}

	public void init() {
		glfwSetWindowSizeCallback(windowID, (long window, int width, int height) -> {
			glViewport(0, 0, width, height);
			currentScene.updateProjection(windowID);
			TextRenderer renderer = TextRenderer.getInstance();
			renderer.updateProjection(windowID);
		});

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		client = new Client(InetAddress.getLoopbackAddress(), 2678, "");

		changeScene(new MenuScene(windowID, client));
	}

	public void run() {
		double startTime = glfwGetTime();
		double endTime = glfwGetTime();
		double dt = endTime - startTime;
		while (!glfwWindowShouldClose(windowID)) {
			if (currentScene.shouldChangeScene()) {
				changeScene(currentScene.nextScene());
			}

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

	public void destroy() {
		currentScene.destroy();
		client.close();
	}
}
