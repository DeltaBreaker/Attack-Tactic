package io.itch.deltabreaker.ui.menu;

import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.effect.EffectPoof;
import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.state.StateDungeon;
import io.itch.deltabreaker.state.StateManager;
import io.itch.deltabreaker.ui.UIBox;

public class MenuSave extends Menu {

	public MenuSave(Vector3f position) {
		super(position, createStringArray());
		StateManager.currentState.cursor.staticView = true;
		StateManager.currentState.cursor.warpLocation(Vector3f.add(position, 0, -9, 0));
	}

	public void tick() {
		super.tick();
		if(open) {
			width = 130;
			openTo = 24 * Inventory.saveSlots.size() + 3;
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
				if(i + Math.max(0, selected - 2) < Inventory.saveSlots.size()) {
					TextRenderer.render(Inventory.getHeader(i + Math.max(0, selected - 4)), Vector3f.add(position, 4, (-i - Math.max(0, selected - options.length)) * 24 - 6, 1), new Vector3f(0, 0, 0), scale, new Vector4f(1, 1, 1, 1), true);
					long time = Inventory.getPlaytime(i + Math.max(0, selected - 4));
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
				// Open sub menu
				// Save
				// Load
				// Erase
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
		for(int i = 0; i < arr.length - 2; i++) {
			arr[i] = "";
		}
		arr[arr.length - 2] = "New";
		arr[arr.length - 1] = "Back";
		return arr;
	}

	public void close() {
		StateManager.currentState.cursor.staticView = false;
		super.close();
	}
	
}
