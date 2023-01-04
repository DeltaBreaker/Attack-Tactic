package io.itch.deltabreaker.effect;

import java.util.Random;

import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Light;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.item.ItemProperty;
import io.itch.deltabreaker.state.StateManager;

public class EffectOpenChest extends Effect {

	private Light light;
	private Flare[] sparks = new Flare[25];
	private ItemProperty item;

	private float alpha = 1;

	public EffectOpenChest(Vector3f position, ItemProperty item) {
		super(position, new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));
		light = new Light(position, Vector3f.mul(ItemProperty.colorList[item.tier], 6, 6, 6), 1.5f, 1f, 0.0535f, null);
		StateManager.currentState.lights.add(light);
		for (int i = 0; i < sparks.length; i++) {
			sparks[i] = new Flare(position, Vector3f.div(light.color, 6, 6, 6));
		}
		this.item = item;
		if (!item.type.equals(ItemProperty.TYPE_EMPTY)) {
			StateManager.currentState.effects.add(new EffectText("+" + item.name, Vector3f.mul(Vector3f.add(position, -("+" + item.name).length() / 1.5f, 8, 0), new Vector3f(2, 2, 2)), new Vector4f(ItemProperty.colorList[item.tier], 1)));
		}
	}

	@Override
	public void tick() {
		if ((light.color.getX() + light.color.getY() + light.color.getZ()) > 0.1) {
			light.color.mul(0.988f);
		}
		for (Flare f : sparks) {
			f.tick();
		}
		if (alpha > 0) {
			alpha -= 0.003f;
		} else {
			remove = true;
			StateManager.currentState.effects.add(new EffectPoof(Vector3f.add(Vector3f.mul(position, 2, 2, 2), 0, 8, 0)));
		}
	}

	@Override
	public void render() {
		for (Flare f : sparks) {
			f.render();
		}
		if (!item.type.equals(ItemProperty.TYPE_EMPTY)) {
			BatchSorter.add("zzzzzz", item.model, item.texture, "main_3d", item.material.toString(), Vector3f.add(Vector3f.mul(position, 2, 2, 2), 0, 8, 0),
					new Vector3f(-Startup.camera.getRotation().getX(), -Startup.camera.getRotation().getY(), -Startup.camera.getRotation().getZ()), new Vector3f(0.5f, 0.5f, 0.5f), Vector4f.COLOR_BASE, false, false);
		}
	}

	@Override
	public void cleanUp() {
		StateManager.currentState.lights.remove(light);
		sparks = new Flare[0];
	}

}

class Flare {

	public Vector3f position;
	public Vector3f rotation;
	public Vector3f scale;
	public Vector3f direction;
	public Vector3f color;

	public float alpha = 1f;

	public float speed = 0.3f;
	public float speedMod = 0.005255f;

	public Flare(Vector3f position, Vector3f color) {
		this.position = Vector3f.add(position, 0, 0, 0);
		scale = new Vector3f(0.5f, 0.5f, 0.5f).mul(new Random().nextFloat() + 0.5f);
		rotation = new Vector3f(new Random().nextInt(360), new Random().nextInt(360), new Random().nextInt(360));
		direction = new Vector3f((new Random().nextFloat() - 0.5f) / 2, 0, (new Random().nextFloat() - 0.5f) / 2);
		this.color = color.copy();
	}

	public void tick() {
		rotation.add(new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat());
		position.add(direction);
		position.setY(position.getY() + speed);
		speed -= speedMod;
		if (alpha > 0) {
			alpha -= 0.009;
		}
	}

	public void render() {
		BatchSorter.add("pixel.dae", "pixel.png", "main_3d_bloom", Material.GLOW.toString(), Vector3f.div(position, scale), rotation, scale, new Vector4f(color.getX(), color.getY(), color.getZ(), alpha), false, false, false);
	}
}