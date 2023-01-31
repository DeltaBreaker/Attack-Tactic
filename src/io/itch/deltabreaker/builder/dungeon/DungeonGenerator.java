package io.itch.deltabreaker.builder.dungeon;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.flowpowered.noise.model.Plane;
import com.flowpowered.noise.module.source.Perlin;

import io.itch.deltabreaker.ai.AIType;
import io.itch.deltabreaker.core.FileManager;
import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.effect.EffectLava;
import io.itch.deltabreaker.effect.EffectWater;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.Item;
import io.itch.deltabreaker.object.item.ItemProperty;
import io.itch.deltabreaker.object.tile.Tile;
import io.itch.deltabreaker.object.tile.TileBrazier;
import io.itch.deltabreaker.object.tile.TileCompound;
import io.itch.deltabreaker.state.StateManager;

public class DungeonGenerator {

	// Used for decoration generation
	public static final String TAG_DECORATION_3X3 = "dungeon.decoration.3x3";
	public static final String TAG_DECORATION_FOLIAGE = "dungeon.decoration.foliage";
	public static final String TAG_DECORATION_ILLUMINATION_FLOOR = "dungeon.decoration.illumination.floor";
	public static final String TAG_DECORATION_ILLUMINATION_WALL = "dungeon.decoration.illumination.wall";
	public static final String TAG_DECORATION_WATER = "dungeon.decoration.water";
	public static final String TAG_DECORATION_DOOR = "dungeon.decoration.door";
	public static final String TAG_DECORATION_CHEST = "dungeon.decoration.chest";
	public static final String TAG_DECORATION_WALL = "dungeon.decoration.wall";

	public static final String TAG_ALT_ITEM_SURPLUS = "dungeon.alt.item.surplus";
	public static final String TAG_ALT_ENEMY_NONE = "dungeon.alt.enemy.none";
	public static final String TAG_ALT_ENEMY_HEALTH_REDUCE = "dungeon.alt.enemy.health.reduce";

	// Used to apply in-game effects and set certain objects, such as the overhead
	// light
	public static final String TAG_LIGHT_MAIN = "dungeon.light.main";
	public static final String TAG_LIGHT_OFFSET = "dungeon.light.offset";
	public static final String TAG_EFFECT_SNOW = "dungeon.effect.snow";
	public static final String TAG_EFFECT_LAVA = "dungeon.effect.lava";
	public static final String TAG_EFFECT_RAIN = "dungeon.effect.rain";
	public static final String TAG_EFFECT_CORRUPTION = "dungeon.effect.corruption";
	public static final String TAG_EFFECT_RESIDUE = "dungeon.effect.residue";
	public static final String TAG_EFFECT_HEAT = "dungeon.effect.heat";

	// Used to change attributes of enemies
	public static final String TAG_ENEMY_TYPE = "dungeon.enemy.type";

	public static HashMap<String, GenerationPattern> patterns = new HashMap<>();

	public static int xActiveSpace = 43;
	public static int yActiveSpace = 23;

	// Variables
	public final long seed;
	public int baseLevel;

	// Private resources
	protected GenerationPattern pattern;
	protected Random r = new Random();
	protected ArrayList<Rectangle> rooms = new ArrayList<>();
	private int w, h;
	private int level;

	// Public results
	public Tile[][] tiles;
	public Plane noise;
	public Rectangle startRoom;
	public Rectangle endRoom;
	public ArrayList<Item> items = new ArrayList<>();
	public ArrayList<Unit> enemyPlacements = new ArrayList<>();

	public DungeonGenerator(String pattern, int baseLevel, long seed) {
		this.pattern = patterns.get(pattern);
		this.seed = seed;
		this.baseLevel = baseLevel;
		level = (int) (baseLevel * this.pattern.levelScaler);
		r.setSeed(seed + baseLevel);

		Perlin perlin = new Perlin();
		perlin.setSeed((int) seed + baseLevel);
		perlin.setPersistence(this.pattern.perlinPersistance);
		noise = new Plane(perlin);
	}

	public DungeonGenerator(String pattern, int baseLevel) {
		this.pattern = patterns.get(pattern);
		this.baseLevel = baseLevel;
		level = (int) (baseLevel * this.pattern.levelScaler);
		seed = new Random().nextLong();
		r.setSeed(seed + baseLevel);

		Perlin perlin = new Perlin();
		perlin.setSeed((int) seed + baseLevel);
		perlin.setPersistence(this.pattern.perlinPersistance);
		noise = new Plane(perlin);
	}

	public int[][] generateMap() {
		long time = System.nanoTime();
		w = pattern.baseWorldSize + (int) ((double) level * pattern.worldSizeScaler);
		h = pattern.baseWorldSize + (int) ((double) level * pattern.worldSizeScaler);
		int[][] map = new int[w][h];

		int tries = 0;
		while (tries < pattern.testLimit) {
			int width = pattern.roomSizeMin + r.nextInt(pattern.roomSizeRandom);
			int height = pattern.roomSizeMin + r.nextInt(pattern.roomSizeRandom);
			while (Math.floorMod(width, 2) == 0) {
				width = pattern.roomSizeMin + r.nextInt(pattern.roomSizeRandom);
			}
			while (Math.floorMod(height, 2) == 0) {
				height = pattern.roomSizeMin + r.nextInt(pattern.roomSizeRandom);
			}

			int x = r.nextInt(map.length - width);
			int y = r.nextInt(map[0].length - height);
			while (Math.floorMod(x, 2) == 0) {
				x = r.nextInt(map.length - width);
			}
			while (Math.floorMod(y, 2) == 0) {
				y = r.nextInt(map[0].length - height);
			}

			Rectangle room = new Rectangle(x, y, width, height);

			boolean valid = false;
			while (!valid && tries < pattern.testLimit) {
				valid = true;
				for (Rectangle roomCheck : rooms) {
					if (room.intersects(roomCheck)) {
						valid = false;
						tries++;
						width = pattern.roomSizeMin + r.nextInt(pattern.roomSizeRandom);
						height = pattern.roomSizeMin + r.nextInt(pattern.roomSizeRandom);

						while (Math.floorMod(width, 2) == 0) {
							width = pattern.roomSizeMin + r.nextInt(pattern.roomSizeRandom);
						}
						while (Math.floorMod(height, 2) == 0) {
							height = pattern.roomSizeMin + r.nextInt(pattern.roomSizeRandom);
						}

						x = r.nextInt(map.length - width);
						y = r.nextInt(map[0].length - height);
						while (Math.floorMod(x, 2) == 0) {
							x = r.nextInt(map.length - width);
						}
						while (Math.floorMod(y, 2) == 0) {
							y = r.nextInt(map[0].length - height);
						}
						room.setBounds(x, y, width, height);
					}
				}
			}
			if (valid) {
				rooms.add(room);
			}
		}

		// Start and end can't be the same room
		int sr = r.nextInt(rooms.size());
		int er = r.nextInt(rooms.size());
		while (sr == er) {
			er = r.nextInt(rooms.size());
		}

		startRoom = rooms.get(sr);
		endRoom = rooms.get(er);

		for (Rectangle room : rooms) {
			for (int x = 0; x < room.width; x++) {
				for (int y = 0; y < room.height; y++) {
					map[room.x + x][room.y + y] = 1;
				}
			}
		}

		// Draws a path from each room to another random room
		for (int i = 0; i < rooms.size(); i++) {
			int startX = rooms.get(i).x + r.nextInt(rooms.get(i).width - 1);
			int startY = rooms.get(i).y + r.nextInt(rooms.get(i).height - 1);
			while (Math.floorMod(startX, 2) == 0 || Math.floorMod(startY, 2) == 0) {
				startX = rooms.get(i).x + r.nextInt(rooms.get(i).width - 1);
				startY = rooms.get(i).y + r.nextInt(rooms.get(i).height - 1);
			}
			int target = r.nextInt(rooms.size());
			while (target == i) {
				target = r.nextInt(rooms.size());
			}
			int endX = rooms.get(target).x + r.nextInt(rooms.get(target).width - 1);
			int endY = rooms.get(target).y + r.nextInt(rooms.get(target).height - 1);
			while (Math.floorMod(endX, 2) == 0 || Math.floorMod(endY, 2) == 0) {
				endX = rooms.get(target).x + r.nextInt(rooms.get(target).width - 1);
				endY = rooms.get(target).y + r.nextInt(rooms.get(target).height - 1);
			}

			boolean horizontal = r.nextBoolean();
			while (startX != endX || startY != endY) {
				boolean add = true;

				if (Math.floorMod(startX, 2) == 1 && Math.floorMod(startY, 2) == 1) {
					horizontal = r.nextBoolean();
				}
				if (horizontal) {
					if (startX < endX) {
						startX++;
					} else if (startX > endX) {
						startX--;
					}
				} else {
					if (startY < endY) {
						startY++;
					} else if (startY > endY) {
						startY--;
					}
				}

				if (add) {
					map[startX][startY] = 1;
				}
			}
		}

		// Create path from unit to stairs
		int startX = startRoom.x + r.nextInt(startRoom.width - 1);
		int startY = startRoom.y + r.nextInt(startRoom.height - 1);
		while (Math.floorMod(startX, 2) == 0 || Math.floorMod(startY, 2) == 0) {
			startX = startRoom.x + r.nextInt(startRoom.width - 1);
			startY = startRoom.y + r.nextInt(startRoom.height - 1);
		}
		int endX = endRoom.x + r.nextInt(endRoom.width - 1);
		int endY = endRoom.y + r.nextInt(endRoom.height - 1);
		while (Math.floorMod(endX, 2) == 0 || Math.floorMod(endY, 2) == 0) {
			endX = endRoom.x + r.nextInt(endRoom.width - 1);
			endY = endRoom.y + r.nextInt(endRoom.height - 1);
		}

		boolean horizontal = r.nextBoolean();
		while (startX != endX || startY != endY) {
			boolean add = true;

			if (Math.floorMod(startX, 2) == 1 && Math.floorMod(startY, 2) == 1) {
				horizontal = r.nextBoolean();
			}
			if (horizontal) {
				if (startX < endX) {
					startX++;
				} else if (startX > endX) {
					startX--;
				}
			} else {
				if (startY < endY) {
					startY++;
				} else if (startY > endY) {
					startY--;
				}
			}

			if (add) {
				map[startX][startY] = 1;
			}
		}

		System.out.println("[DungeonGeneration]: Map generated in: " + (int) ((System.nanoTime() - time) / 100.0) / 10000.0 + "ms");
		return map;
	}

