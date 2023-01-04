package io.itch.deltabreaker.effect;

import java.util.ArrayList;
import java.util.Random;

import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.state.StateDungeon;
import io.itch.deltabreaker.state.StateHub;
import io.itch.deltabreaker.state.StateManager;

public class EffectWaterSplash extends Effect {

	private static int splashSfx = 0;
	private static int splashSfxLimit = 2;
	public static boolean playSfx = true;

	private int particleCount = 25;
	private int particles = 0;
	private ArrayList<WaterParticle> water = new ArrayList<>();

	private int startDistance = 1;
	private float speed = 0.15f;
	private float fade = 0.012f;

	public ArrayList<Vector3f> positions = new ArrayList<>();
	public ArrayList<Vector3f> rotations = new ArrayList<>();
	public ArrayList<Vector3f> scales = new ArrayList<>();
	public ArrayList<Vector4f> colors = new ArrayList<>();

	public EffectWaterSplash(Vector3f position, boolean sfx) {
		super(position, Vector3f.EMPTY, Vector3f.EMPTY);
		if (sfx) {
			if (playSfx || StateDungeon.getCurrentContext().freeRoamMode || StateManager.currentState.STATE_ID.equals(StateHub.STATE_ID)) {
				AudioManager.getSound("splash_" + splashSfx + ".ogg").play(AudioManager.defaultSubSFXGain, false);
				if (splashSfx < splashSfxLimit) {
					splashSfx++;
				} else {
					splashSfx = 0;
				}
			}
			playSfx = !playSfx;
		}
	}

	@Override
	public void tick() {
		if (particles < particleCount) {
			water.add(new WaterParticle(position, startDistance, new Random().nextInt(360), speed, fade, new Vector4f(0.427f, 0.765f, 0.9f, 0.75f)));
			particles++;
		} else if (water.size() == 0) {
			remove = true;
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
		positions.clear();
		rotations.clear();
		scales.clear();
		colors.clear();
		for (WaterParticle w : water) {
			positions.add(w.position);
			rotations.add(w.rotation);
			scales.add(w.scale);
			colors.add(w.color);
		}
		BatchSorter.addBatch("z", "pixel.dae", "pixel.png", "main_3d_bloom", Material.WATER.toString(), positions, rotations, scales, colors, true, false);
	}

	@Override
	public void cleanUp() {

	}

}

class WaterParticle {

	public Vector3f position;
	public Vector3f rotation;
	public Vector3f scale;
	public Vector4f color;

	private int age = 0;
	private float angle;
	private float fade;
	private float speed;
	private float height = 3;

	public boolean remove = false;

	public WaterParticle(Vector3f position, int startDistance, float angle, float speed, float fade, Vector4f color) {
		this.position = Vector3f.add(position, (float) Math.cos(Math.toRadians(angle)) * startDistance, 0, (float) Math.sin(Math.toRadians(angle)) * startDistance);
		rotation = Vector3f.randomRotation();
		scale = Vector3f.SCALE_HALF;
		this.color = color;
		this.angle = angle;
		this.fade = fade;
		this.speed = speed;
	}

	public WaterParticle(Vector3f position, int startDistance, float angle, float speed, float fade, Vector4f color, float height) {
		this.position = Vector3f.add(position, (float) Math.cos(Math.toRadians(angle)) * startDistance, 0, (float) Math.sin(Math.toRadians(angle)) * startDistance);
		rotation = Vector3f.randomRotation();
		scale = Vector3f.SCALE_HALF;
		this.color = color;
		this.angle = angle;
		this.fade = fade;
		this.speed = speed;
		this.height = height;
	}

	public void tick() {
		age += 5;
		color.setW(Math.max(0, color.getW() - fade));
		if (color.getW() == 0) {
			remove = true;
		}
		position.add((float) Math.cos(Math.toRadians(angle)) * speed, AdvMath.sin[age % 360] / height, (float) Math.sin(Math.toRadians(angle)) * speed);
	}

}