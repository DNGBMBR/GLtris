package menu.widgets;

import menu.component.Component;

public abstract class Widget extends Component implements OnComponentClick, OnComponentHover{
	protected String displayText;

	public Widget(double xPos, double yPos, double width, double height, boolean isActive, String displayText) {
		super(xPos, yPos, width, height, isActive);
		this.isActive = isActive;
		this.displayText = displayText;
	}

	public String getDisplayText() {
		return displayText;
	}
}
