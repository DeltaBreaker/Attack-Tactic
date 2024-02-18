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
		Vector4f half = new Vector4f(0.5f, 0.5f, 0.5f, 1);
		Vector4f full = new Vector4f(1, 1, 1, 1);
		for (int x = 0; x < tiles.length; x++) {
			for (int y = 0; y < tiles[0].length; y++) {
				if (!tiles[x][y].containsTag(Tile.TAG_FILLER)) {
					pixels.add(new MapPixel(new Vector3f(94 + x - tiles.length, 86 - y * 0.99f - tiles[0].length, -80), Vector4f.mul(tiles[x][y].getAverageColor(), (tiles[x][y].isSolid()) ? half : full)));
				}
			}
		}
	}

	public void tick() {
		
	}

	public void render() {
		ArrayList<Vector3f> positions = new ArrayList<>();
		ArrayList<Vector3f> rotations = new ArrayList<>();
		ArrayList<Vector3f> scales = new ArrayList<>();
		ArrayList<Vector4f> colors = new ArrayList<>();

		for (MapPixel p : pixels) {
			positions.add(p.position);
			rotations.add(p.rotation);
			scales.add(Vector3f.SCALE_HALF.copy());
			colors.add(p.color);
		}

		BatchSorter.addBatch("pixel.dae", "pixel.png", "static_3d", Material.MATTE.toString(), positions, rotations,
				scales, colors, false, true);
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