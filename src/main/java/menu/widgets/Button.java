package menu.widgets;

import render.TextureNineSlice;
import util.*;

import static org.lwjgl.glfw.GLFW.*;

public class Button extends Widget {
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
		super(xPos, yPos, width, height, isInteractable, displayText);
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

		float[][] buttonVertices = {
			{p0x, p0y, uvs[0], uvs[1]},
			{p1x, p0y, uvs[2], uvs[1]},
			{p2x, p0y, uvs[4], uvs[1]},
			{p3x, p0y, uvs[6], uvs[1]},
			{p0x, p1y, uvs[0], uvs[3]},
			{p1x, p1y, uvs[2], uvs[3]},
			{p2x, p1y, uvs[4], uvs[3]},
			{p3x, p1y, uvs[6], uvs[3]},
			{p0x, p2y, uvs[0], uvs[5]},
			{p1x, p2y, uvs[2], uvs[5]},
			{p2x, p2y, uvs[4], uvs[5]},
			{p3x, p2y, uvs[6], uvs[5]},
			{p0x, p3y, uvs[0], uvs[7]},
			{p1x, p3y, uvs[2], uvs[7]},
			{p2x, p3y, uvs[4], uvs[7]},
			{p3x, p3y, uvs[6], uvs[7]},
		};

		float[] vertices = new float[6 * 9 * 4];

		//TODO: PLEASE let me use an element array buffer for batchers
		Utils.addVertices(vertices, buttonVertices[0], 0);
		Utils.addVertices(vertices, buttonVertices[1], 4);
		Utils.addVertices(vertices, buttonVertices[5], 8);
		Utils.addVertices(vertices, buttonVertices[5], 12);
		Utils.addVertices(vertices, buttonVertices[4], 16);
		Utils.addVertices(vertices, buttonVertices[0], 20);

		Utils.addVertices(vertices, buttonVertices[1], 24);
		Utils.addVertices(vertices, buttonVertices[2], 28);
		Utils.addVertices(vertices, buttonVertices[6], 32);
		Utils.addVertices(vertices, buttonVertices[6], 36);
		Utils.addVertices(vertices, buttonVertices[5], 40);
		Utils.addVertices(vertices, buttonVertices[1], 44);

		Utils.addVertices(vertices, buttonVertices[2], 48);
		Utils.addVertices(vertices, buttonVertices[3], 52);
		Utils.addVertices(vertices, buttonVertices[7], 56);
		Utils.addVertices(vertices, buttonVertices[7], 60);
		Utils.addVertices(vertices, buttonVertices[6], 64);
		Utils.addVertices(vertices, buttonVertices[2], 68);

		Utils.addVertices(vertices, buttonVertices[4], 72);
		Utils.addVertices(vertices, buttonVertices[5], 76);
		Utils.addVertices(vertices, buttonVertices[9], 80);
		Utils.addVertices(vertices, buttonVertices[9], 84);
		Utils.addVertices(vertices, buttonVertices[8], 88);
		Utils.addVertices(vertices, buttonVertices[4], 92);

		Utils.addVertices(vertices, buttonVertices[5], 96);
		Utils.addVertices(vertices, buttonVertices[6], 100);
		Utils.addVertices(vertices, buttonVertices[10], 104);
		Utils.addVertices(vertices, buttonVertices[10], 108);
		Utils.addVertices(vertices, buttonVertices[9], 112);
		Utils.addVertices(vertices, buttonVertices[5], 116);

		Utils.addVertices(vertices, buttonVertices[6], 120);
		Utils.addVertices(vertices, buttonVertices[7], 124);
		Utils.addVertices(vertices, buttonVertices[11], 128);
		Utils.addVertices(vertices, buttonVertices[11], 132);
		Utils.addVertices(vertices, buttonVertices[10], 136);
		Utils.addVertices(vertices, buttonVertices[6], 140);

		Utils.addVertices(vertices, buttonVertices[8], 144);
		Utils.addVertices(vertices, buttonVertices[9], 148);
		Utils.addVertices(vertices, buttonVertices[13], 152);
		Utils.addVertices(vertices, buttonVertices[13], 156);
		Utils.addVertices(vertices, buttonVertices[12], 160);
		Utils.addVertices(vertices, buttonVertices[8], 164);

		Utils.addVertices(vertices, buttonVertices[9], 168);
		Utils.addVertices(vertices, buttonVertices[10], 172);
		Utils.addVertices(vertices, buttonVertices[14], 176);
		Utils.addVertices(vertices, buttonVertices[14], 180);
		Utils.addVertices(vertices, buttonVertices[13], 184);
		Utils.addVertices(vertices, buttonVertices[9], 188);

		Utils.addVertices(vertices, buttonVertices[10], 192);
		Utils.addVertices(vertices, buttonVertices[11], 196);
		Utils.addVertices(vertices, buttonVertices[15], 200);
		Utils.addVertices(vertices, buttonVertices[15], 204);
		Utils.addVertices(vertices, buttonVertices[14], 208);
		Utils.addVertices(vertices, buttonVertices[10], 212);

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
	public void onHover(double mouseX, double mouseY) {
		isHovered =
			mouseX >= this.xPos && mouseX <= this.xPos + width &&
			mouseY >= this.yPos && mouseY <= this.yPos + height;
	}

	@Override
	public void destroy() {

	}
}
