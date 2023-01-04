package io.itch.deltabreaker.effect;

import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;

public class EffectPoofMenu extends Effect {

	public int frame = 0;
	public int frames = 5;
	public int frameTimer = 0;
	public int frameTime = 8;

	public EffectPoofMenu(Vector3f position) {
		super(position, new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f));
	}

	@Override
	public void tick() {
		if (frameTimer < frameTime) {
			frameTimer++;
		} else {
			frameTimer = 0;
			if (frame < frames - 1) {
				frame++;
			} else {
				remove = true;
			}
		}
	}

	@Override
	public void render() {
		BatchSorter.add("effect_poof_" + frame + ".dae", "effect_poof_" + frame + ".png", "static_3d", Material.DEFAULT.toString(), position,
				Vector3f.EMPTY, scale, Vector4f.COLOR_BASE, true, true);
	}

	@Override
	public void cleanUp() {

	}

}
