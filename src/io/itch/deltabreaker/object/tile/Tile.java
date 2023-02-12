package io.itch.deltabreaker.object.tile;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import io.itch.deltabreaker.core.FileManager;
import io.itch.deltabreaker.exception.MissingPropertyException;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.Matrix4f;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.state.StateManager;

public class Tile {

	public static final String TAG_AIR = "tile.air";
	public static final String TAG_FILLER = "tile.filler";

	// Base floor tiles
	public static final String TAG_FLOOR_TOP_LEFT = "tile.floor.top.left";
	public static final String TAG_FLOOR_TOP_MIDDLE = "tile.floor.top.middle";
	public static final String TAG_FLOOR_TOP_RIGHT = "tile.floor.top.right";
	public static final String TAG_FLOOR_MIDDLE_LEFT = "tile.floor.middle.left";
	public static final String TAG_FLOOR_CENTER = "tile.floor.center";
	public static final String TAG_FLOOR_MIDDLE_RIGHT = "tile.floor.middle.right";
	public static final String TAG_FLOOR_BOTTOM_LEFT = "tile.floor.bottom.left";
	public static final String TAG_FLOOR_BOTTOM_MIDDLE = "tile.floor.bottom.middle";
	public static final String TAG_FLOOR_BOTTOM_RIGHT = "tile.floor.bottom.right";
	public static final String TAG_FLOOR_HALLWAY_HORIZONTAL = "tile.floor.hallway.horizontal";
	public static final String TAG_FLOOR_HALLWAY_VERTICAL = "tile.floor.hallway.vertical";

	// House tiles
	public static final String TAG_HOUSE_FLOOR = "tile.house.floor";
	public static final String TAG_HOUSE_ROOF = "tile.house.roof";
	public static final String TAG_HOUSE_WALL_DOOR = "tile.house.wall.bottom.door";

	// Road tiles
	public static final String TAG_ROAD_FLOOR_CENTER = "tile.road.floor.center";

	// Base wall tiles
	public static final String TAG_WALL_HORIZONTAL = "tile.wall.horizontal";
	public static final String TAG_WALL_VERTICAL = "tile.wall.vertical";
	public static final String TAG_WALL_TURN_TOP_LEFT = "tile.wall.turn.top.left";
	public static final String TAG_WALL_TURN_TOP_RIGHT = "tile.wall.turn.top.right";
	public static final String TAG_WALL_TURN_BOTTOM_LEFT = "tile.wall.turn.bottom.left";
	public static final String TAG_WALL_TURN_BOTTOM_RIGHT = "tile.wall.turn.bottom.right";
	public static final String TAG_WALL_JUNCTION_BOTTOM = "tile.wall.junction.bottom";
	public static final String TAG_WALL_JUNCTION_TOP = "tile.wall.junction.top";
	public static final String TAG_WALL_JUNCTION_LEFT = "tile.wall.junction.left";
	public static final String TAG_WALL_JUNCTION_RIGHT = "tile.wall.junction.right";
	public static final String TAG_WALL_CROSS = "tile.wall.cross";
	public static final String TAG_WALL_STANDALONE = "tile.wall.standalone";
	public static final String TAG_WALL_CAP_TOP = "tile.wall.cap.top";
	public static final String TAG_WALL_CAP_BOTTOM = "tile.wall.cap.bottom";
	public static final String TAG_WALL_CAP_LEFT = "tile.wall.cap.left";
	public static final String TAG_WALL_CAP_RIGHT = "tile.wall.cap.right";

	// Decoration tags
	public static final String TAG_DECORATION_FOLIAGE = "tile.decoration.foliage";
	public static final String TAG_DECORATION_ILLUMINATION_FLOOR = "tile.decoration.illumination.floor";
	public static final String TAG_DECORATION_ILLUMINATION_WALL = "tile.decoration.illumination.wall";
	public static final String TAG_DECORATION_3X3 = "tile.decoration.3x3";
	public static final String TAG_DECORATION_WALL = "tile.decoration.wall";

	// Unique tags
	public static final String TAG_FORTRESS = "tile.fortress";

	// Interactable tiles
	public static final String TAG_STAIRS = "tile.stairs";
	public static final String TAG_CAMPFIRE = "tile.campfire";
	public static final String TAG_CHEST_CLOSED = "tile.chest.closed";
	public static final String TAG_CHEST_OPEN = "tile.chest.open";
	public static final String TAG_DOOR_LOCKED = "tile.door.locked";
	public static final String TAG_DOOR_CLOSED = "tile.door.closed";
	public static final String TAG_DOOR_OPEN = "tile.door.open";

