package io.itch.deltabreaker.effect;

import java.util.ArrayList;

import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.state.StateManager;

public class EffectBlackHole extends Effect {

	private ArrayList<VortexParticle> particles = new ArrayList<>();
	
	
	private int spawnTimer = 0;
	private int spawnTime = 15;
	private int spawnLimit = 128;
	private int spawnTracker = 0;
	
	private ArrayList<Vector4f> colors = new ArrayList<Vector4f>();
	private ArrayList<Vector3f> positions = new ArrayList<>();
	private ArrayList<Vector3f> rotations = new ArrayList<>();
	private ArrayList<Vector3f> scales = new ArrayList<>();
	
	private EffectBloom e;
	
	public EffectBlackHole(Vector3f position) {
		super(position, new Vector3f(0, 0, 0), Vector3f.SCALE_HALF);
		e = new EffectBloom(new Vector3f(position.getX() / 16, position.getY() / 16 - 1f, position.getZ() / 16), new Vector4f(0, 0, 0, 0.85f), 16f);
		StateManager.currentState.effects.add(e);
	}

	@Override
	public void tick() {
		e.offset.set(Startup.camera.rotation.getX() + 90, +Startup.camera.rotation.getY(), Startup.camera.rotation.getZ());
		rotation.add(0, 0.25f, 0);
		if(rotation.getY() > 360) {
			rotation.setY(rotation.getY() - 360);
		}
		for(int i = 0; i < particles.size(); i++) {
			particles.get(i).tick();
			if(particles.get(i).remove) {
				particles.remove(i);
				i--;
			}
		}
		if(spawnTimer < spawnTime) {
			spawnTimer++;
		} else if(particles.size() < spawnLimit) {
			spawnTimer = 9;
			spawnTracker -= 1;
			if(spawnTracker < 0) {
				spawnTracker += 9;
			}
			particles.add(new VortexParticle(spawnTracker * 45, position, new Vector4f(0f, 0f, 0f, 1f), new Vector3f(0.75f, 0.75f, 0.75f)));
		}
	}

	@Override
	public void render() {
		scales.clear();
		rotations.clear();
		positions.clear();
		colors.clear();
		for(VortexParticle v : particles) {
			positions.add(Vector3f.div(v.position, Vector3f.div(v.scale, Vector3f.SCALE_HALF)));
			scales.add(v.scale);
			rotations.add(Vector3f.EMPTY);
			colors.add(v.color);
		}
		BatchSorter.addBatch("zzzzz", "pixel.dae", "pixel.png", "main_3d_corrupt", Material.DEFAULT.toString(), positions, rotations, scales, colors, false, false);
		BatchSorter.add("vortex.dae", "vortex.png", "main_3d_corrupt", Material.DEFAULT.toString(), Vector3f.div(position, 1, 1, 1), rotation, scale, Vector4f.COLOR_BASE, true, false);
	}

	@Override
	public void cleanUp() {
		
	}

}

class VortexParticle {
	
	public Vector3f position;
	public Vector3f origin;
	public Vector3f scale;
	public Vector4f color;
	public float radius = 16;
	public float offset;
	public float age = 0;
	public boolean remove = false;
	private float alpha = 0;
		
	public VortexParticle(float offset, Vector3f origin, Vector4f color, Vector3f scale) {
		this.offset = offset;
		this.origin = origin;
		this.scale = scale;
		this.color = color;
		
		position = new Vector3f(origin.getX() + AdvMath.sin[(int) ((age + offset) % 360)] * radius, origin.getY(), origin.getZ() + AdvMath.sin[(int) ((age + offset + 90) % 360)] * radius);
	}
	
	public void tick() {
		alpha = Math.min(1, alpha += 0.01f);
		float amt = Math.max(0, scale.getX()-0.0015f);
		scale.set(amt, amt, amt);
		age++;
		radius -= 0.05f;
		if(radius <= 0) {
			remove = true;
		}
		position.set(origin.getX() + -AdvMath.sin[(int) ((age + offset) % 360)] * radius, origin.getY() + 2, origin.getZ() + AdvMath.sin[(int) ((age + offset + 90) % 360)] * radius);
		color.setW(alpha);
	}
	
}