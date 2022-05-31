package util;

public class Utils {
	public static void addVertices(float[] dst, float[] src, int index) {
		for (int i = 0; i < src.length; i++) {
			dst[index + i] = src[i];
		}
	}

	//assuming b > a, returns the percentage of the way c is between a and b
	//i.e. if a = 40, b = 100, c = 60, inverseLerp would return 0.33 because 60 is 33% of the way between 40 and 100
	public static double inverseLerp(double a, double b, double c) {
		return (c - a) / (b - a);
	}

	//assumes data conforms to the format {p0x, p0y, p0u, p0v}
	public static void addBlockVertices(float[] vertexData, int startIndex,
										float p0x, float p0y, float p0u, float p0v,
										float p1x, float p1y, float p1u, float p1v) {
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 0 + 0] = p0x;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 0 + 1] = p0y;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 0 + 2] = p0u;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 0 + 3] = p0v;

		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 1 + 0] = p1x;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 1 + 1] = p0y;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 1 + 2] = p1u;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 1 + 3] = p0v;

		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 2 + 0] = p1x;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 2 + 1] = p1y;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 2 + 2] = p1u;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 2 + 3] = p1v;

		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 3 + 0] = p1x;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 3 + 1] = p1y;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 3 + 2] = p1u;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 3 + 3] = p1v;

		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 4 + 0] = p0x;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 4 + 1] = p1y;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 4 + 2] = p0u;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 4 + 3] = p1v;

		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 5 + 0] = p0x;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 5 + 1] = p0y;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 5 + 2] = p0u;
		vertexData[startIndex + Constants.BLOCK_ATTRIBUTES_PER_VERTEX * 5 + 3] = p0v;
	}
}
