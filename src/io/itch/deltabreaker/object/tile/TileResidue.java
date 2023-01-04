package io.itch.deltabreaker.object.tile;

import java.util.Random;

import io.itch.deltabreaker.effect.Effect;
import io.itch.deltabreaker.effect.EffectMedialChunk;
import io.itch.deltabreaker.effect.EffectMedialResidue;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.state.StateManager;

public class TileResidue extends Tile {

	private Effect effect;

	public TileResidue(TileProperty property, Vector3f position) {
		super(property, position);
		effect = (new Random().nextFloat() > 0.35) ? new EffectMedialResidue(Vector3f.add(Vector3f.mul(this.position, 0.5f, 0.5f, 0.5f), 0, 16, 0)) : new EffectMedialChunk(Vector3f.mul(position, 16, 16, 16));
		StateManager.currentState.effects.add(effect);
	}

	public void tick() {
		super.tick();
		effect.position = Vector3f.add(Vector3f.mul(this.position, 0.5f, 0.5f, 0.5f), 0, 16, 0);
	}

	public void cleanUp() {
		effect.cleanUp();
		StateManager.currentState.effects.remove(effect);
	}

}
