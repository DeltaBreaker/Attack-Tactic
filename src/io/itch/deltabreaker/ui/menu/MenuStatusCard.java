package io.itch.deltabreaker.ui.menu;

import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.ItemProperty;
import io.itch.deltabreaker.state.StateManager;
import io.itch.deltabreaker.ui.UIBox;

public class MenuStatusCard extends Menu {

	public Unit u;
	private Vector3f cursorPos;

	public MenuStatusCard(Vector3f position, Unit u, boolean direct) {
		super(position, getItemList(u), 128, 0);
		this.u = u;
		openTo = 90;
		if (direct) {
			StateManager.currentState.cursor.staticView = true;
			cursorPos = Vector3f.add(StateManager.currentState.cursor.position, 0, 0, 0);
			StateManager.currentState.cursor.warpLocation(Vector3f.add(position, 0, -9, 0));
		}
	}

	private static String[] getItemList(Unit u) {
		String[] items = new String[9];
		return items;
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("back")) {
				switch (command) {

				case "left":
					if (selected > 0) {
						selected--;
						AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
					}
					break;

				case "right":
					if (selected < 8) {
						selected++;
						AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
					}
					break;

				default:
					switch (selected) {

					case 0:
						break;

					case 1:
						if (!u.weapon.type.equals(ItemProperty.TYPE_EMPTY)) {
							subMenu.add(new MenuItemInfoCard(Vector3f.add(position, width + 5, 0, 0), u.weapon, u));
						}
						break;

					case 2:
						if (!u.armor.type.equals(ItemProperty.TYPE_EMPTY)) {
							subMenu.add(new MenuItemInfoCard(Vector3f.add(position, width + 5, 0, 0), u.armor, u));
						}
						break;

					case 3:
						if (!u.accessory.type.equals(ItemProperty.TYPE_EMPTY)) {
							subMenu.add(new MenuItemInfoCard(Vector3f.add(position, width + 5, 0, 0), u.accessory, u));
						}
						break;

					default:
						if (u.getItemList().size() > selected - 4) {
							subMenu.add(new MenuItemInfoCard(Vector3f.add(position, width + 5, 0, 0), u.getItemList().get(selected - 4), u));
						}
						break;

					}
					break;

				}
			} else {
				close();
				AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
			}
		} else {
			subMenu.get(0).action(command, u);
		}
	}

	public void tick() {
		if (subMenu.size() > 0) {
			subMenu.get(0).tick();
			if (!subMenu.get(0).open && subMenu.get(0).height <= 16) {
				subMenu.remove(0);
			}
		}
		if (open) {
			if (height < openTo) {
				height = Math.min(height + 4, openTo);
			}
		} else {
			if (height > 16) {
				height = Math.max(height - 4, 16);
			}
		}
		if (subMenu.size() == 0 && open) {
			Startup.staticView.targetPosition = new Vector3f(position.getX() / 2f - 2 + width / 4f, position.getY() / 2 - openTo / 4f + 1, Startup.staticView.position.getZ());
			if (selected == 0) {
				StateManager.currentState.cursor.setLocation(new Vector3f(position.getX() - 10, position.getY() - 9 - 8 * Math.min(4, selected), position.getZ() + 4));
			} else if (selected < 4) {
				StateManager.currentState.cursor.setLocation(new Vector3f(position.getX() + 38 + (selected - 1) * 16, position.getY() - 60, position.getZ() + 3));
			} else if (selected < 9) {
				StateManager.currentState.cursor.setLocation(new Vector3f(position.getX() + 38 + (selected - 4) * 16, position.getY() - 76, position.getZ() + 3));
			}
		}
	}

	public void move(int amt) {
		if (subMenu.size() == 0) {
			if (selected == 0) {
				if (amt > 0) {
					selected = 1;
					AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
				}
			} else if (selected < 4) {
				if (amt > 0) {
					selected += 3;
					AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
				} else {
					selected = 0;
					AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
				}
			} else if (selected < 9) {
				if (amt < 1) {
					selected = AdvMath.inRange(selected - 3, 1, 3);
					AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
				}
			}
		} else {
			subMenu.get(0).move(amt);
		}
	}

	public void render() {
		if (subMenu.size() > 0) {
			subMenu.get(0).render();
		}
		UIBox.render(position, width, height);
		String[] text = { u.name, "lv " + u.level, "exp " + u.exp, "hp  " + u.currentHp + "_" + u.hp, "mov " + Math.max(u.movement - 2, 0), "atk " + u.atk, "mag " + u.mag, "spd " + u.spd, "def " + u.def, "res " + u.res };
		int[] offsets = { u.offsetHp, u.offsetMovement, u.offsetAtk, u.offsetMag, u.offsetSpd, u.offsetDef, u.offsetRes };
		for (int i = 0; i < text.length; i++) {
			Vector4f color = new Vector4f(1, 1, 1, 1);
			if (i == 0) {
				if (!Inventory.units.contains(u) && !Inventory.active.contains(u)) {
					color = Vector4f.COLOR_RED;
				} else {
					color = Vector4f.COLOR_BLUE;
				}
			}
			if (height > 6 + i * 8) {
				if (i > 2) {
					if (offsets[i - 3] > 0) {
						color = Vector4f.COLOR_GREEN;
					}
					if (offsets[i - 3] < 0) {
						color = Vector4f.COLOR_RED;
					}
				}
				TextRenderer.render(text[i], Vector3f.add(position, 4, -5 - i * 8, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f), color, true);
			}
		}

		if (height > 37) {
			TextRenderer.render("Inventory", Vector3f.add(position, 48, -45, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f), new Vector4f(1, 1, 1, 1), true);
		}
		if (height > 69) {
			if (!u.weapon.type.equals(ItemProperty.TYPE_EMPTY)) {
				BatchSorter.add(u.weapon.model, u.weapon.texture, "static_3d", u.weapon.material.toString(), Vector3f.add(position, 48, -57, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f), new Vector4f(1, 1, 1, 1), false, true);
			}
			if (!u.armor.type.equals(ItemProperty.TYPE_EMPTY)) {
				BatchSorter.add(u.armor.model, u.armor.texture, "static_3d", u.armor.material.toString(), Vector3f.add(position, 64, -57, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f), new Vector4f(1, 1, 1, 1), false, true);
			}
			if (!u.accessory.type.equals(ItemProperty.TYPE_EMPTY)) {
				BatchSorter.add(u.accessory.model, u.accessory.texture, "static_3d", u.accessory.material.toString(), Vector3f.add(position, 80, -57, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f), new Vector4f(1, 1, 1, 1),
						false, true);
			}
		}
		if (height > 85) {
			for (int i = 0; i < u.getItemList().size(); i++) {
				ItemProperty item = u.getItemList().get(i);
				if (!item.id.equals(ItemProperty.TYPE_EMPTY)) {
					BatchSorter.add(item.model, item.texture, "static_3d", item.material.toString(), Vector3f.add(position, 48 + i * 16, -73, 1), Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, false, true);
					if (item.type.equals(ItemProperty.TYPE_USABLE) || item.type.equals(ItemProperty.TYPE_OTHER)) {
						TextRenderer.render("" + item.stack, Vector3f.add(position, 54 + i * 16 - (("" + item.stack).length() - 1) * 6, -78, 2), Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, true);
					}
				}
			}
		}
	}

	public void close() {
		open = false;
		if (cursorPos != null) {
			StateManager.currentState.cursor.staticView = false;
			StateManager.currentState.cursor.warpLocation(Vector3f.add(cursorPos, 0, 0, 0));
		}
	}
}
