package io.itch.deltabreaker.effect.dungeon;

import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.effect.Effect;
import io.itch.deltabreaker.math.Vector3f;

public class EffectDungeonLavaSFX extends Effect {

	public EffectDungeonLavaSFX() {
		super(Vector3f.EMPTY, Vector3f.EMPTY, Vector3f.EMPTY);
		if (!AudioManager.getSound("lava.ogg").isPlaying()) {
			AudioManager.getSound("lava.ogg").play(0, true);
		}
		AudioManager.getSound("lava.ogg").fade(AudioManager.defaultSubSFXGain, 144, true);
	}

	@Override
	public void tick() {
		
	}

	@Override
	public void render() {
		
	}

	@Override
	public void cleanUp() {
		AudioManager.getSound("lava.ogg").fade(0, 144, true);
	}

}
