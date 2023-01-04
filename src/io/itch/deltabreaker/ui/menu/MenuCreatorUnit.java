package io.itch.deltabreaker.ui.menu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.itch.deltabreaker.ai.AIType;
import io.itch.deltabreaker.core.FileManager;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.ItemAbility;
import io.itch.deltabreaker.object.item.ItemProperty;
import io.itch.deltabreaker.state.StateCreatorHub;
import io.itch.deltabreaker.state.StateManager;
import io.itch.deltabreaker.state.StateUnitCreator;

public class MenuCreatorUnit extends Menu {

	private Unit unit;
	private Vector3f cursorPos;

	public MenuCreatorUnit(Vector3f position, Unit unit) {
		super(position, new String[] { "Edit", "Equipment", unit.AIPattern.toString(), "Export", "Import" });
		this.unit = unit;
		cursorPos = Vector3f.add(StateManager.currentState.cursor.position, 0, 0, 0);
		StateManager.currentState.cursor.warpLocation(Vector3f.add(position, 0, -9, 0));
		StateManager.currentState.cursor.staticView = true;
	}

	public void tick() {
		options[2] = unit.AIPattern.toString();
		super.tick();
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("back")) {
				if (command.equals("")) {
					switch (selected) {

					case 0:
						StateUnitCreator.swap(this.unit, StateCreatorHub.STATE_ID);
						break;

					case 1:
						subMenu.add(new MenuCreatorUnitEquipment(Vector3f.add(position, width + 5, 0, 0), this.unit));
						break;

					case 2:
						subMenu.add(new MenuCreatorUnitAI(Vector3f.add(position, width + 5, 0, 0), this.unit));
						break;

					case 3:
						File dir = new File("save/custom/unit");
						if (!dir.exists()) {
							dir.mkdirs();
						}
						this.unit.saveUnit("save/custom/unit/" + this.unit.uuid + ".dat");
						break;

					case 4:
						subMenu.add(new MenuCreatorUnitImport(Vector3f.add(position, width + 5, 0, 0), this.unit).setParent(this));
						break;

					}
				}
			} else {
				close();
			}
		} else {
			subMenu.get(0).action(command, unit);
		}
	}

	@Override
	public void close() {
		super.close();
		StateManager.currentState.cursor.staticView = false;
		StateManager.currentState.cursor.warpLocation(Vector3f.add(cursorPos, 0, 0, 0));
	}

	public void closeAll() {
		super.closeAll();
		StateManager.currentState.cursor.staticView = false;
		StateManager.currentState.cursor.warpLocation(Vector3f.add(cursorPos, 0, 0, 0));
	}

}

class MenuCreatorUnitEquipment extends Menu {

	private Unit unit;

	public MenuCreatorUnitEquipment(Vector3f position, Unit unit) {
		super(position, new String[] { unit.weapon.name, "Skills", unit.armor.name, unit.accessory.name });
		this.unit = unit;
	}

	@Override
	public void tick() {
		options[0] = unit.weapon.name;
		options[2] = unit.armor.name;
		options[3] = unit.accessory.name;
		update();
		super.tick();
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("back")) {
				if (command.equals("")) {
					switch (selected) {

					case 0:
						subMenu.add(new MenuCreatorUnitEquipmentList(this, Vector3f.add(position, width + 5, 0, 0), this.unit, ItemProperty.TYPE_WEAPON,
								ItemProperty.searchForType(ItemProperty.TYPE_WEAPON, ItemProperty.getItemList(), true)));
						break;

					case 1:
						subMenu.add(new MenuCreatorUnitSkill(Vector3f.add(position, width + 5, 0, 0), this.unit.weapon));
						break;

					case 2:
						subMenu.add(new MenuCreatorUnitEquipmentList(this, Vector3f.add(position, width + 5, 0, 0), this.unit, ItemProperty.TYPE_ARMOR,
								ItemProperty.searchForType(ItemProperty.TYPE_ARMOR, ItemProperty.getItemList(), true)));
						break;

					case 3:
						subMenu.add(new MenuCreatorUnitEquipmentList(this, Vector3f.add(position, width + 5, 0, 0), this.unit, ItemProperty.TYPE_ACCESSORY,
								ItemProperty.searchForType(ItemProperty.TYPE_ACCESSORY, ItemProperty.getItemList(), true)));
						break;

					}
				}
			} else {
				close();
			}
		} else {
			subMenu.get(0).action(command, unit);
		}
	}

}

class MenuCreatorUnitEquipmentList extends Menu {

	private Unit unit;
	private ItemProperty[] items;
	private String type;

	public MenuCreatorUnitEquipmentList(Menu parent, Vector3f position, Unit unit, String type, ItemProperty[] items) {
		super(position, ItemProperty.toStringList(items));
		this.parent = parent;
		this.unit = unit;
		this.items = items;
		this.type = type;
	}

