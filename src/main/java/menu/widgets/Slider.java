package menu.widgets;

import menu.component.Component;
import menu.component.TextInfo;
import org.joml.Math;
import render.texture.TextureAtlas;
import util.*;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class Slider extends Component implements OnComponentClick, OnComponentHover {
	protected double percentage;

	protected double minValue, maxValue;

	protected double length;
	protected double barWidth;
	protected double clickerSize;
	protected boolean isHorizontal;
	protected boolean isClicked;
	private TextureAtlas texture;
	private int px, py;
	OnSliderMove onDrag;

	public Slider(double xPos, double yPos, boolean isInteractable, String displayText,
				  double percentage, double length, double minValue, double maxValue, double clickerSize, double barWidth, boolean isHorizontal,
				  TextureAtlas texture, int px, int py,
				  OnSliderMove onDrag) {
		super(xPos, yPos, isHorizontal ? length : Math.max(clickerSize, barWidth), !isHorizontal ? length : Math.max(clickerSize, barWidth), displayText, isInteractable);
		this.percentage = percentage;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.length = length;
		this.clickerSize = clickerSize;
		this.barWidth = barWidth;
		this.isHorizontal = isHorizontal;
		this.texture = texture;
		this.px = px;
		this.py = py;
		this.isClicked = false;
		this.onDrag = onDrag;
	}

	public double getPercentage() {
		return percentage;
	}

	public double getLength() {
		return length;
	}

	public double getBarWidth() {
		return barWidth;
	}

	public double getClickerSize() {
		return clickerSize;
	}

	public boolean isHorizontal() {
		return isHorizontal;
	}

	public boolean isClicked() {
		return isClicked;
	}

	@Override
	public void onHover(double mouseX, double mouseY, boolean isInFrame) {
		if (!isActive || !isInFrame || !isClicked()) {
			isClicked = false;
			return;
		}
		double percentagePosition = (isHorizontal ? mouseX - this.xPos - 0.5 * clickerSize : mouseY - this.yPos - 0.5 * clickerSize) / length;

		percentage = Math.clamp(0.0, 1.0, percentagePosition);
		this.onDrag.onMove(percentage);
	}

	@Override
	public void onClick(double mouseX, double mouseY, int button, int action, int mods) {
		if (!isActive) {
			return;
		}
		if (action == GLFW_RELEASE) {
			isClicked = false;
			return;
		}

		double clickerX = getClickablePositionX();
		double clickerY = getClickablePositionY();
		isClicked = action == GLFW_PRESS &&
			mouseX >= clickerX && mouseX <= clickerX + clickerSize &&
			mouseY >= clickerY && mouseY <= clickerY + clickerSize;
	}

	public double getClickablePositionX() {
		return isHorizontal ? xPos + length * percentage : xPos;
	}

	public double getClickablePositionY() {
		return !isHorizontal ? yPos + length * percentage : yPos;
	}

	@Override
	public float[] generateVertices() {
		float[] uvsClicker = texture.getElementUVs(px, py, 1, 1);
		float[] uvsBar = texture.getElementUVs(px + 1, py, 1, 1);

		float p0xBar = (float) (xPos + (clickerSize - barWidth) * 0.5);
		float p0yBar = (float) (yPos + (clickerSize - barWidth) * 0.5);
		float p1xBar = (float) (p0xBar + (isHorizontal ? length + (clickerSize - barWidth) * 0.5 : barWidth));
		float p1yBar = (float) (p0yBar + (!isHorizontal ? length + (clickerSize - barWidth) * 0.5 : barWidth));

		float p0xClicker = (float) (xPos + (isHorizontal ? percentage * length : 0));
		float p0yClicker = (float) (yPos + (!isHorizontal ? percentage * length : 0));
		float p1xClicker = p0xClicker + (float) clickerSize;
		float p1yClicker = p0yClicker + (float) clickerSize;

		float[] vertices = new float[2 * Constants.WIDGET_ATTRIBUTES_PER_VERTEX * Constants.WIDGET_ELEMENTS_PER_QUAD];

		Utils.addBlockVertices(vertices, 0,
			p0xBar, p0yBar, uvsBar[0], uvsBar[1],
			p1xBar, p1yBar, uvsBar[2], uvsBar[3]);

		Utils.addBlockVertices(vertices, Constants.WIDGET_ATTRIBUTES_PER_VERTEX * Constants.WIDGET_ELEMENTS_PER_QUAD,
			p0xClicker, p0yClicker, uvsClicker[0], uvsClicker[1],
			p1xClicker, p1yClicker, uvsClicker[2], uvsClicker[3]);

		return vertices;
	}

	@Override
	public List<TextInfo> getTextInfo() {
		float fontSize = (float) clickerSize * 0.75f;

		float startXName = (float) xPos - fontSize * (displayText.length() + 1);
		float startYName = (float) yPos;
		TextInfo infoName = new TextInfo(displayText, fontSize, startXName, startYName, 0.0f, 0.0f, 0.0f);

		float startXValue = (float) (xPos + clickerSize + (isHorizontal ? length : 0));
		float startYValue = (float) (yPos + (!isHorizontal ? length : 0));
		TextInfo infoValue = new TextInfo(String.valueOf(Math.lerp(minValue, maxValue, percentage)), fontSize, startXValue, startYValue, 0.0f, 0.0f, 0.0f);

		List<TextInfo> ret = new ArrayList<>();
		ret.add(infoName);
		ret.add(infoValue);
		return ret;
	}

	@Override
	public void onScroll(double mouseX, double mouseY, double xOffset, double yOffset) {

	}

	@Override
	public void destroy() {

	}
}
