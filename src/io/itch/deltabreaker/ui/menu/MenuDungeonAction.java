package io.itch.deltabreaker.ui.menu;

import java.awt.Dimension;
import java.util.ArrayList;

import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.effect.EffectPoof;
import io.itch.deltabreaker.event.Event;
import io.itch.deltabreaker.event.EventScript;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.Item;
import io.itch.deltabreaker.object.item.ItemAbility;
import io.itch.deltabreaker.object.item.ItemProperty;
import io.itch.deltabreaker.object.tile.Tile;
import io.itch.deltabreaker.state.StateDungeon;
import io.itch.deltabreaker.state.StateManager;
import io.itch.deltabreaker.ui.ItemInfoCard;
import io.itch.deltabreaker.ui.UIBox;

public class MenuDungeonAction extends Menu {

	private Unit unit;
	private StateDungeon context;
	private Vector3f cursorPos;

	public MenuDungeonAction(Vector3f position, Unit unit, StateDungeon context) {
		super(position, getOptions(unit, context));
		Startup.staticView.position = new Vector3f(position.getX() / 2 - 4 + getDimensions(options).width / 4, position.getY() / 2 - getDimensions(options).height / 4, Startup.staticView.position.getZ());
		StateManager.currentState.cursor.staticView = true;
		cursorPos = Vector3f.add(StateManager.currentState.cursor.position, 0, 0, 0);
		StateManager.currentState.cursor.warpLocation(Vector3f.add(position, 0, -9, 0));
		this.unit = unit;
		this.context = context;
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("return")) {
				if (unit.path.isEmpty()) {
					switch (options[selected]) {

					case "Attack":
						context.selectedAbility = ItemAbility.ITEM_ABILITY_ATTACK;
						context.selectedAbility.use(unit, context);
						height = 16;
						close();
						AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
						break;

					default:
						context.selectedAbility = ItemAbility.getAbilityFromName(options[selected]);
						if (context.selectedAbility.use(unit, context)) {
							height = 16;
							close();
						}
						AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
						break;

					case "Trade":
						if(context.freeRoamMode) {
							subMenu.add(new MenuTradeUnitSelect(Vector3f.add(position, width + 5, 0, 0), true));
						} else {
							context.selectedAbility = ItemAbility.getAbilityFromName(options[selected]);
							if (context.selectedAbility.use(unit, context)) {
								height = 16;
								close();
							}
						}
						AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
						break;
						
					case "Continue":
						context.alphaTo = 1;
						context.action = StateDungeon.ACTION_PROGRESS;
						close();
						AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
						break;

					case "Leave":
						// Laeve dungeon here
						break;

					case "Chest":
						// Removes the key from units inventory
						for (int i = 0; i < unit.getItemList().size(); i++) {
							if (unit.getItemList().get(i).type.equals(ItemProperty.TYPE_KEY_CHEST)) {
								unit.removeItem(unit.getItemList().get(i));
								break;
							}
						}

						// Determines which tile to use as reference
						Tile t = StateManager.currentState.tiles[unit.locX - 1][unit.locY];
						if (StateManager.currentState.tiles[unit.locX][unit.locY - 1].getProperty().equals(Tile.PROPERTY_CHEST)) {
							t = StateManager.currentState.tiles[unit.locX][unit.locY - 1];
						} else if (StateManager.currentState.tiles[unit.locX + 1][unit.locY].getProperty().equals(Tile.PROPERTY_CHEST)) {
							t = StateManager.currentState.tiles[unit.locX + 1][unit.locY];
						} else if (StateManager.currentState.tiles[unit.locX][unit.locY + 1].getProperty().equals(Tile.PROPERTY_CHEST)) {
							t = StateManager.currentState.tiles[unit.locX][unit.locY + 1];
						}

						// Sorts and chooses item to give
						t.action(unit, new String[] { "" + (context.dungeon.getTier() + 1) });

						closeAll();
						if (!context.freeRoamMode) {
							unit.setTurn(false);
							context.clearSelectedTiles();
							context.clearUnit();
						}
						AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
						break;

					case "Door":
						// Determines which tile to use as reference
						Tile t1 = StateManager.currentState.tiles[unit.locX - 1][unit.locY];
						if (StateManager.currentState.tiles[unit.locX][unit.locY - 1].getProperty().equals(Tile.PROPERTY_DOOR_LOCKED) || StateManager.currentState.tiles[unit.locX][unit.locY - 1].getProperty().equals(Tile.PROPERTY_DOOR)) {
							t1 = StateManager.currentState.tiles[unit.locX][unit.locY - 1];
						} else if (StateManager.currentState.tiles[unit.locX + 1][unit.locY].getProperty().equals(Tile.PROPERTY_DOOR_LOCKED)
								|| StateManager.currentState.tiles[unit.locX + 1][unit.locY].getProperty().equals(Tile.PROPERTY_DOOR)) {
							t1 = StateManager.currentState.tiles[unit.locX + 1][unit.locY];
						} else if (StateManager.currentState.tiles[unit.locX][unit.locY + 1].getProperty().equals(Tile.PROPERTY_DOOR_LOCKED)
								|| StateManager.currentState.tiles[unit.locX][unit.locY + 1].getProperty().equals(Tile.PROPERTY_DOOR)) {
							t1 = StateManager.currentState.tiles[unit.locX][unit.locY + 1];
						}

						t1.action(unit, new String[] {});

						closeAll();
						if (!context.freeRoamMode) {
							unit.setTurn(false);
							context.clearSelectedTiles();
							context.clearUnit();
						}
						AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
						break;

					case "Pick Up":
						for (int k = 0; k < context.items.size(); k++) {
							if (unit.locX == context.items.get(k).locX && unit.locY == context.items.get(k).locY) {
								ItemProperty item = context.items.get(k).item;
								if (unit.canAddItem(item)) {
									int overflow = unit.addItem(item);
									if (overflow == 0) {
										StateManager.currentState.effects.add(new EffectPoof(Vector3f.add(context.items.get(k).position, 0, StateManager.currentState.tiles[unit.locX][unit.locY].getPosition().getY(), 0)));
										context.items.remove(context.items.get(k));
										k--;
									}
									closeAll();
									if (!context.freeRoamMode) {
										unit.setTurn(false);
										context.clearSelectedTiles();
										context.clearUnit();
									}
									AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
									AudioManager.getSound("loot.ogg").play(AudioManager.defaultMainSFXGain, false);
								} else {
									AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
								}
								break;
							}
						}
						break;

					case "Items":
						// Check if the unit has any items and display a message if not. Add a new sub
						// menu if so.
						if (unit.getItemList().size() > 0) {
							subMenu.add(new MenuDungeonActionItems(Vector3f.add(position, width + 5, 0, 0), unit, context).setParent(this));
						} else {
							AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
						}
						break;

					case "Wait":
						unit.setTurn(false);
						context.clearSelectedTiles();
						context.clearUnit();
						close();
						AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);

						for (EventScript e : context.eventList.values()) {
							String[] args = e.activator.split(" ");
							if (args[0].equals(EventScript.ACTIVATOR_WAIT) && unit.locX == Integer.parseInt(args[1]) && unit.locY == Integer.parseInt(args[2])) {
								context.events.add(new Event(e));
							}
						}
						break;

					case "Switch":
						subMenu.add(new MenuDungeonActionSwitch(Vector3f.add(position, width + 5, 0, 0), context).setParent(this));
						break;

					}
				}
			} else {
				if (open) {
					if (!context.freeRoamMode) {
						unit.reset();
						context.clearSelectedTiles();
						context.clearUnit();
					}
					close();
					AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
				}
			}
		} else {
			subMenu.get(0).action(command, unit);
		}
	}

	// Determine whoich menu options are valid
	private static String[] getOptions(Unit unit, StateDungeon context) {
		ArrayList<String> options = new ArrayList<>();

		int xPos = (unit.path.size() > 0) ? unit.path.get(0).x : unit.locX;
		int yPos = (unit.path.size() > 0) ? unit.path.get(0).y : unit.locY;

		// Check for enemies and allies in range and validate weapon type
		if (!context.freeRoamMode) {
			boolean attack = false;
			for (Unit u : ((StateDungeon) StateManager.currentState).enemies) {
				if (Math.abs(u.locX - xPos) + Math.abs(u.locY - yPos) <= unit.weapon.range) {
					attack = true;
					break;
				}
			}
			boolean assist = false;
			for (Unit u : Inventory.active) {
				if (u != unit) {
					if (Math.abs(u.locX - xPos) + Math.abs(u.locY - yPos) <= unit.weapon.range) {
						assist = true;
						break;
					}
				}
			}

			if (attack) {
				options.add("Attack");
			}

			// Check if weapon has a usable ability
			for (String s : unit.weapon.abilities) {
				ItemAbility a = ItemAbility.valueOf(s);
				if (a.activated && (a.target.equals(ItemAbility.TARGET_NONE) || (assist && a.target.equals(ItemAbility.TARGET_UNIT)) || (attack && a.target.equals(ItemAbility.TARGET_ENEMY)))) {
					options.add(ItemAbility.valueOf(s).toString());
				}
			}
		}

		// Check for keys in units inventory and then chests around the unit
		boolean hasKey = false;
		boolean besideChest = false;
		for (ItemProperty i : unit.getItemList()) {
			if (i.type.equals(ItemProperty.TYPE_KEY_CHEST)) {
				hasKey = true;
				break;
			}
		}
		if ((xPos > 0 && StateManager.currentState.tiles[xPos - 1][yPos].getProperty().equals(Tile.PROPERTY_CHEST))
				|| (xPos < StateManager.currentState.tiles.length - 1 && StateManager.currentState.tiles[xPos + 1][yPos].getProperty().equals(Tile.PROPERTY_CHEST))
				|| (yPos > 0 && StateManager.currentState.tiles[xPos][yPos - 1].getProperty().equals(Tile.PROPERTY_CHEST))
				|| (yPos < StateManager.currentState.tiles[0].length - 1 && StateManager.currentState.tiles[xPos][yPos + 1].getProperty().equals(Tile.PROPERTY_CHEST))) {
			besideChest = true;
		}
		if (besideChest && (hasKey || (unit.weapon.hasAbility(ItemAbility.ITEM_ABILITY_LOCKSMITH) && ItemAbility.ITEM_ABILITY_LOCKSMITH.isUnlocked(unit.weapon)))) {
			options.add("Chest");
		}

		// Check for keys in units inventory and then chests around the unit
		if ((xPos > 0 && StateManager.currentState.tiles[xPos - 1][yPos].getProperty().equals(Tile.PROPERTY_DOOR_LOCKED))
				|| (xPos < StateManager.currentState.tiles.length - 1 && StateManager.currentState.tiles[xPos + 1][yPos].getProperty().equals(Tile.PROPERTY_DOOR_LOCKED))
				|| (yPos > 0 && StateManager.currentState.tiles[xPos][yPos - 1].getProperty().equals(Tile.PROPERTY_DOOR_LOCKED))
				|| (yPos < StateManager.currentState.tiles[0].length - 1 && StateManager.currentState.tiles[xPos][yPos + 1].getProperty().equals(Tile.PROPERTY_DOOR_LOCKED))) {
			if (!hasKey) {
				for (ItemProperty i : unit.getItemList()) {
					if (i.type.equals(ItemProperty.TYPE_KEY_CHEST)) {
						hasKey = true;
						break;
					}
				}
			}
			if (hasKey || (unit.weapon.hasAbility(ItemAbility.ITEM_ABILITY_LOCKSMITH) && ItemAbility.ITEM_ABILITY_LOCKSMITH.isUnlocked(unit.weapon))) {
				options.add("Door");
			}
		} else if ((xPos > 0 && StateManager.currentState.tiles[xPos - 1][yPos].getProperty().equals(Tile.PROPERTY_DOOR))
				|| (xPos < StateManager.currentState.tiles.length - 1 && StateManager.currentState.tiles[xPos + 1][yPos].getProperty().equals(Tile.PROPERTY_DOOR))
				|| (yPos > 0 && StateManager.currentState.tiles[xPos][yPos - 1].getProperty().equals(Tile.PROPERTY_DOOR))
				|| (yPos < StateManager.currentState.tiles[0].length - 1 && StateManager.currentState.tiles[xPos][yPos + 1].getProperty().equals(Tile.PROPERTY_DOOR))) {
			options.add("Door");
		}

		// Checks for the units inventory size and then if they're on an item
		for (Item i : ((StateDungeon) StateManager.currentState).items) {
			if (xPos == i.locX && yPos == i.locY) {
				if (unit.canAddItem(i.item)) {
					options.add("Pick Up");
				}
				break;
			}
		}

		// Checks inventory size
		if (unit.getItemList().size() > 0) {
			options.add("Items");
		}

		for (Unit u : Inventory.active) {
			if (!context.freeRoamMode) {
				if (u == unit) {
					continue;
				}
				if (Math.abs(u.locX - xPos) + Math.abs(u.locY - yPos) > 1) {
					continue;
				}
			}
			if (u.getItemList().size() == 0 && unit.getItemList().size() == 0) {
				continue;
			}
			options.add("Trade");
			break;
		}

		// Check to see if the unit is on the stairs tile
		if (StateManager.currentState.tiles[xPos][yPos].containsTag(Tile.TAG_STAIRS)) {
			if (context.dungeon.baseLevel < context.dungeon.getBottomFloor()) {
				options.add("Continue");
			} else {
				options.add("Leave");
			}
		}

		if (!context.freeRoamMode) {
			options.add("Wait");
		} else {
			options.add("Switch");
		}

		return options.toArray(new String[options.size()]);
	}

	@Override
	public void update() {
		options = getOptions(unit, context);
		super.update();
	}

	@Override
	public void close() {
		super.close();
		StateManager.currentState.cursor.staticView = false;
		StateManager.currentState.cursor.warpLocation(Vector3f.add(cursorPos, 0, 0, 0));
	}

	@Override
	public void closeAll() {
		super.close();
		StateManager.currentState.cursor.staticView = false;
		StateManager.currentState.cursor.warpLocation(Vector3f.add(cursorPos, 0, 0, 0));
		Startup.staticView.setTargetPosition(new Vector3f(0, 0, 0));
	}

}

