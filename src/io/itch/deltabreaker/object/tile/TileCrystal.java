package io.itch.deltabreaker.object.tile;

import java.util.Random;

import io.itch.deltabreaker.effect.Effect;
import io.itch.deltabreaker.effect.EffectBloom;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Light;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.state.StateManager;

public class TileCrystal extends Tile {
	
	private Tile base;
	private Light light;
	private Effect effect;
	
	public TileCrystal(TileProperty property, Vector3f position) {
		super(property, position);
		base = getTile(new String[] { property.tags[0], Tile.TAG_FLOOR_CENTER }, position);
		
		light = new Light(Vector3f.add(Vector3f.mul(this.position, 0.5f, 0.5f, 0.5f), 0, 10, 0), new Vector3f(1.5f, 0.75f, 0f), 1.5f, 1f, 0.1f, null);
		this.light.linear = 1.4f - (float) Math.sin(Math.toRadians(45.0f)) / 4.0f;
	    this.light.color = Vector3f.mul(new Vector3f(6f, 6f, 6f), new Vector3f(0.436f, 0.436f, 0.477f));
		StateManager.currentState.lights.add(light);
		effect = new EffectBloom(position.copy().add(0, 0.45f, -0.5f), Vector4f.COLOR_WATER.copy().setW(0.5f), 16);
		StateManager.currentState.effects.add(effect);
		
		rotation.add(0, new Random().nextInt(4) * 90, 0);
		shade.setW(0.25f);
	}
	
	public void tick() {
		light.position = Vector3f.add(Vector3f.mul(this.position, 0.5f, 0.5f, 0.5f), 0, 10, 0);
	}
	
	public void render(boolean staticView) {
		base.position = position;
		BatchSorter.add("i", property.model, property.texture, (staticView) ? "static_3d" : property.shader,
				property.material.toString(), Vector3f.add(position, property.offset), rotation, scale,
				shade, true, staticView);
		base.render(staticView);
	}
	
	public void cleanUp() {
		StateManager.currentState.lights.remove(light);
		effect.cleanUp();
		StateManager.currentState.effects.remove(effect);
	}
	
}
