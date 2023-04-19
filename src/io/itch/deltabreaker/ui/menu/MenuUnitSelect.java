package io.itch.deltabreaker.ui.menu;

import java.awt.Dimension;
import java.util.ArrayList;

import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.state.StateManager;
import io.itch.deltabreaker.ui.UIBox;

public abstract class MenuUnitSelect extends Menu {

	public int limit;
	public boolean[] selectedUnits;
	public ArrayList<Unit> units;
	private Vector3f cursorPos;
	private boolean canClose;

	public MenuUnitSelect(Vector3f position, int limit, ArrayList<Unit> units, boolean direct, boolean canClose) {
		super(position, getNameList(units));
		selectedUnits = new boolean[options.length - 1];
		this.limit = limit;
		this.units = units;
		this.canClose = canClose;
		if (direct) {
			StateManager.currentState.cursor.staticView = true;
			cursorPos = Vector3f.add(StateManager.currentState.cursor.position, 0, 0, 0);
			StateManager.currentState.cursor.warpLocation(Vector3f.add(position, 0, -9, 0));
		}
	}

	public abstract void returnedUnits(ArrayList<Unit> units);

	public void close() {
		super.close();
		if (cursorPos != null) {
			StateManager.currentState.cursor.staticView = false;
			StateManager.currentState.cursor.warpLocation(Vector3f.add(cursorPos, 0, 0, 0));
		}
	}

	private static String[] getNameList(ArrayList<Unit> units) {
		String[] names = new String[units.size() + 1];

		for (int i = 0; i < units.size(); i++) {
			names[i] = units.get(i).name;
		}

		names[names.length - 1] = "ready";

		return names;
	}

	private int getReadyCount() {
		int ready = 0;
		for (boolean b : selectedUnits) {
			if (b) {
				ready++;
			}
		}
		return ready;
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
		return new Dimension(longest.length() * 6 + 40, height);
	}

	public void tick() {
		super.tick();
		width = getDimensions(options).width;
		openTo = getDimensions(options).height;
		if (subMenu.size() == 0 && open) {
			StateManager.currentState.cursor.setLocation(new Vector3f(position.getX() - 10, position.getY() - 12 - 18 * Math.min(3, selected), position.getZ() + 2));
		}
	}

	public void render() {
		UIBox.render(position, width, height);
		for (int i = 0; i < Math.min(4, options.length); i++) {
			int unit = i + AdvMath.inRange(selected - 3, 0, units.size() - 1);
			if (i * 18 + 12 < height) {
				TextRenderer.render(options[unit], Vector3f.add(position, 4, -i * 18 - 9, 1), new Vector3f(0, 0, 0), scale, new Vector4f(1, 1, 1, 1), true);
				if (unit < options.length - 1) {
					units.get(unit).renderFlat(Vector3f.add(position, width - 28, -i * 18 - 7, 1), Vector3f.SCALE_HALF, Vector4f.COLOR_BASE);
					String icon = (selectedUnits[unit]) ? "check" : "cross";
					BatchSorter.add(icon + ".dae", icon + ".png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, width - 15, -i * 18 - 8, 1), Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, false, true);
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
			if (!command.equals("back")) {
				if (command.equals("")) {
					if (selected < options.length - 1) {
						subMenu.add(new Menu(Vector3f.add(position, width + 5, 0, 0), new String[] { "select", "info", "trade" }) {

							@Override
							public void action(String command, Unit unit) {
								if (subMenu.size() == 0) {
									if (!command.equals("back")) {
										if (command.equals("")) {
											switch (selected) {

											case 0:
												if (getReadyCount() < limit || selectedUnits[parent.selected]) {
													selectedUnits[parent.selected] = !selectedUnits[parent.selected];
													AudioManager.getSound((selectedUnits[parent.selected]) ? "menu_open.ogg" : "menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
												} else {
													AudioManager.getSound("invalid.ogg").play(AudioManager.defaultMainSFXGain, false);
												}
												break;

											case 1:
												subMenu.add(new MenuStatusCard(Vector3f.add(position, width + 5, 0, 0), units.get(parent.selected), false));
												break;

											case 2:
												subMenu.add(new MenuTradeSecondUnitSelect(Vector3f.add(position, width + 5, 0, 0), false, unit));
												break;

											}
										}
									} else {
										close();
										AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
									}
								} else {
									subMenu.get(0).action(command, units.get(selected));
								}
							}

						}.setParent(this));
					} else {
						ArrayList<Unit> results = new ArrayList<>();
						for (int i = 0; i < units.size(); i++) {
							if (selectedUnits[i]) {
								results.add(units.get(i));
							}
						}
						if (results.size() == limit) {
							close();
							returnedUnits(results);
							AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
						} else {
							AudioManager.getSound("invalid.ogg").play(AudioManager.defaultMainSFXGain, false);
						}
					}
				}
			} else if (canClose) {
				close();
				AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
			}
		} else {
			subMenu.get(0).action(command, units.get(selected));
		}
	}

}