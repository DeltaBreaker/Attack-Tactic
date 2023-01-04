package io.itch.deltabreaker.graphics;

import io.itch.deltabreaker.math.Matrix4f;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;

public class Camera {

	private static final int FRUST_PLANES = 6;

	public float speedX = 1f;
	public float speedY = 1f;
	public float speedZ = 1f;
	public float rotateSpeed = 1f;

	public float fov = 70.0f;
	private float distance = 0.05f;
	public float range = 200.0f;

	public Vector3f position;
	public Vector3f rotation;

	public Matrix4f projection;
	public Matrix4f projectionView;
	private Vector4f[] frustumPlanes = new Vector4f[FRUST_PLANES];

	public Vector3f targetPosition;
	public Vector3f targetRotation;

	public Camera(Vector3f position, Vector3f rotation, int width, int height, float speedX, float speedY, float speedZ) {
		this.position = position;
		this.rotation = rotation;
		projection = Matrix4f.projection(fov, (float) width / (float) height, distance, range);
		targetPosition = new Vector3f(position.getX(), position.getY(), position.getZ());
		targetRotation = new Vector3f(rotation.getX(), rotation.getY(), rotation.getZ());
		this.speedX = speedX;
		this.speedY = speedY;
		this.speedZ = speedZ;

		for (int i = 0; i < frustumPlanes.length; i++) {
			frustumPlanes[i] = new Vector4f(0, 0, 0, 0);
		}
	}

	public void tick() {
		position = moveToTarget(position, targetPosition, speedX, speedY, speedZ);
		rotation = moveToTarget(rotation, targetRotation, rotateSpeed, rotateSpeed, rotateSpeed);
		updatePlanes();
	}

	private Vector3f moveToTarget(Vector3f position, Vector3f target, float speedX, float speedY, float speedZ) {
		if (position.getX() < target.getX()) {
			position.set(Math.min(position.getX() + speedX, target.getX()), position.getY(), position.getZ());
		}
		if (position.getY() < target.getY()) {
			position.set(position.getX(), Math.min(position.getY() + speedY, target.getY()), position.getZ());
		}
		if (position.getZ() < target.getZ()) {
			position.set(position.getX(), position.getY(), Math.min(position.getZ() + speedZ, target.getZ()));
		}
		if (position.getX() > target.getX()) {
			position.set(Math.max(position.getX() - speedX, target.getX()), position.getY(), position.getZ());
		}
		if (position.getY() > target.getY()) {
			position.set(position.getX(), Math.max(position.getY() - speedY, target.getY()), position.getZ());
		}
		if (position.getZ() > target.getZ()) {
			position.set(position.getX(), position.getY(), Math.max(position.getZ() - speedZ, target.getZ()));
		}
		return position;
	}

	public void updateProjection(int width, int height) {
		projection = Matrix4f.projection(fov, (float) width / (float) height, distance, range);
	}

	public void updatePlanes() {
		projectionView = Matrix4f.multiply(getView(), projection);
		frustumPlanes[0].set(projectionView.get(0, 3) + projectionView.get(0, 0), projectionView.get(1, 3) + projectionView.get(1, 0), projectionView.get(2, 3) + projectionView.get(2, 0),
				projectionView.get(3, 3) + projectionView.get(3, 0));
		frustumPlanes[1].set(projectionView.get(0, 3) - projectionView.get(0, 0), projectionView.get(1, 3) - projectionView.get(1, 0), projectionView.get(2, 3) - projectionView.get(2, 0),
				projectionView.get(3, 3) - projectionView.get(3, 0));
		frustumPlanes[2].set(projectionView.get(0, 3) + projectionView.get(0, 1), projectionView.get(1, 3) + projectionView.get(1, 1), projectionView.get(2, 3) + projectionView.get(2, 1),
				projectionView.get(3, 3) + projectionView.get(3, 1));
		frustumPlanes[3].set(projectionView.get(0, 3) - projectionView.get(0, 1), projectionView.get(1, 3) - projectionView.get(1, 1), projectionView.get(2, 3) - projectionView.get(2, 1),
				projectionView.get(3, 3) - projectionView.get(3, 1));
		frustumPlanes[4].set(projectionView.get(0, 3) + projectionView.get(0, 2), projectionView.get(1, 3) + projectionView.get(1, 2), projectionView.get(2, 3) + projectionView.get(2, 2),
				projectionView.get(3, 3) + projectionView.get(3, 2));
		frustumPlanes[5].set(projectionView.get(0, 3) - projectionView.get(0, 2), projectionView.get(1, 3) - projectionView.get(1, 2), projectionView.get(2, 3) - projectionView.get(2, 2),
				projectionView.get(3, 3) - projectionView.get(3, 2));
	}

	public void setTargetPosition(Vector3f targetPosition) {
		this.targetPosition = targetPosition;
	}

	public void setPosition(Vector3f position) {
		this.targetPosition = position.copy();
		this.position = position;
	}

	public void setPosition(float x, float y, float z) {
		targetPosition.set(x, y, z);
		position.set(x, y, z);
	}

	public void setTargetPosition(float f, float g, float h) {
		targetPosition.set(f, g, h);
	}

	public Vector3f getTargetPosition() {
		return targetPosition;
	}

	public void setRotation(Vector3f rotation) {
		this.rotation = rotation;
		targetRotation = rotation.copy();
	}

	public void setTargetRotation(Vector3f targetRotation) {
		this.targetRotation = targetRotation;
	}

	public Vector3f getRotation() {
		return rotation;
	}

	public Vector3f getTargetRotation() {
		return targetRotation;
	}

	public void addPosition(Vector3f position) {
		this.position.add(position);
	}

	public Matrix4f getProjection() {
		return projection;
	}

	public Matrix4f getView() {
		return Matrix4f.view(position, rotation);
	}

	public boolean isInsideView(Vector3f position, float bias) {
		for (int i = 0; i < FRUST_PLANES; i++) {
			Vector4f plane = frustumPlanes[i];
			if (plane.getX() * position.getX() + plane.getY() * position.getY() + plane.getZ() * position.getZ() + plane.getW() <= -bias) {
				return false;
			}
		}
		return true;
	}

	public boolean isInsideView(Vector3f position, float mx, float my, float mz, float bias) {
		for (int i = 0; i < FRUST_PLANES; i++) {
			Vector4f plane = frustumPlanes[i];
			if (plane.getX() * (position.getX() * mx) + plane.getY() * (position.getY() * my) + plane.getZ() * (position.getZ() * mz) + plane.getW() <= -bias) {
				return false;
			}
		}
		return true;
	}

}
