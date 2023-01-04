package io.itch.deltabreaker.object.tile;

import io.itch.deltabreaker.effect.Effect;
import io.itch.deltabreaker.effect.EffectTorchFire;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.state.StateManager;

public class TileTorch extends Tile {
	
	private Effect effect;
	
	public TileTorch(TileProperty property, Vector3f position) {
		super(property, position);
		effect = new EffectTorchFire(Vector3f.add(Vector3f.mul(this.position, 0.5f, 0.5f, 0.5f), 0, -6, 0), new Vector3f(10f, 5f, 0f));
		StateManager.currentState.effects.add(effect);
	}

	public void tick() {
		effect.position = Vector3f.add(Vector3f.mul(this.position, 0.5f, 0.5f, 0.5f), 0, -3, 0);
	}
	
	public void cleanUp() {
		effect.cleanUp();
		StateManager.currentState.effects.remove(effect);
	}
	
}
