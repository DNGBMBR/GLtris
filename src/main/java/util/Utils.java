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
}
