package io.itch.deltabreaker.effect.battle;

import java.util.ArrayList;

import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Light;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.state.StateManager;

public class EffectBattleDarkBurst extends EffectBattle {

	public ArrayList<EffectBattleSpark> sparks = new ArrayList<>();
	private Light light;
	
	public int sparkAmount = 100;
	
	public EffectBattleDarkBurst(Vector3f position, boolean dir) {
		super(position, new Vector3f(60, 0, 0));
		frames = 5;
		frameTime = 8;
		light = new Light(Vector3f.mul(this.position.add(new Vector3f(0, 1, 0)), new Vector3f(0.5f, 0.5f, 0.5f)),
				new Vector3f(0.384f, 0f, 0.45f).mul(8), 1f, 0.002f, 0.035f, null);
		StateManager.currentState.lights.add(light);
		AudioManager.getSound("battle_dark_small.ogg").play(AudioManager.defaultBattleSFXGain, false);
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
			light.color.setX(Math.max(0, light.color.getX() - 0.05f));
			light.color.setZ(Math.max(0, light.color.getZ() - 0.05f));
		}
		for(int i = 0; i < sparks.size(); i++) {
			sparks.get(i).tick();
			if(sparks.get(i).remove) {
				sparks.remove(i);
				i--;
			}
		}
		if(frame == frames && sparks.size() == 0) {
			remove = true;
		}
	}

	public void onFrame() {
		if (frame == 1) {
			for (int i = 0; i < sparkAmount; i++) {
				sparks.add(new EffectBattleSpark(Vector3f.mul(Vector3f.add(position, 0, -12, 2), 0.5f, 0.5f, 0.5f),
						new Vector3f(0.384f, 0f, 0.45f).mul(3), 0.01f));
			}
		}
	}
	
	@Override
	public void render() {
		if (frame < frames - 1) {
			BatchSorter.add("z", "effect_battle_dark_2_" + frame + ".dae", "effect_battle_dark_2_" + frame + ".png",
					"main_3d_bloom", "DEFAULT", Vector3f.add(position, 3, 0, -6), rotation, scale, new Vector4f(1f, 1f, 1f, 1), true, false);
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
