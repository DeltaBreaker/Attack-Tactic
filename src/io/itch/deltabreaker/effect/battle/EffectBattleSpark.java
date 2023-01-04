package io.itch.deltabreaker.effect.battle;

import java.util.Random;

import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.state.StateManager;

public class EffectBattleSpark {

	public Vector3f position;
	public Vector3f rotation;
	public Vector3f scale;
	public Vector3f color;
	
	public float alpha = 0.75f;
	public float decay;
	
	public Vector3f path;

	public boolean remove = false;
	
	public EffectBattleSpark(Vector3f position, Vector3f color, float decay) {
		scale = new Vector3f(0.5f, 0.5f, 0.5f).mul(new Random().nextFloat() + 0.5f);
		rotation = new Vector3f(new Random().nextInt(360), new Random().nextInt(360), new Random().nextInt(360));
		this.color = color;
		this.position = new Vector3f(position.getX(), position.getY() + 8, position.getZ());
		path = new Vector3f((new Random().nextFloat() - 0.5f) / 1.5f, (new Random().nextFloat() - 0.5f) / 1.5f,
				(new Random().nextFloat() - 0.5f) / 1.5f);
		this.decay = decay;
	}

	public void tick() {
		position.add(path);
		rotation.add(new Vector3f(new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat()));
		scale.add(-0.01f);
		if (alpha > 0) {
			alpha -= decay;
		} else {
			remove = true;
		}
	}

	public void render() {
		if (position.getX() > (int) StateManager.currentState.camX - 208
				&& position.getX() < (int) StateManager.currentState.camX + 192
				&& position.getZ() < StateManager.currentState.camY + 16
				&& position.getZ() > StateManager.currentState.camY - 160) {
			BatchSorter.add("z", "pixel.dae", "pixel.png", "main_3d_bloom", Material.GLOW.toString(),
					Vector3f.div(position, scale), rotation, scale, new Vector4f(color.getX(), color.getY(), color.getZ(), alpha), false, false);
		}
	}

}
