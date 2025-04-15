package io.itch.deltabreaker.ui.menu;

import java.awt.Dimension;
import java.util.ArrayList;

import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.ItemAbility;
import io.itch.deltabreaker.object.item.ItemProperty;
import io.itch.deltabreaker.state.StateManager;
import io.itch.deltabreaker.ui.UIBox;

public class MenuSkillTransfer extends Menu {

	private ItemProperty weapon;
	private static ArrayList<ItemProperty> gems = new ArrayList<>();
	private Vector3f cursorPos;
	private int selGem = 0;

	public MenuSkillTransfer(Vector3f position, ItemProperty weapon) {
		super(position, getSkillList());
		StateManager.currentState.cursor.staticView = true;
		cursorPos = Vector3f.add(StateManager.currentState.cursor.position, 0, 0, 0);
		StateManager.currentState.cursor.warpLocation(Vector3f.add(position, 0, -9, 0));
		this.weapon = weapon;
		width = 132;
		height = 94;
		openTo = 94;
	}

	protected static Dimension getDimensions(String[] text) {
		String longest = "";
		int height = 10;
		int times = 0;
		for (String s : text) {
			if (s.length() > longest.length()) {
				longest = s;
			}
			if (times < 5) {
				height += 8;
				times++;
			}
		}
		return new Dimension(longest.length() * 6 + 9, height);
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("back")) {
				if (command.equals("left")) {
					selGem--;
					if (selGem < 0) {
						selGem = weapon.abilities.length - 1;
					}
					return;
				}
				if (command.equals("right")) {
					selGem++;
					if (selGem > weapon.abilities.length - 1) {
						selGem = 0;
					}
					return;
				}
			} else {
				close();
			}
		} else {
			subMenu.get(0).action(command, unit);
		}
	}

	public void tick() {
		super.tick();

		if (subMenu.size() == 0 && open) {
			if (moveCamera) {
				Startup.staticView.targetPosition = new Vector3f(position.getX() / 2 - 1 + width / 4, position.getY() / 2 - openTo / 4, Startup.staticView.position.getZ());
			}
			StateManager.currentState.cursor.setLocation(new Vector3f(position.getX() - 10 + ((selected == 1) ? selGem * 18 : 0), position.getY() - 11 - 18 * Math.min(4, selected), position.getZ() + 4));
		}
	}

	public void render() {
		UIBox.render(position, width, height);

		BatchSorter.add(weapon.model, weapon.texture, "static_3d", weapon.material, Vector3f.add(position, 8, -8, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f), Vector4f.COLOR_BASE, false, true);
		TextRenderer.render(weapon.name, Vector3f.add(position, 22, -8, 1), Vector3f.EMPTY, Vector3f.SCALE_HALF, new Vector4f(ItemProperty.colorList[weapon.tier], 1), true);

		int skillTotal = 0;
		for (int i = 0; i < weapon.abilities.length; i++) {
			skillTotal += ItemAbility.valueOf(weapon.abilities[i]).size;
			BatchSorter.add("u", "item_material_gem.dae", "item_material_diamond.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, 8 + i * 18, -24, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f),
					ItemAbility.valueOf(weapon.abilities[i]).getColor(), false, true);
		}

		for (int i = 0; i < weapon.capacity - skillTotal; i++) {
			BatchSorter.add("item_material_gem.dae", "item_material_diamond.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, skillTotal * 18 + 8 + i * 18, -24, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f),
					Vector4f.COLOR_DARK_GRAY, false, true);
		}

		if (selected == 1 && weapon.abilities.length > selGem) {
			UIBox.render(Vector3f.add(StateManager.currentState.cursor.position, 16.5f, 23, 0), ItemAbility.valueOf(weapon.abilities[selGem]).toString().length() * 6 + 8, 16);
			TextRenderer.render(ItemAbility.valueOf(weapon.abilities[selGem]).toString(), Vector3f.add(StateManager.currentState.cursor.position, 20.5f, 18.5f, 1), Vector3f.EMPTY, Vector3f.SCALE_HALF,
					new Vector4f(ItemProperty.colorList[ItemAbility.valueOf(weapon.abilities[selGem]).rarity], 1), true);
		}

		// Render inventory gems
		for (int i = 0; i < Math.min(3, options.length); i++) {
			//if (i * 8 + 12 < height) {
				TextRenderer.render(options[i + Math.max(2, selected - 2)], Vector3f.add(position, 4, (-i - Math.max(2, selected - options.length)) * 8 - 6, 1), new Vector3f(0, 0, 0), scale, new Vector4f(1, 1, 1, 1), true);
			//}
		}
		
		for(ItemProperty i : Inventory.getItemList()) {
			//System.out.println(i.abilities[0]);
		}
	}

	public static String[] getSkillList() {
		ArrayList<String> names = new ArrayList<>();
		names.add("");
		names.add("");
		for (ItemProperty i : Inventory.getItemList()) {
			if (i.type.equals(ItemProperty.TYPE_GEM_ABILITY)) {
				gems.add(i);
				names.add(ItemAbility.valueOf(i.abilities[0]).toString());
			}
		}
		return names.toArray(new String[names.size()]);
	}

	@Override
	public void close() {
		super.close();
		StateManager.currentState.cursor.staticView = false;
		StateManager.currentState.cursor.warpLocation(Vector3f.add(cursorPos, 0, 0, 0));
		AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
	}

}
