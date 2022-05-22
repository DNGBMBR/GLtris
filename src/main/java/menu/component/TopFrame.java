package menu.component;

import static org.lwjgl.glfw.GLFW.*;

import util.*;

public class TopFrame extends Frame implements MouseClickCallback, MouseMoveCallback {
	public TopFrame(double windowWidth, double windowHeight, boolean isActive) {
		super(0.0, 0.0, windowWidth, windowHeight, isActive, false, 0.0);
		MouseListener.registerMouseClickCallback(this);
		MouseListener.registerMouseMoveCallback(this);
	}

	@Override
	public float[] generateVertices() {
		return super.generateVertices();
	}

	@Override
	public void destroy() {
		super.destroy();
		MouseListener.unregisterMouseMoveCallback(this);
		MouseListener.unregisterMouseClickCallback(this);
	}

	@Override
	public void onClick(long window, int button, int action, int mods) {
		double[] mouseX = new double[1];
		double[] mouseY = new double[1];
		int[] windowWidth = new int[1];
		int[] windowHeight = new int[1];
		glfwGetWindowSize(window, windowWidth, windowHeight);
		glfwGetCursorPos(window, mouseX, mouseY);
		mouseX[0] = mouseX[0] / windowWidth[0] * width;
		mouseY[0] = (windowHeight[0] - mouseY[0]) / windowHeight[0] * height;
		this.onClick(mouseX[0], mouseY[0], button, action, mods);
	}

	@Override
	public void onMove(long window, double xPos, double yPos) {
		int[] windowWidth = new int[1];
		int[] windowHeight = new int[1];
		glfwGetWindowSize(window, windowWidth, windowHeight);
		xPos = xPos / windowWidth[0] * width;
		yPos = (windowHeight[0] - yPos) / windowHeight[0] * height;
		this.onHover(xPos, yPos);
	}
}
