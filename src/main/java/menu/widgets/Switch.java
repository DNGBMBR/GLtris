package menu.widgets;

import menu.component.Component;
import menu.component.TextInfo;
import menu.widgets.callbacks.*;
import org.joml.Math;
import render.texture.TextureAtlas;
import util.Constants;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class Switch extends Component implements OnComponentClick, OnComponentHover {
	private double switchSize;
	private double backgroundWidth;

	private TextureAtlas texture;

	protected boolean isOn;
	private boolean isPressed = false;
	private OnSwitchClick onClickCallback;

	public Switch(double xPos, double yPos, double switchSize, double backgroundWidth,
				  boolean isOn, boolean isActive,
				  TextureAtlas texture,
				  String displayText,
				  OnSwitchClick onClickCallback) {
		super(xPos, yPos, 2 * switchSize, Math.max(switchSize, backgroundWidth), displayText, isActive);
		this.switchSize = switchSize;
		this.backgroundWidth = backgroundWidth;
		this.isOn = isOn;
		this.onClickCallback = onClickCallback;
		this.texture = texture;
	}

	@Override
	public float[] generateVertices() {
		float[] uvsSwitch = texture.getElementUVs(
			isPressed ? Constants.SWITCH_PX_PRESSED : isOn ? Constants.SWITCH_PX_ON : Constants.SWITCH_PX_OFF, Constants.SWITCH_PY,
			Constants.SWITCH_TEX_WIDTH, Constants.SWITCH_TEX_HEIGHT);
		float[] uvsBackground = texture.getElementUVs(Constants.SWITCH_PX_BACKGROUND, Constants.SWITCH_PY, Constants.SWITCH_TEX_WIDTH, Constants.SWITCH_TEX_HEIGHT);

		float p0x = (float) (xPos + (isOn ? switchSize : 0));
		float p0y = (float) yPos;
		float p1x = (float) (xPos + switchSize + (isOn ? switchSize : 0));
		float p1y = (float) (yPos + switchSize);

		float p0xBackground = (float) (xPos + (switchSize - backgroundWidth) * 0.5);
		float p0yBackground = (float) (yPos + (switchSize - backgroundWidth) * 0.5);
		float p1xBackground = (float) (xPos + 2 * switchSize - (switchSize - backgroundWidth) * 0.5);
		float p1yBackground = (float) (yPos + (switchSize + backgroundWidth) * 0.5);

		float[] vertices = {
			p0xBackground, p0yBackground, uvsBackground[0], uvsBackground[1],
			p1xBackground, p0yBackground, uvsBackground[2], uvsBackground[1],
			p1xBackground, p1yBackground, uvsBackground[2], uvsBackground[3],
			p1xBackground, p1yBackground, uvsBackground[2], uvsBackground[3],
			p0xBackground, p1yBackground, uvsBackground[0], uvsBackground[3],
			p0xBackground, p0yBackground, uvsBackground[0], uvsBackground[1],

			p0x, p0y, uvsSwitch[0], uvsSwitch[1],
			p1x, p0y, uvsSwitch[2], uvsSwitch[1],
			p1x, p1y, uvsSwitch[2], uvsSwitch[3],
			p1x, p1y, uvsSwitch[2], uvsSwitch[3],
			p0x, p1y, uvsSwitch[0], uvsSwitch[3],
			p0x, p0y, uvsSwitch[0], uvsSwitch[1],
		};
		return vertices;
	}

	@Override
	public List<TextInfo> getTextInfo() {
		float fontSize = (float) switchSize * 0.75f;
		float startXName = (float) (xPos - fontSize * (displayText.length() + 1));
		float startYName = (float) yPos;
		TextInfo infoName = new TextInfo(displayText, fontSize, startXName, startYName, 0.0f, 0.0f, 0.0f);
		List<TextInfo> ret = new ArrayList<>();
		ret.add(infoName);
		return ret;
	}

	@Override
	public void destroy() {

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
			this.isOn = !this.isOn;
			onClickCallback.onClick(isOn);
		}
	}

	@Override
	public void onScroll(double mouseX, double mouseY, double xOffset, double yOffset) {

	}

	@Override
	public void onHover(double mouseX, double mouseY, boolean isInFrame) {

	}
}