// Used to separate the item menu code since it is a generous amount
class MenuDungeonActionItems extends Menu {

	private Unit unit;
	private StateDungeon context;

	public MenuDungeonActionItems(Vector3f position, Unit unit, StateDungeon context) {
		super(position, getItems(unit), getDimensions(getItems(unit)).width, getDimensions(getItems(unit)).height);
		this.unit = unit;
		this.context = context;
	}

	protected static Dimension getDimensions(String[] text) {
		String longest = "";
		int height = 6;
		int times = 0;
		for (String s : text) {
			if (s.length() > longest.length()) {
				longest = s;
			}
			if (times < 4) {
				height += 18;
				times++;
			}
		}
		return new Dimension(longest.length() * 6 + 34, height);
	}

	@Override
	public void tick() {
		super.tick();

		// Keeps the menu updated with the list of items the unit has
		ArrayList<String> items = new ArrayList<>();
		for (ItemProperty i : unit.getItemList()) {
			items.add(i.name);
		}
		options = items.toArray(new String[items.size()]);
		Dimension d = getDimensions(options);
		width = d.width;
		openTo = d.height;

		selected = AdvMath.inRange(selected, 0, options.length - 1);

		if (subMenu.size() == 0 && open && StateManager.currentState.status.size() == 0 && StateManager.currentState.itemInfo.size() == 0) {
			StateManager.currentState.cursor.setLocation(new Vector3f(position.getX() - 10, position.getY() - 12 - 18 * Math.min(3, selected), position.getZ() + 2));
		}
	}

