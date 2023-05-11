package io.itch.deltabreaker.effect;

import java.util.ArrayList;
import java.util.Random;

import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;

public class EffectFountain extends Effect {

	private int particleCount = 100;
	private int spawnTimer = 0;
	private int spawnTime = 0;

	private int startDistance = 1;
	private float speed = 0.075f;
	private float fade = 0.0095f;

	private ArrayList<WaterParticle> water = new ArrayList<>();

	private ArrayList<Vector3f> positions = new ArrayList<>();
	private ArrayList<Vector3f> rotations = new ArrayList<>();
	private ArrayList<Vector3f> scales = new ArrayList<>();
	private ArrayList<Vector4f> colors = new ArrayList<>();

	public EffectFountain(Vector3f position) {
		super(Vector3f.add(position, 0, 16, 0), Vector3f.EMPTY, Vector3f.SCALE_HALF);
	}

	@Override
	public void tick() {
		if (spawnTimer < spawnTime && water.size() < particleCount) {
			spawnTimer++;
		} else {
			spawnTimer = 0;
			water.add(new WaterParticle(position, startDistance, new Random().nextInt(360), speed, fade, Vector4f.mul(new Vector4f(0.427f, 0.765f, 1.05f, 0.75f), 1.75f).setW(0.8f), 5));
		}

		for (int i = 0; i < water.size(); i++) {
			water.get(i).tick();
			if (water.get(i).remove) {
				water.remove(i);
				i--;
			}
		}
	}

	@Override
	public void render() {
		if (Startup.camera.isInsideView(position, 0.5f, 0.5f, 0.5f, 8)) {
			positions.clear();
			rotations.clear();
			scales.clear();
			colors.clear();
			for (WaterParticle s : water) {
				positions.add(s.position);
				rotations.add(s.rotation);
				scales.add(s.scale);
				colors.add(s.color);
			}
			BatchSorter.addBatch("z", "pixel.dae", "pixel.png", "main_3d_nobloom", Material.DEFAULT.toString(), positions, rotations, scales, colors, true, false);
		}
	}

	@Override
	public void cleanUp() {

	}

}