package io.itch.deltabreaker.math;

import java.util.ArrayList;
import java.util.Arrays;

public class Matrix4f {

	public static final float[] IDENTITY = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };

	public static final int SIZE = 4;
	private float[] elements = new float[SIZE * SIZE];

	private static ArrayList<Matrix4f> matrixReserve = new ArrayList<>();

	public static int created = 0;
	public static int reused = 0;
	
	public Matrix4f(float[] elements) {
		if (elements != null) {
			this.elements = elements;
		}
	}

	public static Matrix4f identity() {
		Matrix4f result;
		if (matrixReserve.size() == 0) {
			result = new Matrix4f(null);
//			System.out.println("new " + System.currentTimeMillis());
			created++;
			
//			if(created > 5000) {
//				new Exception().printStackTrace();
//			}

		} else {
			result = matrixReserve.get(matrixReserve.size() - 1);
			matrixReserve.remove(matrixReserve.size() - 1);
			Arrays.fill(result.elements, 0);
//			System.out.println("reuse " + System.currentTimeMillis());
			reused++;
		}

		result.set(0, 0, 1);
		result.set(1, 1, 1);
		result.set(2, 2, 1);
		result.set(3, 3, 1);

		return result;
	}

	public static void release(Matrix4f value) {
		if (value != null) {
			matrixReserve.add(value);
		}
	}

	public float[] fliped() {
		float[] reverse = new float[elements.length];
		for (int i = 0; i < elements.length; i++) {
			reverse[i] = elements[elements.length - i - 1];
		}
		return elements;
	}

	public static float[] identityArray() {
		float[] result = new float[16];

		result[0] = 1;
		result[5] = 1;
		result[10] = 1;
		result[15] = 1;

		return result;
	}

	public static Matrix4f translate(Vector3f translate) {
		Matrix4f result = Matrix4f.identity();

		result.set(3, 0, translate.getX());
		result.set(3, 1, translate.getY());
		result.set(3, 2, translate.getZ());

		return result;
	}

	public static float[] translateArray(Vector3f translate) {
		float[] result = identityArray();

		result[3] = translate.getX();
		result[7] = translate.getY();
		result[11] = translate.getZ();

		return result;
	}

	public static Matrix4f rotateX(float angle) {
		Matrix4f result = Matrix4f.identity();

		float cos = (float) Math.cos(Math.toRadians(angle));
		float sin = (float) Math.sin(Math.toRadians(angle));

		result.set(0, 0, cos + (1 - cos));
		result.set(1, 1, cos);
		result.set(1, 2, -sin);
		result.set(2, 1, sin);
		result.set(2, 2, cos);

		return result;
	}

	public static Matrix4f rotateY(float angle) {
		Matrix4f result = Matrix4f.identity();

		float cos = (float) Math.cos(Math.toRadians(angle));
		float sin = (float) Math.sin(Math.toRadians(angle));

		result.set(0, 0, cos);
		result.set(0, 2, sin);
		result.set(1, 1, cos + (1 - cos));
		result.set(2, 0, -sin);
		result.set(2, 2, cos);

		return result;
	}

	public static Matrix4f rotateZ(float angle) {
		Matrix4f result = Matrix4f.identity();

		float cos = (float) Math.cos(Math.toRadians(angle));
		float sin = (float) Math.sin(Math.toRadians(angle));

		result.set(0, 0, cos);
		result.set(0, 1, -sin);
		result.set(1, 0, sin);
		result.set(1, 1, cos);
		result.set(2, 2, cos + 1 - cos);

		return result;
	}

	public static float[] rotateArrayX(float angle) {
		float[] result = identityArray();

		float cos = (float) Math.cos(Math.toRadians(angle));
		float sin = (float) Math.sin(Math.toRadians(angle));

		result[0] = cos + (1 - cos);
		result[5] = cos;
		result[9] = -sin;
		result[6] = sin;
		result[10] = cos;

		return result;
	}

	public static float[] rotateArrayY(float angle) {
		float[] result = identityArray();

		float cos = (float) Math.cos(Math.toRadians(angle));
		float sin = (float) Math.sin(Math.toRadians(angle));

		result[0] = cos;
		result[8] = sin;
		result[5] = cos + (1 - cos);
		result[2] = -sin;
		result[10] = cos;

		return result;
	}

	public static float[] rotateArrayZ(float angle) {
		float[] result = identityArray();

		float cos = (float) Math.cos(Math.toRadians(angle));
		float sin = (float) Math.sin(Math.toRadians(angle));

		result[0] = cos;
		result[4] = -sin;
		result[1] = sin;
		result[5] = cos;
		result[10] = cos + 1 - cos;

		return result;
	}

	public static Matrix4f scale(Vector3f scalar) {
		Matrix4f result = Matrix4f.identity();

		result.set(0, 0, scalar.getX());
		result.set(1, 1, scalar.getY());
		result.set(2, 2, scalar.getZ());

		return result;
	}

	public static float[] scaleArray(Vector3f scalar) {
		float[] result = identityArray();

		result[0] = scalar.getX();
		result[5] = scalar.getY();
		result[10] = scalar.getZ();

		return result;
	}

	public static Matrix4f transform(Vector3f position, Vector3f rotation, Vector3f scale) {
		Matrix4f translationMatrix = Matrix4f.translate(position);
		Matrix4f scaleMatrix = Matrix4f.scale(scale);

		if (rotation.total() == 0) {
			Matrix4f result = multiply(translationMatrix, scaleMatrix);
			release(translationMatrix);
			release(scaleMatrix);
			return result;
		} else {
			Matrix4f rotY = rotateY(rotation.getY());
			Matrix4f rotZ = rotateZ(rotation.getZ());
			Matrix4f rotYZ = multiply(rotY, rotZ);
			release(rotY);
			release(rotZ);

			Matrix4f rotX = rotateX(rotation.getX());
			Matrix4f rotationMatrix = multiply(rotX, rotYZ);
			release(rotX);
			release(rotYZ);

			Matrix4f transScale = multiply(translationMatrix, scaleMatrix);
			Matrix4f result = multiply(rotationMatrix, transScale);
			release(transScale);
			release(translationMatrix);
			release(scaleMatrix);
			release(rotationMatrix);
			return result;
		}
	}

	public static float[] transformArray(Vector3f position, Vector3f rotation, Vector3f scale) {
		float[] translationMatrix = translateArray(position);
		float[] scaleMatrix = scaleArray(scale);

		if (rotation.total() == 0) {
			return multiplyArray(translationMatrix, scaleMatrix);
		} else {
			float[] rotationMatrix = multiplyArray(rotateArrayX(rotation.getX()), multiplyArray(rotateArrayY(rotation.getY()), rotateArrayZ(rotation.getZ())));
			return multiplyArray(rotationMatrix, multiplyArray(translationMatrix, scaleMatrix));
		}
	}

	public static Matrix4f projection(float fov, float aspect, float near, float far) {
		Matrix4f result = Matrix4f.identity();

		float tanFOV = (float) Math.tan(Math.toRadians(fov / 2));
		float range = far - near;

		result.set(0, 0, 1.0f / (aspect * tanFOV));
		result.set(1, 1, 1.0f / tanFOV);
		result.set(2, 2, -((far + near) / range));
		result.set(2, 3, -1.0f);
		result.set(3, 2, -((2 * far * near) / range));
		result.set(3, 3, 0.0f);

		return result;
	}

	public static Matrix4f view(Vector3f position, Vector3f rotation) {
		Vector3f negative = new Vector3f(-position.getX(), -position.getY(), -position.getZ());
		Matrix4f translationMatrix = Matrix4f.translate(negative);

		if (rotation.total() == 0) {
			return translationMatrix;
		} else {
			Matrix4f rotX = rotateX(rotation.getX());
			Matrix4f rotY = rotateY(rotation.getY());
			Matrix4f rotYX = multiply(rotY, rotX);
			release(rotX);
			release(rotY);

			Matrix4f rotZ = rotateZ(rotation.getZ());
			Matrix4f rotationMatrix = multiply(rotZ, rotYX);
			release(rotZ);
			release(rotYX);

			Matrix4f result = multiply(translationMatrix, rotationMatrix);
			release(translationMatrix);
			release(rotationMatrix);
			return result;
		}

	}

	public static Matrix4f multiply(Matrix4f matrix, Matrix4f other) {
		Matrix4f result = Matrix4f.identity();

		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				result.set(i, j, matrix.get(i, 0) * other.get(0, j) + matrix.get(i, 1) * other.get(1, j) + matrix.get(i, 2) * other.get(2, j) + matrix.get(i, 3) * other.get(3, j));
			}
		}

		return result;
	}

	public static float[] multiplyArray(float[] matrix, float[] other) {
		float[] result = new float[16];

		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				result[j * 4 + i] = matrix[i] * other[j * 4] + matrix[i + 4] * other[1 + j * 4] + matrix[i + 8] * other[2 + j * 4] + matrix[i + 12] * other[3 + j * 4];
			}
		}

		return result;
	}

	public void print() {
		for (int y = 0; y < 4; y++) {
			System.out.println(elements[y * 4] + " " + elements[y * 4 + 1] + " " + elements[y * 4 + 2] + " " + elements[y * 4 + 3]);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(elements);
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
		Matrix4f other = (Matrix4f) obj;
		if (!Arrays.equals(elements, other.elements))
			return false;
		return true;
	}

	public float get(int x, int y) {
		return elements[y * SIZE + x];
	}

	public void set(int x, int y, float value) {
		elements[y * SIZE + x] = value;
	}

	public void set(int location, float value) {
		elements[location] = value;
	}

	public float[] getAll() {
		return elements;
	}

}
