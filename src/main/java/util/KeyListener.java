package util;

import org.lwjgl.glfw.GLFWKeyCallback;

import java.util.*;

import static org.lwjgl.glfw.GLFW.*;

public class KeyListener {
	private static boolean[] keyPressed = new boolean[350];
	private static Set<KeyCallback> callbacks = Collections.synchronizedSet(new HashSet<>());

	private KeyListener() {}

	public static void keyCallback(long window, int key, int scancode, int action, int mods) {
		if (action == GLFW_PRESS) {
			keyPressed[key] = true;
		}
		else if (action == GLFW_RELEASE) {
			keyPressed[key] = false;
		}
		for (KeyCallback callback : callbacks) {
			callback.callback(window, key, scancode, action, mods);
		}
	}

	public static boolean isKeyPressed(int key) {
		return keyPressed[key];
	}

	public static void registerCallback(KeyCallback callback) {
		callbacks.add(callback);
	}

	public static void unregisterCallback(KeyCallback callback) {
		callbacks.remove(callback);
	}
}
