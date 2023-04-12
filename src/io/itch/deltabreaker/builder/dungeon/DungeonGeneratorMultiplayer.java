package io.itch.deltabreaker.builder.dungeon;

import java.awt.Point;

import io.itch.deltabreaker.object.tile.Tile;

public class DungeonGeneratorMultiplayer extends DungeonGenerator {

	public DungeonGeneratorMultiplayer(String pattern, int baseLevel, long seed) {
		super(pattern, baseLevel, seed);
	}

	public Point[] generatePlacements(int units) {
		Point[] placements = new Point[units * 2];
		
		for (int i = 0; i < units; i++) {
			int x = r.nextInt(startRoom.width);
			int y = r.nextInt(startRoom.height);
			boolean onUnit = false;
			for (Point p : placements) {
				if (p != null && p.x == startRoom.x + x && p.y == startRoom.y + y) {
					onUnit = true;
					break;
				}
			}
			while (tiles[startRoom.x + x][startRoom.y + y].isSolid() || onUnit) {
				x = r.nextInt(startRoom.width);
				y = r.nextInt(startRoom.height);
				onUnit = false;
				for (Point p : placements) {
					if (p != null && p.x == startRoom.x + x && p.y == startRoom.y + y) {
						onUnit = true;
						break;
					}
				}
			}
			placements[i] = new Point(startRoom.x + x, startRoom.y + y);
		}
		
		for (int i = 0; i < units; i++) {
			int x = r.nextInt(endRoom.width);
			int y = r.nextInt(endRoom.height);
			boolean onUnit = false;
			for (Point p : placements) {
				if (p != null && p.x == endRoom.x + x && p.y == endRoom.y + y) {
					onUnit = true;
					break;
				}
			}
			while (tiles[endRoom.x + x][endRoom.y + y].isSolid() || onUnit) {
				x = r.nextInt(endRoom.width);
				y = r.nextInt(endRoom.height);
				onUnit = false;
				for (Point p : placements) {
					if (p != null && p.x == endRoom.x + x && p.y == endRoom.y + y) {
						onUnit = true;
						break;
					}
				}
			}
			placements[placements.length / 2 + i] = new Point(endRoom.x + x, endRoom.y + y);
		}
		return placements;
	}
	
	public DungeonGenerator start() {
		long time = System.nanoTime();
		tiles = convertMap(generateMap());
		generateDecor();
		generateItems();
		for (Tile[] x : tiles) {
			for (Tile y : x) {
				y.updateMatrix();
			}
		}
		System.out.println("[DungeonGeneration]: Generation completed in: " + (int) ((System.nanoTime() - time) / 100.0) / 10000.0 + "ms");
		System.out.println("[DungeonGeneration]: Dungeon generated with seed: " + seed);
		return this;
	}
	
}