	// Converts the int array into the correct tiles based on the positioning
	// relative to other tiles
	public Tile[][] convertMap(int[][] heightmap) {
		long time = System.nanoTime();
		Tile[][] tiles = new Tile[heightmap.length][heightmap[0].length];

		for (int x = 0; x < heightmap.length; x++) {
			for (int y = 0; y < heightmap[0].length; y++) {
				if (heightmap[x][y] >= 0.5) {
					tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_FLOOR_CENTER },
							new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
				} else {
					tiles[x][y] = Tile.getTile(new String[] { Tile.TAG_AIR }, new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
				}
			}
		}

		for (int x = 0; x < tiles.length; x++) {
			for (int y = 0; y < tiles[0].length; y++) {
				if (tiles[x][y].containsTag(Tile.TAG_AIR)) {
					if (y > 0) {
						if (tiles[x][y - 1].containsTag(Tile.TAG_FLOOR_CENTER)) {
							tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_WALL_HORIZONTAL },
									new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
							if (x > 0) {
								if (tiles[x - 1][y].containsTag(Tile.TAG_FLOOR_CENTER)) {
									tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_WALL_TURN_TOP_LEFT },
											new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
								}
							}
							if (x < tiles.length - 1) {
								if (tiles[x + 1][y].containsTag(Tile.TAG_FLOOR_CENTER)) {
									tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_WALL_TURN_TOP_RIGHT },
											new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
								}
							}
						}
					}
					if (y < tiles[0].length - 1) {
						if (tiles[x][y + 1].containsTag(Tile.TAG_FLOOR_CENTER)) {
							tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_WALL_HORIZONTAL },
									new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
							if (x > 0) {
								if (tiles[x - 1][y].containsTag(Tile.TAG_FLOOR_CENTER)) {
									tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_WALL_TURN_BOTTOM_LEFT },
											new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
								}
							}
							if (x < tiles.length - 1) {
								if (tiles[x + 1][y].containsTag(Tile.TAG_FLOOR_CENTER)) {
									tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_WALL_TURN_BOTTOM_RIGHT },
											new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
								}
							}
						}
					}
				}
				if (tiles[x][y].containsTag(Tile.TAG_AIR)) {
					if (x < tiles.length - 1) {
						if (tiles[x + 1][y].containsTag(Tile.TAG_FLOOR_CENTER)) {
							tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_WALL_VERTICAL },
									new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
						}
					}
					if (x > 0) {
						if (tiles[x - 1][y].containsTag(Tile.TAG_FLOOR_CENTER)) {
							tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_WALL_VERTICAL },
									new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
						}
					}
				}
			}
		}

		for (int x = 0; x < tiles.length; x++) {
			for (int y = 0; y < tiles[0].length; y++) {
				if (tiles[x][y].containsTag(Tile.TAG_AIR)) {
					if (x > 0 && y < tiles[0].length - 1) {
						if ((tiles[x - 1][y].containsTag(Tile.TAG_WALL_TURN_TOP_LEFT) || tiles[x - 1][y].containsTag(Tile.TAG_WALL_HORIZONTAL) || tiles[x - 1][y].containsTag(Tile.TAG_WALL_TURN_BOTTOM_LEFT))
								&& (tiles[x][y + 1].containsTag(Tile.TAG_WALL_TURN_BOTTOM_LEFT) || tiles[x][y + 1].containsTag(Tile.TAG_WALL_VERTICAL) || tiles[x][y + 1].containsTag(Tile.TAG_WALL_TURN_BOTTOM_RIGHT))) {
							tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_WALL_TURN_TOP_RIGHT },
									new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
						}
					}
					if (x > 0 && y > 0) {
						if ((tiles[x - 1][y].containsTag(Tile.TAG_WALL_TURN_TOP_LEFT) || tiles[x - 1][y].containsTag(Tile.TAG_WALL_HORIZONTAL) || tiles[x - 1][y].containsTag(Tile.TAG_WALL_TURN_BOTTOM_LEFT))
								&& (tiles[x][y - 1].containsTag(Tile.TAG_WALL_TURN_TOP_LEFT) || tiles[x][y - 1].containsTag(Tile.TAG_WALL_TURN_TOP_RIGHT) || tiles[x][y - 1].containsTag(Tile.TAG_WALL_VERTICAL))) {
							tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_WALL_TURN_BOTTOM_RIGHT },
									new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
						}
					}
					if (x < tiles.length - 1 && y < tiles[0].length - 1) {
						if ((tiles[x + 1][y].containsTag(Tile.TAG_WALL_TURN_TOP_RIGHT) || tiles[x + 1][y].containsTag(Tile.TAG_WALL_HORIZONTAL) || tiles[x + 1][y].containsTag(Tile.TAG_WALL_TURN_BOTTOM_RIGHT))
								&& (tiles[x][y + 1].containsTag(Tile.TAG_WALL_TURN_BOTTOM_LEFT) || tiles[x][y + 1].containsTag(Tile.TAG_WALL_VERTICAL) || tiles[x][y + 1].containsTag(Tile.TAG_WALL_TURN_BOTTOM_RIGHT))) {
							tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_WALL_TURN_TOP_LEFT },
									new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
						}
					}
					if (x < tiles.length - 1 && y > 0) {
						if ((tiles[x + 1][y].containsTag(Tile.TAG_WALL_TURN_TOP_RIGHT) || tiles[x + 1][y].containsTag(Tile.TAG_WALL_HORIZONTAL) || tiles[x + 1][y].containsTag(Tile.TAG_WALL_TURN_BOTTOM_RIGHT))
								&& (tiles[x][y - 1].containsTag(Tile.TAG_WALL_TURN_TOP_LEFT) || tiles[x][y - 1].containsTag(Tile.TAG_WALL_TURN_TOP_RIGHT) || tiles[x][y - 1].containsTag(Tile.TAG_WALL_VERTICAL))) {
							tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_WALL_TURN_BOTTOM_LEFT },
									new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
						}
					}
				}
			}
		}

		for (int x = 0; x < tiles.length; x++) {
			for (int y = 0; y < tiles[0].length; y++) {
				if (x > 0 && y > 0 && y < tiles[0].length - 1) {
					if ((tiles[x][y - 1].containsTag(Tile.TAG_WALL_TURN_TOP_LEFT) || tiles[x][y - 1].containsTag(Tile.TAG_WALL_TURN_TOP_RIGHT) || tiles[x][y - 1].containsTag(Tile.TAG_WALL_VERTICAL)
							|| tiles[x][y - 1].containsTag(Tile.TAG_WALL_JUNCTION_LEFT) || tiles[x][y - 1].containsTag(Tile.TAG_WALL_JUNCTION_RIGHT))
							&& (tiles[x][y + 1].containsTag(Tile.TAG_WALL_VERTICAL) || tiles[x][y + 1].containsTag(Tile.TAG_WALL_TURN_BOTTOM_LEFT) || tiles[x][y + 1].containsTag(Tile.TAG_WALL_TURN_BOTTOM_RIGHT)
									|| tiles[x][y + 1].containsTag(Tile.TAG_WALL_JUNCTION_LEFT) || tiles[x][y + 1].containsTag(Tile.TAG_WALL_JUNCTION_RIGHT))
							&& (tiles[x - 1][y].containsTag(Tile.TAG_WALL_TURN_TOP_LEFT) || tiles[x - 1][y].containsTag(Tile.TAG_WALL_HORIZONTAL) || tiles[x - 1][y].containsTag(Tile.TAG_WALL_TURN_BOTTOM_LEFT)
									|| tiles[x - 1][y].containsTag(Tile.TAG_WALL_JUNCTION_TOP) || tiles[x - 1][y].containsTag(Tile.TAG_WALL_JUNCTION_BOTTOM))) {
						tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_WALL_JUNCTION_RIGHT },
								new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
					}
				}
				if (x < tiles.length - 1 && y > 0 && y < tiles[0].length - 1) {
					if ((tiles[x][y - 1].containsTag(Tile.TAG_WALL_TURN_TOP_LEFT) || tiles[x][y - 1].containsTag(Tile.TAG_WALL_TURN_TOP_RIGHT) || tiles[x][y - 1].containsTag(Tile.TAG_WALL_VERTICAL)
							|| tiles[x][y - 1].containsTag(Tile.TAG_WALL_JUNCTION_LEFT) || tiles[x][y - 1].containsTag(Tile.TAG_WALL_JUNCTION_RIGHT))
							&& (tiles[x][y + 1].containsTag(Tile.TAG_WALL_VERTICAL) || tiles[x][y + 1].containsTag(Tile.TAG_WALL_TURN_BOTTOM_LEFT) || tiles[x][y + 1].containsTag(Tile.TAG_WALL_TURN_BOTTOM_RIGHT)
									|| tiles[x][y + 1].containsTag(Tile.TAG_WALL_JUNCTION_LEFT) || tiles[x][y + 1].containsTag(Tile.TAG_WALL_JUNCTION_RIGHT))
							&& (tiles[x + 1][y].containsTag(Tile.TAG_WALL_HORIZONTAL) || tiles[x + 1][y].containsTag(Tile.TAG_WALL_TURN_TOP_RIGHT) || tiles[x + 1][y].containsTag(Tile.TAG_WALL_TURN_BOTTOM_RIGHT)
									|| tiles[x + 1][y].containsTag(Tile.TAG_WALL_JUNCTION_TOP) || tiles[x + 1][y].containsTag(Tile.TAG_WALL_JUNCTION_BOTTOM))) {
						tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_WALL_JUNCTION_LEFT },
								new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
					}
				}
				if (x > 0 && x < tiles.length - 1 && y < tiles[0].length - 1) {
					if ((tiles[x][y + 1].containsTag(Tile.TAG_WALL_VERTICAL) || tiles[x][y + 1].containsTag(Tile.TAG_WALL_TURN_BOTTOM_LEFT) || tiles[x][y + 1].containsTag(Tile.TAG_WALL_TURN_BOTTOM_RIGHT)
							|| tiles[x][y + 1].containsTag(Tile.TAG_WALL_JUNCTION_LEFT) || tiles[x][y + 1].containsTag(Tile.TAG_WALL_JUNCTION_RIGHT))
							&& (tiles[x - 1][y].containsTag(Tile.TAG_WALL_TURN_TOP_LEFT) || tiles[x - 1][y].containsTag(Tile.TAG_WALL_HORIZONTAL) || tiles[x - 1][y].containsTag(Tile.TAG_WALL_TURN_BOTTOM_LEFT)
									|| tiles[x - 1][y].containsTag(Tile.TAG_WALL_JUNCTION_TOP) || tiles[x - 1][y].containsTag(Tile.TAG_WALL_JUNCTION_BOTTOM))
							&& (tiles[x + 1][y].containsTag(Tile.TAG_WALL_HORIZONTAL) || tiles[x + 1][y].containsTag(Tile.TAG_WALL_TURN_TOP_RIGHT) || tiles[x + 1][y].containsTag(Tile.TAG_WALL_TURN_BOTTOM_RIGHT)
									|| tiles[x + 1][y].containsTag(Tile.TAG_WALL_JUNCTION_TOP) || tiles[x + 1][y].containsTag(Tile.TAG_WALL_JUNCTION_BOTTOM))) {
						tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_WALL_JUNCTION_TOP },
								new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
					}
				}
				if (x > 0 && x < tiles.length - 1 && y > 0) {
					if ((tiles[x][y - 1].containsTag(Tile.TAG_WALL_TURN_TOP_LEFT) || tiles[x][y - 1].containsTag(Tile.TAG_WALL_TURN_TOP_RIGHT) || tiles[x][y - 1].containsTag(Tile.TAG_WALL_VERTICAL)
							|| tiles[x][y - 1].containsTag(Tile.TAG_WALL_JUNCTION_LEFT) || tiles[x][y - 1].containsTag(Tile.TAG_WALL_JUNCTION_RIGHT))
							&& (tiles[x - 1][y].containsTag(Tile.TAG_WALL_TURN_TOP_LEFT) || tiles[x - 1][y].containsTag(Tile.TAG_WALL_HORIZONTAL) || tiles[x - 1][y].containsTag(Tile.TAG_WALL_TURN_BOTTOM_LEFT)
									|| tiles[x - 1][y].containsTag(Tile.TAG_WALL_JUNCTION_TOP) || tiles[x - 1][y].containsTag(Tile.TAG_WALL_JUNCTION_BOTTOM))
							&& (tiles[x + 1][y].containsTag(Tile.TAG_WALL_HORIZONTAL) || tiles[x + 1][y].containsTag(Tile.TAG_WALL_TURN_TOP_RIGHT) || tiles[x + 1][y].containsTag(Tile.TAG_WALL_TURN_BOTTOM_RIGHT)
									|| tiles[x + 1][y].containsTag(Tile.TAG_WALL_JUNCTION_TOP) || tiles[x + 1][y].containsTag(Tile.TAG_WALL_JUNCTION_BOTTOM))) {
						tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_WALL_JUNCTION_BOTTOM },
								new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
					}
				}
				if (x > 0 && x < tiles.length - 1 && y > 0 && y < tiles[0].length - 1) {
					if ((tiles[x][y - 1].containsTag(Tile.TAG_WALL_TURN_TOP_LEFT) || tiles[x][y - 1].containsTag(Tile.TAG_WALL_TURN_TOP_RIGHT) || tiles[x][y - 1].containsTag(Tile.TAG_WALL_VERTICAL)
							|| tiles[x][y - 1].containsTag(Tile.TAG_WALL_JUNCTION_LEFT) || tiles[x][y - 1].containsTag(Tile.TAG_WALL_JUNCTION_RIGHT))
							&& (tiles[x][y + 1].containsTag(Tile.TAG_WALL_VERTICAL) || tiles[x][y + 1].containsTag(Tile.TAG_WALL_TURN_BOTTOM_LEFT) || tiles[x][y + 1].containsTag(Tile.TAG_WALL_TURN_BOTTOM_RIGHT)
									|| tiles[x][y + 1].containsTag(Tile.TAG_WALL_JUNCTION_LEFT) || tiles[x][y + 1].containsTag(Tile.TAG_WALL_JUNCTION_RIGHT))
							&& (tiles[x - 1][y].containsTag(Tile.TAG_WALL_TURN_TOP_LEFT) || tiles[x - 1][y].containsTag(Tile.TAG_WALL_HORIZONTAL) || tiles[x - 1][y].containsTag(Tile.TAG_WALL_TURN_BOTTOM_LEFT)
									|| tiles[x - 1][y].containsTag(Tile.TAG_WALL_JUNCTION_TOP) || tiles[x - 1][y].containsTag(Tile.TAG_WALL_JUNCTION_BOTTOM))
							&& (tiles[x + 1][y].containsTag(Tile.TAG_WALL_HORIZONTAL) || tiles[x + 1][y].containsTag(Tile.TAG_WALL_TURN_TOP_RIGHT) || tiles[x + 1][y].containsTag(Tile.TAG_WALL_TURN_BOTTOM_RIGHT)
									|| tiles[x + 1][y].containsTag(Tile.TAG_WALL_JUNCTION_TOP) || tiles[x + 1][y].containsTag(Tile.TAG_WALL_JUNCTION_BOTTOM))) {
						tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_WALL_CROSS },
								new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
					}
				}
			}
		}

		for (int x = 0; x < tiles.length; x++) {
			for (int y = 0; y < tiles[0].length; y++) {
				if (!tiles[x][y].containsTag(Tile.TAG_FLOOR_CENTER)) {
					if (x > 0 && x < tiles.length - 1) {
						if (tiles[x - 1][y].containsTag(Tile.TAG_FLOOR_CENTER) && tiles[x + 1][y].containsTag(Tile.TAG_FLOOR_CENTER)) {
							if (y > 0) {
								if (tiles[x][y - 1].containsTag(Tile.TAG_FLOOR_CENTER)) {
									tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_WALL_CAP_TOP },
											new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
								}
							}
							if (y < tiles[0].length - 1) {
								if (tiles[x][y + 1].containsTag(Tile.TAG_FLOOR_CENTER)) {
									tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_WALL_CAP_BOTTOM },
											new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
								}
							}
						}
					}
					if (y > 0 && y < tiles[0].length - 1) {
						if (tiles[x][y - 1].containsTag(Tile.TAG_FLOOR_CENTER) && tiles[x][y + 1].containsTag(Tile.TAG_FLOOR_CENTER)) {
							if (x < tiles.length - 1) {
								if (tiles[x + 1][y].containsTag(Tile.TAG_FLOOR_CENTER)) {
									tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_WALL_CAP_RIGHT },
											new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
								}
							}
							if (x > 0) {
								if (tiles[x - 1][y].containsTag(Tile.TAG_FLOOR_CENTER)) {
									tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_WALL_CAP_LEFT },
											new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
								}
							}
						}
					}
					if (x > 0 && x < tiles.length - 1 && y > 0 && y < tiles[0].length - 1) {
						if (tiles[x][y - 1].containsTag(Tile.TAG_FLOOR_CENTER) && tiles[x][y + 1].containsTag(Tile.TAG_FLOOR_CENTER) && tiles[x - 1][y].containsTag(Tile.TAG_FLOOR_CENTER)
								&& tiles[x + 1][y].containsTag(Tile.TAG_FLOOR_CENTER)) {
							tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_WALL_STANDALONE },
									new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
						}
					}
				}
			}
		}

		for (int x = 1; x < tiles.length - 1; x++) {
			for (int y = 1; y < tiles[0].length - 1; y++) {
				if (tiles[x][y].containsTag(Tile.TAG_FLOOR_CENTER)) {
					if (tiles[x - 1][y].isSolid() && !tiles[x + 1][y].isSolid()) {
						tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_FLOOR_MIDDLE_LEFT },
								new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
					}
					if (tiles[x + 1][y].isSolid() && !tiles[x - 1][y].isSolid()) {
						tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_FLOOR_MIDDLE_RIGHT },
								new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
					}
					if (tiles[x][y - 1].isSolid() && !tiles[x][y + 1].isSolid()) {
						tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_FLOOR_TOP_MIDDLE },
								new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
					}
					if (tiles[x][y + 1].isSolid() && !tiles[x][y - 1].isSolid()) {
						tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_FLOOR_BOTTOM_MIDDLE },
								new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
					}
					if (tiles[x - 1][y].isSolid() && tiles[x + 1][y].isSolid()) {
						tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_FLOOR_HALLWAY_VERTICAL },
								new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
					}
					if (tiles[x][y - 1].isSolid() && tiles[x][y + 1].isSolid()) {
						tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_FLOOR_HALLWAY_HORIZONTAL },
								new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
					}
					if (tiles[x - 1][y].isSolid() && tiles[x][y + 1].isSolid()) {
						tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_FLOOR_BOTTOM_LEFT },
								new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
					}
					if (tiles[x + 1][y].isSolid() && tiles[x][y + 1].isSolid()) {
						tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_FLOOR_BOTTOM_RIGHT },
								new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
					}
					if (tiles[x - 1][y].isSolid() && tiles[x][y - 1].isSolid()) {
						tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_FLOOR_TOP_LEFT },
								new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
					}
					if (tiles[x + 1][y].isSolid() && tiles[x][y - 1].isSolid()) {
						tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_FLOOR_TOP_RIGHT },
								new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
					}
				}
			}
		}

		for (int x = 0; x < tiles.length; x++) {
			for (int y = 0; y < tiles[0].length; y++) {
				if (tiles[x][y].containsTag(Tile.TAG_AIR)) {
					tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_FILLER },
							new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
				}
			}
		}

		System.out.println("[DungeonGeneration]: Map converted in: " + (int) ((System.nanoTime() - time) / 100.0) / 10000.0 + "ms");
		return tiles;

	}

	public void generateUnitPlacements() {
		for (Unit unit : Inventory.active) {
			int x = r.nextInt(startRoom.width);
			int y = r.nextInt(startRoom.height);
			boolean onUnit = false;
			for (Unit u : Inventory.active) {
				if (u.locX == startRoom.x + x && u.locY == startRoom.y + y) {
					onUnit = true;
					break;
				}
			}
			while (tiles[startRoom.x + x][startRoom.y + y].isSolid() || onUnit) {
				x = r.nextInt(startRoom.width);
				y = r.nextInt(startRoom.height);
				onUnit = false;
				for (Unit u : Inventory.active) {
					if (u.locX == startRoom.x + x && u.locY == startRoom.y + y) {
						onUnit = true;
						break;
					}
				}
			}
			unit.placeAt(startRoom.x + x, startRoom.y + y);
		}
	}

	public void generateEnemies() {
		int rooms = (int) (this.rooms.size() / pattern.enemyRoomCountDevisor) + r.nextInt((int) (this.rooms.size() / pattern.enemyRoomCountRandomDevisor));

		// Keeps track of rooms that already have units and checks to make sure there
		// are no duplicates
		ArrayList<Integer> usedRooms = new ArrayList<>();
		for (int i = 0; i < rooms; i++) {
			int room = r.nextInt(this.rooms.size());
			while (usedRooms.contains(room) || this.rooms.get(room) == startRoom) {
				room = r.nextInt(this.rooms.size());
			}
			usedRooms.add(room);

			// Determines the amount of enemies per room and makes sure their positions are
			// valid (no overlap, no solid tiles)
			int enemies = 1 + pattern.enemyCountCertain + r.nextInt(pattern.enemyCountRandom);
			for (int e = 0; e < enemies; e++) {

				int x = r.nextInt(this.rooms.get(room).width);
				int y = r.nextInt(this.rooms.get(room).height);
				boolean onUnit = false;

				for (Unit u : enemyPlacements) {
					if (u.locX == this.rooms.get(room).x + x && u.locY == this.rooms.get(room).y + y) {
						onUnit = true;
						break;
					}
				}
				for (Unit u : Inventory.active) {
					if (u.locX == this.rooms.get(room).x + x && u.locY == this.rooms.get(room).y + y) {
						onUnit = true;
						break;
					}
				}

				while (tiles[this.rooms.get(room).x + x][this.rooms.get(room).y + y].isSolid() || onUnit) {
					x = r.nextInt(this.rooms.get(room).width);
					y = r.nextInt(this.rooms.get(room).height);
					onUnit = false;
					for (Unit u : enemyPlacements) {
						if (u.locX == this.rooms.get(room).x + x && u.locY == this.rooms.get(room).y + y) {
							onUnit = true;
							break;
						}
					}
					for (Unit u : Inventory.active) {
						if (u.locX == this.rooms.get(room).x + x && u.locY == this.rooms.get(room).y + y) {
							onUnit = true;
							break;
						}
					}
				}

				// Chooses an AI type based on given values
				AIType[] types = AIType.values();
				int type = r.nextInt(types.length);
				float value = r.nextFloat();
				while (value > pattern.aiRates[type]) {
					type = r.nextInt(types.length);
					value = r.nextFloat();
				}
				enemyPlacements.add(Unit.randomCombatUnit(this.rooms.get(room).x + x, this.rooms.get(room).y + y, new Vector4f(1, 1, 1, 1), pattern.tier * 10 - pattern.enemyLevelReduction, pattern.enemyLevelDeviation,
						Unit.GROWTH_PROFILES.get(pattern.getRandomProfile()), types[type]));
			}
		}
	}

	public void generateItems() {
		ItemProperty[] items = ItemProperty.searchForTier(pattern.tier, ItemProperty.searchForLocation(pattern.palletTag, ItemProperty.getItemList()), true);
		for (int i = 0; i < rooms.size(); i++) {
			int drops = r.nextInt(pattern.itemCountRandom) + pattern.itemCountCertain;

			for (int j = 0; j < drops; j++) {
				Vector3f position = new Vector3f(r.nextInt(rooms.get(i).width), 0, r.nextInt(rooms.get(i).height));

				boolean isOnItem = false;
				do {
					isOnItem = false;
					for (Item k : this.items) {
						if (tiles[(int) (rooms.get(i).x + position.getX())][(int) (rooms.get(i).y + position.getZ())].isSolid()
								|| (int) k.position.getX() == (int) (rooms.get(i).x + position.getX()) * 16 && (int) k.position.getZ() == (int) (rooms.get(i).y + position.getZ()) * 16) {
							isOnItem = true;
							position = new Vector3f(r.nextInt(rooms.get(i).width), 0, r.nextInt(rooms.get(i).height));
							break;
						}
					}
				} while (isOnItem);

				// Determine item to place on the map
				// Sort out all items not in current tier
				int item = r.nextInt(items.length);
				while (r.nextFloat() > items[item].rate) {
					item = r.nextInt(items.length);
				}
				this.items.add(new Item(position.add(new Vector3f(rooms.get(i).x, 16, rooms.get(i).y)).mul(new Vector3f(16, 1, 16)), items[item].copy()));
			}
		}
	}

	public void alterDungeon() {
		for (int p = 0; p < pattern.altTags.length; p++) {
			switch (pattern.altTags[p]) {

			case TAG_ALT_ITEM_SURPLUS:
				if (r.nextFloat() < Float.parseFloat(pattern.altVariables[p][0])) {
					for (Item i : items) {
						i.item = ItemProperty.get(pattern.altVariables[p][1]);
					}
				}
				break;

			case TAG_ALT_ENEMY_NONE:
				if (r.nextFloat() < Float.parseFloat(pattern.altVariables[p][0])) {
					enemyPlacements.clear();
				}
				break;

			case TAG_ALT_ENEMY_HEALTH_REDUCE:
				if (r.nextFloat() < Float.parseFloat(pattern.altVariables[p][0])) {
					for (Unit u : enemyPlacements) {
						u.baseHp *= Float.parseFloat(pattern.altVariables[p][1]);
					}
				}
				break;

			}
		}
	}

	public void generateDecor() {
		for (int p = 0; p < pattern.decorationTags.length; p++) {
			switch (pattern.decorationTags[p]) {

			case TAG_DECORATION_CHEST:
				int chests = (int) (pattern.decorationVariables[p][0] + level / pattern.decorationVariables[p][1]);
				for (int i = 0; i < chests; i++) {
					int tries = 0;
					int x = r.nextInt(tiles.length);
					int y = r.nextInt(tiles[0].length);
					while ((tiles[x][y].containsTag(Tile.TAG_STAIRS) || tiles[x][y].isSolid() || !tiles[x][y].containsTag(Tile.TAG_FLOOR_CENTER)) && tries < pattern.testLimit) {
						x = r.nextInt(tiles.length);
						y = r.nextInt(tiles[0].length);
						tries++;
					}
					if (tries < pattern.testLimit) {
						tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_CHEST_CLOSED }, Vector3f.div(tiles[x][y].getPosition(), new Vector3f(16, 16, 16)));
						generateChestKey();
					}
				}
				break;

			case TAG_DECORATION_WALL:
				ArrayList<Integer> placedRooms = new ArrayList<>();
				for (int i = 0; i < rooms.size() / pattern.decorationVariables[p][0]; i++) {
					int room = r.nextInt(rooms.size());
					while (placedRooms.contains(room) && placedRooms.size() < rooms.size()) {
						room = r.nextInt(rooms.size());
					}
					int amt = (int) pattern.decorationVariables[p][1] + r.nextInt((int) pattern.decorationVariables[p][2]);
					for (int a = 0; a < amt; a++) {
						int x = r.nextInt(rooms.get(room).width);
						int y = r.nextInt(rooms.get(room).height);

						int attempts = 0;

						while (attempts < pattern.testLimit && (tiles[rooms.get(room).x + x - 1][rooms.get(room).y + y].containsTag(Tile.TAG_FLOOR_CENTER)
								|| !tiles[rooms.get(room).x + x + 1][rooms.get(room).y + y].containsTag(Tile.TAG_FLOOR_CENTER) || !tiles[rooms.get(room).x + x][rooms.get(room).y + y].containsTag(Tile.TAG_FLOOR_TOP_MIDDLE))) {
							attempts++;
							x = r.nextInt(rooms.get(room).width);
							y = r.nextInt(rooms.get(room).height);
						}
						if (attempts < pattern.testLimit) {
							tiles[rooms.get(room).x + x][rooms.get(room).y + y] = new TileCompound(Tile.getRandomProperty(r, new String[] { pattern.palletTag, Tile.TAG_DECORATION_WALL }),
									Vector3f.div(tiles[rooms.get(room).x + x][rooms.get(room).y + y].getPosition(), new Vector3f(16, 16, 16)), tiles[rooms.get(room).x + x][rooms.get(room).y + y]);
						}
					}
					placedRooms.add(room);
				}
				break;

			// Makes sure theres a 3x3 space of movable tiles to place an object down
			case TAG_DECORATION_3X3:
				placedRooms = new ArrayList<>();
				for (int i = 0; i < rooms.size() / pattern.decorationVariables[p][0]; i++) {
					int room = r.nextInt(rooms.size());
					while (placedRooms.contains(room) && placedRooms.size() < rooms.size()) {
						room = r.nextInt(rooms.size());
					}
					int amt = (int) pattern.decorationVariables[p][1] + r.nextInt((int) pattern.decorationVariables[p][2]);
					for (int a = 0; a < amt; a++) {
						int x = r.nextInt(rooms.get(room).width);
						int y = r.nextInt(rooms.get(room).height);

						int attempts = 0;
						boolean isValid = true;

						for (int x2 = 0; x2 < 3; x2++) {
							for (int y2 = 0; y2 < 3; y2++) {
								if (!tiles[rooms.get(room).x + x + x2 - 1][rooms.get(room).y + y + y2 - 1].containsTag(Tile.TAG_FLOOR_CENTER)) {
									isValid = false;
									break;
								}
							}
						}

						while (attempts < pattern.testLimit && !isValid) {
							attempts++;
							x = r.nextInt(rooms.get(room).width);
							y = r.nextInt(rooms.get(room).height);
							isValid = true;
							for (int x2 = 0; x2 < 3; x2++) {
								for (int y2 = 0; y2 < 3; y2++) {
									if (!tiles[rooms.get(room).x + x + x2 - 1][rooms.get(room).y + y + y2 - 1].containsTag(Tile.TAG_FLOOR_CENTER)) {
										isValid = false;
										break;
									}
								}
							}
						}

						if (attempts < pattern.testLimit) {
							tiles[rooms.get(room).x + x][rooms.get(room).y + y] = Tile.getRandomTile(r, new String[] { pattern.palletTag, Tile.TAG_DECORATION_3X3 },
									Vector3f.div(tiles[rooms.get(room).x + x][rooms.get(room).y + y].getPosition(), new Vector3f(16, 16, 16)));
							for (int x2 = 0; x2 < 3; x2++) {
								for (int y2 = 0; y2 < 3; y2++) {
									if (!(x == 1 && y == 1)) {
										tiles[rooms.get(room).x + x + x2 - 1][rooms.get(room).y + y + y2 - 1].setPositionY(tiles[rooms.get(room).x + x][rooms.get(room).y + y].getPosition().getY());
									}
								}
							}
						}
					}
					placedRooms.add(room);
				}
				break;

			case TAG_DECORATION_DOOR:
				int doors = 0;
				for (int x = 0; x < tiles.length; x++) {
					for (int y = 0; y < tiles[0].length; y++) {
						if (tiles[x][y].containsTag(Tile.TAG_FLOOR_HALLWAY_HORIZONTAL) && tiles[x][y - 1].containsTag(Tile.TAG_WALL_CAP_BOTTOM) && tiles[x][y + 1].containsTag(Tile.TAG_WALL_CAP_TOP)
								&& r.nextFloat() < pattern.decorationVariables[p][0]) {
							tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_DOOR_LOCKED }, Vector3f.div(tiles[x][y].getPosition(), new Vector3f(16, 16, 16)));
							tiles[x][y].rotate(0, 90, 0);
							tiles[x][y - 1] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_WALL_VERTICAL }, Vector3f.div(tiles[x][y - 1].getPosition(), new Vector3f(16, 16, 16)));
							tiles[x][y + 1] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_WALL_VERTICAL }, Vector3f.div(tiles[x][y + 1].getPosition(), new Vector3f(16, 16, 16)));
							doors++;
						}
						if (tiles[x][y].containsTag(Tile.TAG_FLOOR_HALLWAY_VERTICAL) && tiles[x - 1][y].containsTag(Tile.TAG_WALL_CAP_RIGHT) && tiles[x + 1][y].containsTag(Tile.TAG_WALL_CAP_LEFT)
								&& r.nextFloat() < pattern.decorationVariables[p][0]) {
							tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_DOOR_LOCKED }, Vector3f.div(tiles[x][y].getPosition(), new Vector3f(16, 16, 16)));
							tiles[x - 1][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_WALL_HORIZONTAL }, Vector3f.div(tiles[x - 1][y].getPosition(), new Vector3f(16, 16, 16)));
							tiles[x + 1][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_WALL_HORIZONTAL }, Vector3f.div(tiles[x + 1][y].getPosition(), new Vector3f(16, 16, 16)));
							doors++;
						}
					}
				}
				for (int i = 0; i < doors; i++) {
					generateDoorKey();
				}
				break;

			// Places objects randomly
			case TAG_DECORATION_FOLIAGE:
				placedRooms = new ArrayList<>();
				for (int i = 0; i < rooms.size() / pattern.decorationVariables[p][0]; i++) {
					int room = r.nextInt(rooms.size());
					while (placedRooms.contains(room) && placedRooms.size() < rooms.size()) {
						room = r.nextInt(rooms.size());
					}
					int amt = (int) pattern.decorationVariables[p][1] + r.nextInt((int) pattern.decorationVariables[p][2]);
					for (int a = 0; a < amt; a++) {
						int x = r.nextInt(rooms.get(room).width);
						int y = r.nextInt(rooms.get(room).height);

						int attempts = 0;
						boolean blocking = false;

						for (int x2 = 0; x2 < 3; x2++) {
							for (int y2 = 0; y2 < 3; y2++) {
								if (tiles[rooms.get(room).x + x + x2 - 1][rooms.get(room).y + y + y2 - 1].containsTag(Tile.TAG_FLOOR_HALLWAY_HORIZONTAL)
										|| tiles[rooms.get(room).x + x + x2 - 1][rooms.get(room).y + y + y2 - 1].containsTag(Tile.TAG_FLOOR_HALLWAY_VERTICAL)) {
									blocking = true;
									break;
								}
							}
						}

						while (attempts < pattern.testLimit && (blocking || !tiles[rooms.get(room).x + x][rooms.get(room).y + y].containsTag(Tile.TAG_FLOOR_CENTER))) {
							attempts++;
							x = r.nextInt(rooms.get(room).width);
							y = r.nextInt(rooms.get(room).height);

							blocking = false;
							for (int x2 = 0; x2 < 3; x2++) {
								for (int y2 = 0; y2 < 3; y2++) {
									if (tiles[rooms.get(room).x + x + x2 - 1][rooms.get(room).y + y + y2 - 1].containsTag(Tile.TAG_FLOOR_HALLWAY_HORIZONTAL)
											|| tiles[rooms.get(room).x + x + x2 - 1][rooms.get(room).y + y + y2 - 1].containsTag(Tile.TAG_FLOOR_HALLWAY_VERTICAL)) {
										blocking = true;
										break;
									}
								}
							}
						}
						if (attempts < pattern.testLimit) {
							tiles[rooms.get(room).x + x][rooms.get(room).y + y] = Tile.getRandomTile(r, new String[] { pattern.palletTag, Tile.TAG_DECORATION_FOLIAGE },
									Vector3f.div(tiles[rooms.get(room).x + x][rooms.get(room).y + y].getPosition(), new Vector3f(16, 16, 16)));
						}
					}
					placedRooms.add(room);
				}
				break;

			// Places light props
			case TAG_DECORATION_ILLUMINATION_FLOOR:
				// Used to keep track of what rooms have been illuminated
				placedRooms = new ArrayList<>();
				for (int i = 0; i < rooms.size() / pattern.decorationVariables[p][0]; i++) {
					int room = r.nextInt(rooms.size());
					while (placedRooms.contains(room) && placedRooms.size() < rooms.size()) {
						room = r.nextInt(rooms.size());
					}
					int amt = (int) pattern.decorationVariables[p][1] + r.nextInt((int) pattern.decorationVariables[p][2]);
					for (int a = 0; a < amt; a++) {
						int x = r.nextInt(rooms.get(room).width);
						int y = r.nextInt(rooms.get(room).height);

						int count = 0;
						for (int t = 0; t < 2; t++) {
							if (tiles[rooms.get(room).x + x - 1 + t * 2][rooms.get(room).y + y].containsTag(Tile.TAG_FLOOR_CENTER)) {
								count++;
							}
							if (tiles[rooms.get(room).x + x][rooms.get(room).y + y - 1 + t * 2].containsTag(Tile.TAG_FLOOR_CENTER)) {
								count++;
							}
						}

						boolean onItem = false;
						for (Item item : items) {
							if (item.locX == rooms.get(room).x + x && item.locY == rooms.get(room).y + y) {
								onItem = true;
								break;
							}
						}

						int attempts = 0;
						while (attempts < pattern.testLimit && (onItem || x % 2 == 0 || y % 2 == 0 || count < 2 || !tiles[rooms.get(room).x + x][rooms.get(room).y + y].containsTag(Tile.TAG_FLOOR_CENTER))) {
							count = 0;
							x = r.nextInt(rooms.get(room).width);
							y = r.nextInt(rooms.get(room).height);
							for (int t = 0; t < 2; t++) {
								if (tiles[rooms.get(room).x + x - 1 + t * 2][rooms.get(room).y + y].containsTag(Tile.TAG_FLOOR_CENTER)) {
									count++;
								}
								if (tiles[rooms.get(room).x + x][rooms.get(room).y + y - 1 + t * 2].containsTag(Tile.TAG_FLOOR_CENTER)) {
									count++;
								}
							}

							onItem = false;
							for (Item item : items) {
								if (item.locX == rooms.get(room).x + x && item.locY == rooms.get(room).y + y) {
									onItem = true;
									break;
								}
							}
							attempts++;
						}
						if (attempts < pattern.testLimit) {
							tiles[rooms.get(room).x + x][rooms.get(room).y + y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_DECORATION_ILLUMINATION_FLOOR },
									Vector3f.div(tiles[rooms.get(room).x + x][rooms.get(room).y + y].getPosition(), new Vector3f(16, 16, 16)));

						}
					}
					placedRooms.add(room);
				}
				break;

			// Places light props
			case TAG_DECORATION_ILLUMINATION_WALL:
				int chunkSize = (int) pattern.decorationVariables[p][0];
				for (int rx = 0; rx < tiles.length / chunkSize; rx++) {
					for (int ry = 0; ry < tiles[0].length / chunkSize; ry++) {
						if ((rx + ry) % 2 == 0) {
							ArrayList<Point> points = new ArrayList<>();
							for (int x = 0; x < chunkSize; x++) {
								for (int y = 0; y < chunkSize; y++) {
									int ax = Math.min(tiles.length - 1, rx * chunkSize + x);
									int ay = Math.min(tiles[0].length - 1, ry * chunkSize + y);
									if (tiles[ax][ay].containsTag(Tile.TAG_WALL_VERTICAL) || tiles[ax][ay].containsTag(Tile.TAG_WALL_HORIZONTAL)) {
										points.add(new Point(ax, ay));
									}
								}
							}
							if (points.size() > 0) {
								Point location = points.get(r.nextInt(points.size()));
								tiles[location.x][location.y] = new TileBrazier(Tile.getRandomProperty(r, new String[] { pattern.palletTag, Tile.TAG_DECORATION_ILLUMINATION_WALL }),
										Vector3f.div(tiles[location.x][location.y].getPosition(), new Vector3f(16, 16, 16)), tiles[location.x][location.y]);
							}
						}
					}
				}
				break;

			case TAG_DECORATION_WATER:
				ArrayList<Integer> usedRooms = new ArrayList<>();
				int spreadLimit = (int) pattern.decorationVariables[p][0] + r.nextInt((int) pattern.decorationVariables[p][1]);
				for (int i = 0; i < rooms.size() / pattern.decorationVariables[p][2]; i++) {
					int room = r.nextInt(rooms.size());
					while (usedRooms.contains(room) && usedRooms.size() < rooms.size()) {
						room = r.nextInt(rooms.size());
					}
					usedRooms.add(room);

					ArrayList<Point> locations = new ArrayList<>();
					int startX = r.nextInt(rooms.get(room).width);
					int startY = r.nextInt(rooms.get(room).height);
					while (tiles[rooms.get(room).x + startX][rooms.get(room).y + startY].isSolid()) {
						startX = r.nextInt(rooms.get(room).width);
						startY = r.nextInt(rooms.get(room).height);
					}

					locations.add(new Point(rooms.get(room).x + startX, rooms.get(room).y + startY));

					int spreads = 0;
					for (int l = 0; l < locations.size(); l++) {
						if (spreads < spreadLimit) {
							if (!tiles[locations.get(l).x - 1][locations.get(l).y].isSolid() && tiles[locations.get(l).x - 1][locations.get(l).y].getPosition().getY() >= tiles[locations.get(l).x][locations.get(l).y].getPosition().getY()) {
								if (!containsPoint(locations, locations.get(l).x - 1, locations.get(l).y)) {
									locations.add(new Point(locations.get(l).x - 1, locations.get(l).y));
								}
							}
							if (!tiles[locations.get(l).x][locations.get(l).y - 1].isSolid() && tiles[locations.get(l).x][locations.get(l).y + 1].getPosition().getY() >= tiles[locations.get(l).x][locations.get(l).y].getPosition().getY()) {
								if (!containsPoint(locations, locations.get(l).x, locations.get(l).y - 1)) {
									locations.add(new Point(locations.get(l).x, locations.get(l).y - 1));
								}
							}
							if (!tiles[locations.get(l).x + 1][locations.get(l).y].isSolid() && tiles[locations.get(l).x - 1][locations.get(l).y].getPosition().getY() >= tiles[locations.get(l).x][locations.get(l).y].getPosition().getY()) {
								if (!containsPoint(locations, locations.get(l).x + 1, locations.get(l).y)) {
									locations.add(new Point(locations.get(l).x + 1, locations.get(l).y));
								}
							}
							if (!tiles[locations.get(l).x][locations.get(l).y + 1].isSolid() && tiles[locations.get(l).x][locations.get(l).y + 1].getPosition().getY() >= tiles[locations.get(l).x][locations.get(l).y].getPosition().getY()) {
								if (!containsPoint(locations, locations.get(l).x, locations.get(l).y + 1)) {
									locations.add(new Point(locations.get(l).x, locations.get(l).y + 1));
								}
							}
							spreads++;
						}
					}

					for (Point t : locations) {
						if (!tiles[t.x][t.y].isWaterLogged() && !tiles[t.x][t.y].isLavaLogged()) {
							tiles[t.x][t.y].translate(0, -8, 0);
						}
					}

					for (Point t : locations) {
						if (!tiles[t.x][t.y].isWaterLogged() && !tiles[t.x][t.y].isLavaLogged()) {
							boolean sink = false;
							if (!containsPoint(locations, t.x - 1, t.y) && tiles[t.x - 1][t.y].getPosition().getY() > tiles[t.x][t.y].getPosition().getY()
									&& tiles[t.x - 1][t.y].getPosition().getY() - tiles[t.x][t.y].getPosition().getY() < 8) {
								sink = true;
							}
							if (!containsPoint(locations, t.x + 1, t.y) && tiles[t.x + 1][t.y].getPosition().getY() > tiles[t.x][t.y].getPosition().getY()
									&& tiles[t.x + 1][t.y].getPosition().getY() - tiles[t.x][t.y].getPosition().getY() < 8) {
								sink = true;
							}
							if (!containsPoint(locations, t.x, t.y - 1) && tiles[t.x][t.y - 1].getPosition().getY() > tiles[t.x][t.y].getPosition().getY()
									&& tiles[t.x][t.y - 1].getPosition().getY() - tiles[t.x][t.y].getPosition().getY() < 8) {
								sink = true;
							}
							if (!containsPoint(locations, t.x, t.y + 1) && tiles[t.x][t.y + 1].getPosition().getY() > tiles[t.x][t.y].getPosition().getY()
									&& tiles[t.x][t.y + 1].getPosition().getY() - tiles[t.x][t.y].getPosition().getY() < 8) {
								sink = true;
							}
							while (sink) {
								sink = false;
								tiles[t.x][t.y].getPosition().add(new Vector3f(0, -4, 0));
								if (!containsPoint(locations, t.x - 1, t.y) && tiles[t.x - 1][t.y].getPosition().getY() > tiles[t.x][t.y].getPosition().getY()
										&& tiles[t.x - 1][t.y].getPosition().getY() - tiles[t.x][t.y].getPosition().getY() < 8) {
									sink = true;
								}
								if (!containsPoint(locations, t.x + 1, t.y) && tiles[t.x + 1][t.y].getPosition().getY() > tiles[t.x][t.y].getPosition().getY()
										&& tiles[t.x + 1][t.y].getPosition().getY() - tiles[t.x][t.y].getPosition().getY() < 8) {
									sink = true;
								}
								if (!containsPoint(locations, t.x, t.y - 1) && tiles[t.x][t.y - 1].getPosition().getY() > tiles[t.x][t.y].getPosition().getY()
										&& tiles[t.x][t.y - 1].getPosition().getY() - tiles[t.x][t.y].getPosition().getY() < 8) {
									sink = true;
								}
								if (!containsPoint(locations, t.x, t.y + 1) && tiles[t.x][t.y + 1].getPosition().getY() > tiles[t.x][t.y].getPosition().getY()
										&& tiles[t.x][t.y + 1].getPosition().getY() - tiles[t.x][t.y].getPosition().getY() < 8) {
									sink = true;
								}
							}
						}
					}
					for (Point t : locations) {
						if (!tiles[t.x][t.y].isWaterLogged() && !tiles[t.x][t.y].isLavaLogged()) {
							if (pattern.decorationVariables[p][3] < 0.5f) {
								StateManager.currentState.effects.add(new EffectWater(tiles[t.x][t.y], new Vector3f(0.427f, 0.765f, 0.9f), tiles, locations));
							} else {
								StateManager.currentState.effects.add(new EffectLava(tiles[t.x][t.y], tiles, locations));
							}
						}
					}
				}
				break;

			}
		}

	}

	public void generateDoorKey() {
		int x = r.nextInt(tiles.length);
		int y = r.nextInt(tiles[0].length);
		boolean hasPath = false;
		boolean isOnItem = false;

		highlightTiles(x, y, tiles.length * tiles[0].length, 1);
		if (getPath(startRoom.x, startRoom.y).size() > 0) {
			hasPath = true;
		}
		clearSelectedTiles();

		for (Item i : items) {
			if (i.locX == x && i.locY == y) {
				isOnItem = true;
				break;
			}
		}

		while (tiles[x][y].isSolid() || !hasPath || isOnItem) {
			x = r.nextInt(tiles.length);
			y = r.nextInt(tiles[0].length);
			hasPath = false;
			isOnItem = false;

			highlightTiles(x, y, tiles.length * tiles[0].length, 1);
			if (getPath(startRoom.x + r.nextInt(startRoom.width), startRoom.y + r.nextInt(startRoom.height)).size() > 0) {
				hasPath = true;
			}
			clearSelectedTiles();

			for (Item i : items) {
				if (i.locX == x && i.locY == y) {
					isOnItem = true;
					break;
				}
			}
		}

		items.add(new Item(Vector3f.add(tiles[x][y].getPosition(), 0, 16, 0), ItemProperty.searchForType(ItemProperty.TYPE_KEY_CHEST, ItemProperty.getItemList(), true)[0].copy()));
	}

	public void generateChestKey() {
		int x = r.nextInt(tiles.length);
		int y = r.nextInt(tiles[0].length);
		boolean isOnItem = false;

		for (Item i : items) {
			if (i.locX == x && i.locY == y) {
				isOnItem = true;
				break;
			}
		}

		while (tiles[x][y].isSolid() || isOnItem) {
			x = r.nextInt(tiles.length);
			y = r.nextInt(tiles[0].length);
			isOnItem = false;

			for (Item i : items) {
				if (i.locX == x && i.locY == y) {
					isOnItem = true;
					break;
				}
			}
		}

		items.add(new Item(Vector3f.add(tiles[x][y].getPosition(), 0, 16, 0), ItemProperty.searchForType(ItemProperty.TYPE_KEY_CHEST, ItemProperty.getItemList(), false)[0].copy()));
	}

	public void generateStairs() {
		int x = r.nextInt(tiles.length);
		int y = r.nextInt(tiles[0].length);

		while (!tiles[x][y].containsTag(Tile.TAG_FLOOR_CENTER)) {
			x = r.nextInt(tiles.length);
			y = r.nextInt(tiles[0].length);
		}

		tiles[x][y] = Tile.getTile(new String[] { pattern.palletTag, Tile.TAG_STAIRS },
				new Vector3f(x, (float) Math.round(noise.getValue(((float) x / (tiles.length + xActiveSpace)), ((float) y / (tiles[0].length + yActiveSpace))) * 10) / 4f, y));
	}

	public DungeonGenerator start() {
		try {
			long time = System.nanoTime();
			tiles = convertMap(generateMap());
			generateDecor();
			generateItems();
			generateUnitPlacements();
			generateEnemies();
			generateStairs();
			for (Tile[] x : tiles) {
				for (Tile y : x) {
					y.updateMatrix();
				}
			}
			alterDungeon();
			System.out.println("[DungeonGeneration]: Generation completed in: " + (int) ((System.nanoTime() - time) / 100.0) / 10000.0 + "ms");
			System.out.println("[DungeonGeneration]: Dungeon generated with seed: " + seed);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	public DungeonGenerator startWithoutUnits() {
		try {
			tiles = convertMap(generateMap());
			generateDecor();
			generateItems();
			System.out.println("[DungeonGeneration]: Dungeon generated with seed: " + seed);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	public int getBottomFloor() {
		return pattern.maxDepth;
	}

	public String getPalletTag() {
		return pattern.palletTag;
	}

	public String[] getEffectTags() {
		return pattern.effectTags;
	}

	public double[][] getEffectVars() {
		return pattern.effectVariables;
	}

	public int getTier() {
		return pattern.tier;
	}

	public static void loadPatterns() {
		for (File f : FileManager.getFiles("res/data/dungeon")) {
			if (f.getName().endsWith(".json")) {
				try {
					JSONObject jo = (JSONObject) new JSONParser().parse(new FileReader(f));

					String name = (String) jo.get("name");
					String palletTag = (String) jo.get("pallet_tag");
					int maxDepth = Math.toIntExact((long) jo.get("max_depth"));
					double levelScaler = (double) jo.get("level_scaler");
					double worldSizeScaler = (double) jo.get("world_size_scaler");
					float perlinPersistance = (float) ((double) jo.get("perlin_persistance"));
					int testLimit = Math.toIntExact((long) jo.get("test_limit"));
					int baseWorldSize = Math.toIntExact((long) jo.get("base_world_size"));
					int roomSizeMin = Math.toIntExact((long) jo.get("room_size_min"));
					int roomSizeRandom = Math.toIntExact((long) jo.get("room_size_random"));
					int itemCountRandom = Math.toIntExact((long) jo.get("item_count_random"));
					int itemCountCertain = Math.toIntExact((long) jo.get("item_count_certain"));
					double enemyRoomCountDevisor = (double) jo.get("enemy_room_count_devisor");
					double enemyRoomCountRandomDevisor = (double) jo.get("enemy_room_count_random_devisor");
					int enemyCountRandom = Math.toIntExact((long) jo.get("enemy_count_random"));
					int enemyCountCertain = Math.toIntExact((long) jo.get("enemy_count_certain"));
					int enemyLevelReduction = Math.toIntExact((long) jo.get("enemy_level_reduction"));
					int enemyLevelDeviation = Math.toIntExact((long) jo.get("enemy_level_deviation"));
					int tier = Math.toIntExact((long) jo.get("tier"));

					JSONArray aiRates = (JSONArray) jo.get("ai_type_rates");
					float[] aiTypeRates = new float[aiRates.size()];
					for (int i = 0; i < aiTypeRates.length; i++) {
						aiTypeRates[i] = Float.parseFloat((String) aiRates.get(i));
					}

					JSONArray screen = (JSONArray) jo.get("screen_color");
					float[] screenColors = new float[screen.size()];
					for (int i = 0; i < screenColors.length; i++) {
						screenColors[i] = Float.parseFloat((String) screen.get(i));
					}

					JSONArray decorTagList = (JSONArray) jo.get("decor_tags");
					String[] decorTags = new String[decorTagList.size()];
					double[][] decorVariables = new double[decorTagList.size()][];
					for (int i = 0; i < decorTagList.size(); i++) {
						decorTags[i] = (String) ((JSONObject) decorTagList.get(i)).get("tag");
						JSONArray decorVariablesList = (JSONArray) ((JSONObject) decorTagList.get(i)).get("vars");
						decorVariables[i] = new double[decorVariablesList.size()];
						for (int j = 0; j < decorVariablesList.size(); j++) {
							decorVariables[i][j] = (double) decorVariablesList.get(j);
						}
					}

					JSONArray effectTagList = (JSONArray) jo.get("effect_tags");
					String[] effectTags = new String[effectTagList.size()];
					double[][] effectVariables = new double[effectTagList.size()][];
					for (int i = 0; i < effectTagList.size(); i++) {
						effectTags[i] = (String) ((JSONObject) effectTagList.get(i)).get("tag");
						JSONArray effectVariablesList = (JSONArray) ((JSONObject) effectTagList.get(i)).get("vars");
						effectVariables[i] = new double[effectVariablesList.size()];
						for (int j = 0; j < effectVariablesList.size(); j++) {
							effectVariables[i][j] = (double) effectVariablesList.get(j);
						}
					}

					JSONArray altTagList = (JSONArray) jo.get("alt_tags");
					String[] altTags = new String[altTagList.size()];
					String[][] altVariables = new String[altTagList.size()][];
					for (int i = 0; i < altTagList.size(); i++) {
						altTags[i] = (String) ((JSONObject) altTagList.get(i)).get("tag");
						JSONArray altVariablesList = (JSONArray) ((JSONObject) altTagList.get(i)).get("vars");
						altVariables[i] = new String[altVariablesList.size()];
						for (int j = 0; j < altVariablesList.size(); j++) {
							altVariables[i][j] = (String) altVariablesList.get(j);
						}
					}

					JSONArray profileList = (JSONArray) jo.get("unit_profiles");
					String[] profileNames = new String[profileList.size()];
					float[] profileValues = new float[profileList.size()];
					for(int i = 0; i < profileList.size(); i++) {
						JSONObject profile = (JSONObject) profileList.get(i);
						profileNames[i] = (String) profile.get("name");
						profileValues[i] = (float) ((double) profile.get("rate"));
					}
					
					patterns.put(f.getName(),
							new GenerationPattern(f.getName(), name, palletTag, maxDepth, levelScaler, baseWorldSize, worldSizeScaler, perlinPersistance, testLimit, roomSizeMin, roomSizeRandom, itemCountRandom, itemCountCertain,
									enemyRoomCountDevisor, enemyRoomCountRandomDevisor, enemyCountRandom, enemyCountCertain, enemyLevelReduction, enemyLevelDeviation, decorTags, decorVariables, effectTags, effectVariables, altTags,
									altVariables, aiTypeRates, profileNames, profileValues, screenColors, tier));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("[DungeonGenerator]: Dungeon patterns loaded");
	}

	public static String[] getPatternNames() {
		ArrayList<String> names = new ArrayList<>();
		for (String s : patterns.keySet()) {
			names.add(patterns.get(s).name);
		}
		return names.toArray(new String[names.size()]);
	}

	public static String[] getPalletTags() {
		return patterns.keySet().toArray(new String[patterns.size()]);
	}

	public String getName() {
		return pattern.name;
	}

	public float[] getScreenColor() {
		return pattern.screenColor;
	}

	public String getPattern() {
		return pattern.pattern;
	}

	private static boolean containsPoint(ArrayList<Point> point, int x, int y) {
		boolean contains = false;
		for (Point p : point) {
			if (p.x == x && p.y == y) {
				contains = true;
				break;
			}
		}
		return contains;
	}

	public ArrayList<Point> getPath(int x, int y) {
		ArrayList<Point> path = new ArrayList<Point>();

		if (x < tiles.length * 16 && y < tiles[0].length * 16) {
			if (tiles[x][y].status > 1) {
				path.add(new Point(x, y));
				for (int i = 0; i < path.size(); i++) {
					if (path.get(i).x > 0) {
						if (tiles[path.get(i).x - 1][path.get(i).y].status > tiles[path.get(i).x][path.get(i).y].status && !tiles[path.get(i).x - 1][path.get(i).y].isSolid()) {
							path.add(new Point(path.get(i).x - 1, path.get(i).y));
							continue;
						}
					}
					if (path.get(i).x < tiles.length - 1) {
						if (tiles[path.get(i).x + 1][path.get(i).y].status > tiles[path.get(i).x][path.get(i).y].status && !tiles[path.get(i).x + 1][path.get(i).y].isSolid()) {
							path.add(new Point(path.get(i).x + 1, path.get(i).y));
							continue;
						}
					}
					if (path.get(i).y > 0) {
						if (tiles[path.get(i).x][path.get(i).y - 1].status > tiles[path.get(i).x][path.get(i).y].status && !tiles[path.get(i).x][path.get(i).y - 1].isSolid()) {
							path.add(new Point(path.get(i).x, path.get(i).y - 1));
							continue;
						}
					}
					if (path.get(i).y < tiles[0].length - 1) {
						if (tiles[path.get(i).x][path.get(i).y + 1].status > tiles[path.get(i).x][path.get(i).y].status && !tiles[path.get(i).x][path.get(i).y + 1].isSolid()) {
							path.add(new Point(path.get(i).x, path.get(i).y + 1));
							continue;
						}
					}
				}
			}
		}

		return path;
	}

	public void clearSelectedTiles() {
		for (int x = 0; x < tiles.length; x++) {
			for (int y = 0; y < tiles[0].length; y++) {
				tiles[x][y].status = 0;
				tiles[x][y].range = 0;
			}
		}
	}

	public void highlightTiles(int locX, int locY, int movement, int range) {
		tiles[locX][locY].status = movement;
		tiles[locX][locY].range = range;

		int distance = movement + range;

		for (int m = 0; m < distance; m++) {
			for (int x = 0; x < tiles.length; x++) {
				for (int y = 0; y < tiles[0].length; y++) {

					boolean onUnit = true;

					if (x < tiles.length && y < tiles[0].length) {
						if (x > 0) {
							if (tiles[x - 1][y].status > tiles[x][y].status) {
								if (tiles[x - 1][y].status > 1) {
									if (onUnit && !tiles[x][y].isSolid()) {
										tiles[x][y].status = Math.max(1, tiles[x - 1][y].status - tiles[x][y].movementPenalty);
									} else {
										tiles[x][y].status = 1;
									}
									tiles[x][y].range = tiles[x - 1][y].range;
								}
							}
							if (tiles[x - 1][y].status == 1) {
								if (tiles[x - 1][y].range > tiles[x][y].range && tiles[x - 1][y].range > 1) {
									tiles[x][y].status = 1;
									tiles[x][y].range = tiles[x - 1][y].range - 1;
								}
							}
						}
						if (y > 0) {
							if (tiles[x][y - 1].status > tiles[x][y].status) {
								if (tiles[x][y - 1].status > 1) {
									if (onUnit && !tiles[x][y].isSolid()) {
										tiles[x][y].status = Math.max(1, tiles[x][y - 1].status - tiles[x][y].movementPenalty);
									} else {
										tiles[x][y].status = 1;
									}
									tiles[x][y].range = tiles[x][y - 1].range;
								}
							}
							if (tiles[x][y - 1].status == 1) {
								if (tiles[x][y - 1].range > tiles[x][y].range && tiles[x][y - 1].range > 1) {
									tiles[x][y].status = 1;
									tiles[x][y].range = tiles[x][y - 1].range - 1;
								}
							}
						}
						if (x < tiles.length - 1) {
							if (tiles[x + 1][y].status > tiles[x][y].status) {
								if (tiles[x + 1][y].status > 1) {
									if (onUnit && !tiles[x][y].isSolid()) {
										tiles[x][y].status = Math.max(1, tiles[x + 1][y].status - tiles[x][y].movementPenalty);
									} else {
										tiles[x][y].status = 1;
									}
									tiles[x][y].range = tiles[x + 1][y].range;
								}
							}
							if (tiles[x + 1][y].status == 1) {
								if (tiles[x + 1][y].range > tiles[x][y].range && tiles[x + 1][y].range > 1) {
									tiles[x][y].status = 1;
									tiles[x][y].range = tiles[x + 1][y].range - 1;
								}
							}
						}
						if (y < tiles[0].length - 1) {
							if (tiles[x][y + 1].status > tiles[x][y].status) {
								if (tiles[x][y + 1].status > 1) {
									if (onUnit && !tiles[x][y].isSolid()) {
										tiles[x][y].status = Math.max(1, tiles[x][y + 1].status - tiles[x][y].movementPenalty);
									} else {
										tiles[x][y].status = 1;
									}
									tiles[x][y].range = tiles[x][y + 1].range;
								}
							}
							if (tiles[x][y + 1].status == 1) {
								if (tiles[x][y + 1].range > tiles[x][y].range && tiles[x][y + 1].range > 1) {
									tiles[x][y].status = 1;
									tiles[x][y].range = tiles[x][y + 1].range - 1;
								}
							}
						}
					}
				}
			}
		}
	}

}

class GenerationPattern {

	public String pattern;
	public String name;
	public String palletTag;
	public int maxDepth;
	public double levelScaler;
	public int baseWorldSize;
	public double worldSizeScaler;
	public float perlinPersistance;
	public int testLimit;
	public int roomSizeMin;
	public int roomSizeRandom;
	public int itemCountRandom;
	public int itemCountCertain;
	public double enemyRoomCountDevisor;
	public double enemyRoomCountRandomDevisor;
	public int enemyCountRandom;
	public int enemyCountCertain;
	public int enemyLevelReduction;
	public int enemyLevelDeviation;
	public String[] decorationTags;
	public double[][] decorationVariables;
	public String[] effectTags;
	public double[][] effectVariables;
	public String[] altTags;
	public String[][] altVariables;
	public float[] aiRates;
	public String[] profileNames;
	public float[] profileValues;
	public float[] screenColor;
	public int tier;

	public GenerationPattern(String pattern, String name, String palletTag, int maxDepth, double levelScaler, int baseWorldSize, double worldSizeScaler, float perlinPersistance, int testLimit, int roomSizeMin, int roomSizeRandom,
			int itemCountRandom, int itemCountCertain, double enemyRoomCountDevisor, double enemyRoomCountRandomDevisor, int enemyCountRandom, int enemyCountCertain, int enemyLevelReduction, int enemyLevelDeviation, String[] decorationTags,
			double[][] decorationVariables, String[] effectTags, double[][] effectVariables, String[] altTags, String[][] altVariables, float[] aiRates, String[] profileNames, float[] profileValues, float[] screenColor, int tier) {
		this.pattern = pattern;
		this.name = name;
		this.palletTag = palletTag;
		this.maxDepth = maxDepth;
		this.levelScaler = levelScaler;
		this.baseWorldSize = baseWorldSize;
		this.worldSizeScaler = worldSizeScaler;
		this.perlinPersistance = perlinPersistance;
		this.testLimit = testLimit;
		this.roomSizeMin = roomSizeMin;
		this.roomSizeRandom = roomSizeRandom;
		this.itemCountCertain = itemCountCertain;
		this.itemCountRandom = itemCountRandom;
		this.enemyRoomCountDevisor = enemyRoomCountDevisor;
		this.enemyRoomCountRandomDevisor = enemyRoomCountRandomDevisor;
		this.enemyCountRandom = enemyCountRandom;
		this.enemyCountCertain = enemyCountCertain;
		this.enemyLevelReduction = enemyLevelReduction;
		this.enemyLevelDeviation = enemyLevelDeviation;
		this.decorationTags = decorationTags;
		this.decorationVariables = decorationVariables;
		this.effectTags = effectTags;
		this.effectVariables = effectVariables;
		this.altTags = altTags;
		this.altVariables = altVariables;
		this.aiRates = aiRates;
		this.profileNames = profileNames;
		this.profileValues = profileValues;
		this.screenColor = screenColor;
		this.tier = tier;
	}

	public String getRandomProfile() {
		int n = new Random().nextInt(profileNames.length);
		float r = new Random().nextFloat();
		while (r > profileValues[n]) {
			n = new Random().nextInt(profileNames.length);
			r = new Random().nextFloat();
		}
		
		return profileNames[n];
	}

}