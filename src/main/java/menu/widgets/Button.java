package menu.widgets;

import menu.component.Component;
import menu.component.TextInfo;
import render.texture.TextureNineSlice;
import util.Constants;
import util.Utils;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class Button extends Component implements OnComponentClick, OnComponentHover {
	//always assumes we're working on a 1920x1080 canvas
	public double borderWidth;
	TextureNineSlice texture;
	private int px, py;
	OnComponentClick onClickCallback;

	public boolean isPressed;
	public boolean isHovered;

	public Button(double xPos, double yPos, boolean isInteractable, double width, double height,
				  double borderWidth, String displayText,
				  TextureNineSlice texture, int px, int py,
				  OnComponentClick onClickCallback) {
		super(xPos, yPos, width, height, displayText, isInteractable);
		this.width = width;
		this.height = height;
		this.borderWidth = borderWidth;
		this.texture = texture;
		this.px = px;
		this.py = py;
		this.onClickCallback = onClickCallback;
		this.isPressed = false;
	}

	//generates vertices in local coordinates
	@Override
	public float[] generateVertices() {
		int textureOffset = 0;
		if (isPressed) {
			textureOffset = 2;
		}
		else if (isHovered) {
			textureOffset = 1;
		}
		float[] uvs = texture.getElementUVsNineSlice(px + textureOffset, py, 1, 1);

		float p0x = (float) xPos;
		float p0y = (float) yPos;
		float p1x = (float) (xPos + borderWidth);
		float p1y = (float) (yPos + borderWidth);
		float p2x = (float) (xPos + this.width - borderWidth);
		float p2y = (float) (yPos + this.height - borderWidth);
		float p3x = (float) (xPos + this.width);
		float p3y = (float) (yPos + this.height);

		float[] vertices = new float[6 * 9 * 4];

		Utils.addBlockVertices(vertices, 0 * Constants.WIDGET_ELEMENTS_PER_QUAD * Constants.WIDGET_ATTRIBUTES_PER_VERTEX,
			p0x, p0y, uvs[0], uvs[1],
			p1x, p1y, uvs[2], uvs[3]);

		Utils.addBlockVertices(vertices, 1 * Constants.WIDGET_ELEMENTS_PER_QUAD * Constants.WIDGET_ATTRIBUTES_PER_VERTEX,
			p1x, p0y, uvs[2], uvs[1],
			p2x, p1y, uvs[4], uvs[3]);

		Utils.addBlockVertices(vertices, 2 * Constants.WIDGET_ELEMENTS_PER_QUAD * Constants.WIDGET_ATTRIBUTES_PER_VERTEX,
			p2x, p0y, uvs[4], uvs[1],
			p3x, p1y, uvs[6], uvs[3]);

		Utils.addBlockVertices(vertices, 3 * Constants.WIDGET_ELEMENTS_PER_QUAD * Constants.WIDGET_ATTRIBUTES_PER_VERTEX,
			p0x, p1y, uvs[0], uvs[3],
			p1x, p2y, uvs[2], uvs[5]);

		Utils.addBlockVertices(vertices, 4 * Constants.WIDGET_ELEMENTS_PER_QUAD * Constants.WIDGET_ATTRIBUTES_PER_VERTEX,
			p1x, p1y, uvs[2], uvs[3],
			p2x, p2y, uvs[4], uvs[5]);

		Utils.addBlockVertices(vertices, 5 * Constants.WIDGET_ELEMENTS_PER_QUAD * Constants.WIDGET_ATTRIBUTES_PER_VERTEX,
			p3x, p2y, uvs[6], uvs[5],
			p2x, p1y, uvs[4], uvs[3]);

		Utils.addBlockVertices(vertices, 6 * Constants.WIDGET_ELEMENTS_PER_QUAD * Constants.WIDGET_ATTRIBUTES_PER_VERTEX,
			p0x, p2y, uvs[0], uvs[5],
			p1x, p3y, uvs[2], uvs[7]);

		Utils.addBlockVertices(vertices, 7 * Constants.WIDGET_ELEMENTS_PER_QUAD * Constants.WIDGET_ATTRIBUTES_PER_VERTEX,
			p1x, p2y, uvs[2], uvs[5],
			p2x, p3y, uvs[4], uvs[7]);

		Utils.addBlockVertices(vertices, 8 * Constants.WIDGET_ELEMENTS_PER_QUAD * Constants.WIDGET_ATTRIBUTES_PER_VERTEX,
			p2x, p2y, uvs[4], uvs[5],
			p3x, p3y, uvs[6], uvs[7]);

		//TODO: PLEASE let me use an element array buffer for batchers
		return vertices;
	}

	@Override
	public void onClick(double mouseX, double mouseY, int button, int action, int mods) {
		boolean isInRegion =
			mouseX >= xPos && mouseX <= xPos + width &&
			mouseY >= yPos && mouseY <= yPos + height;
		if (!isInRegion) {
			isPressed = false;
			return;
		}
		if (action == GLFW_PRESS) {
			isPressed = true;
		}
		if (action == GLFW_RELEASE) {
			isPressed = false;
			onClickCallback.onClick(mouseX, mouseY, button, action, mods);
		}
	}

	@Override
	public void onHover(double mouseX, double mouseY, boolean isInFrame) {
		isHovered =
			isInFrame &&
			mouseX >= this.xPos && mouseX <= this.xPos + width &&
			mouseY >= this.yPos && mouseY <= this.yPos + height;
		isPressed = isInFrame && isPressed;
	}

	@Override
	public void onScroll(double mouseX, double mouseY, double xOffset, double yOffset) {
		isHovered =
			mouseX >= this.xPos && mouseX <= this.xPos + width &&
			mouseY >= this.yPos && mouseY <= this.yPos + height;
	}

	@Override
	public List<TextInfo> getTextInfo() {
		float centerX = ((float) (2.0 * xPos + width)) * 0.5f;
		float centerY = ((float) (2.0f * yPos + height)) * 0.5f;
		float fontSize = (float) height * 0.5f;
		float startX = centerX - fontSize * displayText.length() * 0.5f;
		float startY = centerY - (float) height * 0.25f;

		TextInfo info = new TextInfo(displayText, fontSize, startX, startY, 1.0f, 1.0f, 1.0f);
		List<TextInfo> ret = new ArrayList<>();
		ret.add(info);
		return ret;
	}

	@Override
	public void destroy() {

	}
}
