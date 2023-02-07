package io.itch.deltabreaker.ui.menu;

import java.io.File;

import io.itch.deltabreaker.core.FileManager;
import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.state.StateManager;
import io.itch.deltabreaker.state.StateTitle;
import io.itch.deltabreaker.ui.UIBox;

public class MenuTitle extends Menu {

	public MenuTitle(Vector3f position) {
		super(position, createStringArray());
		StateManager.currentState.cursor.staticView = true;
		StateManager.currentState.cursor.warpLocation(Vector3f.add(position, 0, -9, 0));
	}

	public void tick() {
		super.tick();
		if (open) {
			width = 130;
			openTo = 24 * AdvMath.inRange(Inventory.saveSlots.size() + 2, 2, 3) + 3;
			if (options.length - 2 != Inventory.saveSlots.size()) {
				options = createStringArray();
			}
		}

		if (subMenu.size() == 0 && open && StateManager.currentState.status.size() == 0 && StateManager.currentState.itemInfo.size() == 0) {
			Startup.staticView.targetPosition = new Vector3f(position.getX() / 2 - 1 + width / 4, position.getY() / 2 - openTo / 4, Startup.staticView.position.getZ());
			StateManager.currentState.cursor.setLocation(new Vector3f(position.getX() - 10, position.getY() - 13 - 24 * Math.min(2, selected), position.getZ() + 4));
		}
	}

	public void render() {
		UIBox.render(position, width, height);
		for (int i = 0; i < Math.min(3, options.length); i++) {
			if (i * 24 + 12 < height) {
				if (i + Math.max(0, selected - 2) < Inventory.saveSlots.size()) {
					TextRenderer.render(Inventory.getHeader(i + Math.max(0, selected - 2)), Vector3f.add(position, 4, (-i - Math.max(0, selected - options.length)) * 24 - 6, 1), new Vector3f(0, 0, 0), scale, new Vector4f(1, 1, 1, 1), true);
					long time = Inventory.getPlaytime(i + Math.max(0, selected - 2));
					long minutes = time / 60;
					long hours = minutes / 60;
					String playtime = "playtime " + hours + "h " + minutes + "m";
					TextRenderer.render(playtime, Vector3f.add(position, 4, (-i - Math.max(0, selected - options.length)) * 24 - 14, 1), new Vector3f(0, 0, 0), scale, new Vector4f(1, 1, 1, 1), true);
				} else {
					TextRenderer.render(options[i + Math.max(0, selected - 2)], Vector3f.add(position, 4, (-i - Math.max(0, selected - options.length)) * 24 - 10, 1), new Vector3f(0, 0, 0), scale, new Vector4f(1, 1, 1, 1), true);
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
				switch (options[selected]) {

				case "New":
					subMenu.add(new Menu(Vector3f.add(position, width + 5, 0, 0), new String[] { "Confirm", "Back" }) {
						@Override
						public void action(String command, Unit unit) {
							if (subMenu.size() == 0) {
								if (!command.equals("return")) {
									switch (options[selected]) {

									case "Confirm":
										StateTitle.getCurrentContext().fadeOption = StateTitle.OPTION_NEW;
										Startup.screenColorTarget.setW(1);
										AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
										closeAll();
										StateTitle.getCurrentContext().hideMenu = true;
										break;

									case "Back":
										AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
										close();
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
					});
					break;

				case "Back":
					close();
					AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
					break;

				default:
					subMenu.add(new MenuTitleSub(Vector3f.add(position, width + 5, 0, 0), selected).setParent(this));
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

	private static String[] createStringArray() {
		String[] arr = new String[Inventory.saveSlots.size() + 2];
		for (int i = 0; i < arr.length - 2; i++) {
			arr[i] = "";
		}
		arr[arr.length - 2] = "New";
		arr[arr.length - 1] = "Back";
		return arr;
	}

	public void close() {
		prepareClose();
		super.close();
	}
	
	public void closeAll() {
		prepareClose();
		super.closeAll();
	}

	private void prepareClose() {
		Startup.staticView.setTargetPosition(0, 0, 0);
		StateTitle.getCurrentContext().hideMenu = false;
	}
	
}

class MenuTitleSub extends Menu {

	private int slot;

	public MenuTitleSub(Vector3f position, int slot) {
		super(position, new String[] { "Load", "Delete" });
		this.slot = slot;
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("return")) {
				switch (options[selected]) {

				case "Load":
					subMenu.add(new Menu(Vector3f.add(position, width + 5, 0, 0), new String[] { "Confirm", "Back" }) {
						@Override
						public void action(String command, Unit unit) {
							if (subMenu.size() == 0) {
								if (!command.equals("return")) {
									switch (options[selected]) {

									case "Confirm":
										StateManager.currentState.controlLock = true;
										StateTitle.getCurrentContext().fadeOption = StateTitle.OPTION_LOAD;
										StateTitle.getCurrentContext().loadMap = slot;
										Startup.screenColorTarget.setW(1);
										closeAll();
										StateTitle.getCurrentContext().hideMenu = true;
										AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
										break;

									case "Back":
										AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
										break;

									}
									close();
								} else {
									close();
									AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
								}
							} else {
								subMenu.get(0).action(command, unit);
							}
						}
					}.setParent(this));
					break;

				case "Delete":
					subMenu.add(new Menu(Vector3f.add(position, width + 5, 0, 0), new String[] { "Confirm", "Back" }) {
						@Override
						public void action(String command, Unit unit) {
							if (subMenu.size() == 0) {
								if (!command.equals("return")) {
									switch (options[selected]) {

									case "Confirm":
										for (File f : FileManager.getFiles("save/" + Inventory.getSlot(slot))) {
											f.delete();
										}
										new File("save/" + Inventory.getSlot(slot) + "/unit").delete();
										new File("save/" + Inventory.getSlot(slot)).delete();
										Inventory.loadHeaderData();
										parent.close();
										AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
										break;

									case "Back":
										AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
										break;

									}
									close();
								} else {
									close();
									AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
								}
							} else {
								subMenu.get(0).action(command, unit);
							}
						}
					}.setParent(this));
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