package menu.component;

import util.Constants;

import java.util.*;

public class Frame extends Component {
	boolean scrollVertically;
	double scrollHeight;
	double currentScrollHeight;

	List<Component> components = new ArrayList<>();

	public Frame(double xPos, double yPos, double width, double height, boolean isActive,
				 boolean scrollVertically, double scrollHeight) {
		super(xPos, yPos, width, height, isActive);
		this.scrollVertically = scrollVertically;
		this.scrollHeight = scrollHeight;
		currentScrollHeight = 0.0;
	}

	public void addComponent(Component component) {
		components.add(component);
	}

	public List<Component> getComponents() {
		return components;
	}

	@Override
	public void onClick(double mouseX, double mouseY, int button, int action, int mods) {
		if (isActive) {
			for (Component component : components) {
				component.onClick(mouseX - xPos, mouseY - yPos, button, action, mods);
			}
		}
	}

	@Override
	public void onHover(double mouseX, double mouseY) {
		if (isActive) {
			for (Component component : components) {
				component.onHover(mouseX - xPos, mouseY - yPos);
			}
		}
	}

	@Override
	public float[] generateVertices() {
		ArrayList<Float> vertexInfo = new ArrayList<>();
		for (Component component : components) {
			float[] componentVertices = component.generateVertices();
			for (int i = 0; i < componentVertices.length / Constants.WIDGET_ELEMENTS_PER_VERTEX; i++) {
				componentVertices[Constants.WIDGET_ELEMENTS_PER_VERTEX * i + 0] += xPos;
				componentVertices[Constants.WIDGET_ELEMENTS_PER_VERTEX * i + 1] += yPos;
				//trim component if it's partially or fully out of the frame
			}
			for (int i = 0; i < componentVertices.length; i++) {
				vertexInfo.add(componentVertices[i]);
			}
		}
		float[] ret = new float[vertexInfo.size()];
		int i = 0;
		for (Iterator<Float> it = vertexInfo.iterator(); it.hasNext(); i++) {
			float next = it.next();
			ret[i] = next;
		}
		return ret;
	}

	@Override
	public void destroy() {
		for (Component component : components) {
			component.destroy();
		}
	}
}
