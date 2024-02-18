package io.itch.deltabreaker.ui;

import java.util.ArrayList;

import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.tile.Tile;

public class Map {

	private ArrayList<MapPixel> pixels = new ArrayList<>();
	private Tile[][] tiles;
		
	public Map(Tile[][] tiles) {
		this.tiles = tiles;
		for (int x = 0; x < tiles.length; x++) {
			for (int y = 0; y < tiles[0].length; y++) {				
				pixels.add(new MapPixel(new Vector3f(x, y, -80), tiles[x][y].getAverageColor()));
			}
		}
	}

	public void tick() {
		for (int x = 0; x < tiles.length; x++) {
			for (int y = 0; y < tiles[0].length; y++) {
//				Vector3f tilePosition = Vector3f.div(tiles[x][y].getPosition(), 16);
//				
//				float sin = AdvMath.sin[(xRotation + 360) % 360];
//				float cos = AdvMath.sin[(xRotation + 360 + 90) % 360];
//				
//				pixels.get(x * tiles.length + y).position.set(x * rootOfTwo - y * rootOfTwo, y * rootOfTwo + x * rootOfTwo, -80);
//				pixels.get(x * tiles.length + y).rotation.set(0, 0, -45);
			}
		}
	}
	
	public void render() {
		ArrayList<Vector3f> positions = new ArrayList<>();
		ArrayList<Vector3f> rotations = new ArrayList<>();
		ArrayList<Vector3f> scales = new ArrayList<>();
		ArrayList<Vector4f> colors = new ArrayList<>();
		
		for(MapPixel p  : pixels) {
			positions.add(p.position);
			rotations.add(p.rotation);
			scales.add(Vector3f.SCALE_HALF.copy());
			colors.add(p.color);
		}
		
		BatchSorter.addBatch("pixel.dae", "pixel.png", "static_3d", Material.DEFAULT.toString(), positions, rotations, scales, colors, false, true);
	}
	
}

class MapPixel {

	public Vector3f position;
	public Vector3f rotation = new Vector3f(0, 0, 0);
	public Vector4f color;

	public MapPixel(Vector3f position, Vector4f color) {
		this.position = position;
		this.color = color;
	}

}