package io.itch.deltabreaker.ui.menu;

import java.awt.Dimension;
import java.util.ArrayList;

import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.core.audio.AudioManager;
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

public class MenuTrade extends Menu {

	private Unit host, target, selectedUnit;
	private Vector3f cursorPos;
	protected boolean traded = false;

	public MenuTrade(Vector3f position, Unit host, Unit target) {
		super(position, getItemList(host, target), getDimensions(getItemList(host, target)).width, getDimensions(getItemList(host, target)).height);
		selectedUnit = host;
		this.host = host;
		this.target = target;
		Startup.staticView.position = new Vector3f(position.getX() / 2 - 4 + getDimensions(options).width / 4, position.getY() / 2 - getDimensions(options).height / 4, Startup.staticView.position.getZ());
		StateManager.currentState.cursor.staticView = true;
		cursorPos = Vector3f.add(StateManager.currentState.cursor.position, 0, 0, 0);
		StateManager.currentState.cursor.warpLocation(Vector3f.add(position, 0, -9, 0));
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("return")) {
				if (selected == options.length - 1) {
					selectedUnit = (selectedUnit == host) ? target : host;
					AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
				} else {
					subMenu.add(new MenuTradeRecieve(Vector3f.add(position, width + 5, 0, 0), selectedUnit, (selectedUnit == host) ? target : host, selected, this));
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
		options = getItemList(selectedUnit, (selectedUnit == host) ? target : host);
		width = getDimensions(options).width;
		openTo = getDimensions(options).height;
		super.tick();
		if (subMenu.size() == 0 && open && StateManager.currentState.status.size() == 0 && StateManager.currentState.itemInfo.size() == 0) {
			StateManager.currentState.cursor.setLocation(new Vector3f(position.getX() - 10, position.getY() - 12 - 18 * Math.min(3, selected), position.getZ() + 2));
		}
	}

	public void render() {
		ArrayList<ItemProperty> items = selectedUnit.getItemList();
		UIBox.render(position, width, height);
		for (int i = 0; i < Math.min(4, options.length); i++) {
			int item = i + AdvMath.inRange(selected - 3, 0, selectedUnit.getItemList().size() - 1);
			if (i * 18 + 12 < height) {
				TextRenderer.render(options[item], Vector3f.add(position, 4, -i * 18 - 9, 1), new Vector3f(0, 0, 0), scale, new Vector4f(1, 1, 1, 1), true);
				if (item == options.length - 1) {
					Unit unit = (selectedUnit == host) ? target : host;
					unit.renderFlat(Vector3f.add(position, width - 44, -i * 18 - 7, 1), Vector3f.SCALE_HALF, Vector4f.COLOR_BASE);
				}
			}
			if (i * 18 + 16 < height && open && item < options.length - 1) {
				BatchSorter.add(selectedUnit.getItemList().get(item).model, selectedUnit.getItemList().get(item).texture, "static_3d", selectedUnit.getItemList().get(item).material, Vector3f.add(position, width - 44, -i * 18 - 8, 1),
						new Vector3f(0, 0, 0), Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, false, true);
				String type = selectedUnit.getItemList().get(item).type;
				if (type.equals(ItemProperty.TYPE_USABLE) || type.equals(ItemProperty.TYPE_OTHER)) {
					TextRenderer.render("x " + items.get(item).stack, Vector3f.add(position, width - 29, -i * 18 - 9, 1), new Vector3f(0, 0, 0), scale, new Vector4f(1, 1, 1, 1), true);
				}
			}
		}
		if (subMenu.size() > 0) {
			subMenu.get(0).render();
		}
	}

	private static String[] getItemList(Unit u, Unit target) {
		ArrayList<ItemProperty> itemList = u.getItemList();
		String[] items = new String[itemList.size() + 1];
		for (int i = 0; i < items.length - 1; i++) {
			items[i] = itemList.get(i).name;
		}
		items[items.length - 1] = target.name;
		return items;
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

	@Override
	public void close() {
		super.close();
		StateManager.currentState.cursor.staticView = false;
		StateManager.currentState.cursor.warpLocation(Vector3f.add(cursorPos, 0, 0, 0));
		if (StateManager.currentState.STATE_ID.equals(StateDungeon.STATE_ID)) {
			if (!traded) {
				host.reset();
			} else {
				host.setTurn(false);
			}
		}
	}

}

class MenuTradeRecieve extends Menu {

	private Unit host, target;
	private int itemIndex;
	protected MenuTrade parent;

	public MenuTradeRecieve(Vector3f position, Unit host, Unit target, int itemIndex, MenuTrade parent) {
		super(position, getItemList(target), getDimensions(getItemList(target)).width, getDimensions(getItemList(target)).height);
		this.host = host;
		this.target = target;
		this.itemIndex = itemIndex;
		this.parent = parent;
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("return")) {
				if (open) {
					switch (options[selected]) {

					default:
						ItemProperty temp = host.getItemList().get(itemIndex);
						host.removeItem(temp, temp.stack);

						ItemProperty targetItem = target.getItemList().get(selected);
						host.addItem(targetItem);
						target.removeItem(targetItem);
						target.addItem(temp);
						AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
						parent.traded = true;
						close();
						break;

					case "Add":
						String type = host.getItemList().get(itemIndex).type;
						if (type.equals(ItemProperty.TYPE_USABLE) || type.equals(ItemProperty.TYPE_OTHER)) {
							subMenu.add(new MenuTradeCount(Vector3f.add(position, width + 5, 0, 0), host.getItemList().get(itemIndex).stack, host, target, itemIndex, this));
						} else {
							int overflow = target.addItem(host.getItemList().get(itemIndex));
							if (overflow == 0) {
								host.removeItem(host.getItemList().get(itemIndex));
								close();
								AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
							} else {
								AudioManager.getSound("invalid.ogg").play(AudioManager.defaultMainSFXGain, false);
							}
						}
						break;

					}
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
		options = getItemList(target);
		width = getDimensions(options).width;
		openTo = getDimensions(options).height;
		super.tick();
		if (subMenu.size() == 0 && open && StateManager.currentState.status.size() == 0 && StateManager.currentState.itemInfo.size() == 0) {
			StateManager.currentState.cursor.setLocation(new Vector3f(position.getX() - 10, position.getY() - 12 - 18 * Math.min(3, selected), position.getZ() + 2));
		}
	}

	public void render() {
		ArrayList<ItemProperty> items = target.getItemList();
		UIBox.render(position, width, height);
		for (int i = 0; i < Math.min(4, options.length); i++) {
			int item = i + AdvMath.inRange(selected - 3, 0, target.getItemList().size() - 1);
			if (i * 18 + 12 < height) {
				TextRenderer.render(options[item], Vector3f.add(position, 4, -i * 18 - 9, 1), new Vector3f(0, 0, 0), scale, new Vector4f(1, 1, 1, 1), true);
			}
			if (i * 18 + 16 < height && open && item < options.length - 1) {
				BatchSorter.add(target.getItemList().get(item).model, target.getItemList().get(item).texture, "static_3d", target.getItemList().get(item).material, Vector3f.add(position, width - 44, -i * 18 - 8, 1), new Vector3f(0, 0, 0),
						Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, false, true);
				String type = target.getItemList().get(item).type;
				if (type.equals(ItemProperty.TYPE_USABLE) || type.equals(ItemProperty.TYPE_OTHER)) {
					TextRenderer.render("x " + items.get(item).stack, Vector3f.add(position, width - 29, -i * 18 - 9, 1), new Vector3f(0, 0, 0), scale, new Vector4f(1, 1, 1, 1), true);
				}
			}
		}
		if (subMenu.size() > 0) {
			subMenu.get(0).render();
		}
	}

	private static String[] getItemList(Unit u) {
		ArrayList<ItemProperty> itemList = u.getItemList();
		String[] items = new String[itemList.size() + 1];
		for (int i = 0; i < items.length - 1; i++) {
			items[i] = itemList.get(i).name;
		}
		items[items.length - 1] = "Add";
		return items;
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

}

class MenuTradeCount extends Menu {

	private Unit host, target;
	private int itemIndex;
	private int limit;
	private int trade = 1;
	private MenuTradeRecieve parent;

	public MenuTradeCount(Vector3f position, int limit, Unit host, Unit target, int itemIndex, MenuTradeRecieve parent) {
		super(position, new String[] { "Add 1" });
		this.limit = limit;
		this.host = host;
		this.target = target;
		this.itemIndex = itemIndex;
		this.parent = parent;
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("return")) {
				int leftover = target.addItem(host.getItemList().get(itemIndex), trade);
				if (leftover == trade) {
					AudioManager.getSound("invalid.ogg").play(AudioManager.defaultMainSFXGain, false);
				} else {
					if (host.getItemList().get(itemIndex).stack <= 0) {
						host.removeItem(host.getItemList().get(itemIndex));
						parent.close();
					}
					parent.parent.traded = true;
					close();
					AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
				}
			} else {
				close();
				AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
			}
		} else {
			subMenu.get(0).action(command, unit);
		}
	}

	@Override
	public void move(int amt) {
		trade = (int) AdvMath.inRange(trade - amt, 1, limit);
		options[0] = "Add " + trade;
		update();
	}

}