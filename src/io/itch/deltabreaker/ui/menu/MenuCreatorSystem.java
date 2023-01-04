package io.itch.deltabreaker.ui.menu;

import javax.swing.JOptionPane;

import io.itch.deltabreaker.builder.dungeon.DungeonGenerator;
import io.itch.deltabreaker.core.PerformanceManager;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.state.StateCreatorHub;
import io.itch.deltabreaker.state.StateManager;

public class MenuCreatorSystem extends Menu {

	private int width, height;
	private StateCreatorHub context;
	private Vector3f cursorPos;

	private int pallet = 0;
	
	public MenuCreatorSystem(StateCreatorHub context, Vector3f position) {
		super(position, new String[] { context.dungeon.getName(), "Width " + context.tiles.length, "Height " + context.tiles[0].length, "Name", "Save", "Load", "Reset" });
		this.context = context;
		this.width = context.tiles.length;
		this.height = context.tiles[0].length;
		cursorPos = Vector3f.add(StateManager.currentState.cursor.position, 0, 0, 0);
		StateManager.currentState.cursor.warpLocation(Vector3f.add(position, 0, -9, 0));
		StateManager.currentState.cursor.staticView = true;
		
		String[] names = DungeonGenerator.getPatternNames();
		for(int i = 0; i < names.length; i++) {
			if(names[i] == options[0]) {
				pallet = i;
			}
		}
	}

	public void tick() {
		super.tick();
	}

	@Override
	public void action(String command, Unit unit) {
		if (!command.equals("back") || selected == 3) {
			if (subMenu.size() == 0) {
				if (command.equals("") || selected == 3 || command.equals("left") || command.equals("right")) {
					switch (selected) {

					case 0:
						if (command.equals("left")) {
							if(pallet > 0) {
								pallet--;
							}
						} else if (command.equals("right")) {
							if(pallet < DungeonGenerator.getPatternNames().length - 1) {
								pallet++;
							}
						}
						options[selected] = DungeonGenerator.getPatternNames()[pallet];
						update();
						break;
					
					case 1:
						if (command.equals("left")) {
							if (width > 16) {
								width--;
							}
						} else if (command.equals("right")) {
							width++;
						}
						options[selected] = "width " + width;
						update();
						break;

					case 2:
						if (command.equals("left")) {
							if (height > 16) {
								height--;
							}
						} else if (command.equals("right")) {
							height++;
						}
						options[selected] = "height " + height;
						update();
						break;

					case 3:
						PerformanceManager.checkForCrash = false;
						String name = JOptionPane.showInputDialog("Filename");
						options[selected] = (name == null) ? options[selected] : name;
						PerformanceManager.checkForCrash = true;
						update();
						break;

					case 4:
						context.save(options[3].toLowerCase());
						break;

					case 5:
						context.load(options[3].toLowerCase());
						break;
						
					case 6:
						close();
						System.out.println(context);
						System.out.println(context.dungeon);
						StateCreatorHub.startCreating(width, height, context.dungeon.getPattern());
						break;

					}
				}
			} else {
				subMenu.get(0).action(command, unit);
			}
		} else {
			close();
		}
	}

	@Override
	public void close() {
		super.close();
		StateManager.currentState.cursor.staticView = false;
		StateManager.currentState.cursor.warpLocation(Vector3f.add(cursorPos, 0, 0, 0));
		
		if (width != context.tiles.length || height != context.tiles[0].length) {
			context.resize(width, height);
		}
		
		if(!DungeonGenerator.getPatternNames()[pallet].equals(context.dungeon.getName())) {
			StateCreatorHub.startCreating(16, 16, DungeonGenerator.getPalletTags()[pallet]);
		}
	}

}
