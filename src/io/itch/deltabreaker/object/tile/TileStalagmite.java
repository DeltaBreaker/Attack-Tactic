package io.itch.deltabreaker.object.tile;

import java.util.Random;

import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.math.Vector3f;

public class TileStalagmite extends TileCompound {
	
	public TileStalagmite(TileProperty property, Vector3f position) {
		super(property, position);
		t = getTile(new String[] { property.tags[0], Tile.TAG_FLOOR_CENTER }, position);
		rotate(0, new Random().nextInt(4) * 90, 0);
	}
	
	public void render(boolean staticView) {
		BatchSorter.add(property.model, property.texture, (staticView) ? "static_3d" : property.shader,
				property.material.toString(), Vector3f.add(position, property.offset), rotation, scale,
				shade, true, staticView);
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
	}
}
