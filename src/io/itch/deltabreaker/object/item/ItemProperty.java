package io.itch.deltabreaker.object.item;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.swing.JOptionPane;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import io.itch.deltabreaker.core.FileManager;
import io.itch.deltabreaker.exception.MissingPropertyException;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.state.StateDungeon;

public class ItemProperty implements Cloneable {

	private static HashMap<String, ItemProperty> items = new HashMap<>();
	public static ItemProperty empty = new ItemProperty();

	public static Vector3f[] colorList = { new Vector3f(1, 1, 1), new Vector3f(0, 0.909f, 0.419f), new Vector3f(1, 1, 0), new Vector3f(1, 0.15f, 0.15f), new Vector3f(1, 0, 1) };

	public static final int STACK_CAP = 99;

	public static final int TIER_COMMON = 0;
	public static final int TIER_RARE = 1;
	public static final int TIER_EXOTIC = 2;
	public static final int TIER_LEGENDARY = 3;
	public static final int TIER_MYTHIC = 4;

	public static final String TYPE_EMPTY = "item.type.empty";
	public static final String TYPE_USABLE = "item.type.usable";
	public static final String TYPE_WEAPON = "item.type.weapon";
	public static final String TYPE_ARMOR = "item.type.armor";
	public static final String TYPE_ACCESSORY = "item.type.accessory";
	public static final String TYPE_OTHER = "item.type.other";
	public static final String TYPE_KEY_CHEST = "item.type.key.chest";
	public static final String TYPE_GEM_ABILITY = "item.type.gem.ability";

	public static final String TYPE_DAMAGE_PHYSICAL = "item.damage.physical";
	public static final String TYPE_DAMAGE_MAGIC = "item.damage.magic";
	public static final String TYPE_DAMAGE_COMBO = "item.damage.combo";

	public static final String ANIMATION_SLASH = "ITEM_ANIMATION_SLASH";
	public static final String ANIMATION_PIERCE = "ITEM_ANIMATION_PIERCE";
	public static final String ANIMATION_BASH = "ITEM_ANIMATION_BASH";

	public static final String ANIMATION_FIRE_SMALL = "ITEM_ANIMATION_FIRE_SMALL";
	public static final String ANIMATION_FIRE_MID = "ITEM_ANIMATION_FIRE_MEDIUM";
	public static final String ANIMATION_FIRE_LARGE = "ITEM_ANIMATION_FIRE_LRAGE";

	public static final String ANIMATION_HEX_SMALL = "ITEM_ANIMATION_DARK_SMALL";
	public static final String ANIMATION_HEX_MID = "ITEM_ANIMATION_DARK_MEDIUM";
	public static final String ANIMATION_HEX_LARGE = "ITEM_ANIMATION_DARK_LARGE";

	public String uuid = UUID.randomUUID().toString();

	// Generic modifiers
	public String id = "";
	public String type = "";
	public int tier;
	public double rate;
	public int price;
	public String material;
	public String model;
	public String texture;
	public String[] locations;
	public int stack = 1;

	// Consumable modifiers
	public String use;

	// Weapon modifiers
	public String damageType;
	public String animation;
	public int range;
	public String[] abilities;
	public int capacity;

	// Wearable modifiers
	public int hp;
	public int atk;
	public int mag;
	public int spd;
	public int def;
	public int res;
	public int mov;
	public String wearableModelID;
	public String wearableTextureID;

	// Descriptive modifiers
	public String[] text;
	public String name;

