package render.manager;

import render.*;
import render.texture.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class ResourceManager {
	private static Map<String, Shader> shaders = new HashMap<>();
	private static Map<String, Texture2D> textureResources = new HashMap<>();
	private static Map<String, TextureAtlas> textureAtlasResources = new HashMap<>();
	private static Map<String, TextureNineSlice> textureNineSliceResources = new HashMap<>();

	private ResourceManager() {}

	public static void initializeResources() throws IOException, URISyntaxException {
		ResourceManager.createShader("shaders/block_vertex.glsl", "shaders/block_fragment.glsl");
		ResourceManager.createShader("shaders/widget_vertex.glsl", "shaders/widget_fragment.glsl");
		ResourceManager.createShader("shaders/text_vertex.glsl", "shaders/text_fragment.glsl");

		ResourceManager.createTextureAtlas("images/default_skin.png", 1, 32, 32);
		//ResourceManager.createTextureAtlas("images/slider.png", 1, 32, 32);
		ResourceManager.createTextureAtlas("fonts/font.png", 0, 8, 8);

		ResourceManager.createTextureNineSlice("images/widgets.png", 2, 32, 32, 8, 8);
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

	public static Texture2D createTexture(String fileName, int textureSlot) throws IOException {
		Texture2D texture = new Texture2D(fileName, textureSlot);
		textureResources.put(fileName, texture);
		return texture;
	}

	public static Texture2D getTextureByName(String fileName) {
		return textureResources.get(fileName);
	}

	public static TextureAtlas createTextureAtlas(String fileName, int textureSlot, int elementWidth, int elementHeight) throws IOException {
		TextureAtlas atlas = new TextureAtlas(fileName, textureSlot, elementWidth, elementHeight);
		textureAtlasResources.put(fileName, atlas);
		return atlas;
	}

	public static TextureAtlas getAtlasByName(String fileName) {
		return textureAtlasResources.get(fileName);
	}

	public static TextureNineSlice createTextureNineSlice(String fileName, int textureSlot, int elementWidth, int elementHeight, int borderWidth, int borderHeight) throws IOException {
		TextureNineSlice texture = new TextureNineSlice(fileName, textureSlot, elementWidth, elementHeight, borderWidth, borderHeight);
		textureNineSliceResources.put(fileName, texture);
		return texture;
	}

	public static TextureNineSlice getTextureNineSliceByName(String fileName) {
		return textureNineSliceResources.get(fileName);
	}
}
