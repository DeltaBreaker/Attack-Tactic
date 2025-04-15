package io.itch.deltabreaker.effect.battle;

import java.util.ArrayList;
import java.util.Random;

import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Light;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.state.StateManager;

public class EffectBattleSlash extends EffectBattle {

	public Light light;
	public ArrayList<EffectBattleSpark> sparks = new ArrayList<>();

	public int sparkAmount = 50;

	public boolean dir;

	public EffectBattleSlash(Vector3f position, boolean dir) {
		super(position, new Vector3f(90, 0, dir ? 0 : 180));
		frames = 11;
		frameTime = 4;
		light = new Light(Vector3f.mul(this.position.add(new Vector3f(0, 1, 0)), new Vector3f(0.5f, 0.5f, 0.5f)),
				new Vector3f(2f, 2f, 2f), 1.5f, 1f, 0.035f, null);
		this.dir = dir;
		AudioManager.getSound("battle_sword_" + new Random().nextInt(2) + ".ogg").play(AudioManager.defaultBattleSFXGain, false);
	}

	public void tick() {
		if (frameTimer < frameTime) {
			frameTimer++;
		} else {
			frameTimer = 0;
			if (frame < frames) {
				frame++;
				onFrame();
			}
		}
		if (frame >= 4) {
			light.color.setX(Math.max(0, light.color.getX() - 0.03f));
			light.color.setY(Math.max(0, light.color.getY() - 0.03f));
			light.color.setZ(Math.max(0, light.color.getZ() - 0.03f));
		}
		for (int i = 0; i < sparks.size(); i++) {
			sparks.get(i).tick();
			if (sparks.get(i).remove) {
				sparks.remove(i);
				i--;
			}
		}
		if (frame == frames && sparks.size() == 0) {
			remove = true;
		}
	}

	public void onFrame() {
		if (frame == 4 && StateManager.currentState.effects.size() < 100) {
			StateManager.currentState.lights.add(light);
			for (int i = 0; i < sparkAmount; i++) {
				sparks.add(new EffectBattleSpark(Vector3f.mul(Vector3f.add(position, 0, -12, 2), 0.5f, 0.5f, 0.5f),
						new Vector3f(2, 2, 2), 0.0075f));
			}
		}
	}

	@Override
	public void render() {
		if (frame < frames - 1) {
			BatchSorter.add("z", "effect_battle_physical_0_" + frame + ".dae", "effect_battle_physical_0_" + frame + ".png",
					"main_3d", "DEFAULT", Vector3f.add(position, dir ? -11 : 11, 0, -8), rotation, scale,
					new Vector4f(1, 1, 1, 1), true, false);
		}
		for (int i = 0; i < sparks.size(); i++) {
			sparks.get(i).render();
		}
	}

	@Override
	public void cleanUp() {
		StateManager.currentState.lights.remove(light);
		sparks.clear();
	}
	
}
