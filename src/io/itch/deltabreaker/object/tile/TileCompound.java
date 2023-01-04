package io.itch.deltabreaker.object.tile;

import io.itch.deltabreaker.math.Vector3f;

public class TileCompound extends Tile {

	public Tile t;

	public TileCompound(TileProperty property, Vector3f position) {
		super(property, position);
		t = getTile(new String[] { property.tags[0], Tile.TAG_FLOOR_CENTER }, position);
	}

	public TileCompound(TileProperty property, Vector3f position, Tile t) {
		super(property, position);
		this.t = t;
	}

	public void tick() {
		super.tick();
		t.tick();
		t.status = status;
		t.range = range;
	}

	public void setPosition(float x, float y, float z) {
		position.set(x, y, z);
		updateMatrix();
		
		t.setPosition(x, y, z);
	}
	
	public void setPositionY(float y) {
		position.setY(y);
		updateMatrix();
		
		t.setPositionY(y);
	}

	public void translate(float x, float y, float z) {
		position.add(x, y, z);
		updateMatrix();
		
		t.translate(x, y, z);
	}
	
	public void render(boolean staticView) {
		super.render(staticView);
		t.render(staticView);
	}

}
