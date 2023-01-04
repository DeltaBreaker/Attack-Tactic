package io.itch.deltabreaker.object.tile;

import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.math.Vector3f;

public class TileHousingRoof extends TileHousing {

	public TileHousingRoof(TileProperty property, Vector3f position) {
		super(property, position);
	}

	public TileHousingRoof(TileProperty property, Vector3f position, Tile t) {
		super(property, position, t);
	}

	@Override
	public void render(boolean staticView) {
		if (!containsTag(TAG_AIR)) {
			int xDist = (int) Math.abs(Startup.camera.position.getX() / 2 - position.getX()) / 16;
			int yDist = (int) Math.abs(Startup.camera.position.getZ() / 2 - position.getZ()) / 16;
			BatchSorter.add("z" + yDist + xDist, property.model, property.texture, (staticView) ? "main_3d" : property.shader, property.material.toString(), Vector3f.add(position, property.offset), rotation, scale, shade, false,
					staticView);
		}
		t.position = position;
		t.render(staticView);
	}

}
