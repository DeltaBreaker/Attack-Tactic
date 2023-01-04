package io.itch.deltabreaker.effect;

import java.util.Random;

import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;

public class EffectMedialChunk extends Effect {

	public Vector3f truePosition;
	public float sin = new Random().nextInt(360);
	
	public EffectMedialChunk(Vector3f position) {
		super(position.copy(), new Vector3f(0, new Random().nextInt(4) * 90, 0), Vector3f.SCALE_HALF);
		truePosition = position.copy();
	}

	@Override
	public void tick() {
		if(sin < 359) {
			sin += 0.5f;
		} else {
			sin = 0;
		}
		truePosition.setY(position.getY() * 2 + AdvMath.sin[(int) sin] * 2 + 8);
	}

	@Override
	public void render() {
		BatchSorter.add("corruption_chunk2.dae", "corruption_chunk2.png", "main_3d_medial", Material.DEFAULT.toString(), truePosition, rotation, scale, Vector4f.COLOR_BASE, true, false);
	}

	@Override
	public void cleanUp() {
		
	}

}
