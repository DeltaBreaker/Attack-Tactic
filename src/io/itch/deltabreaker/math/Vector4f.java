package io.itch.deltabreaker.math;

public class Vector4f {

	public static final Vector4f COLOR_BASE = new Vector4f(1, 1, 1, 1);
	public static final Vector4f COLOR_BLACK = new Vector4f(0, 0, 0, 1);
	public static final Vector4f COLOR_RED = new Vector4f(1, 0.317f, 0.341f, 1);
	public static final Vector4f COLOR_GREEN = new Vector4f(0.5f, 1, 0.5f, 1);
	public static final Vector4f COLOR_BLUE = new Vector4f(0, 0.580f, 1, 1);
	public static final Vector4f COLOR_GRAY = new Vector4f(0.5f, 0.5f, 0.5f, 1);
	public static final Vector4f COLOR_LAVA = new Vector4f(1.35f, 0.64f, 0.64f, 0.75f);
	public static final Vector4f COLOR_WATER = new Vector4f(0.427f, 0.765f, 0.9f, 0.75f);
	public static final Vector4f COLOR_SPLASH_MAIN = new Vector4f(0.39216f, 0.44314f, 0.53333f, 1);
	public static final Vector4f COLOR_SPLASH_ALT = new Vector4f(0.243f, 0.153f, 0.192f, 1);
	public static final Vector4f COLOR_LAVA_HEAT = new Vector4f(1f, 0.35f, 0.35f, 1f);
	public static final Vector4f COLOR_POISON = new Vector4f(0.498f, 0.003f, 0.996f, 1f);
	public static final Vector4f COLOR_PEBBLE = new Vector4f(0.290f, 0.329f, 0.384f, 1);
	public static final Vector4f CLEAR = new Vector4f(0, 0, 0, 0);
	
	private float x, y, z, w;

	public Vector4f(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public Vector4f(Vector3f vector, float w) {
		x = vector.getX();
		y = vector.getY();
		z = vector.getZ();
		this.w = w;
	}

	public Vector4f copy() {
		return new Vector4f(x, y, z, w);
	}

	public boolean equals(Vector4f comp) {
		return x == comp.getX() && y == comp.getY() && z == comp.getZ() && w == comp.getW();
	}
	
	public Vector4f add(float x, float y, float z, float w) {
		this.x += x;
		this.y += y;
		this.z += z;
		this.w += w;
		return this;
	}

	public void add(Vector4f amt) {
		x += amt.x;
		y += amt.y;
		z += amt.z;
		w += amt.w;
	}

	public static Vector4f mul(Vector4f vector1, Vector4f vector2) {
		return new Vector4f(vector1.getX() * vector2.getX(), vector1.getY() * vector2.getY(), vector1.getZ() * vector2.getZ(), vector1.getW() * vector2.getW());
	}

	public Vector4f set(float x, float y, float z, float w) {
		setX(x);
		setY(y);
		setZ(z);
		setW(w);
		
		return this;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getZ() {
		return z;
	}

	public void setZ(float z) {
		this.z = z;
	}

	public float getW() {
		return w;
	}

	public Vector4f setW(float w) {
		this.w = w;
		return this;
	}

	public static Vector4f mul(Vector4f bodyColor, float f) {
		return new Vector4f(bodyColor.getX() * f, bodyColor.getY() * f, bodyColor.getZ() * f, bodyColor.getW() * f);
	}

	public float[] getElements() {
		return new float[] { x, y, z, w };
	}

	public void printValues() {
		System.out.println(x + " | " + y + " | " + z + " | " + w);
	}
	
}
