package io.itch.deltabreaker.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.ItemProperty;

public class Inventory {

	public static ArrayList<HeaderData> saveSlots = new ArrayList<>();

	// This class contains all data that should be written to a save file

	public static ArrayList<Unit> units = new ArrayList<>(); // A complete array of the units the player has
	public static ArrayList<Unit> active = new ArrayList<>(); // A list of units currently being used
	public static HashMap<String, Unit> loaded = new HashMap<>(); // Every unit currently loaded accessed by UUID

	public static HashMap<String, Integer> variables = new HashMap<>();
	public static HashMap<String, ArrayList<String>> lists = new HashMap<>();
	private static ArrayList<ItemProperty> items = new ArrayList<ItemProperty>();
	public static int gold = 0;
	public static String loadMap = "";

	// Header info
	public static String header = "";
	public static long playtime = 0;

	public static void addItem(ItemProperty item) {
		if (item.type.equals(ItemProperty.TYPE_USABLE) || item.type.equals(ItemProperty.TYPE_OTHER)) {
			for (ItemProperty i : items) {
				if (i.id.equals(item.id)) {
					if (i.stack < ItemProperty.STACK_CAP) {
						int overflow = Math.max(i.stack + item.stack, ItemProperty.STACK_CAP) % ItemProperty.STACK_CAP;
						i.stack += (item.stack - overflow);
						item.stack = overflow;
						break;
					}
				}
			}
			while (item.stack > 0) {
				ItemProperty copy = item.copy();
				int overflow = Math.max(item.stack, ItemProperty.STACK_CAP) % ItemProperty.STACK_CAP;
				copy.stack = item.stack - overflow;
				item.stack = overflow;
				items.add(copy);
			}
		} else {
			items.add(item.copy());
		}
	}

	public static void addItem(ItemProperty item, int stack) {
		if (item.type.equals(ItemProperty.TYPE_USABLE) || item.type.equals(ItemProperty.TYPE_OTHER)) {
			for (ItemProperty i : items) {
				if (i.id.equals(item.id)) {
					if (i.stack < ItemProperty.STACK_CAP) {
						int overflow = Math.max(i.stack + stack, ItemProperty.STACK_CAP) % ItemProperty.STACK_CAP;
						i.stack += (stack - overflow);
						item.stack -= (stack - overflow);
						stack -= (stack - overflow);
						if(stack <= 0) {
							break;
						}
					}
				}
			}
			if(stack > 0) {
				ItemProperty copy = item.copy();
				copy.stack = stack;
				item.stack -= stack;
				items.add(copy);
			}
		} else {
			items.add(item.copy());
		}
	}
	
	public static void saveHeader(long time) {
		File dir = new File("save/" + time);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		File file = new File(dir + "/header.dat");
		try {
			DataOutputStream out = new DataOutputStream(new FileOutputStream(file));

			out.writeLong(time);
			out.writeUTF(header);
			out.writeLong(playtime);

			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void saveGame(long time) {
		File dir = new File("save/" + time);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		File file = new File(dir + "/game.dat");

		try {
			DataOutputStream out = new DataOutputStream(new FileOutputStream(file));

			out.writeUTF(loadMap);
			out.writeInt(gold);

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

			for (File f : FileManager.getFiles("save/" + time + "/unit")) {
				f.delete();
			}
			for (Unit u : Inventory.units) {
				u.saveUnit("save/" + time + "/unit/" + u.uuid + ".dat");
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

	public static void loadHeaderData() {
		saveSlots.clear();
		for (File f : FileManager.getFiles("save")) {
			if (f.getName().equals("header.dat")) {
				try {
					DataInputStream in = new DataInputStream(new FileInputStream(f));

					long slot = in.readLong();
					String header = in.readUTF();
					long playtime = in.readLong();

					saveSlots.add(new HeaderData(slot, header, playtime));

					in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void loadGame(int slot) {
		HeaderData headerData = saveSlots.get(slot);
		File dir = new File("save/" + headerData.slot);

		try {
			DataInputStream in = new DataInputStream(new FileInputStream(dir + "/game.dat"));

			header = headerData.header;
			playtime = headerData.playtime;

			loadMap = in.readUTF();
			gold = in.readInt();

			items.clear();
			int loopSize = in.readInt();
			for (int i = 0; i < loopSize; i++) {
				ItemProperty item = ItemProperty.get(in.readUTF());
				item.uuid = in.readUTF();

				if (item.type.equals(ItemProperty.TYPE_WEAPON) || item.type.equals(ItemProperty.TYPE_ACCESSORY) || item.type.equals(ItemProperty.TYPE_EMPTY)) {
					int abilitySize = in.readInt();
					String[] abilities = new String[abilitySize];
					for (int j = 0; j < abilitySize; j++) {
						abilities[i] = in.readUTF();
					}
				}
			}

			variables.clear();
			loopSize = in.readInt();
			for (int i = 0; i < loopSize; i++) {
				variables.put(in.readUTF(), in.readInt());
			}

			lists.clear();
			loopSize = in.readInt();
			for (int i = 0; i < loopSize; i++) {
				String key = in.readUTF();

				int listSize = in.readInt();
				ArrayList<String> list = new ArrayList<>();
				for (int j = 0; j < listSize; j++) {
					list.add(in.readUTF());
				}
				lists.put(key, list);
			}

			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Inventory.loaded.clear();
		Inventory.units.clear();
		Inventory.active.clear();
		for (File f : FileManager.getFiles(dir + "/unit")) {
			if (f.getName().endsWith(".dat")) {
				try {
					Inventory.units.add(Unit.loadUnit(0, 0, new Vector4f(1, 1, 1, 1), f.getPath()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String getHeader(int i) {
		return saveSlots.get(i).header;
	}

	public static long getSlot(int i) {
		return saveSlots.get(i).slot;
	}

	public static long getPlaytime(int i) {
		return saveSlots.get(i).playtime;
	}

	public static ArrayList<ItemProperty> getItemList() {
		return items;
	}

}

class HeaderData {

	public long slot;
	public String header;
	public long playtime;

	public HeaderData(long slot, String header, long playtime) {
		this.slot = slot;
		this.header = header;
		this.playtime = playtime;
	}

	public String getHeader() {
		return header;
	}

}