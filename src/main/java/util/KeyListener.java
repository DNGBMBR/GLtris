package util;

import org.lwjgl.glfw.GLFWKeyCallback;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class KeyListener {
	private static KeyListener instance;
	private static boolean[] keyPressed = new boolean[350];
	private static List<KeyCallback> callbacks = new ArrayList<>();

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
		return instance.keyPressed[key];
	}

	public static void registerCallback(KeyCallback callback) {
		callbacks.add(callback);
	}
}
