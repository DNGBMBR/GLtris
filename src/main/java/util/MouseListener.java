package util;

import java.util.*;

public class MouseListener {
	private static Set<MouseMoveCallback> moveCallbacks = Collections.synchronizedSet(new HashSet<>());
	private static Set<MouseClickCallback> clickCallbacks = Collections.synchronizedSet(new HashSet<>());
	private static Set<MouseScrollCallback> scrollCallbacks = Collections.synchronizedSet(new HashSet<>());

	private MouseListener() {}

	public static void mousePosCallback(long window, double xPos, double yPos) {
		for (MouseMoveCallback callback : moveCallbacks) {
			callback.onMove(window, xPos, yPos);
		}
	}

	public static void mouseButtonCallback(long window, int button, int action, int mods) {
		for (MouseClickCallback callback : clickCallbacks) {
			callback.onClick(window, button, action, mods);
		}
	}

	public static void mouseScrollCallback(long window, double xOffset, double yOffset) {
		for (MouseScrollCallback callback : scrollCallbacks) {
			callback.onScroll(window, xOffset, yOffset);
		}
	}

	public static void registerMouseMoveCallback(MouseMoveCallback callback) {
		moveCallbacks.add(callback);
	}

	public static void unregisterMouseMoveCallback(MouseMoveCallback callback) {
		moveCallbacks.remove(callback);
	}

	public static void registerMouseClickCallback(MouseClickCallback callback) {
		clickCallbacks.add(callback);
	}

	public static void unregisterMouseClickCallback(MouseClickCallback callback) {
		clickCallbacks.remove(callback);
	}

	public static void registerMouseScrollCallback(MouseScrollCallback callback) {
		scrollCallbacks.add(callback);
	}

	public static void unregisterMouseScrollCallback(MouseScrollCallback callback) {
		scrollCallbacks.remove(callback);
	}
}
