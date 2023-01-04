package io.itch.deltabreaker.effect;

import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;

public class EffectBloom extends Effect {

	public Vector4f color;
	public Vector3f offset;

	public EffectBloom(Vector3f position, Vector4f color, float size) {
		super(position, Vector3f.EMPTY.copy(), new Vector3f(size, size, size));
		this.color = color;
		offset = Vector3f.EMPTY.copy();
	}

	public EffectBloom(Vector3f position, Vector4f color, float size, Vector3f offset) {
		super(position, Vector3f.EMPTY.copy(), new Vector3f(size, size, size));
		this.color = color;
		this.offset = offset;
	}
	
	@Override
	public void tick() {
		rotation.set(-Startup.camera.rotation.getX() + 90 + offset.getX(), -Startup.camera.rotation.getY() + offset.getY(), -Startup.camera.rotation.getZ() + offset.getZ());
	}

	@Override
	public void render() {
		BatchSorter.add("pixel.dae", "bloom.png", "main_3d_nobloom", Material.DEFAULT.toString(), Vector3f.div(position, Vector3f.div(scale, 8, 8, 8)), rotation, scale, color, false, false, false);
	}

	@Override
	public void cleanUp() {

	}

}
