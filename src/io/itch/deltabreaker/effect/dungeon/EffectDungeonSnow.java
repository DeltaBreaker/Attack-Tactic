package io.itch.deltabreaker.effect.dungeon;

import java.util.ArrayList;
import java.util.Random;

import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.effect.Effect;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.tile.Tile;
import io.itch.deltabreaker.state.StateManager;

public class EffectDungeonSnow extends Effect {

	private static final int SPAWN_LIMIT = 500;

	private ArrayList<Snowflake> snowflakes = new ArrayList<>();

	private int spawnTimer = 0;
	private int spawnRate = 1;

	private int switchTimer = 0;
	private int switchTime = 144 * 5;
	private float randInf = 0.25f;

	public ArrayList<Vector3f> positions = new ArrayList<>();
	public ArrayList<Vector3f> rotations = new ArrayList<>();
	public ArrayList<Vector3f> scales = new ArrayList<>();
	public ArrayList<Vector4f> colors = new ArrayList<>();

	public EffectDungeonSnow() {
		super(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(0, 0, 0));
		if (!AudioManager.getSound("wind.ogg").isPlaying()) {
			AudioManager.getSound("wind.ogg").play(0, true);
		}
		AudioManager.getSound("wind.ogg").fade(AudioManager.defaultSubSFXGain, 144, true);
	}

	public void tick() {
		if (spawnTimer < spawnRate) {
			spawnTimer++;
		} else {
			if (snowflakes.size() < SPAWN_LIMIT) {
				spawnTimer = 0;
				snowflakes.add(new Snowflake());
			} else {
				spawnTimer = 0;
				for(Snowflake s : snowflakes) {
					if(s.remove) {
						s.reset();
						break;
					}
				}
			}
		}
		for (int i = 0; i < snowflakes.size(); i++) {
			snowflakes.get(i).tick();
		}
		if (switchTimer < switchTime) {
			switchTimer++;
		} else {
			switchTimer = 0;
			switchTime = 144 * (new Random().nextInt(5) + 5);
			randInf = new Random().nextFloat() % 0.25f;
		}
		float x = Snowflake.influence.getX();
		float speed = 0.0001f;
		if (x < randInf) {
			Snowflake.influence.setX(Math.min(x + speed, randInf));
		}
		if (x > randInf) {
			Snowflake.influence.setX(Math.max(x - speed, randInf));
		}
	}

	public void render() {
		positions.clear();
		rotations.clear();
		scales.clear();
		colors.clear();
		for (int i = 0; i < snowflakes.size(); i++) {
			Snowflake s = snowflakes.get(i);
			Vector3f position = s.position;
			float distance = Vector3f.distance(position, Startup.camera.position);
			if (distance < 96) {
				positions.add(Vector3f.div(Vector3f.add(s.position, (float) Math.sin(s.loop) * 10, 0, (float) Math.sin(s.loop2) * 10), s.scale));
				rotations.add(s.rotation);
				scales.add(s.scale);
				colors.add(s.color);
			}
		}
		BatchSorter.addBatch("pixel.dae", "pixel.png", "main_3d_bloom", Material.GLOW.toString(), positions, rotations, scales, colors, true, false, false);
	}

	@Override
	public void cleanUp() {
		snowflakes.clear();
		AudioManager.getSound("wind.ogg").fade(0, 144, true);
	}

}

class Snowflake {

	public boolean remove = false;

	public double loop = 0;
	public double loop2 = 0;

	public Vector3f position;
	public Vector3f rotation;
	public Vector3f scale;
	public static Vector3f influence = new Vector3f(0.25f, -0.15f, 0);

	public float alpha = 0.5f;
	public Vector4f color = new Vector4f(1, 1, 1, 1);

	public Snowflake() {
		scale = new Vector3f(0.5f, 0.5f, 0.5f).mul(new Random().nextFloat() + 0.5f);
		rotation = new Vector3f(new Random().nextInt(360), new Random().nextInt(360), new Random().nextInt(360));
		position = new Vector3f(new Random().nextInt(StateManager.currentState.tiles.length * 16) - StateManager.currentState.tiles.length * 8, Startup.camera.position.getY() + new Random().nextInt(64),
				new Random().nextInt(StateManager.currentState.tiles[0].length * 8));
		loop = new Random().nextInt(360);
		loop2 = new Random().nextInt(360);
	}

	public void reset() {
		remove = false;
		alpha = 0.5f;
		
		scale = new Vector3f(0.5f, 0.5f, 0.5f).mul(new Random().nextFloat() + 0.5f);
		rotation = new Vector3f(new Random().nextInt(360), new Random().nextInt(360), new Random().nextInt(360));
		position = new Vector3f(new Random().nextInt(StateManager.currentState.tiles.length * 16) - StateManager.currentState.tiles.length * 8, Startup.camera.position.getY() + new Random().nextInt(64),
				new Random().nextInt(StateManager.currentState.tiles[0].length * 8));
		loop = new Random().nextInt(360);
		loop2 = new Random().nextInt(360);
	}
	
	public void tick() {
		Tile t = StateManager.currentState.tiles[AdvMath.inRange((int) (position.getX() + (float) Math.sin(loop) * 10) / 8, 0, StateManager.currentState.tiles.length - 1)][AdvMath
				.inRange((int) (position.getZ() + (float) Math.sin(loop2) * 10) / 8, 0, StateManager.currentState.tiles[0].length - 1)];
		if (position.getY() > (t.getPosition().getY() - t.getOffset().getY()) / 2 + 4.5f) {
			if (loop < 360) {
				loop += 0.01;
				loop2 += 0.01;
			} else {
				loop = 0;
			}
			rotation.add(new Vector3f(new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat()));
			position.add(influence);
		} else if (alpha > 0) {
			alpha -= 0.005;
		} else {
			remove = true;
		}

		float distance = Vector3f.distance(position, Startup.camera.position);
		color.setW(alpha * Math.min(1, 1 - distance / 176));
	}

}