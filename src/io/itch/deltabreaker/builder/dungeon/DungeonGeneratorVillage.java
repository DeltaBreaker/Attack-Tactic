package io.itch.deltabreaker.builder.dungeon;

public class DungeonGeneratorVillage extends DungeonGenerator {

	public int width, height;
	public String palletTag;

	public DungeonGeneratorVillage(int width, int height, String palletTag) {
		super(palletTag, 0);

		this.width = width;
		this.height = height;
		this.palletTag = palletTag;
	}

	public DungeonGeneratorVillage(int width, int height, String palletTag, long seed) {
		super(palletTag, 0, seed);

		this.width = width;
		this.height = height;
		this.palletTag = palletTag;
	}

	@Override
	public int[][] generateMap() {
		long time = System.nanoTime();
		int[][] map = new int[width][height];

		for (int x = 0; x < map.length; x++) {
			for (int y = 0; y < map[0].length; y++) {
				map[x][y] = 0;
			}
		}

		System.out.println("[DungeonGeneration]: Map generated in: " + (int) ((System.nanoTime() - time) / 100.0) / 10000.0 + "ms");
		return map;
	}

	@Override
	public DungeonGenerator start() {
		try {
			tiles = convertMap(generateMap());
			System.out.println("[DungeonGeneration]: Dungeon generated");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

}
