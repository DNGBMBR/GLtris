import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import render.manager.ResourceManager;
import util.KeyListener;
import util.MouseListener;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
	private long windowID;

	private int width;
	private int height;
	private String title;

	Engine engine;

	private static Window instance;

	private Window(int width, int height, String title) {
		this.width = width;
		this.height = height;
		this.title = title;
	}

	public static Window getInstance() {
		if (instance == null) {
			instance = new Window(1280, 720, "default");
		}
		return instance;
	}

	public void initialize() {
		if (!glfwInit()) {
			System.err.println("Failed to initialize GLFW.");
			System.exit(1);
		}

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

		//we'll cross resizing the window when we get to it
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

		windowID = glfwCreateWindow(width, height, title, NULL, NULL);

		if (windowID == NULL) {
			System.err.println("Failed to create GLFW window");
			glfwTerminate();
			System.exit(1);
		}

		glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));

		glfwMakeContextCurrent(windowID);
		glfwShowWindow(windowID);

		GL.createCapabilities();

		glViewport(0, 0, width, height);

		KeyListener.setWindowID(windowID);

		glfwSetKeyCallback(windowID, KeyListener::keyCallback);
		glfwSetCharCallback(windowID, KeyListener::textCallback);
		glfwSetCursorPosCallback(windowID, MouseListener::mousePosCallback);
		glfwSetMouseButtonCallback(windowID, MouseListener::mouseButtonCallback);
		glfwSetScrollCallback(windowID, MouseListener::mouseScrollCallback);

		try {
			ResourceManager.initializeResources();
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
			glfwTerminate();
			System.exit(1);
		}

		engine = new Engine(windowID);
		engine.init();
	}

	public void run() {
		engine.run();
	}

	public void destroy() {
		glfwTerminate();
	}
}
