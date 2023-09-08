package io.itch.deltabreaker.ui.menu;

import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.ItemProperty;
import io.itch.deltabreaker.state.StateManager;
import io.itch.deltabreaker.ui.UIBox;

public class MenuItemInfoCard extends Menu {

	private ItemProperty item;

	public Unit comparedUnit;

	public MenuItemInfoCard(Vector3f position, ItemProperty item, Unit comparedUnit) {
		super(position, item.type.equals(ItemProperty.TYPE_WEAPON) ? new String[] { "", "Skills" } : new String[] { "" }, getWidth(item), 16);
		this.item = item;
		this.comparedUnit = comparedUnit;
		openTo = 90;
		if (item.type.equals(ItemProperty.TYPE_OTHER) || item.type.equals(ItemProperty.TYPE_USABLE)) {
			openTo = 26 + (item.text.length + 2) * 8;
		}
	}

	public void tick() {
		if (open) {
			if (height < openTo) {
				height = Math.min(height + 4, openTo);
			}
			Startup.staticView.targetPosition = new Vector3f(position.getX() / 2f - 2 + width / 4f, position.getY() / 2 - openTo / 4f + 1, Startup.staticView.position.getZ());
		} else {
			if (height > 16) {
				height = Math.max(height - 4, 16);
			}
		}
		if (subMenu.size() == 0 && open) {
			StateManager.currentState.cursor.setLocation(new Vector3f(position.getX() - 10 + selected * 20, position.getY() - 9 - 12 * Math.min(4, selected), position.getZ() + 4));
		}
	}

	public void render() {
		UIBox.render(position, width, height);
		TextRenderer.render(item.name, Vector3f.add(position, 4, -6, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f), new Vector4f(ItemProperty.colorList[item.tier], 1), true);

		if (height > 26) {
			BatchSorter.add(item.model, item.texture, "static_3d", item.material.toString(), Vector3f.add(position, 8, -18, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f), new Vector4f(1, 1, 1, 1), false, true);
		}

		int xPosition = 4;
		if (item.type.equals(ItemProperty.TYPE_WEAPON) || item.type.equals(ItemProperty.TYPE_ARMOR) || item.type.equals(ItemProperty.TYPE_ACCESSORY)) {
			xPosition = 52;
			String[] ext = { "hp ", "atk", "mag", "spd", "def", "res" };
			int[] text = { item.hp, item.atk, item.mag, item.spd, item.def, item.res };

			ItemProperty comparedTo = comparedUnit.weapon;
			switch (item.type) {

			case ItemProperty.TYPE_ARMOR:
				comparedTo = comparedUnit.armor;
				break;

			case ItemProperty.TYPE_ACCESSORY:
				comparedTo = comparedUnit.accessory;
				break;

			}
			int[] stats = { comparedTo.hp, comparedTo.atk, comparedTo.mag, comparedTo.spd, comparedTo.def, comparedTo.res };
			for (int i = 0; i < text.length; i++) {
				if (height > 30 + i * 8) {
					Vector4f color = new Vector4f(1, 1, 1, 1);
					if (text[i] < stats[i]) {
						color = new Vector4f(1, 0.5f, 0.5f, 1);
					} else if (text[i] > stats[i]) {
						color = new Vector4f(0.5f, 1, 0.5f, 1);
					}
					TextRenderer.render(ext[i] + " " + text[i], Vector3f.add(position, 4, -30 - i * 8, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f), color, true);
				}
			}
		}

		if (item.type.equals(ItemProperty.TYPE_WEAPON)) {
			TextRenderer.render("Skills", Vector3f.add(position, 22, -18, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f), Vector4f.COLOR_BASE, true);
		}
		
		for (int i = 0; i < item.text.length; i++) {
			if (height > 15 + i * 8) {
				TextRenderer.render(item.text[i], Vector3f.add(position, xPosition, -30 - i * 8, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f), new Vector4f(1, 1, 1, 1), true);
			}
		}

		if (height == openTo) {
			TextRenderer.render(item.price + " g", Vector3f.add(position, 4, -openTo + 12, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f), new Vector4f(1, 1, 1, 1), true);
		}
	}

	private static int getWidth(ItemProperty item) {
		int width = item.name.length() * 6 + 9;

		int xPosition = 4;
		if (item.type.equals(ItemProperty.TYPE_WEAPON) || item.type.equals(ItemProperty.TYPE_ARMOR) || item.type.equals(ItemProperty.TYPE_ACCESSORY)) {
			xPosition = 52;
		}

		for (int i = 0; i < item.text.length; i++) {
			int length = xPosition + item.text[i].length() * 6 + 5;
			if (length > width) {
				width = length;
			}
		}

		return width;
	}
	
	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("back")) {
				if (command.equals("")) {

				}
			} else {
				close();
				AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
			}
		} else {
			subMenu.get(0).action(command, Inventory.units.get(selected));
		}
	}

}
