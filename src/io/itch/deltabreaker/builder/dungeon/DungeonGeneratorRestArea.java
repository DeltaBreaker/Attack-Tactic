package io.itch.deltabreaker.builder.dungeon;

import com.flowpowered.noise.model.Plane;
import com.flowpowered.noise.module.source.Perlin;

import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.tile.Tile;

public class DungeonGeneratorRestArea extends DungeonGenerator {

	private static double radius = 4.25;

	public DungeonGeneratorRestArea(String pattern, int baseLevel, long seed) {
		super(pattern, baseLevel, seed);

		Perlin perlin = new Perlin();
		perlin.setSeed((int) seed + baseLevel);
		perlin.setFrequency(0);
		noise = new Plane(perlin);
	}

	@Override
	public int[][] generateMap() {
		long time = System.nanoTime();
		int[][] map = new int[25][25];

		for (int x = 0; x < map.length; x++) {
			for (int y = 0; y < map[0].length; y++) {
				double xCoord = x - map.length / 2.0 + 0.5;
				double yCoord = y - map[0].length / 2.0 + 0.5;
				if (xCoord * xCoord + yCoord * yCoord <= radius * radius) {
					map[x][y] = 1;
				} else {
					map[x][y] = 0;
				}
			}
		}

		System.out.println("[DungeonGeneration]: Map generated in: " + (int) ((System.nanoTime() - time) / 100.0) / 10000.0 + "ms");
		return map;
	}

	@Override
	public Tile[][] convertMap(int[][] map) {
		Tile[][] tiles = super.convertMap(map);

		tiles[12][12] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_CAMPFIRE },
				new Vector3f(12, (float) Math.round(noise.getValue(((float) 12 / (tiles.length + xActiveSpace)), ((float) 12 / (tiles[0].length + yActiveSpace))) * 10) / 4f, 12));
		
		tiles[12][14] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_CAMPFIRE },
				new Vector3f(12, (float) Math.round(noise.getValue(((float) 12 / (tiles.length + xActiveSpace)), ((float) 12 / (tiles[0].length + yActiveSpace))) * 10) / 4f, 12));

		return tiles;
	}

	@Override
	public void generateUnitPlacements() {
		for (Unit u : Inventory.active) {
			int x = 10 + r.nextInt(5);
			int y = 10 + r.nextInt(5);
			u.placeAt(x, y);
			boolean onUnit = false;
			for (Unit u2 : Inventory.active) {
				if (u != u2 && u.locX == u2.locX && u.locY == u2.locY) {
					onUnit = true;
					break;
				}
			}
			while (tiles[x][y].isSolid() || onUnit || (x == 12 && y == 12)) {
				onUnit = false;
				x = 10 + r.nextInt(5);
				y = 10 + r.nextInt(5);
				u.placeAt(x, y);
				for (Unit u2 : Inventory.active) {
					if (u != u2 && u.locX == u2.locX && u.locY == u2.locY) {
						onUnit = true;
						break;
					}
				}
			}
		}
	}

	@Override
	public DungeonGenerator start() {
		try {
			tiles = convertMap(generateMap());
			generateDecor();
			generateUnitPlacements();
			System.out.println("[DungeonGeneration]: Dungeon generated with seed: " + seed);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

}
