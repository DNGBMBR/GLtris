package menu.widgets;

import menu.component.Component;
import menu.component.TextInfo;
import menu.widgets.callbacks.OnComponentClick;
import menu.widgets.callbacks.OnComponentHover;
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
	OnComponentClick onClickCallback;

	public boolean isPressed;
	public boolean isHovered;

	public Button(double xPos, double yPos, boolean isInteractable, double width, double height,
				  double borderWidth, String displayText,
				  TextureNineSlice texture,
				  OnComponentClick onClickCallback) {
		super(xPos, yPos, width, height, displayText, isInteractable);
		this.width = width;
		this.height = height;
		this.borderWidth = borderWidth;
		this.texture = texture;
		this.onClickCallback = onClickCallback;
		this.isPressed = false;
	}

	//generates vertices in local coordinates
	@Override
	public float[] generateVertices() {
		if (!isActive) {
			return new float[0];
		}
		int px = isPressed ? Constants.BUTTON_PX_PRESSED : isHovered ? Constants.BUTTON_PX_HOVERED : Constants.BUTTON_PX_DEFAULT;
		float[] uvs = texture.getElementUVsNineSlice(px, Constants.BUTTON_PY, Constants.BUTTON_TEX_WIDTH, Constants.BUTTON_TEX_HEIGHT);

		float p0x = (float) xPos;
		float p0y = (float) yPos;
		float p3x = (float) (xPos + this.width);
		float p3y = (float) (yPos + this.height);

		float[] vertices = new float[9 * Constants.WIDGET_ELEMENTS_PER_QUAD * Constants.WIDGET_ATTRIBUTES_PER_VERTEX];

		Utils.addBlockVerticesNineSlice(vertices, 0, p0x, p0y, p3x, p3y, (float) borderWidth, uvs);

		//TODO: PLEASE let me use an element array buffer for batchers
		return vertices;
	}

	@Override
	public void onClick(double mouseX, double mouseY, int button, int action, int mods) {
		if (!isActive) {
			return;
		}
		boolean isInRegion =
			mouseX >= xPos && mouseX <= xPos + width &&
			mouseY >= yPos && mouseY <= yPos + height;
		if (!isInRegion) {
			isPressed = false;
		}
		else if (action == GLFW_PRESS) {
			isPressed = true;
		}
		else if (action == GLFW_RELEASE && isPressed) {
			isPressed = false;
			onClickCallback.onClick(mouseX, mouseY, button, action, mods);
		}
	}

	@Override
	public void onHover(double mouseX, double mouseY, boolean isInFrame) {
		if (!isActive) {
			return;
		}
		isHovered =
			isInFrame &&
			mouseX >= this.xPos && mouseX <= this.xPos + width &&
			mouseY >= this.yPos && mouseY <= this.yPos + height;
		isPressed = isInFrame && isPressed;
	}

	@Override
	public void onScroll(double mouseX, double mouseY, double xOffset, double yOffset) {
		if (!isActive) {
			return;
		}
		isHovered =
			mouseX >= this.xPos && mouseX <= this.xPos + width &&
			mouseY >= this.yPos && mouseY <= this.yPos + height;
	}

	@Override
	public List<TextInfo> getTextInfo() {
		List<TextInfo> ret = new ArrayList<>();
		if (!isActive) {
			return ret;
		}
		float centerX = ((float) (2.0 * xPos + width)) * 0.5f;
		float centerY = ((float) (2.0f * yPos + height)) * 0.5f;
		float fontSize = (float) height * 0.5f;
		float startX = centerX - fontSize * displayText.length() * 0.5f;
		float startY = centerY - (float) height * 0.25f;

		TextInfo info = new TextInfo(displayText, fontSize, startX, startY, 1.0f, 1.0f, 1.0f);
		ret.add(info);
		return ret;
	}

	@Override
	public void destroy() {

	}
}
