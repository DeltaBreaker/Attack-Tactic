package io.itch.deltabreaker.object.tile;

import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.state.StateManager;

public class TileHousingSave extends TileHousingFurnishing {

	public TileHousingSave(TileProperty property, Vector3f position) {
		super(property, position);
	}

	public void render(boolean staticView) {
		super.render(staticView);
		BatchSorter.add("gui_action.dae", "gui_action.png", "main_3d_nobloom_texcolor", Material.DEFAULT.toString(),
				Vector3f.add(position, 0, 30 + AdvMath.sin[(int) Startup.universalAge % 360] * 1.5f, -1), Inventory.units.get(0).rotation, Vector3f.SCALE_HALF,
				Vector4f.COLOR_BASE, false, false);
	}
	
	public void action(Unit u, String[] args) {
		if(StateManager.currentState.menus.size() == 0) {
			// Add save / load menu here
		}
	}
	
}
