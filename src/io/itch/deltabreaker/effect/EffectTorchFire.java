package io.itch.deltabreaker.effect;

import java.util.ArrayList;
import java.util.Random;

import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.graphics.Light;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.multiprocessing.TaskThread;
import io.itch.deltabreaker.multiprocessing.WorkerTask;
import io.itch.deltabreaker.state.StateManager;

public class EffectTorchFire extends Effect {

	private static final int SPAWN_LIMIT = 16;

	private ArrayList<Spark> snowflakes = new ArrayList<>();

	private int spawnTimer = 0;
	private int spawnRate = 16;

	public Light light;
	public int sin = 0;
	public boolean bounce = false;
	public Vector3f color;

	private WorkerTask task = new WorkerTask() {
		@Override
		public void tick() {
			light.position.set(position.getX(), position.getY() + 14, position.getZ());
			if (sin < 360 && sin > -1) {
				if (bounce) {
					sin += 1;
				} else {
					sin -= 1;
				}
			} else if (sin < 0) {
				sin = 360;
			} else {
				sin = 0;
			}
			bounce = new Random().nextBoolean();
			float x = 1f + (float) Math.sin(Math.toRadians(sin)) / 3;
			light.linear = 1.4f - (float) Math.sin(Math.toRadians(sin)) / 4;
			light.color.set(color.getX() * x, color.getY() * x, color.getZ() * x);
		}
	};

	public EffectTorchFire(Vector3f position, Vector3f color) {
		super(Vector3f.add(position, 0, 2, 0), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));
		light = new Light(position, color.copy(), 1.5f, 1f, 0.075f, null);
		this.color = color;
		StateManager.currentState.lights.add(light);

		TaskThread.process(task);
	}

	@Override
	public void tick() {
		if (spawnTimer < spawnRate) {
			spawnTimer++;
		} else {
			if (snowflakes.size() < SPAWN_LIMIT) {
				spawnTimer = 0;
				snowflakes.add(new Spark(Vector3f.add(position, 0, 6, 0), new Vector4f(2f, 0.75f, 0f, 0.75f)));
				snowflakes.add(new Spark(Vector3f.add(position, 0, 6, 0), new Vector4f(2f, 0.75f, 0f, 0.75f)));
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
		for (int i = 0; i < snowflakes.size(); i++) {
			Spark s = snowflakes.get(i);
			if (s.position.getX() > (int) StateManager.currentState.camX - 208 && s.position.getX() < (int) StateManager.currentState.camX + 192 && s.position.getZ() < StateManager.currentState.camY + 16
					&& s.position.getZ() > StateManager.currentState.camY - 160) {
				BatchSorter.add("zzz", "pixel.dae", "pixel.png", "main_3d_bloom", Material.GLOW.toString(), Vector3f.div(s.position, s.scale), s.rotation, s.scale, s.color, false, false);
			}
		}
	}

	@Override
	public void cleanUp() {
		StateManager.currentState.lights.remove(light);
		task.finish();
	}

}