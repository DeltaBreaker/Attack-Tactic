package io.itch.deltabreaker.core;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.ItemProperty;

public class Inventory {

	// This class contains all data that should be written to a save file

	public static ArrayList<Unit> units = new ArrayList<>(); // A complete array of the units the player has
	public static ArrayList<Unit> active = new ArrayList<>(); // A list of units currently being used
	public static HashMap<String, Unit> loaded = new HashMap<>(); // Every unit currently loaded accessed by UUID

	public static HashMap<String, Integer> variables = new HashMap<>();
	public static HashMap<String, ArrayList<String>> lists = new HashMap<>();
	private static ArrayList<ItemProperty> items = new ArrayList<ItemProperty>();
	public static int gold = 0;
	public static String loadScript = "";

	// Header info
	public static byte slot = 0;
	public static String header = "";
	public static long playtime = 0;

	public static int addItem(ItemProperty item) {
		if (item.type.equals(ItemProperty.TYPE_USABLE) || item.type.equals(ItemProperty.TYPE_OTHER)) {
			for (ItemProperty i : items) {
				if (i.id.equals(item.id)) {
					if (i.stack < ItemProperty.STACK_CAP) {
						int overflow = Math.max(i.stack + item.stack, ItemProperty.STACK_CAP) % ItemProperty.STACK_CAP;
						i.stack += (item.stack - overflow);
						item.stack = overflow;
						return overflow;
					} else {
						return item.stack;
					}
				}
			}
		}
		items.add(item.copy());
		return 0;
	}

	public static void saveHeader() {
		File dir = new File("save/slot_" + slot);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		File file = new File(dir + "/header.dat");
		try {
			DataOutputStream out = new DataOutputStream(new FileOutputStream(file));

			out.writeInt(slot);
			out.writeUTF(header);
			out.writeLong(playtime);

			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void saveGame(int slot) {
		File dir = new File("save/slot_" + slot);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		File file = new File(dir + "/game.dat");

		try {
			DataOutputStream out = new DataOutputStream(new FileOutputStream(file));

			out.writeUTF(loadScript);
			out.writeInt(gold);

			File itemsDir = new File(dir + "/item");
			if (!itemsDir.exists()) {
				itemsDir.mkdirs();
			}

			out.writeInt(items.size());
			for (ItemProperty i : items) {
				out.writeUTF(i.id);
				out.writeUTF(i.uuid);

				if (i.type.equals(ItemProperty.TYPE_WEAPON) || i.type.equals(ItemProperty.TYPE_ACCESSORY) || i.type.equals(ItemProperty.TYPE_EMPTY)) {
					out.writeInt(i.abilities.length);
					for (String s : i.abilities) {
						out.writeUTF(s);
					}
				}
			}

			File unitsDir = new File(dir + "/unit");
			if (!unitsDir.exists()) {
				unitsDir.mkdirs();
			}

			out.writeInt(units.size());
			for (Unit u : Inventory.units) {
				u.saveUnit("save/slot_" + slot + "/unit/" + u.uuid + ".dat");
			}

			out.writeInt(variables.size());
			for (String s : variables.keySet()) {
				out.writeUTF(s);
				out.writeInt(variables.get(s));
			}

			out.writeInt(lists.size());
			for (String s : lists.keySet()) {
				out.writeUTF(s);
				out.writeInt(lists.get(s).size());
				for (String v : lists.get(s)) {
					out.writeUTF(v);
				}
			}

			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}