package io.itch.deltabreaker.ui;

import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.ItemProperty;

public class StatusCard extends UIBox {

	public Unit u;

	public int openTo = 90;
	public boolean close = false;

	public StatusCard(Vector3f position, Unit u) {
		super(position, 128, 0);
		this.u = u;
	}

	public void tick() {
		if (!close) {
			if (height < openTo) {
				height = Math.min(height + 4, openTo);
			}
			Startup.staticView.targetPosition = new Vector3f(position.getX() / 2 - 2 + width / 4, position.getY() / 2 - openTo / 4 + 1, Startup.staticView.position.getZ());
		} else {
			if (height > 16) {
				height = Math.max(height - 4, 16);
			}
		}
	}

	public void render() {
		super.render();
		String[] text = { u.name, "lv " + u.level, "exp " + u.exp, "hp  " + u.currentHp + "_" + u.hp, "", "atk " + u.atk, "mag " + u.mag, "spd " + u.spd, "def " + u.def, "res " + u.res };
		int[] offsets = { u.offsetHp, 0, u.offsetAtk, u.offsetMag, u.offsetSpd, u.offsetDef, u.offsetRes };
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

}
