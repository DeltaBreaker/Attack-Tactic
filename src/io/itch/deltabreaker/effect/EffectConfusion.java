package io.itch.deltabreaker.effect;

import java.util.ArrayList;
import java.util.Random;

import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;

public class EffectConfusion extends Effect {

	private static final int SPAWN_LIMIT = 8;
	
	private int spawnTimer = 0;
	private int spawnRate = 24;
	
	public ArrayList<ParticleQuestionMark> marks = new ArrayList<>();
		
	public EffectConfusion(Vector3f position) {
		super(position, Vector3f.EMPTY, Vector3f.SCALE_HALF);
	}

	@Override
	public void tick() {
		spawnRate = 64;
		ParticleQuestionMark.life = 288;
		ParticleQuestionMark.colorSpeedMultiplier = 360f / ParticleQuestionMark.life;
		ParticleQuestionMark.speed.setY(0.065f);
		
		if (!die) {
			if (marks.size() < SPAWN_LIMIT) {
				if (spawnTimer < spawnRate) {
					spawnTimer++;
				} else {
					spawnTimer = 0;
					marks.add(new ParticleQuestionMark(Vector3f.mul(position, 2)));
				}
			}
		} else if (allParticlesDone()) {
			remove = true;
		}
		for (int i = 0; i < marks.size(); i++) {
			marks.get(i).tick();
			if (marks.get(i).age <= 0 && !die) {
				marks.remove(i);
				i--;
			}
		}
	}

	@Override
	public void render() {
		for(ParticleQuestionMark p : marks) {
			p.render();
		}
	}

	@Override
	public void cleanUp() {
		
	}

	public boolean allParticlesDone() {
		for (ParticleQuestionMark p : marks) {
			if (p.age > 0) {
				return false;
			}
		}
		return true;
	}

	
}

class ParticleQuestionMark {
	
	public static Vector3f speed = new Vector3f(0, 0.1f, 0);
	public static int life = 144;
	public static float colorSpeedMultiplier = 360f / life;
	
	public Vector3f position;
	public Vector3f rotation = new Vector3f(-45, 180,  -45 + new Random().nextInt(90));
	public Vector4f color = new Vector4f(new Random().nextFloat() % 0.75f, new Random().nextFloat() % 0.75f, new Random().nextFloat() % 0.75f, 0);
	public Vector3f colorTarget = new Vector3f(new Random().nextFloat() % 0.75f, new Random().nextFloat() % 0.75f, new Random().nextFloat() % 0.75f);
	public int age = life;
	
	public ParticleQuestionMark(Vector3f position) {
		int circlePosition = new Random().nextInt(360);
		this.position = Vector3f.add(position, new Vector3f(AdvMath.sin[circlePosition] * 8, 8 + new Random().nextInt(8), AdvMath.sin[(circlePosition + 90) % 360] * 8));
		
		
	}

	public void tick() {
		age = Math.max(0, age - 1);
		position.add(speed);
		color.setW(AdvMath.sin[(int) (age * colorSpeedMultiplier)]);
		
		if(color.getX() > colorTarget.getX()) {
			color.setX(Math.max(color.getX() - 0.002f, colorTarget.getX()));
		}
		if(color.getX() < colorTarget.getX()) {
			color.setX(Math.min(color.getX() + 0.002f, colorTarget.getX()));
		}
		if(color.getY() > colorTarget.getY()) {
			color.setY(Math.max(color.getY() - 0.002f, colorTarget.getY()));
		}
		if(color.getY() < colorTarget.getY()) {
			color.setY(Math.min(color.getY() + 0.002f, colorTarget.getY()));
		}
		if(color.getZ() > colorTarget.getZ()) {
			color.setZ(Math.max(color.getZ() - 0.002f, colorTarget.getZ()));
		}
		if(color.getZ() < colorTarget.getZ()) {
			color.setZ(Math.min(color.getZ() + 0.002f, colorTarget.getZ()));
		}
	}
	
	public void render() {
		BatchSorter.add("zzzzzzzzz", "&.dae", "@.png", "main_3d_nobloom", Material.DEFAULT.toString(), position, rotation, Vector3f.SCALE_HALF, color,
				false, false);
	}
	
}