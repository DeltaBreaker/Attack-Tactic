package io.itch.deltabreaker.effect;

import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.ItemProperty;
import io.itch.deltabreaker.state.StateDungeon;
import io.itch.deltabreaker.state.StateManager;

public class EffectItemShow extends Effect {

	private Unit u;
	private ItemProperty item;

	private int timer = 0;
	private int time = 144;

	public EffectItemShow(Unit u, ItemProperty item) {
		super(new Vector3f(u.x, 20 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y - 8), Vector3f.EMPTY, Vector3f.SCALE_HALF);
		this.u = u;
		this.item = item;
		u.poseLock = true;
		u.dir = 0;
		u.frame = 2;
		StateManager.currentState.effects.add(new EffectPoof(position));
		StateDungeon.getCurrentContext().usingItem = true;
	}

	@Override
	public void tick() {
		if (timer < time) {
			timer++;
		} else {
			remove = true;
		}
	}

	@Override
	public void render() {
		BatchSorter.add(item.model, item.texture, "main_3d", item.material, position, Vector3f.DEFAULT_INVERSE_CAMERA_ROTATION, scale, Vector4f.COLOR_BASE, true, false);
	}

	@Override
	public void cleanUp() {
		u.poseLock = false;
		u.frame = 0;
		u.frameTimer = 0;
		StateManager.currentState.effects.add(new EffectPoof(position));
		StateDungeon.getCurrentContext().usingItem = false;
	}

}
