package io.itch.deltabreaker.effect;

import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.state.StateManager;

public class EffectShield extends Effect {

	public int frame = 0;
	public int frames;
	public int frameTimer = 0;
	public int frameTime;
	
	public EffectShield(Vector3f position) {
		super(position, new Vector3f(60, 0, 0), Vector3f.SCALE_HALF);
		frames = 16;
		frameTime = 7;
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
		if (frame == frames) {
			remove = true;
		}
	}

	public void onFrame() {
		if(frame == 2) {
			StateManager.currentState.effects.add(new EffectBuff(position.copy()));
		}
	}

	@Override
	public void render() {
		if (frame < frames - 1) {
			BatchSorter.add("z", "effect_shield_" + frame + ".dae", "effect_shield_" + frame + ".png",
					"main_3d", "DEFAULT", Vector3f.add(position, 0, 8, -4), rotation, scale,
					new Vector4f(1, 1, 1, 1), true, false);
		}
	}

	@Override
	public void cleanUp() {
		
	}
	
}
