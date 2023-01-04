package io.itch.deltabreaker.effect;

import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.ItemProperty;
import io.itch.deltabreaker.state.StateDungeon;
import io.itch.deltabreaker.state.StateManager;

public class EffectItemUse extends Effect {

	private Unit u;
	private Unit target;
	private ItemProperty item;
	private StateDungeon context;

	private int timer = 0;
	private int time = 144;

	public EffectItemUse(Unit target, ItemProperty item, StateDungeon context) {
		super(new Vector3f(context.selectedUnit.x, 20 + StateManager.currentState.tiles[context.selectedUnit.locX][context.selectedUnit.locY].getPosition().getY(), context.selectedUnit.y - 8), Vector3f.EMPTY, Vector3f.SCALE_HALF);
		u = context.selectedUnit;
		this.target = target;
		this.item = item;
		this.context = context;
		u.poseLock = true;
		u.dir = 0;
		u.frame = 2;
		context.effects.add(new EffectPoof(position));
		context.usingItem = true;
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
		u.frameTimer = 0;
		u.frame = 0;
		context.selectedItemUse.afterAnimation(target, context);
		context.effects.add(new EffectPoof(position));
		context.usingItem = false;
	}

}
