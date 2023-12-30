package io.itch.deltabreaker.effect;

import java.util.Random;

import io.itch.deltabreaker.math.Vector3f;

public abstract class Effect {

	public boolean remove = false;
	public Vector3f position, rotation, scale;
	
	public long priority = new Random().nextLong();
	
	public boolean die = false;
	
	public Effect(Vector3f position, Vector3f rotation, Vector3f scale) {
		this.position = position;
		this.rotation = rotation;
		this.scale = scale;
	}
	
	public abstract void tick();
	
	public abstract void render();
	
	public abstract void cleanUp();
	
}
