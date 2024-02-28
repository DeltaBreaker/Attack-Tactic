package io.itch.deltabreaker.ui;

import java.util.ArrayList;
import java.util.Random;

import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.Item;
import io.itch.deltabreaker.object.tile.Tile;
import io.itch.deltabreaker.state.StateDungeon;
import io.itch.deltabreaker.state.StateManager;

public class Map {

	private ArrayList<MapPixel> pixels = new ArrayList<>();
	private Tile[][] tiles;

	private boolean visible = false;
	private ArrayList<MapPixel> moveList = new ArrayList<>();
	private int time;
	private Random r = new Random();
	private int target = 120;
	private float alphaTarget = 0;
	private float alpha = 0;

	public Map(Tile[][] tiles) {
		this.tiles = tiles;
		Vector4f half = new Vector4f(0.5f, 0.5f, 0.5f, 1);
		Vector4f full = new Vector4f(1, 1, 1, 1);
		for (int x = 0; x < tiles.length; x++) {
			for (int y = 0; y < tiles[0].length; y++) {
				if (!tiles[x][y].containsTag(Tile.TAG_FILLER)) {
					pixels.add(new MapPixel(new Vector3f(x - tiles.length / 2, -y * 0.99f + tiles[0].length / 2, -80),
							Vector4f.mul(tiles[x][y].getAverageColor(), (tiles[x][y].isSolid()) ? half : full)));
				}
			}
		}
		time = pixels.size() / 72;
	}

	public void toggle() {
		visible = !visible;
		if (visible) {
			moveList.clear();
			for (MapPixel m : pixels) {
				target = 0;
				moveList.add(m);
			}
			alphaTarget = 1;
		} else {
			moveList.clear();
			for (MapPixel m : pixels) {
				target = 120;
				moveList.add(m);
			}
			alphaTarget = 0;
		}
	}

	public void tick() {
		for (int x = 0; x < Math.max(1, time); x++) {
			if (moveList.size() > 0) {
				int i = r.nextInt(moveList.size());
				moveList.get(i).target = (r.nextBoolean()) ? target : target * -1;
				moveList.remove(i);
			}
		}
		for (MapPixel m : pixels) {
			m.tick();
		}
		if(alpha > alphaTarget) {
			alpha -= (alpha - alphaTarget) * 0.02f;
		}
		if(alpha < alphaTarget) {
			alpha += (alphaTarget - alpha) * 0.02f;
		}
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

		if (StateManager.currentState.STATE_ID.equals(StateDungeon.STATE_ID)) {
			for (Unit u : StateDungeon.getCurrentContext().enemies) {
				positions.add(new Vector3f(u.locX - tiles.length / 2, -u.locY * 0.99f + tiles[0].length / 2, -79.75f));
				rotations.add(Vector3f.EMPTY);
				scales.add(Vector3f.SCALE_HALF);
				colors.add(Vector4f.COLOR_RED.copy().setW(alpha));
			}

			for (Unit u : Inventory.active) {
				positions.add(new Vector3f(u.locX - tiles.length / 2, -u.locY * 0.99f + tiles[0].length / 2, -79.75f));
				rotations.add(Vector3f.EMPTY);
				scales.add(Vector3f.SCALE_HALF);
				colors.add(Vector4f.COLOR_BLUE.copy().setW(alpha));
			}

			for (Item i : StateDungeon.getCurrentContext().items) {
				positions.add(new Vector3f(i.locX - tiles.length / 2, -i.locY * 0.99f + tiles[0].length / 2, -79.75f));
				rotations.add(Vector3f.EMPTY);
				scales.add(Vector3f.SCALE_HALF);
				colors.add(Vector4f.COLOR_GREEN.copy().setW(alpha));
			}
		}

		BatchSorter.addBatch("pixel.dae", "pixel.png", "static_3d", Material.MATTE.toString(), positions, rotations,
				scales, colors, false, true);
	}

}

class MapPixel {

	public Vector3f position;
	public Vector3f origin;
	public Vector3f rotation = new Vector3f(0, 0, 0);
	public float height = (new Random().nextBoolean()) ? 120 : -120;
	public float target = height;
	public Vector4f color;

	public MapPixel(Vector3f position, Vector4f color) {
		this.position = position;
		origin = position.copy();
		this.color = color;
	}

	public void tick() {
		if (height > target) {
			height -= (height - target) * 0.05f;
		}
		if (height < target) {
			height += (target - height) * 0.05f;
		}
		position.setX(origin.getX() + height);
	}

}