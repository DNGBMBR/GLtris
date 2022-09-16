package scenes;

import menu.component.TopFrame;
import menu.widgets.Button;
import menu.widgets.TextField;
import network.lobby.Client;
import render.Shader;
import render.batch.WidgetBatch;
import render.manager.ResourceManager;
import render.manager.TextRenderer;
import render.texture.TextureNineSlice;
import util.Constants;

import java.io.IOException;
import java.net.*;

import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class LobbySearchScene extends Scene{

	private static final double BUTTON_WIDTH = 600.0;
	private static final double BUTTON_HEIGHT = 100.0;
	private static final double BUTTON_BORDER_WIDTH = 20.0;

	Shader menuShader;
	WidgetBatch widgetBatch;
	TextureNineSlice widgetTexture;
	TextRenderer textRenderer;

	TopFrame topFrame = new TopFrame(Constants.VIEWPORT_W, Constants.VIEWPORT_H, true);

	Scene nextScene;

	LobbySearchScene(long windowID, Client client) {
		super(windowID, client);

		menuShader = ResourceManager.getShaderByName("shaders/block_vertex.glsl", "shaders/block_fragment.glsl");
		widgetTexture = ResourceManager.getTextureNineSliceByName("images/widgets.png");
		widgetBatch = new WidgetBatch(40);
		textRenderer = TextRenderer.getInstance();

		topFrame.addComponent(new Button(50, 50, true,
			BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_BORDER_WIDTH, "Back",
			widgetTexture, Constants.BUTTON_PX, Constants.BUTTON_PY,
			(double mouseX, double mouseY, int button, int action, int mods) -> {
				if (action == GLFW_RELEASE) {
					nextScene = new MenuScene(windowID, client);
					shouldChangeScene = true;
				}
			}));
		TextField textFieldUsername = new TextField((Constants.VIEWPORT_W - 500) * 0.5, Constants.VIEWPORT_H * 0.5, 500, 50, true,
			"Username", "", 24, 0, 0, 0);
		topFrame.addComponent(textFieldUsername);
		TextField textFieldIP = new TextField((Constants.VIEWPORT_W - 500) * 0.5, Constants.VIEWPORT_H * 0.5 - 70, 500, 50, true,
			"IP Address", "", 24, 0, 0, 0);
		topFrame.addComponent(textFieldIP);
		TextField textFieldPort = new TextField((Constants.VIEWPORT_W - 500) * 0.5, Constants.VIEWPORT_H * 0.5 - 140, 500, 50, true,
			"Port", "", 24, 0, 0, 0);
		topFrame.addComponent(textFieldPort);
		topFrame.addComponent(new Button((Constants.VIEWPORT_W - BUTTON_WIDTH) * 0.5, 50, true,
			BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_BORDER_WIDTH, "Join",
			widgetTexture, Constants.BUTTON_PX, Constants.BUTTON_PY,
			(double mouseX, double mouseY, int button, int action, int mods) -> {
				if (action == GLFW_RELEASE) {
					try {
						InetAddress address = InetAddress.getByName(textFieldIP.getText());
						client.setAddress(address, Integer.parseInt(textFieldPort.getText()), textFieldUsername.getText());
						boolean foundServer = client.start();
						if (foundServer) {
							nextScene = new LobbyScene(windowID, client);
							shouldChangeScene = true;
						}
					} catch (NumberFormatException e) {
						//error message on screen
						System.out.println("Could not parse port number");
					} catch (IOException e) {
						//error message
						System.out.println("Could not connect to address");
					}
				}
			}));
	}

	@Override
	public void update(double dt) {

	}

	@Override
	public void draw() {
		menuShader.bind();

		//bind texture for button here
		menuShader.bindTexture2D("uTexture", widgetTexture);

		float[] buffer = new float[16];
		menuShader.uploadUniformMatrix4fv("uProjection", false, projection.get(buffer));

		widgetBatch.addComponent(topFrame);

		widgetBatch.flush();

		textRenderer.bind();

		textRenderer.addText(topFrame);

		textRenderer.draw();
	}

	@Override
	public Scene nextScene() {
		return nextScene;
	}

	@Override
	public void destroy() {
		topFrame.destroy();
	}
}
