package io.itch.deltabreaker.graphics;

import io.itch.deltabreaker.math.Matrix4f;
import io.itch.deltabreaker.math.Vector3f;

public class Light {

	public Vector3f position;
	public Vector3f color;

	public float constant;
	public float linear;
	public float quadratic;

	public Vector3f direction;
	private Matrix4f mat4 = Matrix4f.identity();

	public Light(Vector3f position, Vector3f color, float constant, float linear, float quadratic, Vector3f direction) {
		this.position = position;
		this.color = color;
		this.constant = constant;
		this.linear = linear;
		this.quadratic = quadratic;
		this.direction = (direction == null) ? Vector3f.EMPTY.copy() : direction;
	}

	public void getMat4() {
		mat4.set(0, 0, constant);
	}

}
