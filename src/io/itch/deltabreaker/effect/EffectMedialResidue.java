package io.itch.deltabreaker.effect;

import java.util.Random;

import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;

public class EffectMedialResidue extends Effect {

	public Vector3f truePosition1;
	public Vector3f truePosition2;
	public Vector3f truePosition3;
	
	public Vector3f trueRotation1;
	public Vector3f trueRotation2;
	public Vector3f trueRotation3;
	
	public float sin = 0;
	public int offset1 = new Random().nextInt(360);
	public int offset2 = new Random().nextInt(360);
	public int offset3 = new Random().nextInt(360);
	
	public EffectMedialResidue(Vector3f position) {
		super(position, Vector3f.EMPTY.copy(), Vector3f.SCALE_FULL.copy());
		truePosition1 = position.copy();
		truePosition2 = position.copy();
		truePosition3 = position.copy();
		
		trueRotation1 = rotation.copy();
		trueRotation2 = rotation.copy();
		trueRotation3 = rotation.copy();
	}

	@Override
	public void tick() {
		if(sin < 359) {
			sin += 0.5f;
		} else {
			sin = 0;
		}
		
		trueRotation1.set(sin, sin, 0);
		truePosition1.set(position.getX() - 9f, position.getY() + AdvMath.sin[((int) sin + offset1) % 360] * 3, position.getZ() - 4.5f);
		
		trueRotation2.set(0, sin, sin);
		truePosition2.set(position.getX() + 9f, position.getY() + AdvMath.sin[((int) sin + offset2) % 360] * 3, position.getZ() - 4.5f);
		
		trueRotation3.set(sin, 0, sin);
		truePosition3.set(position.getX(), position.getY() + AdvMath.sin[((int) sin + offset3) % 360] * 3, position.getZ() + 9f);
	}

	@Override
	public void render() {
		BatchSorter.add("pixel.dae", "pixel.png", "main_3d_medial", Material.DEFAULT.toString(), truePosition1, trueRotation1, scale, Vector4f.COLOR_BASE, true, false);
		BatchSorter.add("pixel.dae", "pixel.png", "main_3d_medial", Material.DEFAULT.toString(), truePosition2, trueRotation2, scale, Vector4f.COLOR_BASE, true, false);
		BatchSorter.add("pixel.dae", "pixel.png", "main_3d_medial", Material.DEFAULT.toString(), truePosition3, trueRotation3, scale, Vector4f.COLOR_BASE, true, false);
	}

	@Override
	public void cleanUp() {
		
	}

}
