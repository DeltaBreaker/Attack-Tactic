package io.itch.deltabreaker.effect.dungeon;

import java.util.ArrayList;
import java.util.Random;

import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.effect.Effect;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.multiprocessing.TaskThread;
import io.itch.deltabreaker.multiprocessing.WorkerTask;
import io.itch.deltabreaker.state.StateManager;

public class EffectDungeonResidue extends Effect {

	public ArrayList<Pixel> movingResidue = new ArrayList<>();
	public ArrayList<Pixel> staticResidue = new ArrayList<>();

	public int staticLimit = 75;
	public int movingLimit = 100;
	public int spawnTimer = 0;
	public int spawnTime = 12;

	private ArrayList<Vector3f> positions = new ArrayList<>();
	private ArrayList<Vector3f> rotations = new ArrayList<>();
	private ArrayList<Vector3f> scales = new ArrayList<>();
	private ArrayList<Vector4f> colors = new ArrayList<>();

	private WorkerTask task = new WorkerTask() {
		@Override
		public void tick() {
			for (Pixel p : staticResidue) {
				p.staticTick();
			}
		}
	};

	public EffectDungeonResidue() {
		super(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(0, 0, 0));

		TaskThread.process(task);

		for (int i = 0; i < staticLimit; i++) {
			int scale = 8 + new Random().nextInt(4);
			staticResidue.add(new Pixel(
					new Vector3f(new Random().nextInt((StateManager.currentState.tiles.length + 24) * 8) / (float) scale - 12, -32f / scale, (new Random().nextInt(StateManager.currentState.tiles.length + 24) * 8 - 64) / (float) scale - 12),
					new Vector3f(new Random().nextInt(1), new Random().nextInt(1), new Random().nextInt(1)), new Vector3f(scale, scale, scale), 1));
		}
		if (!AudioManager.getSound("medial_plane.ogg").isPlaying()) {
			AudioManager.getSound("medial_plane.ogg").play(0, true);
		}
		AudioManager.getSound("medial_plane.ogg").fade(AudioManager.defaultSubSFXGain, 144, true);
	}

	@Override
	public void tick() {
		//Startup.screenColor.set(0, 0, 0, 1)
		if (movingResidue.size() < movingLimit) {
			if (spawnTimer < spawnTime) {
				spawnTimer++;
			} else {
				spawnTimer = 0;
				int scale = 2 + new Random().nextInt(4);
				movingResidue.add(new Pixel(new Vector3f(-64, -16, new Random().nextInt(StateManager.currentState.tiles.length * 16) - 64), new Vector3f(new Random().nextInt(360), new Random().nextInt(360), new Random().nextInt(360)),
						new Vector3f(scale, scale, scale), 0));
			}
		}
		for (int i = 0; i < movingResidue.size(); i++) {
			movingResidue.get(i).tick();
			if (movingResidue.get(i).remove) {
				movingResidue.remove(i);
			}
		}
	}

	public void desyncedTick() {
		for (Pixel p : staticResidue) {
			p.staticTick();
		}
	}

	@Override
	public void render() {
		positions.clear();
		rotations.clear();
		scales.clear();
		colors.clear();

		for (Pixel p : staticResidue) {
			positions.add(p.position);
			rotations.add(p.rotation);
			scales.add(p.scale);
			colors.add(p.color);
		}

		for (Pixel p : movingResidue) {
			positions.add(Vector3f.div(p.position, p.scale));
			rotations.add(p.rotation);
			scales.add(p.scale);
			colors.add(p.color);
		}

		BatchSorter.addBatch("pixel.dae", "pixel.png", "main_3d_medial", Material.DEFAULT.toString(), positions, rotations, scales, colors, false, false);
	}

	@Override
	public void cleanUp() {
		task.finish();
		AudioManager.getSound("medial_plane.ogg").fade(0, 144, true);
	}

}

class Pixel {

	public Vector3f position;
	public Vector3f floatPosition;
	public Vector3f rotation;
	public Vector3f rotationSpeed = new Vector3f(new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat());
	public Vector3f scale;
	public Vector4f color;

	public float alpha;
	public float fade = 0.0075f;
	public float speed = 0.25f;

	public float sin = new Random().nextInt(360);

	public boolean remove = false;

	public Pixel(Vector3f position, Vector3f rotation, Vector3f scale, float alpha) {
		this.position = position;
		this.rotation = rotation;
		this.scale = scale;
		this.alpha = alpha;
		color = new Vector4f(1, 1, 1, alpha);
		floatPosition = position;
	}

	public void tick() {
		position.setX(position.getX() + speed);
		rotation.add(rotationSpeed);
		if (position.getX() < StateManager.currentState.tiles.length * 8) {
			if (alpha < 1) {
				alpha += fade;
			}
		} else {
			if (alpha > 0) {
				alpha -= fade;
			} else {
				remove = true;
			}
		}
		color.setW(alpha);
	}

	public void staticTick() {
		if (sin < 359) {
			sin += 0.5f;
		} else {
			sin = 0;
		}
		position.setY(floatPosition.getY() + AdvMath.sin[(int) sin] / scale.getY() / 20);
	}

}