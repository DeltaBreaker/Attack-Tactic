package io.itch.deltabreaker.effect;

import java.util.ArrayList;
import java.util.Random;

import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.multiprocessing.TaskThread;
import io.itch.deltabreaker.multiprocessing.WorkerTask;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.state.StateManager;

public class EffectBurst extends Effect {

	private Unit u;

	public ArrayList<BurstParticle> particles = new ArrayList<>();

	public int burstTimer = 0;
	public int burstTime = 90;
	public int rebuildTimer = 0;
	public int rebuildTime = 90;
	public int buildParticles = 32;

	private ArrayList<Vector3f> positions = new ArrayList<>();
	private ArrayList<Vector3f> rotations = new ArrayList<>();
	private ArrayList<Vector3f> scales = new ArrayList<>();
	private ArrayList<Vector4f> colors = new ArrayList<>();

	private WorkerTask task = new WorkerTask() {

		@Override
		public void tick() {
			for (BurstParticle p : particles) {
				p.rotation.add(p.rotationVector);
			}
		}

	};

	public EffectBurst(Vector3f location, Unit u) {
		super(location, Vector3f.EMPTY, Vector3f.SCALE_HALF);
		this.u = u;

		for (int i = 0; i < buildParticles; i++) {
			particles.add(new BurstParticle(location.copy(), new Vector3f((new Random().nextFloat() - 0.5f) * 64, new Random().nextFloat() * 32, (new Random().nextFloat() - 0.5f) * 64)));
		}

		TaskThread.process(task);
	}

	@Override
	public void tick() {
		u.unitColor.setW(0);
		if (burstTimer < burstTime) {
			burstTimer++;
			for (BurstParticle p : particles) {
				p.position = Vector3f.add(p.origin, Vector3f.mul(p.vector, AdvMath.sin[burstTimer]));
			}
		} else {
			if (rebuildTimer == 0) {
				for (BurstParticle p : particles) {
					p.origin = p.position.copy();
					p.vector = Vector3f.sub(new Vector3f(u.x, 13 + u.height, u.y), p.position);
				}
			}
			if (rebuildTimer < rebuildTime) {
				rebuildTimer++;
				for (BurstParticle p : particles) {
					p.position = Vector3f.add(p.origin, Vector3f.mul(p.vector, 1 - AdvMath.sin[90 + rebuildTimer]));
				}
			} else {
				StateManager.currentState.effects.add(new EffectPoof(new Vector3f(u.x, 13 + u.height, u.y + 2)));
				u.unitColor.setW(1);
				remove = true;
			}
		}
	}

	@Override
	public void render() {
		positions.clear();
		rotations.clear();
		scales.clear();
		colors.clear();
		for (BurstParticle p : particles) {
			positions.add(p.position);
			rotations.add(p.rotation);
			scales.add(p.scale);
			colors.add(p.color);
		}
		BatchSorter.addBatch("pixel.dae", "pixel.png", "main_3d_bloom", Material.DEFAULT.toString(), positions, rotations, scales, colors, true, false, false);
	}

	@Override
	public void cleanUp() {
		task.finish();
	}

}

class BurstParticle {

	public Vector3f origin;
	public Vector3f position;
	public Vector3f vector;

	public Vector3f rotation = Vector3f.EMPTY.copy();
	public Vector3f rotationVector = new Vector3f(new Random().nextFloat() * 4, new Random().nextFloat() * 4, new Random().nextFloat() * 4);
	public Vector3f scale = Vector3f.SCALE_HALF;
	public Vector4f color = new Vector4f(1f, 1f, 1f, 1);

	public BurstParticle(Vector3f position, Vector3f vector) {
		origin = position.copy();
		this.position = position;
		this.vector = vector;
	}

}