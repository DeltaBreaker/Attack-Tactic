package io.itch.deltabreaker.effect;

import java.util.Random;

import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.ItemAbility;

public class EffectProjectile extends Effect {

	private static final Vector4f[] COLORS = { Vector4f.COLOR_PEBBLE, Vector4f.COLOR_LAVA_HEAT, Vector4f.COLOR_POISON, Vector4f.COLOR_BASE };

	private Unit target;

	private float progress = 0;
	private float length = 72;
	private float height = 24;

	private Vector3f origin;
	private Vector3f distance;
	private boolean bounce = false;
	private Vector3f bounceRotation = new Vector3f(new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat());

	private int type;

	private Vector4f color;

	public EffectProjectile(Unit u, Unit target, int type) {
		super(new Vector3f(u.x, 13 + u.height, u.y), Vector3f.EMPTY.copy(), Vector3f.SCALE_FULL);
		distance = Vector3f.sub(new Vector3f(target.x, 13 + target.height, target.y), position);
		origin = position.copy();
		this.target = target;
		this.type = type;

		color = COLORS[type].copy();
	}

	@Override
	public void tick() {
		if (progress < length) {
			progress++;
		} else {
			if (!bounce) {
				if (target.accessory.hasAbility(ItemAbility.ITEM_ABILITY_REFLECT)) {
					origin = Vector3f.mul(position, 2);
					distance = new Vector3f((8 + new Random().nextInt(8)) * ((new Random().nextBoolean()) ? 1 : -1), 0, (8 + new Random().nextInt(8)) * ((new Random().nextBoolean()) ? 1 : -1));
					bounce = true;
					progress = 0;
					height = 8;
					
					AudioManager.getSound("projectile_deflect.ogg").play(AudioManager.defaultMainSFXGain, false);
				} else {
					switch (type) {

					case 0:
						target.hurt(5);
						break;

					case 1:
						target.hurt(10);
						break;

					case 2:
						target.applyStatus(Unit.STATUS_POISON);
						break;

					case 3:
						target.applyStatus(Unit.STATUS_SLEEP);
						break;

					}

					AudioManager.getSound("projectile_hit.ogg").play(AudioManager.defaultMainSFXGain, false);
					remove = true;
				}
			} else {
				remove = true;
			}
		}
		float amount = progress / length;
		position.set(origin.getX() + distance.getX() * amount, origin.getY() + distance.getY() * amount + AdvMath.sin[(int) (180 * amount)] * height, origin.getZ() + distance.getZ() * amount);
		position.set(position.getX() * 0.5f, position.getY() * 0.5f, position.getZ() * 0.5f);
		if (bounce) {
			color.setW(1 - (progress * (1 / length)));
			rotation.add(bounceRotation);
		}
	}

	@Override
	public void render() {
		BatchSorter.add("z", "pixel.dae", "pixel.png", "main_3d_nobloom", Material.DEFAULT.toString(), position, rotation, scale, color, true, false);
	}

	@Override
	public void cleanUp() {

	}

}
