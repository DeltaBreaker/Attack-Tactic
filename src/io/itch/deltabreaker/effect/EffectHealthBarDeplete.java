package io.itch.deltabreaker.effect;

import java.util.ArrayList;
import java.util.Random;

import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;

public class EffectHealthBarDeplete extends Effect {

	private ArrayList<HealthBarParticle> particles = new ArrayList<>();
	private int particleLimit = 10;
	
	public EffectHealthBarDeplete(Vector3f position, Vector4f color) {
		super(position, Vector3f.EMPTY, Vector3f.SCALE_HALF);
		for(int i = 0; i < particleLimit; i++) {
			particles.add(new HealthBarParticle(position, color));
		}
	}
	
	@Override
	public void tick() {
		for(int i = 0; i < particles.size(); i++) {
			particles.get(i).tick();
			if(particles.get(i).remove) {
				particles.remove(i);
				i--;
			}
		}
		if(particles.size() <= 0) {
			remove = true;
		}
	}

	@Override
	public void render() {
		for(HealthBarParticle p : particles) {
			BatchSorter.add("pixel.dae", "pixel.png", "static_3d", Material.DEFAULT.toString(), p.position, p.rotation, Vector3f.SCALE_HALF, p.color, false, true);
		}
	}

	@Override
	public void cleanUp() {
		
	}

}

class HealthBarParticle {
	
	public Vector3f position;
	public float dir = new Random().nextFloat() * 0.2f;
	public Vector3f rotation;
	public Vector4f color;
	public boolean remove = false;
	
	public HealthBarParticle(Vector3f position, Vector4f color) {
		this.position = position.copy();
		this.color = color.copy();
		rotation = new Vector3f(new Random().nextInt(360), new Random().nextInt(360), new Random().nextInt(360));
		if(new Random().nextBoolean()) {
			dir *= -1;
		}
	}
	
	public void tick() {
		position.add(0.1f, dir, 0);
		color.add(0, 0, 0, -0.025f);
		if(color.getW() <= 0) {
			remove = true;
		}
	}
	
}