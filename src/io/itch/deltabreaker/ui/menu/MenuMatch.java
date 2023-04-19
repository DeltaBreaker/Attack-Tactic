package io.itch.deltabreaker.ui.menu;

import io.itch.deltabreaker.builder.dungeon.DungeonGenerator;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.multiplayer.client.MatchPreviewThread;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.state.StateManager;
import io.itch.deltabreaker.state.StateMatchLobby;

public class MenuMatch extends Menu {

	private StateMatchLobby context;
	
	public MenuMatch(StateMatchLobby context, Vector3f position) {
		super(position, new String[] { "Create", "Join" });
		this.context = context;
		StateManager.currentState.hideCursor = false;
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("back")) {
				if (command.equals("")) {
					switch (options[selected]) {

					case "Create":
						subMenu.add(new MenuMatchCreate(context, Vector3f.add(position, width + 5, 0, 0)).setParent(this));
						break;

					case "Join":
						subMenu.add(new MenuMatchJoin(context, Vector3f.add(position, width + 5, 0, 0)).setParent(this));
						break;

					}
				}
			} else {
				close();
			}
		} else {
			subMenu.get(0).action(command, unit);
		}
	}

}

class MenuMatchCreate extends Menu {

	private StateMatchLobby context;
	private String[] maps;
	private int map = 0;
	private int floor = 9;
	private int units = 5;

	public MenuMatchCreate(StateMatchLobby context, Vector3f position) {
		super(position, new String[] { "Name", "Map-Random", "Floor-10", "Units-5", "Password", "Create" });
		this.context = context;
		String[] mapNames = DungeonGenerator.getPalletTags();
		maps = new String[mapNames.length + 2];
		for (int i = 0; i < mapNames.length; i++) {
			maps[i + 2] = mapNames[i];
		}
		maps[0] = "Random";
		maps[1] = "Vote";
	}

	public void tick() {
		super.tick();
		width = getDimensions(options).width;
		openTo = getDimensions(options).height;
	}
	
	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("back")) {
				if (command.equals("")) {
					switch (selected) {
					
					case 0:
						subMenu.add(new MenuTextInput(Vector3f.add(position, width + 5, 0, 0)) {
							@Override
							public void output(String output) {
								parent.options[0] = output;
							}
						}.setParent(this));
						break;
					
					case 4:
						subMenu.add(new MenuTextInput(Vector3f.add(position, width + 5, 0, 0)) {
							@Override
							public void output(String output) {
								parent.options[4] = output;
							}
						}.setParent(this));
						break;
						
					case 5:
						StateManager.currentState.hideCursor = true;
						context.thread = new MatchPreviewThread(context, "100.106.67.243", 36676, options[0], maps[map], (floor > -1) ? "" + floor : "random", units, (options[4].equals("Password")) ? "" : options[4]);
						new Thread(context.thread).start();
						closeAll();
						Startup.staticView.targetPosition.set(0, 0, 0);
						AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
						break;
					
					}
				}
				if (command.equals("left")) {
					switch (selected) {

					case 1:
						if (map > 0) {
							map--;
							AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
						}
						break;

					case 2:
						if (floor > -1) {
							floor--;
							AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
						}
						break;

					case 3:
						if (units > 1) {
							units--;
							AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
						}
						break;

					}
				}
				if (command.equals("right")) {
					switch (selected) {

					case 1:
						if (map < maps.length - 1) {
							map++;
							AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
						}
						break;

					case 2:
						if (floor < 49) {
							floor++;
							AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
						}
						break;

					case 3:
						if (units < 10) {
							units++;
							AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
						}
						break;

					}
				}
				update();
			} else {
				close();
				AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
			}
		} else {
			subMenu.get(0).action(command, unit);
		}
	}

	public void update() {
		options = new String[] { options[0], "Map-" + ((map > 1) ? DungeonGenerator.getPatternNameFromFile(maps[map]) : maps[map]), "Floor-" + ((floor > -1) ? (floor + 1) : "random"), "Units-" + units, options[4], "Create" };
	}
	
}