	@Override
	public void render() {
		UIBox.render(position, width, height);
		for (int i = 0; i < Math.min(4, options.length); i++) {
			int item = i + AdvMath.inRange(selected - 3, 0, unit.getItemList().size() - 1);
			if (i * 18 + 12 < height) {
				TextRenderer.render(options[item], Vector3f.add(position, 4, -i * 18 - 9, 1), new Vector3f(0, 0, 0), scale, new Vector4f(ItemProperty.colorList[unit.getItemList().get(item).tier], 1), true);
			}
			if (i * 18 + 16 < height && open) {
				BatchSorter.add(unit.getItemList().get(item).model, unit.getItemList().get(item).texture, "static_3d", unit.getItemList().get(item).material, Vector3f.add(position, width - 24, -i * 18 - 8, 1), new Vector3f(0, 0, 0),
						Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, false, true);
				if (unit.getItemList().get(item).type.equals(ItemProperty.TYPE_USABLE) || unit.getItemList().get(item).type.equals(ItemProperty.TYPE_OTHER)) {
					TextRenderer.render("" + unit.getItemList().get(item).stack, Vector3f.add(position, width - 18, -i * 18 - 14, 2), new Vector3f(0, 0, 0), Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, true);
				}
			}
		}
		if (subMenu.size() > 0) {
			subMenu.get(0).render();
		}
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("return")) {
				ArrayList<String> options = new ArrayList<String>();

				if (!StateDungeon.getCurrentContext().freeRoamMode && unit.getItemList().get(selected).type.equals(ItemProperty.TYPE_USABLE)) {
					options.add("Use");
				}

				// Get item type and add equip option if applicable
				String type = unit.getItemList().get(selected).type;
				if (type.equals(ItemProperty.TYPE_WEAPON) || type.equals(ItemProperty.TYPE_ARMOR) || type.equals(ItemProperty.TYPE_ACCESSORY)) {
					options.add("Equip");
				}

				options.add("Info");

				// Makes sure the unit is not currently on an item
				boolean onItem = false;
				for (Item i : ((StateDungeon) StateManager.currentState).items) {
					if (i.locX == unit.locX && i.locY == unit.locY) {
						onItem = true;
					}
				}
				if (!onItem) {
					options.add("Drop");
				}

				subMenu.add(new MenuDungeonActionItemsAction(Vector3f.add(position, width + 5, 0, 0), options.toArray(new String[options.size()]), context).setParent(this));
			} else {
				close();
				AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
			}
		} else {
			subMenu.get(0).action(command, unit);
		}
	}

	// Return a string array with the names of the items the unit has
	private static String[] getItems(Unit unit) {
		ArrayList<String> items = new ArrayList<>();
		for (ItemProperty i : unit.getItemList()) {
			items.add(i.name);
		}
		return items.toArray(new String[items.size()]);
	}

}