	public static void loadItems(String dir) {
		List<File> list = FileManager.getFiles(dir);
		try {
			for (File f : list) {
				if (f.getName().endsWith(".json")) {
					try {
						JSONObject jo = (JSONObject) new JSONParser().parse(new FileReader(f));

						ItemProperty item = new ItemProperty();

						item.id = (String) jo.get("id");
						item.name = (String) jo.get("name");
						item.type = (String) jo.get("type");
						item.tier = Math.toIntExact((long) jo.get("tier"));
						item.rate = (double) jo.get("rate");
						item.price = Math.toIntExact((long) jo.get("price"));
						item.material = (String) jo.get("material");
						item.model = (String) jo.get("model");
						item.texture = (String) jo.get("texture");
						JSONArray text = (JSONArray) ((JSONObject) jo).get("text");
						ArrayList<String> textList = new ArrayList<>();
						for (int t = 0; t < text.size(); t++) {
							textList.add((String) text.get(t));
						}
						item.text = textList.toArray(new String[textList.size()]);

						JSONArray locs = (JSONArray) ((JSONObject) jo).get("locations");
						ArrayList<String> locList = new ArrayList<>();
						for (int t = 0; t < locs.size(); t++) {
							locList.add((String) locs.get(t));
						}
						item.locations = locList.toArray(new String[locList.size()]);

						if (item.type.equals(TYPE_USABLE)) {
							item.use = (String) jo.get("use");
						}

						if (item.type.equals(TYPE_WEAPON) || item.type.equals(TYPE_EMPTY)) {
							item.damageType = (String) jo.get("damage_type");
							item.animation = (String) jo.get("animation");
							item.range = Math.toIntExact((long) jo.get("range"));
							item.capacity = Math.toIntExact((long) jo.get("capacity"));
						}

						if (item.type.equals(TYPE_WEAPON) || item.type.equals(TYPE_ACCESSORY) || item.type.equals(TYPE_EMPTY)) {
							JSONArray itemAbilities = (JSONArray) jo.get("abilities");
							String[] abilities = new String[itemAbilities.size()];
							for (int i = 0; i < abilities.length; i++) {
								abilities[i] = (String) itemAbilities.get(i);
							}
							item.abilities = abilities;
						}

						if (item.type.equals(TYPE_WEAPON) || item.type.equals(TYPE_ARMOR)) {
							item.wearableModelID = (String) jo.get("wearable_model_id");
							item.wearableTextureID = (String) jo.get("wearable_texture_id");
						}

						if (item.type.equals(TYPE_WEAPON) || item.type.equals(TYPE_ARMOR) || item.type.equals(TYPE_ACCESSORY)) {
							item.hp = Math.toIntExact((long) jo.get("hp"));
							item.atk = Math.toIntExact((long) jo.get("atk"));
							item.mag = Math.toIntExact((long) jo.get("mag"));
							item.spd = Math.toIntExact((long) jo.get("spd"));
							item.def = Math.toIntExact((long) jo.get("def"));
							item.res = Math.toIntExact((long) jo.get("res"));
							item.mov = Math.toIntExact((long) jo.get("mov"));
						}

						items.put(item.id, item);
						if (item.type.equals(TYPE_EMPTY)) {
							empty = item;
						}
						System.out.println("[ItemProperty]: Item: " + item.name + " loaded");
					} catch (Exception e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(null, "There was an error loading item: " + f.getName());
					}
				}
			}
			if (empty == null) {
				throw new MissingPropertyException("There was no property given for the empty item object");
			}
			System.out.println("[ItemProperty]: " + items.size() + " items loaded");
		} catch (MissingPropertyException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void use(Unit unit, StateDungeon context) {
		ItemUse.valueOf(use).use(unit, context);
	}

	public ItemProperty copy() {
		try {
			ItemProperty item = (ItemProperty) clone();
			item.uuid = UUID.randomUUID().toString();

			if (item.type.equals(TYPE_GEM_ABILITY)) {
				String[] abilities = ItemAbility.getNameList(true);
				ItemAbility ability = ItemAbility.ITEM_ABILITY_TRICKSTER;

				item.price = 1000 * ability.getRarity();
				item.tier = ability.getRarity();
				item.name = ability.toString() + " Gem";

				item.abilities = new String[] { ability.name() };

			}

			return item;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		System.out.println("[ItemShell]: Failed to clone item");
		System.exit(0);
		return null;
	}

	public boolean hasAbility(ItemAbility ability) {
		for (String s : abilities) {
			if (ItemAbility.valueOf(s) == ability) {
				return true;
			}
		}
		return false;
	}

	public void addAbility(String ability) {
		System.out.println(abilities.length);
		abilities = Arrays.copyOf(abilities, abilities.length + 1);
		abilities[abilities.length - 1] = ability;
		System.out.println(abilities.length);
	}

	public void addAbility(ItemAbility ability) {
		System.out.println(abilities.length);
		abilities = Arrays.copyOf(abilities, abilities.length + 1);
		abilities[abilities.length - 1] = ability.name();
		System.out.println(abilities.length);
	}

	public static ItemProperty loadItem(String path) {
		File file = new File(path);
		if (file.exists()) {
			try {
				DataInputStream in = new DataInputStream(new FileInputStream(file));

				ItemProperty item = items.get(in.readUTF());
				item.uuid = in.readUTF();

				if (item.type.equals(TYPE_WEAPON) || item.type.equals(TYPE_ACCESSORY) || item.type.equals(TYPE_EMPTY)) {
					int length = in.readInt();
					item.abilities = new String[length];
					for (int i = 0; i < length; i++) {
						item.abilities[i] = in.readUTF();
					}
				}

				in.close();

				return item;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	// Returns an array of items with the given tier
	public static ItemProperty[] searchForTier(int tier, ItemProperty[] items, boolean cumulative) {
		ArrayList<ItemProperty> list = new ArrayList<>();
		for (ItemProperty i : items) {
			if (((cumulative) ? i.tier <= tier : i.tier == tier) && !i.type.equals(TYPE_EMPTY)) {
				list.add(i);
			}
		}
		return list.toArray(new ItemProperty[list.size()]);
	}

	// Returns an array of items based on the type given
	public static ItemProperty[] searchForType(String type, ItemProperty[] items, boolean includeEmpty) {
		ArrayList<ItemProperty> list = new ArrayList<>();
		for (ItemProperty i : items) {
			if (i.type.equals(type) || (i.type.equals(TYPE_EMPTY) && includeEmpty)) {
				list.add(i);
			}
		}
		return list.toArray(new ItemProperty[list.size()]);
	}

	// Returns a list of items with atk higher than mag
	public static ItemProperty[] searchForAttackStat(ItemProperty[] items) {
		ArrayList<ItemProperty> list = new ArrayList<>();
		for (ItemProperty i : items) {
			if (i.atk >= i.mag && !i.type.equals(TYPE_EMPTY)) {
				list.add(i);
			}
		}
		return list.toArray(new ItemProperty[list.size()]);
	}

	// Returns a list of items with mag higher than atk
	public static ItemProperty[] searchForMagicStat(ItemProperty[] items) {
		ArrayList<ItemProperty> list = new ArrayList<>();
		for (ItemProperty i : items) {
			if (i.atk <= i.mag && !i.type.equals(TYPE_EMPTY)) {
				list.add(i);
			}
		}
		return list.toArray(new ItemProperty[list.size()]);
	}

	// Returns a list of items that contians pallet tag
	public static ItemProperty[] searchForLocation(String palletTag, ItemProperty[] items) {
		ArrayList<ItemProperty> list = new ArrayList<>();
		for (ItemProperty i : items) {
			for (String s : i.locations) {
				if (s.equals(palletTag)) {
					list.add(i);
					break;
				}
			}
		}
		return list.toArray(new ItemProperty[list.size()]);
	}

	public static String[] toStringList(ItemProperty[] items) {
		String[] names = new String[items.length];
		for (int i = 0; i < names.length; i++) {
			names[i] = items[i].name;
		}
		return names;
	}

	public String[] getSkillsNames() {
		String[] names = new String[abilities.length];
		for (int i = 0; i < names.length; i++) {
			names[i] = ItemAbility.valueOf(abilities[i]).toString();
		}
		return names;
	}

	public boolean hasAbility(String ability) {
		for (String s : abilities) {
			if (s.equals(ability)) {
				return true;
			}
		}
		return false;
	}

	public static ItemProperty get(String id) {
		return (items.containsKey(id)) ? items.get(id).copy() : null;
	}

	public static ItemProperty[] getItemList() {
		return items.values().toArray(new ItemProperty[items.size()]);
	}

}