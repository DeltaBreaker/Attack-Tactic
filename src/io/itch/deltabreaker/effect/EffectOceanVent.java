package io.itch.deltabreaker.effect;

import java.util.ArrayList;
import java.util.Random;

import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;

public class EffectOceanVent extends Effect {

	private static final int SPAWN_LIMIT = 16;

	private ArrayList<Bubble> snowflakes = new ArrayList<>();

	private int spawnTimer = 0;
	private int spawnRate = 32;
	
	public EffectOceanVent(Vector3f position) {
		super(Vector3f.add(position, 0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));
	}

	@Override
	public void tick() {
		if (spawnTimer < spawnRate) {
			spawnTimer++;
		} else {
			if (snowflakes.size() < SPAWN_LIMIT) {
				spawnTimer = 0;
				snowflakes.add(new Bubble(Vector3f.add(position, 0, 0, 0)));
				snowflakes.add(new Bubble(Vector3f.add(position, 0, 0, 0)));
			}
		}
		for (int i = 0; i < snowflakes.size(); i++) {
			snowflakes.get(i).tick();
			if (snowflakes.get(i).remove) {
				snowflakes.remove(i);
				i--;
			}
		}
	}

	@Override
	public void render() {
		if (position.getZ() - 16 < Startup.camera.position.getZ() * 2 && Vector3f.distance(position, Startup.camera.position) < 300) {
			for (int i = 0; i < snowflakes.size(); i++) {
				snowflakes.get(i).render();
			}
		}
	}

	@Override
	public void cleanUp() {
		
	}

}

class Bubble {

	public boolean remove = false;

	public double loop = 0;
	public double offset = 0;

	public Vector3f position;
	public Vector3f rotation;
	public Vector3f scale;
	public Vector4f color;
	
	public float alpha = 0.6f;

	public Bubble(Vector3f position) {
		scale = new Vector3f(0.25f, 0.25f, 0.25f).mul(new Random().nextFloat() + 0.15f);
		rotation = new Vector3f(new Random().nextInt(360), new Random().nextInt(360), new Random().nextInt(360));
		this.position = new Vector3f(position.getX(), position.getY() + 8, position.getZ());
		loop = new Random().nextInt(360);
		offset = new Random().nextInt(360);
		this.position.add(new Vector3f((float) Math.sin(Math.toRadians(loop)) * 3, 0, (float) Math.sin(Math.toRadians(loop + offset)) * 3));
		color = new Vector4f(1.427f, 1.765f, 1.9f, 1);
	}

	public void tick() {
		position.add(0, 0.05f, 0);
		rotation.add(new Vector3f(new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat()));
		scale.add(0.0025f);
		if (alpha > 0) {
			alpha -= 0.0025;
		} else {
			remove = true;
		}
		color.setW(alpha);
	}

	public void render() {
		BatchSorter.add("pixel.dae", "pixel.png", "main_3d_bloom", Material.DEFAULT.toString(), Vector3f.div(position, scale), rotation, scale, color, false, false, false);
	}
	
}