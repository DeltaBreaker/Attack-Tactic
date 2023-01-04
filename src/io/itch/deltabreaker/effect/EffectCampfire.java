package io.itch.deltabreaker.effect;

import java.util.ArrayList;
import java.util.Random;

import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.graphics.Light;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.multiprocessing.TaskThread;
import io.itch.deltabreaker.multiprocessing.WorkerTask;
import io.itch.deltabreaker.state.StateManager;

public class EffectCampfire extends Effect {

	private static final int SPAWN_LIMIT = 16;

	private ArrayList<Spark> snowflakes = new ArrayList<>();

	private int spawnTimer = 0;
	private int spawnRate = 16;

	public Light light;
	public int sin = 0;
	public boolean bounce = false;

	private WorkerTask task = new WorkerTask() {
		@Override
		public void tick() {
			light.position = Vector3f.add(position, 0, 12, 0);
			sin = 0;
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
			light.linear = 1.35f - (float) Math.sin(Math.toRadians(sin)) / 4;
			light.color = Vector3f.mul(Vector3f.CAMPFIRE_MULTIPLIER, new Vector3f(x, x, x));
		}
	};
	
	public EffectCampfire(Vector3f position) {
		super(Vector3f.add(position, 0, 4, 0), Vector3f.EMPTY, Vector3f.SCALE_HALF);
		light = new Light(position, new Vector3f(10f, 5f, 0f), 1.5f, 1f, 0.035f, null);
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
				snowflakes.add(new Spark(position, new Vector4f(2f, 0.75f, 0f, 0.75f)));
				snowflakes.add(new Spark(position,new Vector4f(2f, 0.75f, 0f, 0.75f)));
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
				BatchSorter.add("zzzz", "pixel.dae", "pixel.png", "main_3d_bloom", Material.GLOW.toString(), Vector3f.div(s.position, s.scale), s.rotation, s.scale, s.color, false, false);
			}
		}
	}

	@Override
	public void cleanUp() {
		StateManager.currentState.lights.remove(light);
		task.finish();
	}

}

class Spark {

	public boolean remove = false;

	public int loop = 0;
	public int offset = 0;

	public Vector3f position;
	public Vector3f rotation;
	public Vector3f scale;
	public Vector4f color;
	
	public float alpha = 0.75f;

	public Spark(Vector3f position, Vector4f color) {
		scale = new Vector3f(0.5f, 0.5f, 0.5f).mul(new Random().nextFloat() + 0.5f);
		rotation = new Vector3f(new Random().nextInt(360), new Random().nextInt(360), new Random().nextInt(360));
		this.position = new Vector3f(position.getX(), position.getY() + 8, position.getZ());
		loop = new Random().nextInt(360);
		offset = new Random().nextInt(360);
		this.color = color;
	}

	public void tick() {
		if (loop < 360) {
			loop += 1;
		} else {
			loop = 0;
		}
		position.add(AdvMath.sin[loop % 360] / 20, 0.05f, AdvMath.sin[(loop + offset) % 360] / 20);
		rotation.add(new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat());
		scale.add(-0.01f);
		if (alpha > 0) {
			alpha -= 0.005;
		} else {
			remove = true;
		}
		color.setW(alpha);
	}

}