package io.itch.deltabreaker.effect;

import java.util.ArrayList;
import java.util.Random;

import io.itch.deltabreaker.core.SettingsManager;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Matrix4f;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;

public class EffectHeated extends Effect {

	private static final int SPAWN_LIMIT = 16;

	private ArrayList<HeatParticle> snowflakes = new ArrayList<>();

	private int spawnTimer = 0;
	private int spawnRate = 24;

	private ArrayList<Matrix4f> matrices = new ArrayList<>();
	private ArrayList<Vector4f> colors = new ArrayList<>();

	private Vector4f color;

	public boolean die = false;

	public EffectHeated(Vector3f position, Vector4f color) {
		super(Vector3f.add(position, 0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));
		this.color = color;
	}

	@Override
	public void tick() {
		if (SettingsManager.enableFancyWater) {
			if (!die) {
				if (snowflakes.size() < SPAWN_LIMIT) {
					if (spawnTimer < spawnRate) {
						spawnTimer++;
					} else {
						spawnTimer = 0;
						snowflakes.add(new HeatParticle(position, color.copy()));
						snowflakes.add(new HeatParticle(position, color.copy()));
					}
				}
			} else if (allParticlesDone()) {
				remove = true;
			}
			for (int i = 0; i < snowflakes.size(); i++) {
				snowflakes.get(i).tick();
				if (snowflakes.get(i).remove && !die) {
					snowflakes.get(i).reset(position, color.copy());
				}
			}
		}
	}

	@Override
	public void render() {
		if (SettingsManager.enableFancyWater) {
			if (Startup.camera.isInsideView(position, 1, 1f, 1f, 8) && position.getZ() - 16 < Startup.camera.position.getZ() * 2) {
				matrices.clear();
				colors.clear();
				for (HeatParticle s : snowflakes) {
					matrices.add(s.precalc);
					colors.add(s.color);
				}
				BatchSorter.addBatch("zzzzz", "pixel.dae", "pixel.png", "main_3d_bloom", Material.DEFAULT.toString(), matrices, colors, false, false);
			}
		}
	}

	public boolean allParticlesDone() {
		for (HeatParticle p : snowflakes) {
			if (!p.remove) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void cleanUp() {
		for (HeatParticle p : snowflakes) {
			Matrix4f.release(p.precalc);
		}
	}

}

class HeatParticle {

	public boolean remove = false;

	public double loop = 0;
	public double offset = 0;

	public Vector3f position;
	public Vector3f scale;
	public Vector4f color;

	public float alpha = 0.6f;

	public Matrix4f precalc;

	public Random r = new Random();

	public HeatParticle(Vector3f position, Vector4f color) {
		scale = new Vector3f(0.25f, 0.25f, 0.25f).mul(r.nextFloat() + 0.15f);
		this.position = new Vector3f(position.getX(), position.getY() + 8, position.getZ());
		loop = r.nextInt(360);
		offset = r.nextInt(360);
		this.position.add(new Vector3f(AdvMath.sin[(int) (loop % 360)] * 3, 0, AdvMath.sin[(int) ((loop + offset) % 360)] * 3));
		this.color = color;

		precalc = Matrix4f.transform(position, Vector3f.EMPTY, scale);
	}

	public void reset(Vector3f position, Vector4f color) {
		remove = false;
		alpha = 0.6f;

		scale = new Vector3f(0.25f, 0.25f, 0.25f).mul(r.nextFloat() + 0.15f);
		this.position = new Vector3f(position.getX(), position.getY() + 8, position.getZ());
		loop = r.nextInt(360);
		offset = r.nextInt(360);
		this.position.add(new Vector3f(AdvMath.sin[(int) (loop % 360)] * 3, 0, AdvMath.sin[(int) ((loop + offset) % 360)] * 3));
		this.color = color;

		precalc.set(3, position.getX());
		precalc.set(7, position.getY());
		precalc.set(11, position.getZ());

		precalc.set(0, scale.getX());
		precalc.set(5, scale.getY());
		precalc.set(10, scale.getZ());
	}

	public void tick() {
		position.add(0, 0.05f, 0);
		scale.add(0.0025f);
		if (alpha > 0) {
			alpha -= 0.003;
		} else {
			remove = true;
		}
		color.setW(alpha);

		precalc.set(3, position.getX());
		precalc.set(7, position.getY());
		precalc.set(11, position.getZ());

		precalc.set(0, scale.getX());
		precalc.set(5, scale.getY());
		precalc.set(10, scale.getZ());
	}

}
