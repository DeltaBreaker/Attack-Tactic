package io.itch.deltabreaker.effect;

import java.util.ArrayList;
import java.util.Random;

import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.state.StateDungeon;
import io.itch.deltabreaker.state.StateHub;
import io.itch.deltabreaker.state.StateManager;

public class EffectLavaSplash extends Effect {

	private static int splashSfx = 0;
	private static int splashSfxLimit = 2;
	public static boolean playSfx = true;
	
	private int particleCount = 25;
	private int particles = 0;
	private ArrayList<LavaParticle> water = new ArrayList<>();

	private int startDistance = 1;
	private float speed = 0.15f;
	private float fade = 0.012f;

	public EffectLavaSplash(Vector3f position, boolean sfx) {
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
			water.add(new LavaParticle(position, startDistance, new Random().nextInt(360), speed, fade, Vector4f.COLOR_LAVA.copy()));
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
		for (LavaParticle w : water) {
			w.render();
		}
	}

	@Override
	public void cleanUp() {

	}

}

class LavaParticle {

	private Vector3f position;
	private Vector3f rotation;
	private Vector3f scale;
	private Vector4f color;

	private float age = 0;
	private float angle;
	private float fade;
	private float speed;

	public boolean remove = false;

	public LavaParticle(Vector3f position, int startDistance, float angle, float speed, float fade, Vector4f color) {
		this.position = Vector3f.add(position, (float) Math.cos(Math.toRadians(angle)) * startDistance, 0, (float) Math.sin(Math.toRadians(angle)) * startDistance);
		rotation = Vector3f.randomRotation();
		scale = Vector3f.SCALE_HALF;
		this.color = color;
		this.angle = angle;
		this.fade = fade;
		this.speed = speed;
	}

	public void tick() {
		age += 5;
		color.setW(Math.max(0, color.getW() - fade));
		if (color.getW() == 0) {
			remove = true;
		}
		position.add((float) Math.cos(Math.toRadians(angle)) * speed, (float) Math.sin(Math.toRadians(age)) / 3, (float) Math.sin(Math.toRadians(angle)) * speed);
	}

	public void render() {
		BatchSorter.add("z", "pixel.dae", "pixel.png", "main_3d_bloom", Material.WATER.toString(), position, rotation, scale, color, true, false);
	}

}