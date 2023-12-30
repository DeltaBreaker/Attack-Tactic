package io.itch.deltabreaker.effect;

import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;

public class EffectDaze extends Effect {

	public float alpha = 0;
	public float circleRotation = 0;
	public float distance = 360 / 5;

	public EffectDaze(Vector3f position) {
		super(position, new Vector3f(45, 0, 0), Vector3f.SCALE_HALF);
	}

	@Override
	public void tick() {
		if (!die) {
			alpha = Math.min(alpha + 0.01f, 1);
		} else if (alpha > 0) {
			alpha = Math.max(alpha - 0.01f, 0);
		} else {
			remove = true;
		}
		circleRotation += 0.75f;
	}

	@Override
	public void render() {
		for (int i = 0; i < 5; i++) {
			BatchSorter.add("zzzzzzzzz", "star.dae", "star.png", "main_3d_nobloom", Material.DEFAULT.toString(),
					Vector3f.add(Vector3f.mul(position, 2f), AdvMath.sin[(int) ((circleRotation + (i * distance)) % 360)] * 6, 32, AdvMath.sin[(int) ((circleRotation + 90 + (i * distance)) % 360)] * 6), rotation, Vector3f.SCALE_HALF,
					new Vector4f(0.7137254901960784f, 1, 0, 0.75f * alpha), false, false);
		}
	}

	@Override
	public void cleanUp() {

	}

}