class MenuDungeonActionItemsAction extends Menu {

	private StateDungeon context;

	public MenuDungeonActionItemsAction(Vector3f position, String[] options, StateDungeon context) {
		super(position, options);
		this.context = context;
	}

	@Override
	public void tick() {
		super.tick();

		// Keeps the position updated since the parent menu actively changes
		position = Vector3f.add(parent.position, parent.width + 5, 0, 0);
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("return")) {
				switch (options[selected]) {

				case "Use":
					context.selectedItem = unit.getItemList().get(parent.selected);
					unit.getItemList().get(parent.selected).use(unit, context);
					closeAll();
					AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
					break;

				case "Equip":
					switch (unit.getItemList().get(parent.selected).type) {

					case ItemProperty.TYPE_WEAPON:
						unit.lastWeapon = unit.weapon.id;
						ItemProperty wepTemp = unit.weapon;
						unit.weapon = unit.getItemList().get(parent.selected);
						unit.removeItem(unit.getItemList().get(parent.selected));
						if (wepTemp.type.equals(ItemProperty.TYPE_WEAPON)) {
							unit.addItem(wepTemp);
						}
						break;

					case ItemProperty.TYPE_ARMOR:
						ItemProperty armorTemp = unit.armor;
						unit.armor = unit.getItemList().get(parent.selected);
						unit.removeItem(unit.getItemList().get(parent.selected));
						if (armorTemp.id.equals(ItemProperty.TYPE_ARMOR)) {
							unit.addItem(armorTemp);
						}
						break;

					case ItemProperty.TYPE_ACCESSORY:
						ItemProperty accTemp = unit.accessory;
						unit.accessory = unit.getItemList().get(parent.selected);
						unit.removeItem(unit.getItemList().get(parent.selected));
						if (accTemp.id.equals(ItemProperty.TYPE_ACCESSORY)) {
							unit.addItem(accTemp);
						}
						break;

					}
					parent.parent.update();
					close();
					AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
					break;

				case "Info":
					context.itemInfo.add(new ItemInfoCard(Vector3f.add(position, width + 5, 0, 0), unit.getItemList().get(parent.selected), unit));
					AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
					break;

				case "Drop":
					if (unit.getItemList().get(parent.selected).stack > 1) {
						subMenu.add(new MenuDungeonActionItemsActionDrop(Vector3f.add(position, width + 5, 0, 0), context, unit.getItemList().get(parent.selected)).setParent(this));
					} else {
						context.items.add(new Item(new Vector3f(unit.locX * 16, 24, unit.locY * 16), unit.getItemList().get(parent.selected).copy()));
						StateManager.currentState.effects.add(new EffectPoof(Vector3f.add(new Vector3f(unit.locX * 16, 20, unit.locY * 16), 0, StateManager.currentState.tiles[unit.locX][unit.locY].getPosition().getY(), 0)));
						unit.removeItem(unit.getItemList().get(parent.selected));
						close();
						AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
					}
					break;

				}
			} else {
				close();
				AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
			}
		} else {
			subMenu.get(0).action(command, unit);
		}
	}

}

