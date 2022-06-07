package render.manager;

import render.*;
import render.texture.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public class ResourceManager {
	private static Map<String, Shader> shaders = new HashMap<>();
	private static Map<String, Texture2D> textureResources = new HashMap<>();
	private static Map<String, TextureAtlas> textureAtlasResources = new HashMap<>();
	private static Map<String, TextureNineSlice> textureNineSliceResources = new HashMap<>();

	private ResourceManager() {}

	public static void initializeResources() throws IOException, URISyntaxException {
		ResourceManager.createShader("shaders/block_vertex.glsl", "shaders/block_fragment.glsl");
		ResourceManager.createShader("shaders/text_vertex.glsl", "shaders/text_fragment.glsl");

		Texture2DSettings defaultSettings = new Texture2DSettings(GL_REPEAT, GL_REPEAT, GL_NEAREST, GL_NEAREST);
		Texture2DSettings backgroundSettings = new Texture2DSettings(GL_REPEAT, GL_REPEAT, GL_LINEAR, GL_LINEAR);

		ResourceManager.createTextureAtlas("images/default_skin.png", 0, defaultSettings, 32, 32);
		ResourceManager.createTextureAtlas("fonts/font.png", 0, defaultSettings, 8, 8);

		ResourceManager.createTextureNineSlice("images/widgets.png", 0, defaultSettings, 32, 32, 8, 8);
		ResourceManager.createTextureNineSlice("images/game_background.png", 0, backgroundSettings, 32, 32, 16, 16);
	}

	public static Shader createShader(String vertexFile, String fragmentFile) throws IOException, URISyntaxException {
		Shader shader = new Shader(vertexFile, fragmentFile);
		shader.compile();
		shaders.put(vertexFile + "|||" + fragmentFile, shader);
		return shader;
	}

	public static Shader getShaderByName(String vertexFile, String fragmentFile) {
		return shaders.get(vertexFile + "|||" + fragmentFile);
	}

	public static Texture2D createTexture(String fileName, int textureSlot, Texture2DSettings settings) throws IOException {
		Texture2D texture = new Texture2D(fileName, textureSlot, settings);
		textureResources.put(fileName, texture);
		return texture;
	}

	public static Texture2D getTextureByName(String fileName) {
		return textureResources.get(fileName);
	}

	public static TextureAtlas createTextureAtlas(String fileName, int textureSlot, Texture2DSettings settings, int elementWidth, int elementHeight) throws IOException {
		TextureAtlas atlas = new TextureAtlas(fileName, textureSlot, settings, elementWidth, elementHeight);
		textureAtlasResources.put(fileName, atlas);
		return atlas;
	}

	public static TextureAtlas getAtlasByName(String fileName) {
		return textureAtlasResources.get(fileName);
	}

	public static TextureNineSlice createTextureNineSlice(String fileName, int textureSlot, Texture2DSettings settings, int elementWidth, int elementHeight, int borderWidth, int borderHeight) throws IOException {
		TextureNineSlice texture = new TextureNineSlice(fileName, textureSlot, settings, elementWidth, elementHeight, borderWidth, borderHeight);
		textureNineSliceResources.put(fileName, texture);
		return texture;
	}

	public static TextureNineSlice getTextureNineSliceByName(String fileName) {
		return textureNineSliceResources.get(fileName);
	}
}
