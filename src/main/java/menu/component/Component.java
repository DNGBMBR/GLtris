package menu.component;

import menu.widgets.callbacks.*;

import java.util.ArrayList;
import java.util.List;

public abstract class Component implements OnComponentHover, OnComponentClick, OnFrameScroll {
	protected double xPos, yPos;
	protected double width, height;
	protected String displayText;
	protected boolean isActive;

	public Component(double xPos, double yPos, double width, double height, String displayText, boolean isActive) {
		this.xPos = xPos;
		this.yPos = yPos;
		this.width = width;
		this.height = height;
		this.displayText = displayText;
		this.isActive = isActive;
	}

	public double getXPos() {
		return xPos;
	}

	public double getYPos() {
		return yPos;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	public String getDisplayText() {
		return displayText;
	}

	public void setDisplayText(String displayText) {
		this.displayText = displayText;
	}

	public boolean isActive() {
		return isActive;
	}

	public boolean isCursorHovered(double mouseX, double mouseY) {
		return mouseX >= xPos && mouseX <= xPos + width &&
			mouseY >= yPos && mouseY <= yPos + height;
	}

	public void setActive(boolean active) {
		isActive = active;
	}

	public abstract float[] generateVertices();

	public List<TextInfo> getTextInfo() {
		return new ArrayList<>();
	}

	public abstract void destroy();
}
