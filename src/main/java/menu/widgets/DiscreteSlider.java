package menu.widgets;

import menu.component.Component;
import menu.component.TextInfo;
import menu.widgets.callbacks.OnDiscreteSliderMove;
import org.joml.Math;
import render.texture.TextureAtlas;
import util.Constants;
import util.Utils;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class DiscreteSlider extends Component {
	int currentValue, minValue, maxValue, interval;

	protected double length;
	protected double barWidth;
	protected double clickerSize;
	protected boolean isHorizontal;
	protected boolean isClicked;
	private TextureAtlas texture;
	private OnDiscreteSliderMove onDrag;

	public DiscreteSlider(double xPos, double yPos, boolean isInteractable, String displayText,
						  int currentValue, int minValue, int maxValue, int interval,
						  double length, double clickerSize, double barWidth, boolean isHorizontal,
						  TextureAtlas texture, OnDiscreteSliderMove onDrag) {
		super(xPos, yPos, isHorizontal ? length : Math.max(clickerSize, barWidth), !isHorizontal ? length : Math.max(clickerSize, barWidth), displayText, isInteractable);
		this.length = length;
		this.clickerSize = clickerSize;
		this.barWidth = barWidth;
		this.isHorizontal = isHorizontal;
		this.texture = texture;
		this.isClicked = false;
		this.onDrag = onDrag;

		this.currentValue = currentValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.interval = interval;
	}

	@Override
	public float[] generateVertices() {
		float percentage = (float) (currentValue - minValue) / (maxValue - minValue);

		float[] uvsClicker = texture.getElementUVs(Constants.SLIDER_PX_CLICKER, Constants.SLIDER_PY, Constants.SLIDER_TEX_WIDTH, Constants.SLIDER_TEX_HEIGHT);
		float[] uvsBar = texture.getElementUVs(Constants.SLIDER_PX_BACKING, Constants.SLIDER_PY, Constants.SLIDER_TEX_WIDTH, Constants.SLIDER_TEX_HEIGHT);

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

		float startXName = (float) (xPos - fontSize * (displayText.length() + 1));
		float startYName = (float) yPos;
		TextInfo infoName = new TextInfo(displayText, fontSize, startXName, startYName, 0.0f, 0.0f, 0.0f);

		float startXValue = (float) (xPos + clickerSize + (isHorizontal ? length : 0) + 0.5 * clickerSize);
		float startYValue = (float) (yPos + (!isHorizontal ? length : 0));
		TextInfo infoValue = new TextInfo(String.valueOf(currentValue), fontSize, startXValue, startYValue, 0.0f, 0.0f, 0.0f);

		List<TextInfo> ret = new ArrayList<>();
		ret.add(infoName);
		ret.add(infoValue);
		return ret;
	}

	@Override
	public void destroy() {

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
		double percentage = (double) (currentValue - minValue) / (maxValue - minValue);
		return isHorizontal ? xPos + length * percentage : xPos;
	}

	public double getClickablePositionY() {
		double percentage = (double) currentValue / (maxValue - minValue);
		return !isHorizontal ? yPos + length * percentage : yPos;
	}

	@Override
	public void onHover(double mouseX, double mouseY, boolean isInFrame) {
		if (!isActive || !isInFrame || !isClicked) {
			isClicked = false;
			return;
		}
		double percentagePosition = (isHorizontal ? mouseX - this.xPos - 0.5 * clickerSize : mouseY - this.yPos - 0.5 * clickerSize) / length;
		percentagePosition = Math.clamp(0.0, 1.0, percentagePosition);
		currentValue = (int) Math.round(percentagePosition * maxValue + (1.0 - percentagePosition) * minValue);
		currentValue = Math.clamp(minValue, maxValue, currentValue);
		this.onDrag.onMove(currentValue);
	}

	@Override
	public void onScroll(double mouseX, double mouseY, double xOffset, double yOffset) {

	}
}
