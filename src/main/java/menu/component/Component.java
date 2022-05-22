package menu.component;

import menu.widgets.OnComponentClick;
import menu.widgets.OnComponentHover;

import java.util.List;

public abstract class Component implements OnComponentHover, OnComponentClick {
	protected double xPos, yPos;
	protected double width, height;
	protected boolean isActive;

	public Component(double xPos, double yPos, double width, double height, boolean isActive) {
		this.xPos = xPos;
		this.yPos = yPos;
		this.width = width;
		this.height = height;
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

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean active) {
		isActive = active;
	}

	public abstract float[] generateVertices();

	public abstract void destroy();
}