class MenuDungeonActionItemsActionDrop extends Menu {

	private int amt = 1;
	private ItemProperty item;
	private StateDungeon context;

	public MenuDungeonActionItemsActionDrop(Vector3f position, StateDungeon context, ItemProperty item) {
		super(position, new String[] { "Amount 1" });
		this.context = context;
		this.item = item;
	}

	public void tick() {
		super.tick();
		update();
		options[0] = "Amount " + amt;
	}

	@Override
	public void move(int amt) {
		AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
		this.amt += amt * -1;
		if (this.amt < 1) {
			this.amt = item.stack;
		}
		if (this.amt > item.stack) {
			this.amt = 1;
		}
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("return")) {
				ItemProperty newItem = unit.getItemList().get(parent.parent.selected).copy();
				newItem.stack = amt;
				context.items.add(new Item(new Vector3f(unit.locX * 16, 24, unit.locY * 16), newItem));
				StateManager.currentState.effects.add(new EffectPoof(Vector3f.add(new Vector3f(unit.locX * 16, 20, unit.locY * 16), 0, StateManager.currentState.tiles[unit.locX][unit.locY].getPosition().getY(), 0)));
				unit.removeItem(item, amt);
				close();
				parent.close();
				AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
			} else {
				close();
				AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
			}
		} else {
			subMenu.get(0).action(command, unit);
		}
	}

}

class MenuDungeonActionSwitch extends Menu {

	private StateDungeon context;

	public MenuDungeonActionSwitch(Vector3f position, StateDungeon context) {
		super(position, getUnitList());
		this.context = context;
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("return")) {
				StateManager.currentState.effects.add(new EffectPoof(new Vector3f(Inventory.active.get(context.roamUnit).x, 13 + Inventory.active.get(context.roamUnit).height, Inventory.active.get(context.roamUnit).y + 2)));
				int nextUnit = selected;
				Inventory.active.get(nextUnit).placeAt(Inventory.active.get(context.roamUnit).locX, Inventory.active.get(context.roamUnit).locY);
				Inventory.active.get(nextUnit).height = Inventory.active.get(context.roamUnit).height;
				context.roamUnit = nextUnit;
				closeAll();
				AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
			} else {
				close();
				AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
			}
		} else {
			subMenu.get(0).action(command, unit);
		}
	}

	private static String[] getUnitList() {
		String[] names = new String[Inventory.active.size()];

		for (int i = 0; i < names.length; i++) {
			names[i] = Inventory.active.get(i).name;
		}

		return names;
	}

}