package io.itch.deltabreaker.ui.menu;

import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.state.StateManager;

public class MenuChoice extends Menu {

	private Vector3f cursorPos;
	private String variable;

	public MenuChoice(Vector3f position, String variable, String[] options) {
		super(position, options);
		this.variable = variable;
		StateManager.currentState.cursor.staticView = true;
		cursorPos = Vector3f.add(StateManager.currentState.cursor.position, 0, 0, 0);
		StateManager.currentState.cursor.warpLocation(Vector3f.add(position, 0, -9, 0));
	}

	@Override
	public void action(String command, Unit unit) {
		if (command.equals("")) {
			Inventory.variables.put(variable, selected);
			close();
			if (selected == 0) {
				AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
			} else {
				AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
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
