package render.texture;

public class Texture2DSettings {
	public int wrapS;
	public int wrapT;
	public int magFilter;
	public int minFilter;

	public Texture2DSettings(int wrapS, int wrapT, int magFilter, int minFilter) {
		this.wrapS = wrapS;
		this.wrapT = wrapT;
		this.magFilter = magFilter;
		this.minFilter = minFilter;
	}
}
