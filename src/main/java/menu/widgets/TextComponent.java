package menu.widgets;

import menu.component.Component;
import menu.component.TextInfo;

import java.util.ArrayList;
import java.util.List;

//just shows text, no editing capabilities or otherwise
public class TextComponent extends Component {

	private List<TextInfo> text;

	public TextComponent(double xPos, double yPos, String displayText, float fontSize, float r, float g, float b, boolean isActive) {
		super(xPos, yPos, 0, 0, displayText, isActive);
		text = new ArrayList<>();
		text.add(new TextInfo(displayText, fontSize, (float) xPos, (float) yPos, r, g, b));
	}

	@Override
	public float[] generateVertices() {
		return new float[0];
	}

	@Override
	public List<TextInfo> getTextInfo() {
		return text;
	}

	@Override
	public void destroy() {

	}

	@Override
	public void onClick(double mouseX, double mouseY, int button, int action, int mods) {

	}

	@Override
	public void onHover(double mouseX, double mouseY, boolean isInFrame) {

	}

	@Override
	public void onScroll(double mouseX, double mouseY, double xOffset, double yOffset) {

	}
}
