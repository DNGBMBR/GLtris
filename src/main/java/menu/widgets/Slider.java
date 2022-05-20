package menu.widgets;

import org.joml.Math;
import render.TextureAtlas;
import util.*;

import static org.lwjgl.glfw.GLFW.*;

public class Slider extends Widget implements MouseMoveCallback, MouseClickCallback{
	protected double percentage;

	protected double length;
	protected double barWidth;
	protected double clickerSize;
	protected boolean isHorizontal;
	protected boolean isClicked;
	private TextureAtlas texture;
	private int pxClicker, pyClicker;
	private int pxBar, pyBar;
	OnSliderMove onDrag;

	public Slider(double xPos, double yPos, boolean isInteractable, String displayText,
				  double percentage, double length, double clickerSize, double barWidth, boolean isHorizontal,
				  TextureAtlas texture, int pxClicker, int pyClicker, int pxBar, int pyBar,
				  OnSliderMove onDrag) {
		super(xPos, yPos, isInteractable, displayText);
		this.percentage = percentage;
		this.length = length;
		this.clickerSize = clickerSize;
		this.barWidth = barWidth;
		this.isHorizontal = isHorizontal;
		this.texture = texture;
		this.pxClicker = pxClicker;
		this.pyClicker = pyClicker;
		this.pxBar = pxBar;
		this.pyBar = pyBar;
		this.isClicked = false;
		this.onDrag = onDrag;
		MouseListener.registerMouseMoveCallback(this);
		MouseListener.registerMouseClickCallback(this);
	}

	public double getPercentage() {
		return percentage;
	}

	public double getLength() {
		return length;
	}

	public double getBarWidth() {
		return barWidth;
	}

	public double getClickerSize() {
		return clickerSize;
	}

	public boolean isHorizontal() {
		return isHorizontal;
	}

	public boolean isClicked() {
		return isClicked;
	}

	@Override
	public void onMove(long window, double xPos, double yPos) {
		if (!isInteractable) {
			return;
		}
		if (!isClicked()) {
			return;
		}
		int[] windowWidth = new int[1];
		int[] windowHeight = new int[1];
		glfwGetWindowSize(window, windowWidth, windowHeight);
		double transformedMouseX = (float) xPos * ((float) Constants.VIEWPORT_W / windowWidth[0]);
		double transformedMouseY = (1.0f - (float) yPos / windowHeight[0]) * Constants.VIEWPORT_H;
		double percentagePosition = isHorizontal ? (1.0 / length) * transformedMouseX - this.xPos / length : (1.0 / length) * transformedMouseY - this.yPos / length;

		percentage = Math.clamp(0.0, 1.0, percentagePosition);
		this.onDrag.onMove(window, percentage);
	}

	@Override
	public void onClick(long window, int button, int action, int mods) {
		if (!isInteractable) {
			return;
		}
		if (action == GLFW_RELEASE) {
			isClicked = false;
			return;
		}
		double[] cursorX = new double[1];
		double[] cursorY = new double[1];
		int[] windowWidth = new int[1];
		int[] windowHeight = new int[1];
		glfwGetCursorPos(window, cursorX, cursorY);
		glfwGetWindowSize(window, windowWidth, windowHeight);
		double transformedX = (float) cursorX[0] * ((float) Constants.VIEWPORT_W / windowWidth[0]);
		double transformedY = (1.0f - (float) cursorY[0] / windowHeight[0]) * Constants.VIEWPORT_H;

		double clickerX = getClickablePositionX();
		double clickerY = getClickablePositionY();
		if (action == GLFW_PRESS &&
			transformedX >= clickerX && transformedX <= clickerX + clickerSize &&
			transformedY >= clickerY && transformedY <= clickerY + clickerSize) {
			isClicked = true;
		}
		else {
			isClicked = false;
		}
	}

	public double getClickablePositionX() {
		return isHorizontal ? xPos + length * percentage : xPos;
	}

	public double getClickablePositionY() {
		return !isHorizontal ? yPos + length * percentage : yPos;
	}

	@Override
	public float[] generateVertices() {
		float[] uvsClicker = texture.getElementUVs(pxClicker, pyClicker, 1, 1);
		float[] uvsBar = texture.getElementUVs(pxBar, pyBar, 1, 1);

		float p0xBar = (float) (xPos + (clickerSize - barWidth) * 0.5);
		float p0yBar = (float) (yPos + (clickerSize - barWidth) * 0.5);
		float p1xBar = (float) (p0xBar + (isHorizontal ? length + (clickerSize - barWidth) * 0.5 : barWidth));
		float p1yBar = (float) (p0yBar + (!isHorizontal ? length + (clickerSize - barWidth) * 0.5 : barWidth));

		float p0xClicker = (float) (xPos + (isHorizontal ? percentage * length : 0));
		float p0yClicker = (float) (yPos + (!isHorizontal ? percentage * length : 0));
		float p1xClicker = p0xClicker + (float) clickerSize;
		float p1yClicker = p0yClicker + (float) clickerSize;

		float[][] barVertices = {
			{p0xBar, p0yBar, uvsBar[0], uvsBar[1]},
			{p1xBar, p0yBar, uvsBar[2], uvsBar[1]},
			{p1xBar, p1yBar, uvsBar[2], uvsBar[3]},
			{p0xBar, p1yBar, uvsBar[0], uvsBar[3]},
		};

		float[][] clickerVertices = {
			{p0xClicker, p0yClicker, uvsClicker[0], uvsClicker[1]},
			{p1xClicker, p0yClicker, uvsClicker[2], uvsClicker[1]},
			{p1xClicker, p1yClicker, uvsClicker[2], uvsClicker[3]},
			{p0xClicker, p1yClicker, uvsClicker[0], uvsClicker[3]},
		};

		float[] orderedVertices = new float[6 * 2 * 4];

		Utils.addVertices(orderedVertices, barVertices[0], 0);
		Utils.addVertices(orderedVertices, barVertices[1], 4);
		Utils.addVertices(orderedVertices, barVertices[2], 8);
		Utils.addVertices(orderedVertices, barVertices[2], 12);
		Utils.addVertices(orderedVertices, barVertices[3], 16);
		Utils.addVertices(orderedVertices, barVertices[0], 20);

		Utils.addVertices(orderedVertices, clickerVertices[0], 24);
		Utils.addVertices(orderedVertices, clickerVertices[1], 28);
		Utils.addVertices(orderedVertices, clickerVertices[2], 32);
		Utils.addVertices(orderedVertices, clickerVertices[2], 36);
		Utils.addVertices(orderedVertices, clickerVertices[3], 40);
		Utils.addVertices(orderedVertices, clickerVertices[0], 44);
		return orderedVertices;
	}

	@Override
	public void destroy() {
		MouseListener.unregisterMouseMoveCallback(this);
		MouseListener.unregisterMouseClickCallback(this);
	}
}
