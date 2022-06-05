package util;

import org.lwjgl.glfw.*;

import java.util.*;

import static org.lwjgl.glfw.GLFW.*;

public class KeyListener {
	private static Set<GLFWKeyCallbackI> keyCallbacks = Collections.synchronizedSet(new HashSet<>());
	private static Set<GLFWCharCallbackI> textCallbacks = Collections.synchronizedSet(new HashSet<>());

	private static long windowID;

	private KeyListener() {}

	public static void setWindowID(long windowID) {
		KeyListener.windowID = windowID;
	}

	public static void keyCallback(long window, int key, int scancode, int action, int mods) {
		for (GLFWKeyCallbackI callback : keyCallbacks) {
			callback.invoke(window, key, scancode, action, mods);
		}
	}

	public static void textCallback(long window, int codepoint) {
		for (GLFWCharCallbackI callback : textCallbacks) {
			callback.invoke(window, codepoint);
		}
	}

	public static int isKeyPressed(int key) {
		return glfwGetKey(windowID, key);
	}

	public static void registerKeyCallback(GLFWKeyCallbackI callback) {
		keyCallbacks.add(callback);
	}

	public static void unregisterKeyCallback(GLFWKeyCallbackI callback) {
		keyCallbacks.remove(callback);
	}

	public static void registerCharCallback(GLFWCharCallbackI callback) {
		textCallbacks.add(callback);
	}

	public static void unregisterCharCallback(GLFWCharCallbackI callback) {
		textCallbacks.remove(callback);
	}
}
