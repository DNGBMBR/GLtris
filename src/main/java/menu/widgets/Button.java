package menu.widgets;

import render.TextureNineSlice;
import util.MouseClickCallback;
import util.MouseListener;

import static org.lwjgl.glfw.GLFW.*;

public class Button implements MouseClickCallback{
	public static final int WINDOW_W_DEFAULT = 1920;
	public static final int WINDOW_H_DEFAULT = 1080;
	//always assumes we're working on a 1920x1080 canvas
	public double xPos, yPos;
	public double width, height;
	public double borderWidth;
	public String displayText;
	MouseClickCallback onClickCallback;

	public boolean isPressed;

	public Button(double xPos, double yPos, double width, double height, double borderWidth, String displayText, MouseClickCallback onClickCallback) {
		this.xPos = xPos;
		this.yPos = yPos;
		this.width = width;
		this.height = height;
		this.borderWidth = borderWidth;
		this.displayText = displayText;
		this.onClickCallback = onClickCallback;
		this.isPressed = false;
	}

	public float[][] generateVertices(TextureNineSlice texture, int px, int py, int width, int height) {
		float[] uvs = texture.getElementUVs(px, py, width, height);

		float p0x = (float) xPos;
		float p0y = (float) yPos;
		float p1x = (float) (xPos + borderWidth);
		float p1y = (float) (yPos + borderWidth);
		float p2x = (float) (xPos + this.width - borderWidth);
		float p2y = (float) (yPos + this.height - borderWidth);
		float p3x = (float) (xPos + this.width);
		float p3y = (float) (yPos + this.height);

		float[][] vertices = {
			{p0x, p0y, uvs[0], uvs[1]},
			{p1x, p0y, uvs[2], uvs[1]},
			{p2x, p0y, uvs[4], uvs[1]},
			{p3x, p0y, uvs[6], uvs[1]},
			{p0x, p1y, uvs[0], uvs[3]},
			{p1x, p1y, uvs[2], uvs[3]},
			{p2x, p1y, uvs[4], uvs[3]},
			{p3x, p1y, uvs[6], uvs[3]},
			{p0x, p2y, uvs[0], uvs[5]},
			{p1x, p2y, uvs[2], uvs[5]},
			{p2x, p2y, uvs[4], uvs[5]},
			{p3x, p2y, uvs[6], uvs[5]},
			{p0x, p3y, uvs[0], uvs[7]},
			{p1x, p3y, uvs[2], uvs[7]},
			{p2x, p3y, uvs[4], uvs[7]},
			{p3x, p3y, uvs[6], uvs[7]},
		};
		return vertices;
	}

	@Override
	public void onClick(long window, int button, int action, int mods) {
		double[] cursorX = new double[1];
		double[] cursorY = new double[1];
		int[] windowWidth = new int[1];
		int[] windowHeight = new int[1];
		glfwGetCursorPos(window, cursorX, cursorY);
		glfwGetWindowSize(window, windowWidth, windowHeight);
		double transformedX = (float) cursorX[0] * ((float) WINDOW_W_DEFAULT / windowWidth[0]);
		double transformedY = (1.0f - (float) cursorY[0] / windowHeight[0]) * WINDOW_H_DEFAULT;
		if (transformedX >= xPos && transformedX <= xPos + width &&
			transformedY >= yPos && transformedY <= yPos + height) {
			if (action == GLFW_PRESS) {
				isPressed = true;
			}
			else if (action == GLFW_RELEASE) {
				isPressed = false;
			}
			onClickCallback.onClick(window, button, action, mods);
		}
	}

	public void destroy() {
		MouseListener.unregisterMouseClickCallback(this);
	}
}
