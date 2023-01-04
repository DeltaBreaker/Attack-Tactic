package io.itch.deltabreaker.object.tile;

import io.itch.deltabreaker.effect.Effect;
import io.itch.deltabreaker.effect.EffectCampfire;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.state.StateManager;

public class TileCampfire extends Tile {
	
	private Effect effect;
	
	public TileCampfire(TileProperty property, Vector3f position) {
		super(property, position);
		effect = new EffectCampfire(Vector3f.add(Vector3f.mul(this.position, 0.5f, 0.5f, 0.5f), 0, -6, 0));
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
