package io.itch.deltabreaker.object.tile;

import io.itch.deltabreaker.math.Vector3f;

public class TileHousingFurnishing extends Tile {

	private Tile t;

	public TileHousingFurnishing(TileProperty property, Vector3f position) {
		super(property, position);
		t = getTile(new String[] { property.tags[0], Tile.TAG_HOUSE_ROOF }, position);
	}

	@Override
	public void tick() {
		super.tick();
		t.tick();
	}
	
	@Override
	public void render(boolean staticView) {
		super.render(staticView);
		t.position = position;
		t.render(staticView);
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

	public void setRotation(float x, float y, float z) {
		rotation.set(x, y, z);
		updateMatrix();
		
		t.setRotation(x, y, z);
	}

	public void rotate(float x, float y, float z) {
		rotation.add(x, y, z);
		updateMatrix();
		
		t.rotate(x, y, z);
	}
	
}