package io.itch.deltabreaker.object.tile;

import io.itch.deltabreaker.effect.Effect;
import io.itch.deltabreaker.effect.EffectTorchFire;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.state.StateManager;

public class TileBrazier extends TileCompound {
	
	private Effect effect;
	
	public TileBrazier(TileProperty property, Vector3f position) {
		super(property, position);
		effect = new EffectTorchFire(Vector3f.add(Vector3f.mul(this.position, 0.5f, 0.5f, 0.5f), 0, -4, 0), new Vector3f(7.5f, 4.5f, 0f));
		StateManager.currentState.effects.add(effect);
	}
	
	public TileBrazier(TileProperty property, Vector3f position, Tile t) {
		super(property, position, t);
		effect = new EffectTorchFire(Vector3f.add(Vector3f.mul(this.position, 0.5f, 0.5f, 0.5f), 0, -4, 0), new Vector3f(5f, 2.5f, 0f));
		StateManager.currentState.effects.add(effect);
	}

	public void tick() {
		super.tick();
		effect.position.set(position.getX() * 0.5f, position.getY() * 0.5f, position.getZ() * 0.5f);
	}
	
	public void cleanUp() {
		effect.cleanUp();
		StateManager.currentState.effects.remove(effect);
	}
	
}
