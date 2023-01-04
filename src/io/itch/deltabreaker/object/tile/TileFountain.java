package io.itch.deltabreaker.object.tile;

import io.itch.deltabreaker.effect.Effect;
import io.itch.deltabreaker.effect.EffectFountain;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.state.StateManager;

public class TileFountain extends TileRoadObject {

	private Effect effect;
	
	public TileFountain(TileProperty property, Vector3f position) {
		super(property, position);
		effect = new EffectFountain(this.position);
		StateManager.currentState.effects.add(effect);
	}

	@Override
	public void tick() {
		super.tick();
		effect.position.setY(position.getY() + 16);
	}
	
	@Override
	public void cleanUp() {
		super.cleanUp();
		StateManager.currentState.effects.remove(effect);
	}
	
}
