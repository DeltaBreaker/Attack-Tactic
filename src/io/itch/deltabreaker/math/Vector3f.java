package io.itch.deltabreaker.math;

import java.util.Random;

public class Vector3f {

	private float x, y, z;

	public static final Vector3f EMPTY = new Vector3f(0, 0, 0);

	public static final Vector3f FLIPPED = new Vector3f(0, 180, 0);

	public static final Vector3f DOWN = new Vector3f(0, -1, 0);

	public static final Vector3f SCALE_16X = new Vector3f(16f, 16f, 16f);
	public static final Vector3f SCALE_8X = new Vector3f(8f, 8f, 8f);
	public static final Vector3f SCALE_TRIPLE = new Vector3f(3f, 3f, 3f);
	public static final Vector3f SCALE_DOUBLE = new Vector3f(2f, 2f, 2f);
	public static final Vector3f SCALE_FULL = new Vector3f(1f, 1f, 1f);
	public static final Vector3f SCALE_HALF = new Vector3f(0.5f, 0.5f, 0.5f);
	public static final Vector3f SCALE_QUARTER = new Vector3f(0.25f, 0.25f, 0.25f);
	public static final Vector3f DEFAULT_CAMERA_ROTATION = new Vector3f(-60, 0, 0);
	public static final Vector3f DEFAULT_INVERSE_CAMERA_ROTATION = Vector3f.mul(DEFAULT_CAMERA_ROTATION, -1);
	public static final Vector3f CAMPFIRE_MULTIPLIER = new Vector3f(10f, 5f, 0f);

	public static final Vector3f SCREEN_POSITION = new Vector3f(0, 0, -2);

	public Vector3f(float n) {
		this.x = n;
		this.y = n;
		this.z = n;
	}

	public Vector3f(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static Vector3f randomRotation() {
		return new Vector3f(new Random().nextInt(360), new Random().nextInt(360), new Random().nextInt(360));
	}

	public static Vector3f randomScale(float min) {
		return new Vector3f(min, min, min).mul(new Random().nextFloat() + 0.5f);
	}

	public void set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static float distance(Vector3f v1, Vector3f v2) {
		return (float) Math.sqrt((v1.x - v2.x) * (v1.x - v2.x) + (v1.y - v2.y) * (v1.y - v2.y) + (v1.z - v2.z) * (v1.z - v2.z));
	}

	public static float flatDistance(Vector3f v1, Vector3f v2) {
		return (float) Math.sqrt((v1.x - v2.x) * (v1.x - v2.x) + (v1.z - v2.z) * (v1.z - v2.z));
	}

	public static float lineDistance(Vector3f v1, Vector3f v2) {
		return Math.abs(v1.x - v2.x);
	}

	public Vector3f copy() {
		return new Vector3f(x, y, z);
	}

	public Vector3f add(Vector3f vector) {
		x += vector.getX();
		y += vector.getY();
		z += vector.getZ();
		return this;
	}

	public Vector3f mul(Vector3f vector) {
		x *= vector.getX();
		y *= vector.getY();
		z *= vector.getZ();

		return this;
	}

	public Vector3f add(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;

		return this;
	}

	public Vector3f div(float x, float y, float z) {
		this.x /= x;
		this.y /= y;
		this.z /= z;

		return this;
	}

	public Vector3f div(Vector3f v) {
		this.x /= v.x;
		this.y /= v.y;
		this.z /= v.z;

		return this;
	}

	public Vector3f add(float amt) {
		x += amt;
		y += amt;
		z += amt;

		return this;
	}

	public Vector3f sub(float amt) {
		x -= amt;
		y -= amt;
		z -= amt;

		return this;
	}

	public Vector3f sub(float x, float y, float z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;

		return this;
	}

	public Vector3f mul(float amt) {
		x *= amt;
		y *= amt;
		z *= amt;

		return this;
	}

	public Vector3f pow(float amt) {
		x = (float) Math.pow(x, amt);
		y = (float) Math.pow(y, amt);
		z = (float) Math.pow(z, amt);

		return this;
	}

	public boolean empty() {
		return x == 0 && y == 0 && z == 0;
	}

	public float total() {
		return Math.abs(x) + Math.abs(y) + Math.abs(z);
	}

	public float trueTotal() {
		return x + y + z;
	}

	public static Vector3f add(Vector3f vector1, Vector3f vector2) {
		return new Vector3f(vector1.getX() + vector2.getX(), vector1.getY() + vector2.getY(), vector1.getZ() + vector2.getZ());
	}

	public static Vector3f add(Vector3f vector1, float x, float y, float z) {
		return new Vector3f(vector1.getX() + x, vector1.getY() + y, vector1.getZ() + z);
	}

	public static Vector3f addAndCreate(float x, float y, float z, float x2, float y2, float z2) {
		return new Vector3f(x + x2, y + y2, z + z2);
	}

	public static Vector3f sub(Vector3f vector1, Vector3f vector2) {
		return new Vector3f(vector1.getX() - vector2.getX(), vector1.getY() - vector2.getY(), vector1.getZ() - vector2.getZ());
	}

	public static Vector3f div(Vector3f vector1, Vector3f vector2) {
		return new Vector3f(vector1.getX() / vector2.getX(), vector1.getY() / vector2.getY(), vector1.getZ() / vector2.getZ());
	}

	public static Vector3f div(Vector3f vector1, float x, float y, float z) {
		return new Vector3f(vector1.getX() / x, vector1.getY() / y, vector1.getZ() / z);
	}

	public static Vector3f div(Vector3f vector1, float x) {
		return new Vector3f(vector1.getX() / x, vector1.getY() / x, vector1.getZ() / x);
	}
	
	public static Vector3f mul(Vector3f vector1, Vector3f vector2) {
		return new Vector3f(vector1.getX() * vector2.getX(), vector1.getY() * vector2.getY(), vector1.getZ() * vector2.getZ());
	}

	public static Vector3f mul(Vector3f vector1, float x, float y, float z) {
		return new Vector3f(vector1.getX() * x, vector1.getY() * y, vector1.getZ() * z);
	}

	public static Vector3f mul(Vector3f vector1, float amt) {
		return new Vector3f(vector1.getX() * amt, vector1.getY() * amt, vector1.getZ() * amt);
	}

	public static float length(Vector3f vector) {
		return (float) Math.sqrt(vector.getX() * vector.getX() + vector.getY() * vector.getY() + vector.getZ() * vector.getZ());
	}

	public static float dot(Vector3f vector1, Vector3f vector2) {
		return vector1.getX() * vector2.getX() + vector1.getY() * vector2.getY() + vector1.getZ() * vector2.getZ();
	}

	public static Vector3f inverseNormalize(Vector3f input) {
		return div(input, -length(input));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		result = prime * result + Float.floatToIntBits(z);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vector3f other = (Vector3f) obj;
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x))
			return false;
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y))
			return false;
		if (Float.floatToIntBits(z) != Float.floatToIntBits(other.z))
			return false;
		return true;
	}

	public void printValues() {
		System.out.println(x + " | " + y + " | " + z);
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

	public float[] getElements() {
		return new float[] { x, y, z };
	}

}
