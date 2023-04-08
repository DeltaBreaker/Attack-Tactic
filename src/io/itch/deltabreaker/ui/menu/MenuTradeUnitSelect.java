package io.itch.deltabreaker.ui.menu;

import java.awt.Dimension;
import java.util.ArrayList;

import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.state.StateManager;
import io.itch.deltabreaker.ui.UIBox;

public class MenuTradeUnitSelect extends Menu {

	private boolean limited;
	private ArrayList<Unit> units;
	
	public MenuTradeUnitSelect(Vector3f position, boolean limited) {
		super(position, getUnitNames(limited));
		this.limited = limited;
		units = (limited) ? Inventory.active : Inventory.units;
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("return")) {
				subMenu.add(new MenuTradeSecondUnitSelect(Vector3f.add(position, width + 5, 0, 0), limited, units.get(selected)));
				AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
			} else {
				close();
				AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
			}
		} else {
			subMenu.get(0).action(command, unit);
		}
	}

	public void tick() {
		super.tick();
		width = getDimensions(options).width;
		openTo = getDimensions(options).height;
		if (subMenu.size() == 0 && open && StateManager.currentState.status.size() == 0 && StateManager.currentState.itemInfo.size() == 0) {
			StateManager.currentState.cursor.setLocation(new Vector3f(position.getX() - 10, position.getY() - 12 - 18 * Math.min(3, selected), position.getZ() + 2));
		}
	}
	
	public void render() {
		UIBox.render(position, width, height);
		for (int i = 0; i < Math.min(4, options.length); i++) {
			int item = i + AdvMath.inRange(selected - 3, 0, units.size() - 1);
			if (i * 18 + 12 < height) {
				TextRenderer.render(options[item], Vector3f.add(position, 4, -i * 18 - 9, 1), new Vector3f(0, 0, 0), scale, new Vector4f(1, 1, 1, 1), true);
			}
			if (i * 18 + 16 < height && open && item < options.length) {
				units.get(item).renderFlat(Vector3f.add(position, width - 19, -i * 18 - 8, 1), Vector3f.SCALE_HALF, Vector4f.COLOR_BASE);
			}
		}
		if (subMenu.size() > 0) {
			subMenu.get(0).render();
		}
	}
	
	public static String[] getUnitNames(boolean limited) {
		ArrayList<Unit> units = (limited) ? Inventory.active : Inventory.units;
		String[] names = new String[units.size()];
		for(int i = 0; i < names.length; i++) {
			names[i] = units.get(i).name;
		}
		return names;
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
		return new Dimension(longest.length() * 6 + 30, height);
	}
	
}


class MenuTradeSecondUnitSelect extends Menu {

	private boolean limited;
	private Unit host;
	private ArrayList<Unit> units;
	
	public MenuTradeSecondUnitSelect(Vector3f position, boolean limited, Unit host) {
		super(position, getUnitNames(limited));
		this.limited = limited;
		this.host = host;
		units = (limited) ? Inventory.active : Inventory.units;
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("return")) {
				if(units.get(selected) != host) {
					subMenu.add(new MenuTrade(Vector3f.add(position, width + 5, 0, 0), host, units.get(selected), false));
					AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
				} else {
					AudioManager.getSound("invalid.ogg").play(AudioManager.defaultMainSFXGain, false);
				}
			} else {
				close();
				AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
			}
		} else {
			subMenu.get(0).action(command, unit);
		}
	}

	public void tick() {
		super.tick();
		width = getDimensions(options).width;
		openTo = getDimensions(options).height;
		if (subMenu.size() == 0 && open && StateManager.currentState.status.size() == 0 && StateManager.currentState.itemInfo.size() == 0) {
			StateManager.currentState.cursor.setLocation(new Vector3f(position.getX() - 10, position.getY() - 12 - 18 * Math.min(3, selected), position.getZ() + 2));
		}
	}
	
	public void render() {
		ArrayList<Unit> units = (limited) ? Inventory.active : Inventory.units;
		UIBox.render(position, width, height);
		for (int i = 0; i < Math.min(4, options.length); i++) {
			int item = i + AdvMath.inRange(selected - 3, 0, units.size() - 1);
			if (i * 18 + 12 < height) {
				TextRenderer.render(options[item], Vector3f.add(position, 4, -i * 18 - 9, 1), new Vector3f(0, 0, 0), scale, (host == units.get(item)) ? Vector4f.COLOR_GRAY : Vector4f.COLOR_BASE, true);
			}
			if (i * 18 + 16 < height && open && item < options.length) {
				units.get(item).renderFlat(Vector3f.add(position, width - 19, -i * 18 - 8, 1), Vector3f.SCALE_HALF, (host == units.get(item)) ? Vector4f.COLOR_GRAY : Vector4f.COLOR_BASE);
			}
		}
		if (subMenu.size() > 0) {
			subMenu.get(0).render();
		}
	}
	
	public static String[] getUnitNames(boolean limited) {
		ArrayList<Unit> units = (limited) ? Inventory.active : Inventory.units;
		String[] names = new String[units.size()];
		for(int i = 0; i < names.length; i++) {
			names[i] = units.get(i).name;
		}
		return names;
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
		return new Dimension(longest.length() * 6 + 30, height);
	}
	
}