	// Inheritable tile properties
	public static final String PROPERTY_CHEST = "TILE_CHEST";
	public static final String PROPERTY_TORCH = "TILE_TORCH";
	public static final String PROPERTY_CAMPFIRE = "TILE_CAMPFIRE";
	public static final String PROPERTY_CRYSTAL = "TILE_CRYSTAL";
	public static final String PROPERTY_OCEAN_VENT = "TILE_OCEAN_VENT";
	public static final String PROPERTY_STALAGMITE = "TILE_STALAGMITE";
	public static final String PROPERTY_COMPOUND = "TILE_COMPOUND";
	public static final String PROPERTY_ROTATED_COMPOUND = "TILE_ROTATED_COMPOUND";
	public static final String PROPERTY_ROTATED = "TILE_ROTATED";
	public static final String PROPERTY_DOOR_LOCKED = "TILE_DOOR_LOCKED";
	public static final String PROPERTY_DOOR = "TILE_DOOR";
	public static final String PROPERTY_BRAZIER = "TILE_BRAZIER";
	public static final String PROPERTY_RUG = "TILE_RUG";

	public static TreeMap<String, TileProperty> tileProperties = new TreeMap<>();

	public TileProperty property;

	protected Vector3f position;
	protected Vector3f rotation = new Vector3f(0, 0, 0);
	protected Vector3f scale = new Vector3f(0.5f, 0.5f, 0.5f);

	public int defense = 0;
	public int movementPenalty = 1;
	public int status = 0;
	public int range = 0;
	private boolean waterLogged = false;
	private boolean lavaLogged = false;
	public boolean healLogged = false;

	public Vector4f shade = new Vector4f(1, 1, 1, 1);
	public Matrix4f precalc;

	public Tile(TileProperty property, Vector3f position) {
		this.property = property;
		this.position = new Vector3f(position.getX() * 16, position.getY() * 16, position.getZ() * 16);
		precalc = Matrix4f.transform(Vector3f.add(this.position, property.offset), rotation, scale);
	}

	public Tile(TileProperty property, Vector3f position, Vector3f rotation) {
		this.property = property;
		this.position = new Vector3f(position.getX() * 16, position.getY() * 16, position.getZ() * 16);
		this.rotation = rotation;
		precalc = Matrix4f.transform(Vector3f.add(this.position, property.offset), rotation, scale);
	}

	public void tick() {
		if (status > 0) {
			if (status > 1) {
				float mul = Math.max(1, 0.5f + (float) Math.sin(Math.toRadians(StateManager.currentState.shading)));
				shade.setX(1 * mul);
				shade.setY(1 * mul);
				shade.setZ(1.25f * mul);
			} else {
				shade.setX(1.25f);
				shade.setY(0.75f);
				shade.setZ(0.75f);
			}
		} else {
			shade.setX(1);
			shade.setY(1);
			shade.setZ(1);
		}
	}

	public void render(boolean staticView) {
		if (!containsTag(TAG_AIR)) {
			BatchSorter.add(property.model, property.texture, (staticView) ? "static_3d" : property.shader, property.material.toString(), precalc, shade, true, staticView);
		}
	}

	public void renderEditor() {
		if (!containsTag(TAG_AIR)) {
			BatchSorter.add(property.model, property.texture, "static_3d_editor", property.material.toString(), precalc, shade, false, true);
		}
	}

	public String getModel() {
		return property.model;
	}

	public String getTexture() {
		return property.texture;
	}

	public String getShader() {
		return property.shader;
	}

	public Material getMaterial() {
		return property.material;
	}

	public Vector3f getOffset() {
		return property.offset;
	}

	public boolean isSolid() {
		return property.solid;
	}

	public String getProperty() {
		return property.property;
	}

	public String getPropertyName() {
		return property.file;
	}

	public String[] getTags() {
		return property.tags;
	}

	public Vector3f getPosition() {
		return position;
	}
	
	public Vector3f getRotation() {
		return rotation;
	}

	public void setPosition(float x, float y, float z) {
		position.set(x, y, z);
		updateMatrix();
	}
	
	public void setPositionY(float y) {
		position.setY(y);
		updateMatrix();
	}

	public void translate(float x, float y, float z) {
		position.add(x, y, z);
		updateMatrix();
	}

	public void setRotation(float x, float y, float z) {
		rotation.set(x, y, z);
		updateMatrix();
	}

	public void rotate(float x, float y, float z) {
		rotation.add(x, y, z);
		updateMatrix();
	}

	public void updateMatrix() {
		Matrix4f.release(precalc);
		precalc = Matrix4f.transform(Vector3f.add(this.position, property.offset), rotation, scale);
	}

