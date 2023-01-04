package io.itch.deltabreaker.ui.menu;

import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.state.StateDungeon;
import io.itch.deltabreaker.state.StateManager;
import io.itch.deltabreaker.ui.StatusCard;

public class MenuDungeonMain extends Menu {

	private Vector3f cursorPos;

	public MenuDungeonMain(Vector3f position) {
		super(position, new String[] { "Status", "Go To", "End" });
		Startup.staticView.position = new Vector3f(position.getX() / 2 - 4 + getDimensions(options).width / 4, position.getY() / 2 - getDimensions(options).height / 4, Startup.staticView.position.getZ());
		StateManager.currentState.cursor.staticView = true;
		cursorPos = Vector3f.add(StateManager.currentState.cursor.position, 0, 0, 0);
		StateManager.currentState.cursor.warpLocation(Vector3f.add(position, 0, -9, 0));
	}

	@Override
	public void action(String command, Unit u) {
		StateDungeon state = (StateDungeon) StateManager.getState(StateDungeon.STATE_ID);
		if (subMenu.size() == 0) {
			if (!command.equals("return")) {
				switch (options[selected]) {

				case "Status":
					subMenu.add(new Menu(new Vector3f(position.getX() + width + 5, 0, position.getZ()), Unit.getUnitNames(Inventory.active.toArray(new Unit[Inventory.active.size()]))) {

						@Override
						public void action(String todo, Unit u) {
							if (subMenu.size() == 0) {
								if (!todo.equals("return")) {
									state.status.add(new StatusCard(Vector3f.add(position, width + 5, 0, 0), Inventory.active.get(selected)));
									AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
								} else {
									close();
									AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
								}
							} else {
								subMenu.get(0).action(todo, u);
							}
						}
					});
					break;

				case "Go To":
					subMenu.add(new Menu(new Vector3f(position.getX() + width + 5, 0, position.getZ()), Unit.getUnitNames(Inventory.active.toArray(new Unit[Inventory.active.size()]))) {

						@Override
						public void action(String command, Unit u) {
							if (subMenu.size() == 0) {
								if (!command.equals("return")) {
									StateManager.currentState.tcamX = (int) Inventory.active.get(selected).x / 2;
									StateManager.currentState.tcamY = (int) Inventory.active.get(selected).y / 2 + 16;
									state.cursorPos.x = Inventory.active.get(selected).locX;
									state.cursorPos.y = Inventory.active.get(selected).locY;
									closeAll();
									AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
								} else {
									close();
									AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
								}
							} else {
								subMenu.get(0).action(command, u);
							}
						}
					});
					subMenu.get(0).setParent(this);
					break;

				case "End":
					state.changePhase(1);
					close();
					AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
					break;

				}
			} else {
				close();
				AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
			}
		} else {
			subMenu.get(0).action(command, u);
		}
	}

	@Override
	public void close() {
		super.close();
		StateManager.currentState.cursor.staticView = false;
		StateManager.currentState.cursor.warpLocation(Vector3f.add(cursorPos, 0, 0, 0));
	}

	@Override
	public void closeAll() {
		super.close();
		StateManager.currentState.cursor.staticView = false;
		StateManager.currentState.cursor.warpLocation(Vector3f.add(cursorPos, 0, 0, 0));
		Startup.staticView.setTargetPosition(new Vector3f(0, 0, 0));
	}

}
