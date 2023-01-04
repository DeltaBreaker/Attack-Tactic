package io.itch.deltabreaker.object.tile;

import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.object.Unit;

public class TileHousingDoor extends TileHousing {

	private Tile door;

	public TileHousingDoor(TileProperty property, Vector3f position) {
		super(property, position);
		door = new TileDoorSeparate(Tile.getProperty(new String[] { property.tags[0], Tile.TAG_DOOR_CLOSED })[0], position);
	}

	@Override
	public boolean isSolid() {
		return door.isSolid();
	}

	@Override
	public void action(Unit u, String[] args) {
		door.action(u, args);
	}

	@Override
	public void render(boolean staticView) {
		super.render(staticView);
		door.render(staticView);
	}

}
