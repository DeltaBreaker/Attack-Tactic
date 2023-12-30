package io.itch.deltabreaker.object;

import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;

public class Cursor {

	public Vector3f position;
	public Vector3f targetPosition;
	public Vector3f rotation;
	public Vector3f targetRotation;
	public int frame = 0;
	public int frames = 3;
	public int frameTime = 20;
	public int frameTimer = 0;
	public boolean reverse = false;
	public int waitTime = 80;
	public int waitTimer = 0;
	public boolean wait = false;
	private int bob = 0;
	public boolean staticView = false;
	public float speed = 1f;

	public Cursor(Vector3f position) {
		this.position = position;
		targetPosition = position.copy();
		rotation = new Vector3f(0, -60, -90);
	}

	public void tick() {
		if (!staticView) {
			rotation.set(0, -60, -90);
		} else {
			rotation.set(0, 180, 0);
		}
		if (bob < 360) {
			bob++;
		} else {
			bob = 0;
		}
		
		float xSpeed = Math.abs(position.getX() - targetPosition.getX()) / 16.0f;
		float ySpeed = Math.abs(position.getY() - targetPosition.getY()) / 16.0f;
		float zSpeed = Math.abs(position.getZ() - targetPosition.getZ()) / 16.0f;
		if (position.getX() < targetPosition.getX()) {
			position = new Vector3f(Math.min(position.getX() + xSpeed, targetPosition.getX()), position.getY(), position.getZ());
		}
		if (position.getX() > targetPosition.getX()) {
			position = new Vector3f(Math.max(position.getX() - xSpeed, targetPosition.getX()), position.getY(), position.getZ());
		}
		if (position.getY() < targetPosition.getY()) {
			position.setY((float) Math.min(position.getY() + ySpeed, targetPosition.getY()));
		}
		if (position.getY() > targetPosition.getY()) {
			position = new Vector3f(position.getX(), Math.max(position.getY() - ySpeed, targetPosition.getY()), position.getZ());
		}
		if (position.getZ() < targetPosition.getZ()) {
			position = new Vector3f(position.getX(), position.getY(), Math.min(position.getZ() + zSpeed, targetPosition.getZ()));
		}
		if (position.getZ() > targetPosition.getZ()) {
			position = new Vector3f(position.getX(), position.getY(), Math.max(position.getZ() - zSpeed, targetPosition.getZ()));
		}
		if (wait) {
			if (waitTimer < waitTime) {
				waitTimer++;
			} else {
				waitTimer = 0;
				wait = false;
			}
		} else {
			if (frameTimer < frameTime) {
				frameTimer++;
			} else {
				frameTimer = 0;
				if (reverse) {
					frame--;
				} else {
					frame++;
				}
				if (frame == frames - 1 || frame == 0) {
					reverse = !reverse;
					if (frame == frames - 1) {
						wait = true;
					}
				}
			}
		}
	}

	public void render() {
		BatchSorter.add("cursor_" + frame + ".dae", "cursor_" + frame + ".png", (staticView) ? "static_3d" : "main_3d_nobloom_texcolor", Material.DEFAULT.toString(),
				Vector3f.add(position, (staticView) ? (float) (Math.sin(Math.toRadians(bob)) * 1.5) : 0, (staticView) ? 0 : (float) (Math.sin(Math.toRadians(bob)) * 1.5), 0), rotation, Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, false,
				staticView);
	}

	public void setLocation(Vector3f position) {
		targetPosition = position.copy();
	}

	public void warpLocation(Vector3f position) {
		this.position = position.copy();
		targetPosition = position.copy();
	}

	public void warpLocation(float x, float y, float z) {
		position.set(x, y, z);
		targetPosition.set(x, y, z);
	}
	
}