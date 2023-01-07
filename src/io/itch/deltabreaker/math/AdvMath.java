package io.itch.deltabreaker.math;

public class AdvMath {
	
	public static int values = 360;
	public static float[] sin = new float[values];
	
	public static void loadLookupTable() {
		for(int i = 0; i < values; i++) {
			sin[i] = (float) Math.sin(Math.toRadians(i * (360.0 / values)));
		}
	}
	
	public static double inRange(double n, double min, double max) {
		if (n > max) {
			return max;
		}
		if (n < min) {
			return min;
		}
		return n;
	}
	
	public static int inRange(int n, int min, int max) {
		if (n > max) {
			return max;
		}
		if (n < min) {
			return min;
		}
		return n;
	}

	public static float largest(float[] data) {
		float largest = data[0];
		for(float f : data) {
			if(f > largest) {
				largest = f;
			}
		}
		return largest;
	}
	
}
