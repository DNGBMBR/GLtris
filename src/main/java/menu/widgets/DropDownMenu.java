package menu.widgets;

import menu.component.Component;
import menu.component.TextInfo;
import menu.widgets.callbacks.OnComponentClick;
import render.manager.ResourceManager;
import render.texture.TextureNineSlice;
import util.Constants;
import util.Utils;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class DropDownMenu extends Component {
	TextureNineSlice texture = ResourceManager.getTextureNineSliceByName("images/widgets.png");
	String topText;
	boolean isPressed = false;
	boolean isDropped = false;
	List<Selection> selections;

	public DropDownMenu(double xPos, double yPos, double width, double height, String displayText, String topText, boolean isActive) {
		super(xPos, yPos, width, height, displayText, isActive);
		selections = new ArrayList<>();
		this.topText = topText;
	}

	public void addSelection(String text, OnComponentClick callback) {
		selections.add(new Selection(text, callback, selections.size() - 1));
	}

	@Override
	public float[] generateVertices() {
		float borderWidth = (float) height * 0.25f;

		if (!isDropped) {
			float[] vertices = new float[9 * Constants.WIDGET_ATTRIBUTES_PER_VERTEX * Constants.WIDGET_ELEMENTS_PER_QUAD];
			float[] uvs = texture.getElementUVsNineSlice(isPressed ?  Constants.DROP_MENU_PX_PRESSED : Constants.DROP_MENU_PX_DEFAULT, Constants.DROP_MENU_PY, Constants.DROP_MENU_TEX_WIDTH, Constants.DROP_MENU_TEX_HEIGHT);
			float p0x = (float) xPos;
			float p0y = (float) yPos;

			float p1x = (float) (xPos + width);
			float p1y = (float) (yPos + height);

			Utils.addBlockVerticesNineSlice(vertices, 0, p0x, p0y, p1x, p1y, borderWidth, uvs);

			return vertices;
		}

		float[] vertices = new float[9 * Constants.WIDGET_ATTRIBUTES_PER_VERTEX * Constants.WIDGET_ELEMENTS_PER_QUAD * (1 + selections.size())];
		float[] uvsDefault = null;
		float[] uvsPressed = null;
		float[] uvsBase;
		int acc = 0;

		if (isPressed) {
			uvsPressed = texture.getElementUVsNineSlice(Constants.DROP_MENU_PX_PRESSED, Constants.DROP_MENU_PY, Constants.DROP_MENU_TEX_WIDTH, Constants.DROP_MENU_TEX_HEIGHT);
			uvsBase = uvsPressed;
		}
		else {
			uvsDefault = texture.getElementUVsNineSlice(Constants.DROP_MENU_PX_DEFAULT, Constants.DROP_MENU_PY, Constants.DROP_MENU_TEX_WIDTH, Constants.DROP_MENU_TEX_HEIGHT);
			uvsBase = uvsDefault;
		}
		Utils.addBlockVerticesNineSlice(vertices, acc,
			(float) xPos, (float) yPos, (float) (xPos + width), (float) (yPos + height), borderWidth, uvsBase);
		acc += 9 * Constants.WIDGET_ATTRIBUTES_PER_VERTEX * Constants.WIDGET_ELEMENTS_PER_QUAD;
		for (int i = 0; i < selections.size(); i++) {
			Selection selection = selections.get(i);
			if (selection.isPressed && uvsPressed == null) {
				uvsPressed = texture.getElementUVsNineSlice(Constants.DROP_MENU_PX_PRESSED, Constants.DROP_MENU_PY, Constants.DROP_MENU_TEX_WIDTH, Constants.DROP_MENU_TEX_HEIGHT);
			}
			else if (!selection.isPressed && uvsDefault == null) {
				uvsDefault = texture.getElementUVsNineSlice(Constants.DROP_MENU_PX_DEFAULT, Constants.DROP_MENU_PY, Constants.DROP_MENU_TEX_WIDTH, Constants.DROP_MENU_TEX_HEIGHT);
			}

			uvsBase = selection.isPressed ? uvsPressed : uvsDefault;

			float p0x = (float) xPos;
			float p0y = (float) (yPos - (i + 1) * height);

			float p1x = (float) (xPos + width);
			float p1y = (float) (yPos - i * height);

			Utils.addBlockVerticesNineSlice(vertices, acc,
				p0x, p0y, p1x, p1y, borderWidth, uvsBase);

			acc += 9 * Constants.WIDGET_ATTRIBUTES_PER_VERTEX * Constants.WIDGET_ELEMENTS_PER_QUAD;
		}

		return vertices;
	}

	@Override
	public void destroy() {

	}

	@Override
	public void onClick(double mouseX, double mouseY, int button, int action, int mods) {
		if (isDropped) {
			boolean isInRegion = mouseX >= xPos && mouseX <= xPos + width && mouseY >= yPos - (height * selections.size()) && mouseY <= yPos + height;
			if (!isInRegion) {
				isPressed = false;
				isDropped = false;
				for (Selection selection : selections) {
					selection.isPressed = false;
				}
			}
			else {
				int index = (int) Math.floor((yPos - mouseY) / height);
				if (index < 0) {
					for (Selection s : selections) {
						s.isPressed = false;
					}
					if (action == GLFW_PRESS) {
						isPressed = true;
					}
					else if (action == GLFW_RELEASE && isPressed) {
						isPressed = false;
						isDropped = false;
					}
				}
				else {
					Selection selection = selections.get(index);
					isPressed = false;
					if (action == GLFW_PRESS) {
						for (Selection s : selections) {
							s.isPressed = false;
						}
						selection.isPressed = true;
					}
					else if (action == GLFW_RELEASE) {
						if (selection.isPressed) {
							selection.callback.onClick(mouseX, mouseY, button, action, mods);
						}
						for (Selection s : selections) {
							s.isPressed = false;
						}
					}
				}
			}
		}
		else {
			boolean isInMainRegion = mouseX >= xPos && mouseX <= xPos + width && mouseY >= yPos && mouseY <= yPos + height;
			if (!isInMainRegion) {
				isPressed = false;
			}
			else if (action == GLFW_PRESS) {
				isPressed = true;
			}
			else if (action == GLFW_RELEASE && isPressed) {
				isPressed = false;
				isDropped = true;
			}
		}
	}

	@Override
	public void onHover(double mouseX, double mouseY, boolean isInFrame) {

	}

	@Override
	public void onScroll(double mouseX, double mouseY, double xOffset, double yOffset) {

	}

	public String getTopText() {
		return this.topText;
	}

	public void setTopText(String topText) {
		this.topText = topText;
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
		float startX = centerX - fontSize * topText.length() * 0.5f;
		float startY = centerY - (float) height * 0.25f;

		TextInfo title = new TextInfo(displayText, fontSize, (float) (xPos - displayText.length() * fontSize - 0.5 * (height - fontSize)), (float) (yPos + 0.5 * (height - fontSize)), 0.0f, 0.0f, 0.0f);
		ret.add(title);

		TextInfo info = new TextInfo(topText, fontSize, startX, startY, 0.0f, 0.0f, 0.0f);
		ret.add(info);

		if (isDropped) {
			for (Selection selection : selections) {
				ret.add(selection.text);
			}
		}
		return ret;
	}

	private class Selection {
		TextInfo text;
		OnComponentClick callback;
		boolean isPressed;

		Selection(String text, OnComponentClick callback, int index) {
			float centerX = ((float) (2.0 * xPos + width)) * 0.5f;
			float centerY = ((float) (2.0f * (yPos - height * (index + 2)) + height)) * 0.5f;
			float fontSize = (float) height * 0.5f;
			float startX = centerX - fontSize * text.length() * 0.5f;
			float startY = centerY - (float) height * 0.25f;
			this.text = new TextInfo(text, fontSize, startX, startY, 0.0f, 0.0f, 0.0f);
			this.callback = callback;
			this.isPressed = false;
		}
	}
}