	@Override
	public void tick() {
		super.tick();
		position.setX(parent.position.getX() + parent.width + 5);
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("back")) {
				if (command.equals("")) {
					switch (type) {

					case ItemProperty.TYPE_WEAPON:
						this.unit.weapon = items[selected].copy();
						break;

					case ItemProperty.TYPE_ARMOR:
						this.unit.armor = items[selected].copy();
						break;

					case ItemProperty.TYPE_ACCESSORY:
						this.unit.accessory = items[selected].copy();
						break;

					}
					close();
				}
			} else {
				close();
			}
		} else {
			subMenu.get(0).action(command, unit);
		}
	}

}

class MenuCreatorUnitSkill extends Menu {

	private ItemProperty weapon;

	public MenuCreatorUnitSkill(Vector3f position, ItemProperty weapon) {
		super(position, new String[] { "Add", "Remove" });
		this.weapon = weapon;
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("back")) {
				if (command.equals("")) {
					switch (options[selected]) {

					case "Add":
						subMenu.add(new MenuCreatorUnitSkillAdd(Vector3f.add(position, width + 5, 0, 0), this.weapon));
						break;

					case "Remove":
						subMenu.add(new MenuCreatorUnitSkillRemove(Vector3f.add(position, width + 5, 0, 0), this.weapon));
						break;

					}
				}
			} else {
				close();
			}
		} else {
			subMenu.get(0).action(command, unit);
		}
	}

}

class MenuCreatorUnitSkillAdd extends Menu {

	private ItemProperty item;

	public MenuCreatorUnitSkillAdd(Vector3f position, ItemProperty item) {
		super(position, getReleventSkills(item));
		this.item = item;
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("back")) {
				if (command.equals("")) {
					String[] abilities = new String[item.abilities.length + 1];
					for (int i = 0; i < item.abilities.length; i++) {
						abilities[i] = item.abilities[i];
					}
					abilities[abilities.length - 1] = ItemAbility.getAbilityFromName(options[selected]).name();
					item.abilities = abilities;
					close();
				}
			} else {
				close();
			}
		} else {
			subMenu.get(0).action(command, unit);
		}
	}

	private static String[] getReleventSkills(ItemProperty item) {
		ArrayList<String> skills = new ArrayList<>();
		for (ItemAbility i : ItemAbility.values()) {
			boolean canAdd = true;
			for (String s : item.abilities) {
				if (i.name().equals(s)) {
					canAdd = false;
					break;
				}
			}
			if (canAdd) {
				skills.add(i.toString());
			}
		}
		return skills.toArray(new String[skills.size()]);
	}

}

class MenuCreatorUnitSkillRemove extends Menu {

	private ItemProperty item;

	public MenuCreatorUnitSkillRemove(Vector3f position, ItemProperty item) {
		super(position, item.getSkillsNames());
		this.item = item;
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("back")) {
				if (command.equals("")) {
					ArrayList<String> names = new ArrayList<>();
					for (int i = 0; i < item.abilities.length; i++) {
						if (i != selected) {
							names.add(item.abilities[i]);
						}
					}
					item.abilities = names.toArray(new String[names.size()]);
					options = item.getSkillsNames();
					update();
				}
			} else {
				close();
			}
		} else {
			subMenu.get(0).action(command, unit);
		}
	}

}

class MenuCreatorUnitAI extends Menu {

	private Unit unit;

	public MenuCreatorUnitAI(Vector3f position, Unit unit) {
		super(position, AIType.getNameList());
		this.unit = unit;
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("back")) {
				if (command.equals("")) {
					this.unit.AIPattern = AIType.values()[selected];
					close();
				}
			} else {
				close();
			}
		} else {
			subMenu.get(0).action(command, unit);
		}
	}

}

class MenuCreatorUnitImport extends Menu {

	private List<File> files;
	private Unit unit;

	public MenuCreatorUnitImport(Vector3f position, Unit unit) {
		super(position, getList());
		files = FileManager.getFiles("save/custom/unit");
		this.unit = unit;
	}

	private static String[] getList() {
		ArrayList<String> files = new ArrayList<>();

		for (File f : FileManager.getFiles("save/custom/unit")) {
			if (f.getName().endsWith(".dat")) {
				files.add(f.getName());
			}
		}

		return files.toArray(new String[files.size()]);
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("back")) {
				if (command.equals("")) {
					try {
						for (int i = 0; i < ((StateCreatorHub) StateManager.getState(StateCreatorHub.STATE_ID)).npcs.size(); i++) {
							if (((StateCreatorHub) StateManager.getState(StateCreatorHub.STATE_ID)).npcs.get(i).locX == this.unit.locX
									&& ((StateCreatorHub) StateManager.getState(StateCreatorHub.STATE_ID)).npcs.get(i).locY == this.unit.locY) {
								((StateCreatorHub) StateManager.getState(StateCreatorHub.STATE_ID)).npcs.remove(i);
								i--;
							}
						}
						((StateCreatorHub) StateManager.getState(StateCreatorHub.STATE_ID)).npcs.add(Unit.loadUnit(this.unit.locX, this.unit.locY, Vector4f.COLOR_BASE.copy(), files.get(selected).getPath()));
						closeAll();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				close();
			}
		} else {
			subMenu.get(0).action(command, unit);
		}
	}

}