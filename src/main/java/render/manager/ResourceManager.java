package render.manager;

import render.*;

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
