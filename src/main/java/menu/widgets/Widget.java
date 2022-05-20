package menu.widgets;

public abstract class Widget {
	protected double xPos, yPos;
	protected String displayText;

	protected boolean isInteractable = true;

	public Widget(double xPos, double yPos, boolean isInteractable, String displayText) {
		this.xPos = xPos;
		this.yPos = yPos;
		this.isInteractable = isInteractable;
		this.displayText = displayText;
	}

	public double getXPos() {
		return xPos;
	}

	public double getYPos() {
		return yPos;
	}

	public boolean isInteractable() {
		return isInteractable;
	}

	public void setInteractable(boolean interactable) {
		isInteractable = interactable;
	}

	public String getDisplayText() {
		return displayText;
	}

	public abstract float[] generateVertices();
	public abstract void destroy();
}
