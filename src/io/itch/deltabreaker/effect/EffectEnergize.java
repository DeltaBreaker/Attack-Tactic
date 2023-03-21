package io.itch.deltabreaker.effect;

import java.util.ArrayList;
import java.util.Random;

import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.graphics.Light;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.state.StateManager;

public class EffectEnergize extends Effect {

	private ArrayList<Particle> particles = new ArrayList<>();
	private int amt = 50;
	private int count = 0;
	private Light light;
	private Vector4f color;

	public EffectEnergize(Vector3f position, Vector4f color) {
		super(position, Vector3f.EMPTY, Vector3f.EMPTY);
		this.color = color.copy();
		light = new Light(Vector3f.div(position, 2, 2, 2), new Vector3f(color.getX() * 3, color.getY() * 3, color.getZ() * 3), 1.5f, 1.5f, 0.05f, null);
		StateManager.currentState.lights.add(light);
		AudioManager.getSound("buff.ogg").play(AudioManager.defaultSubSFXGain, false);
	}

	@Override
	public void tick() {
		if (count < amt) {
			count++;
			particles.add(new Particle(Vector3f.add(position, 0, 0, 0), color));
		}
		for (int i = 0; i < particles.size(); i++) {
			particles.get(i).tick();
			if (particles.get(i).color.getW() == 0) {
				particles.remove(i);
				i--;
			}
		}
		if (light.color.getX() + light.color.getY() + light.color.getZ() == 0) {
			remove = true;
		}
		light.color.set(Math.max(0, light.color.getX() - 0.023f), Math.max(0, light.color.getY() - 0.023f), Math.max(0, light.color.getZ() - 0.023f));
	}

	@Override
	public void render() {
		for (Particle p : particles) {
			p.render();
		}
	}

	@Override
	public void cleanUp() {
		StateManager.currentState.lights.remove(light);
	}

}

class Particle {

	public Vector3f start;
	public Vector3f position;
	public Vector3f rotation;
	public Vector3f scale;
	public Vector4f color;

	public int age = 0;
	public int sin = 0;
	public float colorInterval = 0.02f;
	public float heightInterval = 0.2f;

	public Particle(Vector3f position, Vector4f color) {
		sin = new Random().nextInt(360);
		start = position;
		rotation = Vector3f.randomRotation();
		scale = Vector3f.SCALE_HALF;
		this.color = color.copy();
	}

	public void tick() {
		age++;
		sin += 4;
		position = Vector3f.add(start, AdvMath.sin[sin % 360] * (8 + age / 10.0f), heightInterval * age, (float) Math.cos(Math.toRadians(sin)) * (8 + age / 10.0f));
		color.setW(Math.max(0, color.getW() - colorInterval));
	}

	public void render() {
		BatchSorter.add("z", "pixel.dae", "pixel.png", "main_3d_bloom", Material.DEFAULT.toString(), position, rotation, scale, color, true, false);
	}

}