package menu.component;

import org.joml.Math;
import render.texture.TextureAtlas;
import render.manager.ResourceManager;
import util.Constants;
import util.Utils;

import java.util.*;

public class Frame extends Component {
	boolean scrollVertically;
	double scrollHeight;
	double currentScrollHeight;

	List<Component> components = new ArrayList<>();

	public Frame(double xPos, double yPos, double width, double height, boolean isActive,
				 boolean scrollVertically, double scrollHeight) {
		super(xPos, yPos, width, height, "", isActive);
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

	public double getCurrentScrollHeight() {
		return currentScrollHeight;
	}

	@Override
	public void onClick(double mouseX, double mouseY, int button, int action, int mods) {
		if (!isActive || !isCursorHovered(mouseX, mouseY)) {
			return;
		}
		for (Component component : components) {
			component.onClick(mouseX - xPos, mouseY - yPos - currentScrollHeight, button, action, mods);
		}
	}

	@Override
	public void onHover(double mouseX, double mouseY, boolean isInFrame) {
		for (Component component : components) {
			component.onHover(mouseX - xPos, mouseY - yPos - currentScrollHeight, isActive && isCursorHovered(mouseX, mouseY));
		}
	}

	@Override
	public void onScroll(double mouseX, double mouseY, double xOffset, double yOffset) {
		if (!isActive || !isCursorHovered(mouseX, mouseY)) {
			return;
		}
		currentScrollHeight -= yOffset * Constants.UNITS_PER_SCROLL;
		currentScrollHeight = Math.clamp(0.0, scrollHeight, currentScrollHeight);
		for (Component component : components) {
			component.onScroll(mouseX - xPos, mouseY - yPos - currentScrollHeight, xOffset, yOffset);
		}
	}

	@Override
	public float[] generateVertices() {
		ArrayList<Float> vertexInfo = new ArrayList<>();
		TextureAtlas texture = ResourceManager.getTextureNineSliceByName("images/widgets.png");
		for (Component component : components) {
			float[] componentVertices = component.generateVertices();
			int stride = Constants.WIDGET_ATTRIBUTES_PER_VERTEX * Constants.WIDGET_ELEMENTS_PER_QUAD;
			//deal with one quad at a time
			for (int i = 0; i < componentVertices.length / stride; i++) {
				//translate all positions to this frame's coordinates
				for (int j = 0; j < Constants.WIDGET_ELEMENTS_PER_QUAD; j++) {
					componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * j + 0] += xPos;
					componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * j + 1] += yPos + currentScrollHeight;
				}
				//trim component if it's partially or fully out of the frame
				float p0x = componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 0 + 0];
				float p0y = componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 0 + 1];
				float p0u = componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 0 + 2];
				float p0v = componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 0 + 3];

				float p1x = componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 2 + 0];
				float p1y = componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 2 + 1];
				float p1u = componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 2 + 2];
				float p1v = componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 2 + 3];

				if (p0x > xPos + width || p1x < xPos || p0y > yPos + height || p1y < yPos) {
					//ignore this quad if it lies completely out of bounds
					continue;
				}

				//interpolate uv if part of the quad is cut off
				float q0x = Math.max((float) xPos, p0x);
				float q0y = Math.max((float) yPos, p0y);
				float q0u = p0x < xPos ? Math.lerp(p0u, p1u, (float) Utils.inverseLerp(p0x, p1x, xPos)) : p0u;
				float q0v = p0y < yPos ? Math.lerp(p0v, p1v, (float) Utils.inverseLerp(p0y, p1y, yPos)) : p0v;

				float q1x = Math.min((float) (xPos + width), p1x);
				float q1y = Math.min((float) (yPos + height), p1y);
				float q1u = p1x > xPos + width ? Math.lerp(p0u, p1u, (float) Utils.inverseLerp(p0x, p1x, xPos + width)) : p1u;
				float q1v = p1y > yPos + height ? Math.lerp(p0v, p1v, (float) Utils.inverseLerp(p0y, p1y, yPos + height)) : p1v;

				componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 0 + 0] = q0x;
				componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 0 + 1] = q0y;
				componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 0 + 2] = q0u;
				componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 0 + 3] = q0v;

				componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 1 + 0] = q1x;
				componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 1 + 1] = q0y;
				componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 1 + 2] = q1u;
				componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 1 + 3] = q0v;

				componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 2 + 0] = q1x;
				componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 2 + 1] = q1y;
				componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 2 + 2] = q1u;
				componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 2 + 3] = q1v;

				componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 3 + 0] = q1x;
				componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 3 + 1] = q1y;
				componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 3 + 2] = q1u;
				componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 3 + 3] = q1v;

				componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 4 + 0] = q0x;
				componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 4 + 1] = q1y;
				componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 4 + 2] = q0u;
				componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 4 + 3] = q1v;

				componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 5 + 0] = q0x;
				componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 5 + 1] = q0y;
				componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 5 + 2] = q0u;
				componentVertices[stride * i + Constants.WIDGET_ATTRIBUTES_PER_VERTEX * 5 + 3] = q0v;

				for (int j = 0; j < stride; j++) {
					vertexInfo.add(componentVertices[stride * i + j]);
				}
			}

		}
		//add border around frame
		float[] uvs = texture.getElementUVs(2, 1, 1, 1);
		float[] borderVertices = {
			//left
			(float) (xPos - Constants.FRAME_BORDER_WIDTH), (float) (yPos - Constants.FRAME_BORDER_WIDTH), uvs[0], uvs[1],
			(float) (xPos), (float) (yPos), uvs[2], uvs[1],
			(float) (xPos), (float) (yPos + height + Constants.FRAME_BORDER_WIDTH), uvs[2], uvs[3],
			(float) (xPos), (float) (yPos + height + Constants.FRAME_BORDER_WIDTH), uvs[2], uvs[3],
			(float) (xPos - Constants.FRAME_BORDER_WIDTH), (float) (yPos + height + Constants.FRAME_BORDER_WIDTH), uvs[0], uvs[3],
			(float) (xPos - Constants.FRAME_BORDER_WIDTH), (float) (yPos - Constants.FRAME_BORDER_WIDTH), uvs[0], uvs[1],

			//top
			(float) (xPos), (float) (yPos + height), uvs[0], uvs[1],
			(float) (xPos + width), (float) (yPos + height), uvs[2], uvs[1],
			(float) (xPos + width), (float) (yPos + height + Constants.FRAME_BORDER_WIDTH), uvs[2], uvs[3],
			(float) (xPos + width), (float) (yPos + height + Constants.FRAME_BORDER_WIDTH), uvs[2], uvs[3],
			(float) (xPos), (float) (yPos + height + Constants.FRAME_BORDER_WIDTH), uvs[0], uvs[3],
			(float) (xPos), (float) (yPos + height), uvs[0], uvs[1],

			//right
			(float) (xPos + width), (float) (yPos - Constants.FRAME_BORDER_WIDTH), uvs[0], uvs[1],
			(float) (xPos + width + Constants.FRAME_BORDER_WIDTH), (float) (yPos), uvs[2], uvs[1],
			(float) (xPos + width + Constants.FRAME_BORDER_WIDTH), (float) (yPos + height + Constants.FRAME_BORDER_WIDTH), uvs[2], uvs[3],
			(float) (xPos + width + Constants.FRAME_BORDER_WIDTH), (float) (yPos + height + Constants.FRAME_BORDER_WIDTH), uvs[2], uvs[3],
			(float) (xPos + width), (float) (yPos + height + Constants.FRAME_BORDER_WIDTH), uvs[0], uvs[3],
			(float) (xPos + width), (float) (yPos - Constants.FRAME_BORDER_WIDTH), uvs[0], uvs[1],

			//bottom
			(float) (xPos), (float) (yPos - Constants.FRAME_BORDER_WIDTH), uvs[0], uvs[1],
			(float) (xPos + width), (float) (yPos - Constants.FRAME_BORDER_WIDTH), uvs[2], uvs[1],
			(float) (xPos + width), (float) (yPos), uvs[2], uvs[3],
			(float) (xPos + width), (float) (yPos), uvs[2], uvs[3],
			(float) (xPos), (float) (yPos), uvs[0], uvs[3],
			(float) (xPos), (float) (yPos - Constants.FRAME_BORDER_WIDTH), uvs[0], uvs[1],
		};

		for (int i = 0; i < borderVertices.length; i++) {
			vertexInfo.add(borderVertices[i]);
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
