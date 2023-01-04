package io.itch.deltabreaker.effect;

import java.util.ArrayList;
import java.util.Random;

import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;

public class EffectCraterVent extends Effect {

	private static final int SPAWN_LIMIT = 32;

	private ArrayList<Smoke> snowflakes = new ArrayList<>();

	private int spawnTimer = 0;
	private int spawnRate = 4;

	private ArrayList<Vector3f> positions = new ArrayList<>();
	private ArrayList<Vector3f> rotations = new ArrayList<>();
	private ArrayList<Vector3f> scales = new ArrayList<>();
	private ArrayList<Vector4f> colors = new ArrayList<>();
	
	public EffectCraterVent(Vector3f position) {
		super(Vector3f.add(position, 0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));
	}

	@Override
	public void tick() {
		if (spawnTimer < spawnRate) {
			spawnTimer++;
		} else {
			if (snowflakes.size() < SPAWN_LIMIT) {
				spawnTimer = 0;
				snowflakes.add(new Smoke(position));
				snowflakes.add(new Smoke(position));
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
			positions.clear();
			rotations.clear();
			scales.clear();
			colors.clear();
			for (Smoke s : snowflakes) {
				positions.add(Vector3f.div(s.position, s.scale));
				rotations.add(s.rotation);
				scales.add(s.scale);
				colors.add(s.color);
			}
			BatchSorter.addBatch("zz", "pixel.dae", "pixel.png", "main_3d_bloom", Material.DEFAULT.toString(), positions, rotations, scales, colors, false, false, false);
		}
	}

	@Override
	public void cleanUp() {
		
	}

}

class Smoke {

	public boolean remove = false;

	public int loop = 0;
	public int offset = 0;

	public Vector3f position;
	public Vector3f rotation;
	public Vector3f scale;
	public Vector4f color;

	public float alpha = 0.6f;

	public Smoke(Vector3f position) {
		scale = new Vector3f(0.35f, 0.35f, 0.35f).mul(new Random().nextFloat() + 0.15f);
		rotation = new Vector3f(new Random().nextInt(360), new Random().nextInt(360), new Random().nextInt(360));
		this.position = new Vector3f(position.getX(), position.getY() + 12, position.getZ());
		loop = new Random().nextInt(360);
		offset = new Random().nextInt(360);
		color = new Vector4f(1, 1, 1, 1);
	}

	public void tick() {
		position.add(0, 0.04f, 0);
		rotation.add(new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat());
		scale.add(0.0025f);
		position.add(AdvMath.sin[loop % 360] / 100, 0, AdvMath.sin[(loop + offset) % 360] / 100);
		if (alpha > 0) {
			alpha -= 0.0025;
		} else {
			remove = true;
		}
		color.setW(alpha);
	}

}
