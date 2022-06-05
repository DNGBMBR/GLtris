package util;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public class Utils {
	private static Map<Integer, String> scancodeNames;

	//assuming b > a, returns the percentage of the way c is between a and b
	//i.e. if a = 40, b = 100, c = 60, inverseLerp would return 0.33 because 60 is 33% of the way between 40 and 100
	public static double inverseLerp(double a, double b, double c) {
		return (c - a) / (b - a);
	}

	//assumes data conforms to the format {p0x, p0y, p0u, p0v}
	public static void addBlockVertices(float[] vertexData, int startIndex,
										float p0x, float p0y, float p0u, float p0v,
										float p1x, float p1y, float p1u, float p1v) {
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 0 + 0] = p0x;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 0 + 1] = p0y;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 0 + 2] = p0u;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 0 + 3] = p0v;

		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 1 + 0] = p1x;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 1 + 1] = p0y;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 1 + 2] = p1u;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 1 + 3] = p0v;

		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 2 + 0] = p1x;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 2 + 1] = p1y;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 2 + 2] = p1u;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 2 + 3] = p1v;

		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 3 + 0] = p1x;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 3 + 1] = p1y;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 3 + 2] = p1u;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 3 + 3] = p1v;

		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 4 + 0] = p0x;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 4 + 1] = p1y;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 4 + 2] = p0u;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 4 + 3] = p1v;

		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 5 + 0] = p0x;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 5 + 1] = p0y;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 5 + 2] = p0u;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 5 + 3] = p0v;
	}

	public static void addBlockVerticesNineSlice(float[] vertexData, int startIndex,
												 float p0x, float p0y, float p3x, float p3y, float borderWidth,
												 float[] uvs) {
		float p1x = p0x + borderWidth;
		float p1y = p0y + borderWidth;
		float p2x = p3x - borderWidth;
		float p2y = p3y - borderWidth;

		Utils.addBlockVertices(vertexData, startIndex + 0 * Constants.WIDGET_ELEMENTS_PER_QUAD * Constants.WIDGET_ATTRIBUTES_PER_VERTEX,
			p0x, p0y, uvs[0], uvs[1],
			p1x, p1y, uvs[2], uvs[3]);

		Utils.addBlockVertices(vertexData, startIndex + 1 * Constants.WIDGET_ELEMENTS_PER_QUAD * Constants.WIDGET_ATTRIBUTES_PER_VERTEX,
			p1x, p0y, uvs[2], uvs[1],
			p2x, p1y, uvs[4], uvs[3]);

		Utils.addBlockVertices(vertexData, startIndex + 2 * Constants.WIDGET_ELEMENTS_PER_QUAD * Constants.WIDGET_ATTRIBUTES_PER_VERTEX,
			p2x, p0y, uvs[4], uvs[1],
			p3x, p1y, uvs[6], uvs[3]);

		Utils.addBlockVertices(vertexData, startIndex + 3 * Constants.WIDGET_ELEMENTS_PER_QUAD * Constants.WIDGET_ATTRIBUTES_PER_VERTEX,
			p0x, p1y, uvs[0], uvs[3],
			p1x, p2y, uvs[2], uvs[5]);

		Utils.addBlockVertices(vertexData, startIndex + 4 * Constants.WIDGET_ELEMENTS_PER_QUAD * Constants.WIDGET_ATTRIBUTES_PER_VERTEX,
			p1x, p1y, uvs[2], uvs[3],
			p2x, p2y, uvs[4], uvs[5]);

		Utils.addBlockVertices(vertexData, startIndex + 5 * Constants.WIDGET_ELEMENTS_PER_QUAD * Constants.WIDGET_ATTRIBUTES_PER_VERTEX,
			p3x, p2y, uvs[6], uvs[5],
			p2x, p1y, uvs[4], uvs[3]);

		Utils.addBlockVertices(vertexData, startIndex + 6 * Constants.WIDGET_ELEMENTS_PER_QUAD * Constants.WIDGET_ATTRIBUTES_PER_VERTEX,
			p0x, p2y, uvs[0], uvs[5],
			p1x, p3y, uvs[2], uvs[7]);

		Utils.addBlockVertices(vertexData, startIndex + 7 * Constants.WIDGET_ELEMENTS_PER_QUAD * Constants.WIDGET_ATTRIBUTES_PER_VERTEX,
			p1x, p2y, uvs[2], uvs[5],
			p2x, p3y, uvs[4], uvs[7]);

		Utils.addBlockVertices(vertexData, startIndex + 8 * Constants.WIDGET_ELEMENTS_PER_QUAD * Constants.WIDGET_ATTRIBUTES_PER_VERTEX,
			p2x, p2y, uvs[4], uvs[5],
			p3x, p3y, uvs[6], uvs[7]);
	}

	private static void generateScancodes() {
		scancodeNames = new HashMap<>();

		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_SPACE), "space");

		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_LEFT), "left arrow");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_RIGHT), "right arrow");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_UP), "up arrow");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_DOWN), "down arrow");

		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_TAB), "tab");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_CAPS_LOCK), "caps lock");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_LEFT_SHIFT), "left shift");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_LEFT_CONTROL), "left ctrl");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_LEFT_ALT), "left alt");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_RIGHT_SHIFT), "right shift");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_RIGHT_CONTROL), "right ctrl");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_RIGHT_ALT), "right alt");

		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_ENTER), "enter");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_BACKSPACE), "backspace");

		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_F1), "f1");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_F2), "f2");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_F3), "f3");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_F4), "f4");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_F5), "f5");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_F6), "f6");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_F7), "f7");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_F8), "f8");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_F9), "f9");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_F10), "f10");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_F11), "f11");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_F12), "f12");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_F13), "f13");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_F14), "f14");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_F15), "f15");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_F16), "f16");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_F17), "f17");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_F18), "f18");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_F19), "f19");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_F20), "f20");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_F21), "f21");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_F22), "f22");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_F23), "f23");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_F24), "f24");
		scancodeNames.put(glfwGetKeyScancode(GLFW_KEY_F25), "f25");
	}

	public static String getKeyName(int scancode) {
		if (scancodeNames == null) {
			generateScancodes();
		}
		String displayText = scancodeNames.get(scancode);
		if (displayText == null) {
			displayText = scancode == 0 ? "Unbound" : glfwGetKeyName(GLFW_KEY_UNKNOWN, scancode);
			if (displayText == null) {
				displayText = "Unbound";
			}
		}

		return displayText;
	}
}
