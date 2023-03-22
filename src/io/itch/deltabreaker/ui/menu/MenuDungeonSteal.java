package io.itch.deltabreaker.ui.menu;

import java.awt.Dimension;
import java.util.Random;

import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.effect.EffectText;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.ItemProperty;
import io.itch.deltabreaker.state.StateDungeon;
import io.itch.deltabreaker.state.StateManager;
import io.itch.deltabreaker.ui.UIBox;

public class MenuDungeonSteal extends Menu {

	private Vector3f cursorPos;
	private Unit u, host;

	public MenuDungeonSteal(Vector3f position, Unit u, Unit host, StateDungeon context) {
		super(position, getOptions(u), getDimensions(getOptions(u)).width, getDimensions(getOptions(u)).height);
		Startup.staticView.position = new Vector3f(position.getX() / 2 - 4 + getDimensions(options).width / 4, position.getY() / 2 - getDimensions(options).height / 4, Startup.staticView.position.getZ());
		StateManager.currentState.cursor.staticView = true;
		cursorPos = Vector3f.add(StateManager.currentState.cursor.position, 0, 0, 0);
		StateManager.currentState.cursor.warpLocation(Vector3f.add(position, 0, -9, 0));
		this.u = u;
		this.host = host;
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
		return new Dimension(longest.length() * 6 + 55, height);
	}

	private static String[] getOptions(Unit u) {
		String[] items = new String[u.getItemList().size()];

		for (int i = 0; i < items.length; i++) {
			items[i] = u.getItemList().get(i).name;
		}

		return items;
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("return")) {
				// This checks to see if the host unit will successfully steal the chosen item
				int chance = (int) AdvMath.inRange(100 - (u.getItemList().get(selected).tier * 20) * Math.max(0, 1 - (host.spd - u.spd) * 0.05), 1, 100);
				if (new Random().nextInt(100) <= chance) {
					StateManager.currentState.effects.add(new EffectText("+" + u.getItemList().get(selected).name,
							new Vector3f(host.x - ("+" + u.getItemList().get(selected).name).length(), 20 + StateManager.currentState.tiles[host.locX][host.locY].getPosition().getY(), host.y - 8),
							new Vector4f(ItemProperty.colorList[u.getItemList().get(selected).tier], 1)));
					host.addItem(u.getItemList().get(selected));
					u.removeItem(u.getItemList().get(selected));
					AudioManager.getSound("loot.ogg").play(AudioManager.defaultMainSFXGain, false);
				} else {
					StateManager.currentState.effects
							.add(new EffectText("miss", new Vector3f(host.x - ("miss").length(), 20 + StateManager.currentState.tiles[host.locX][host.locY].getPosition().getY(), host.y - 8), Vector4f.COLOR_RED.copy()));
					AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
				}
				close();
			}
		} else {
			subMenu.get(0).action(command, unit);
		}
	}

	public void tick() {
		super.tick();
		if (subMenu.size() == 0 && open && StateManager.currentState.status.size() == 0 && StateManager.currentState.itemInfo.size() == 0) {
			StateManager.currentState.cursor.setLocation(new Vector3f(position.getX() - 10, position.getY() - 12 - 18 * Math.min(3, selected), position.getZ() + 2));
		}
	}

	public void render() {
		UIBox.render(position, width, height);
		for (int i = 0; i < Math.min(4, options.length); i++) {
			int item = i + AdvMath.inRange(selected - 3, 0, u.getItemList().size() - 1);
			if (i * 18 + 12 < height) {
				TextRenderer.render(options[item], Vector3f.add(position, 4, -i * 18 - 9, 1), new Vector3f(0, 0, 0), scale, new Vector4f(1, 1, 1, 1), true);
				if (open) {
					int chance = (int) AdvMath.inRange(100 - (u.getItemList().get(selected).tier * 20) * Math.max(0, 1 - (host.spd - u.spd) * 0.05), 1, 100);
					TextRenderer.render(chance + "%", Vector3f.add(position, width - 29, -i * 18 - 9, 1), new Vector3f(0, 0, 0), scale, new Vector4f(1, 1, 1, 1), true);
				}
			}
			if (i * 18 + 16 < height && open) {
				BatchSorter.add(u.getItemList().get(item).model, u.getItemList().get(item).texture, "static_3d", u.getItemList().get(item).material, Vector3f.add(position, width - 44, -i * 18 - 8, 1), new Vector3f(0, 0, 0),
						Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, false, true);
			}
		}
	}

	@Override
	public void close() {
		super.close();
		StateManager.currentState.cursor.staticView = false;
		StateManager.currentState.cursor.warpLocation(Vector3f.add(cursorPos, 0, 0, 0));
	}

}
