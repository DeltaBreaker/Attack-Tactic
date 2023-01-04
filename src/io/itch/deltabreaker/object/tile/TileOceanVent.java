package io.itch.deltabreaker.object.tile;

import io.itch.deltabreaker.effect.Effect;
import io.itch.deltabreaker.effect.EffectOceanVent;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.state.StateManager;

public class TileOceanVent extends Tile {

	private Effect effect;
	
	public TileOceanVent(TileProperty property, Vector3f position) {
		super(property, position);
		effect = new EffectOceanVent(Vector3f.add(Vector3f.mul(this.position, 0.5f, 0.5f, 0.5f), 0, -6, 0));
		StateManager.currentState.effects.add(effect);
	}
	
	public void tick() {
		super.tick();
		effect.position = Vector3f.add(Vector3f.mul(this.position, 0.5f, 0.5f, 0.5f), 0, -6, 0);
	}
	
	public void cleanUp() {
		effect.cleanUp();
		StateManager.currentState.effects.remove(effect);
	}
	
}