	public static String[] getTagsFromString(String file) {
		return tileProperties.get(file).tags;
	}

	public boolean containsTag(String tag) {
		for (String s : property.tags) {
			if (s.equals(tag)) {
				return true;
			}
		}
		return false;
	}

	public void action(Unit u, String[] args) {
		// Empty by default
	}

	public void cleanUp() {
		Matrix4f.release(precalc);
	}

	public static void loadProperties(String file) {
		for (File f : FileManager.getFiles(file)) {
			if (f.getName().endsWith(".json")) {
				try {
					JSONObject jo = (JSONObject) new JSONParser().parse(new FileReader(f));

					JSONArray tagList = (JSONArray) jo.get("tags");
					String[] tags = new String[tagList.size()];
					for (int i = 0; i < tagList.size(); i++) {
						tags[i] = (String) tagList.get(i);
					}

					String property = (String) jo.get("property");
					String model = (String) jo.get("model");
					String texture = (String) jo.get("texture");
					String shader = (String) jo.get("shader");
					boolean solid = (boolean) jo.get("solid");

					JSONArray offsetList = (JSONArray) jo.get("offset");
					float[] offsets = new float[offsetList.size()];
					for (int i = 0; i < offsetList.size(); i++) {
						offsets[i] = (float) ((double) offsetList.get(i));
					}
					String material = (String) jo.get("material");

					tileProperties.put(f.getName(), new TileProperty(f.getName(), tags, property, model, texture, shader, solid, new Vector3f(offsets[0], offsets[1], offsets[2]), Material.valueOf(material)));

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("[Tile]: " + tileProperties.size() + " tile properties loaded");
	}

	public static TileProperty getRandomProperty(Random r, String[] tags) {
		ArrayList<TileProperty> matches = new ArrayList<>();
		try {
			for (TileProperty t : tileProperties.values()) {
				boolean tagsMatch = true;
				for (String s : tags) {
					boolean match = false;
					for (String s2 : t.tags) {
						if (s.equals(s2)) {
							match = true;
							break;
						}
					}
					if (!match) {
						tagsMatch = false;
					}
				}
				if (tagsMatch) {
					matches.add(t);
				}
			}
			if (matches.size() == 0) {
				String tagString = "";
				for (int i = 0; i < tags.length; i++) {
					tagString += tags[i];
					if (i < tags.length - 1) {
						tagString += "\n";
					}
				}
				throw new MissingPropertyException("MissingPropertyException\nNo tile property was found for tags:\n" + tagString);
			}
		} catch (MissingPropertyException e) {
			e.printStackTrace();
			System.exit(0);
		}
		return matches.get(r.nextInt(matches.size()));
	}

	public static TileProperty[] getProperty(String[] tags) {
		ArrayList<TileProperty> matches = new ArrayList<>();
		try {
			for (TileProperty t : tileProperties.values()) {
				boolean tagsMatch = true;
				for (String s : tags) {
					boolean match = false;
					for (String s2 : t.tags) {
						if (s.equals(s2)) {
							match = true;
							break;
						}
					}
					if (!match) {
						tagsMatch = false;
					}
				}
				if (tagsMatch) {
					matches.add(t);
				}
			}
			if (matches.size() == 0) {
				String tagString = "";
				for (int i = 0; i < tags.length; i++) {
					tagString += tags[i];
					if (i < tags.length - 1) {
						tagString += "\n";
					}
				}
				throw new MissingPropertyException("MissingPropertyException\nNo tile property was found for tags:\n" + tagString);
			}
		} catch (MissingPropertyException e) {
			e.printStackTrace();
			System.exit(0);
		}
		return matches.toArray(new TileProperty[matches.size()]);
	}

	public static TileProperty getProperty(String file) {
		return tileProperties.get(file);
	}

	public static Tile getTile(String[] tags, Vector3f position) {
		TileProperty[] property = getProperty(tags);
		return TileMap.valueOf(property[0].property).getTile(property[0], position);
	}

	public static Tile getTile(TileProperty property, Vector3f position) {
		return TileMap.valueOf(property.property).getTile(property, position);
	}

	public static Tile getRandomTile(Random r, String[] tags, Vector3f position) {
		TileProperty[] property = getProperty(tags);
		int i = r.nextInt(property.length);
		return TileMap.valueOf(property[i].property).getTile(property[i], position);
	}

	public static Tile[] getTiles(String[] tags, Vector3f position) {
		TileProperty[] properties = getProperty(tags);
		Tile[] tiles = new Tile[properties.length];
		for (int i = 0; i < tiles.length; i++) {
			tiles[i] = TileMap.valueOf(properties[i].property).getTile(properties[i], position);
		}
		return tiles;
	}

	public void setWaterLogged(boolean isLogged) {
		waterLogged = isLogged;
	}

	public void setLavaLogged(boolean isLogged) {
		lavaLogged = isLogged;
	}

	public boolean isWaterLogged() {
		return waterLogged;
	}

	public boolean isLavaLogged() {
		return lavaLogged;
	}

}

class TileProperty {

	public String file;
	public String[] tags;
	public String property;
	public String model;
	public String texture;
	public String shader;
	public boolean solid;
	public Vector3f offset;
	public Material material;

	public TileProperty(String file, String[] tags, String property, String model, String texture, String shader, boolean solid, Vector3f offset, Material material) {
		this.file = file;
		this.tags = tags;
		this.property = property;
		this.model = model;
		this.texture = texture;
		this.shader = shader;
		this.solid = solid;
		this.offset = offset;
		this.material = material;
	}

}

enum TileMap {

	DEFAULT {
		@Override
		public Tile getTile(TileProperty property, Vector3f position) {
			return new Tile(property, position);
		}
	},

	TILE_CAMPFIRE {
		@Override
		public Tile getTile(TileProperty property, Vector3f position) {
			return new TileCampfire(property, position);
		}
	},

	TILE_TORCH {
		@Override
		public Tile getTile(TileProperty property, Vector3f position) {
			return new TileTorch(property, position);
		}
	},

	TILE_BRAZIER {
		@Override
		public Tile getTile(TileProperty property, Vector3f position) {
			return new TileBrazier(property, position);
		}
	},

	TILE_CRYSTAL {
		@Override
		public Tile getTile(TileProperty property, Vector3f position) {
			return new TileCrystal(property, position);
		}
	},

	TILE_OCEAN_VENT {
		@Override
		public Tile getTile(TileProperty property, Vector3f position) {
			return new TileOceanVent(property, position);
		}
	},

	TILE_CHEST {
		@Override
		public Tile getTile(TileProperty property, Vector3f position) {
			return new TileChest(property, position);
		}
	},

	TILE_STALAGMITE {
		@Override
		public Tile getTile(TileProperty property, Vector3f position) {
			return new TileStalagmite(property, position);
		}
	},

	TILE_CRATER_VENT {
		@Override
		public Tile getTile(TileProperty property, Vector3f position) {
			return new TileCraterVent(property, position);
		}
	},

	TILE_COMPOUND {
		@Override
		public Tile getTile(TileProperty property, Vector3f position) {
			return new TileCompound(property, position);
		}
	},

	TILE_ROTATED_COMPOUND {
		@Override
		public Tile getTile(TileProperty property, Vector3f position) {
			return new TileRotatedCompound(property, position);
		}
	},

	TILE_ROTATED {
		@Override
		public Tile getTile(TileProperty property, Vector3f position) {
			return new Tile(property, position, new Vector3f(0, new Random().nextInt(4) * 90, 0));
		}
	},

	TILE_DOOR_LOCKED {
		@Override
		public Tile getTile(TileProperty property, Vector3f position) {
			return new TileDoor(property, position);
		}

	},

	TILE_DOOR {
		@Override
		public Tile getTile(TileProperty property, Vector3f position) {
			return new TileDoor(property, position);
		}
	},

	TILE_RESIDUE {
		@Override
		public Tile getTile(TileProperty property, Vector3f position) {
			return new TileResidue(property, position);
		}
	},

	TILE_HOUSING {
		@Override
		public Tile getTile(TileProperty property, Vector3f position) {
			return new TileHousing(property, position);
		}
	},

	TILE_HOUSING_ROOF {
		@Override
		public Tile getTile(TileProperty property, Vector3f position) {
			return new TileHousingRoof(property, position);
		}
	},

	TILE_HOUSING_FURNISHING {
		@Override
		public Tile getTile(TileProperty property, Vector3f position) {
			return new TileHousingFurnishing(property, position);
		}
	},

	TILE_HOUSING_DOOR {
		@Override
		public Tile getTile(TileProperty property, Vector3f position) {
			return new TileHousingDoor(property, position);
		}
	},

	TILE_ROAD {
		@Override
		public Tile getTile(TileProperty property, Vector3f position) {
			return new TileRoadObject(property, position);
		}
	},

	TILE_FOUNTAIN {
		@Override
		public Tile getTile(TileProperty property, Vector3f position) {
			return new TileFountain(property, position);
		}
	};

	public abstract Tile getTile(TileProperty property, Vector3f position);

}