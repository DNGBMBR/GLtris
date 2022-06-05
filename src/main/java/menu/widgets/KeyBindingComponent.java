package menu.widgets;

import menu.component.Component;
import menu.component.TextInfo;
import menu.widgets.callbacks.OnKeyBindingChange;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import render.texture.TextureAtlas;
import util.*;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class KeyBindingComponent extends Component implements GLFWKeyCallbackI {
	boolean isClicked = false;
	boolean isConfiguringControls = false;
	String currentKeyName;

	int currentScancode;

	TextureAtlas texture;
	int px, py;

	OnKeyBindingChange callback;

	public KeyBindingComponent(double xPos, double yPos, double width, double height,
							   String displayText, boolean isActive, int currentScancode,
							   TextureAtlas texture, int px, int py,
							   OnKeyBindingChange callback) {
		super(xPos, yPos, width, height, displayText, isActive);
		this.currentScancode = currentScancode;
		currentKeyName = Utils.getKeyName(currentScancode);

		this.texture = texture;
		this.px = px;
		this.py = py;

		this.callback = callback;

		KeyListener.registerKeyCallback(this);
	}

	@Override
	public float[] generateVertices() {
		float[] vertices = new float[Constants.WIDGET_ATTRIBUTES_PER_VERTEX * Constants.WIDGET_ELEMENTS_PER_QUAD];
		float[] uvs = texture.getElementUVs(px, py, 1, 1);

		float p0x = (float) xPos;
		float p0y = (float) yPos;

		float p1x = (float) (xPos + width);
		float p1y = (float) (yPos + height);

		Utils.addBlockVertices(vertices, 0,
			p0x, p0y, uvs[0], uvs[1],
			p1x, p1y, uvs[2], uvs[3]);

		return vertices;
	}

	@Override
	public List<TextInfo> getTextInfo() {
		List<TextInfo> ret = new ArrayList<>();
		TextInfo info = new TextInfo(currentKeyName, 24.0f,
			(float) ((xPos + 0.5 * width - 0.5 * 24.0 * currentKeyName.length())), (float) (yPos + 0.5 * height - 0.5 * 24.0),
			1.0f, 1.0f, 1.0f);
		ret.add(info);
		return ret;
	}

	@Override
	public void destroy() {
		KeyListener.unregisterKeyCallback(this);
	}

	@Override
	public void onClick(double mouseX, double mouseY, int button, int action, int mods) {
		if (isCursorHovered(mouseX, mouseY) && action == GLFW_PRESS) {
			isClicked = true;
		}
		if (isCursorHovered(mouseX, mouseY) && action == GLFW_RELEASE) {
			isClicked = false;
			isConfiguringControls = true;
		}
		else {
			isClicked = false;
			isConfiguringControls = false;
		}
		updateDisplayText();
	}

	@Override
	public void onHover(double mouseX, double mouseY, boolean isInFrame) {

	}

	@Override
	public void onScroll(double mouseX, double mouseY, double xOffset, double yOffset) {

	}

	@Override
	public void invoke(long window, int key, int scancode, int action, int mods) {
		if (isConfiguringControls && action == GLFW_PRESS) {
			currentScancode = scancode;
			callback.onKeyBindingChange(currentScancode);
			isConfiguringControls = false;
		}
		updateDisplayText();
	}

	private void updateDisplayText() {
		if (isConfiguringControls) {
			currentKeyName = "input key";
		}
		else {
			currentKeyName = Utils.getKeyName(currentScancode);
		}
	}
}
