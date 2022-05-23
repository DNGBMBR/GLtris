package menu.widgets;

import org.joml.Math;
import render.TextureAtlas;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class Switch extends Widget{
	private double switchSize;
	private double backgroundWidth;

	private TextureAtlas texture;
	private int px, py, backgroundPx, backgroundPy;

	protected boolean isOn;
	private boolean isPressed = false;
	private OnSwitchClick onClickCallback;

	//assumes there are 3 tiles allocated in the atlas, all side by side, with px, py being the coordinates of the leftmost one.
	public Switch(double xPos, double yPos, double switchSize, double backgroundWidth,
				  boolean isOn, boolean isActive,
				  TextureAtlas texture, int px, int py,
				  String displayText,
				  OnSwitchClick onClickCallback) {
		super(xPos, yPos, 2 * switchSize, Math.max(switchSize, backgroundWidth), isActive, displayText);
		this.switchSize = switchSize;
		this.backgroundWidth = backgroundWidth;
		this.isOn = isOn;
		this.onClickCallback = onClickCallback;
		this.texture = texture;
		this.px = px;
		this.py = py;
	}

	@Override
	public float[] generateVertices() {
		float[] uvsSwitch = isPressed ? texture.getElementUVs(px + 1, py, 1, 1) : texture.getElementUVs(px, py, 1, 1);
		float[] uvsBackground = texture.getElementUVs(px + 2, py, 1, 1);

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
	public void onHover(double mouseX, double mouseY) {

	}
}
