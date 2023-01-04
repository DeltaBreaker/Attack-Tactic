package io.itch.deltabreaker.event;

import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.effect.EffectItemShow;
import io.itch.deltabreaker.effect.EffectPause;
import io.itch.deltabreaker.exception.EventErrorException;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.ItemProperty;
import io.itch.deltabreaker.state.StateDungeon;
import io.itch.deltabreaker.state.StateHub;
import io.itch.deltabreaker.state.StateManager;
import io.itch.deltabreaker.ui.Message;
import io.itch.deltabreaker.ui.TextBox;
import io.itch.deltabreaker.ui.menu.MenuChoice;
import io.itch.deltabreaker.ui.menu.MenuDungeonAction;

public class Event {

	protected EventScript event;
	private int currentLine = 0;
	public boolean finished = false;
	public int waitTimer = 0;
	public int waitTime = 0;
	public boolean skip = false;

	public Event(EventScript event) {
		this.event = event;
	}

	public void tick() {
		if (!finished) {
			if (waitTimer == waitTime) {
				if (currentLine < event.lines.length) {
					try {
						String[] args = getUnitOnActivatorLocation(event.lines[currentLine]).split(" ");
						switch (args[0]) {

						case "if":
							skip = Inventory.variables.containsKey(args[1]) && Inventory.variables.get(args[1]) != Integer.parseInt(args[2]);
							break;

						case "end":
							skip = false;
							break;

						default:
							if (!skip && !args[0].startsWith("//")) {
								EventCommand.valueOf(args[0]).run(args, this);
							}
							break;

						}
					} catch (Exception e) {
						e.printStackTrace();
						try {
							throw new EventErrorException(currentLine + 1, event.lines[currentLine]);
						} catch (Exception e2) {
							e2.printStackTrace();
						}
					}
					currentLine++;
				} else {
					finished = true;
				}
			} else {
				waitTimer++;
			}
		}
	}

	public boolean canProcessDuringMenu() {
		String[] args = event.lines[currentLine].split(" ");
		try {
			if (!args[0].equals("if") && !args[0].equals("end") && !args[0].startsWith("//")) {
				return EventCommand.valueOf(args[0]).menuOp;
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				throw new EventErrorException(currentLine + 1, event.lines[currentLine]);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return false;
	}

	// This method checks the activator for location tags, then replaces any
	// instances of '~' with the UUID of the unit at that location
	private String getUnitOnActivatorLocation(String input) {
		String[] args = event.activator.split(" ");
		if (args[0].equals(EventScript.ACTIVATOR_WAIT)) {
			for (Unit u : Inventory.active) {
				if (u.locX == Integer.parseInt(args[1]) && u.locY == Integer.parseInt(args[2])) {
					return input.replace("~", u.uuid);
				}
			}
		}
		return input;
	}

	public String getActivator() {
		return event.activator;
	}

}

enum EventCommand {

	// Prevents this script from being ran again until it is reloaded
	disable(true) {
		@Override
		public void run(String[] args, Event event) {
			event.event.activator = "";
		}
	},

	// Creates a variable
	init(false) {
		public void run(String[] args, Event event) {
			if (!Inventory.variables.containsKey(args[1])) {
				Inventory.variables.put(args[1], Integer.parseInt(args[2]));
			}
		}
	},

	// Sets a variable
	set(false) {
		@Override
		public void run(String[] args, Event event) {
			Inventory.variables.put(args[1], Integer.parseInt(args[2]));
		}
	},

	// Prints to the console
	print(false) {
		@Override
		public void run(String[] args, Event event) {
			System.out.println("[Event]: The value of " + args[1] + " is " + Inventory.variables.get(args[1]));
		}
	},

	// Makes the scipt wait
	wait(false) {
		@Override
		public void run(String[] args, Event event) {
			event.waitTime = Integer.parseInt(args[1]);
			event.waitTimer = 0;
		}
	},

	// A wait that can be run when a menu is open
	wait_menu(true) {
		@Override
		public void run(String[] args, Event event) {
			event.waitTime = Integer.parseInt(args[1]);
			event.waitTimer = 0;
		}
	},

	// Prevents control input (but not menus)
	controllock(true) {
		@Override
		public void run(String[] args, Event event) {
			StateManager.currentState.controlLock = Boolean.parseBoolean(args[1]);
		}
	},

	// Prevents menu input
	menulock(true) {
		@Override
		public void run(String[] args, Event event) {
			StateManager.currentState.menuLock = Boolean.parseBoolean(args[1]);
		}
	},

	// Hides the cursor
	hidecursor(false) {
		@Override
		public void run(String[] args, Event event) {
			StateManager.currentState.hideCursor = Boolean.parseBoolean(args[1]);
		}
	},

	// Sets if the unit is pose locked or not
	poselock(false) {
		@Override
		public void run(String[] args, Event event) {
			Inventory.loaded.get(args[1]).poseLock = Boolean.parseBoolean(args[2]);
		}
	},

	// Sets the direction a unit is facing
	dir(false) {
		@Override
		public void run(String[] args, Event event) {
			Inventory.loaded.get(args[1]).dir = Integer.parseInt(args[2]);
			Inventory.loaded.get(args[1]).frame = Integer.parseInt(args[3]);
		}
	},

	// Sets the screens color target
	fade(false) {
		@Override
		public void run(String[] args, Event event) {
			Startup.screenColorTarget.set(Float.parseFloat(args[1]), Float.parseFloat(args[2]), Float.parseFloat(args[3]), Float.parseFloat(args[4]));
		}
	},

	// Sets a units target path or tile based on the current state
	moveunit(false) {
		@Override
		public void run(String[] args, Event event) {
			if (StateManager.currentState == StateManager.getState(StateDungeon.STATE_ID)) {
				Unit u = Inventory.loaded.get(args[1]);
				StateDungeon context = StateDungeon.getCurrentContext();
				context.clearSelectedTiles();
				context.highlightTiles(u.locX, u.locY, StateManager.currentState.tiles.length * StateManager.currentState.tiles[0].length, 1, "");
				u.move(context.getPath(Integer.parseInt(args[2]), Integer.parseInt(args[3])));
				context.clearSelectedTiles();
			} else {
				// Account for the hub here
				Inventory.loaded.get(args[1]).setLocation(Integer.parseInt(args[2]), Integer.parseInt(args[3]));
			}
		}
	},

	// Sets the cursor's target position
	movecursor(false) {
		@Override
		public void run(String[] args, Event event) {
			AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
			StateManager.currentState.cursorPos.setLocation(StateManager.currentState.cursorPos.x + Integer.parseInt(args[1]), StateManager.currentState.cursorPos.y + Integer.parseInt(args[2]));
			StateManager.currentState.cursor.setLocation(new Vector3f((int) StateManager.currentState.cursorPos.getX() * 16 + 3,
					34 + StateManager.currentState.tiles[StateManager.currentState.cursorPos.x][StateManager.currentState.cursorPos.y].getPosition().getY(), (int) StateManager.currentState.cursorPos.getY() * 16 - 8));
		}
	},

	// Teleports the cursor
	warpcursor(false) {
		@Override
		public void run(String[] args, Event event) {
			StateManager.currentState.cursorPos.setLocation(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
			StateManager.currentState.cursor.warpLocation(new Vector3f((int) StateManager.currentState.cursorPos.getX() * 16 + 3,
					34 + StateManager.currentState.tiles[StateManager.currentState.cursorPos.x][StateManager.currentState.cursorPos.y].getPosition().getY(), (int) StateManager.currentState.cursorPos.getY() * 16 - 8));
		}
	},

	// Sets the camera's target position
	movecamera(false) {
		@Override
		public void run(String[] args, Event event) {
			StateManager.currentState.tcamX += Integer.parseInt(args[1]) * 8;
			StateManager.currentState.tcamY += Integer.parseInt(args[2]) * 8;
		}
	},

	// Teleports the camera
	warpcamera(false) {
		@Override
		public void run(String[] args, Event event) {
			StateManager.currentState.tcamX = Integer.parseInt(args[1]) * 8;
			StateManager.currentState.tcamY = Integer.parseInt(args[2]) * 8;
			StateManager.currentState.camX = Integer.parseInt(args[1]) * 8;
			StateManager.currentState.camY = Integer.parseInt(args[2]) * 8;
		}
	},

	// Displays a script
	script(false) {
		@Override
		public void run(String[] args, Event event) {
			StateManager.currentState.text.add(new TextBox(DialogueScript.scripts.get(args[1])));
		}
	},

	// Adds an event to the event list
	event(true) {
		@Override
		public void run(String[] args, Event event) {
			StateDungeon.getCurrentContext().events.add(new Event(StateDungeon.getCurrentContext().eventList.get(args[1])));
		}
	},

	// Sets the current corruption
	corruption(false) {
		@Override
		public void run(String[] args, Event event) {
			Startup.corruption = (float) AdvMath.inRange(Float.parseFloat(args[1]), 0, 1);
		}
	},

	// Sets the corruption speed
	corruptionS(false) {
		@Override
		public void run(String[] args, Event event) {
			Startup.corruptionSpeed = (float) AdvMath.inRange(Float.parseFloat(args[1]), 0, 1);
		}
	},

	// Sets the corruption target
	corruptionT(false) {
		@Override
		public void run(String[] args, Event event) {
			Startup.corruptionTarget = (float) AdvMath.inRange(Float.parseFloat(args[1]), 0, 1);
		}
	},

	// Loads a unit into the loaded array
	loadunit(false) {
		@Override
		public void run(String[] args, Event event) {
			try {
				Unit.loadUnit(-1, -1, Vector4f.COLOR_BASE.copy(), args[1]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	},

	// Puts a unit into your reserves
	deployunit(false) {
		@Override
		public void run(String[] args, Event event) {
			if (!Inventory.units.contains(Inventory.loaded.get(args[1]))) {
				Inventory.units.add(Inventory.loaded.get(args[1]));
			}
		}
	},

	// Puts a passive unit into the active array
	useunit(false) {
		@Override
		public void run(String[] args, Event event) {
			if (StateManager.currentState == StateManager.getState(StateDungeon.STATE_ID)) {
				if (!Inventory.active.contains(Inventory.loaded.get(args[1]))) {
					Inventory.active.add(Inventory.loaded.get(args[1]));
				}
			}
		}
	},

	// Fully recruits an enemy units and moves them to your team
	recruitunit(false) {
		@Override
		public void run(String[] args, Event event) {
			if (StateManager.currentState == StateManager.getState(StateDungeon.STATE_ID)) {
				if (!Inventory.units.contains(Inventory.loaded.get(args[1]))) {
					Inventory.units.add(Inventory.loaded.get(args[1]));
				}
				if (!Inventory.active.contains(Inventory.loaded.get(args[1]))) {
					Inventory.active.add(Inventory.loaded.get(args[1]));
				}

				for (int i = 0; i < StateDungeon.getCurrentContext().enemies.size(); i++) {
					if (StateDungeon.getCurrentContext().enemies.get(i).uuid.equals(args[1])) {
						StateDungeon.getCurrentContext().enemies.remove(i);
						i--;
					}
				}
			}
		}
	},

	// Prepares a unit as when they are first loaded into a map
	prepareunit(false) {
		@Override
		public void run(String[] args, Event event) {
			Inventory.loaded.get(args[1]).prepare();
		}
	},

	// Spawns a unit on the map
	spawnunit(false) {
		@Override
		public void run(String[] args, Event event) {
			Unit u = Inventory.loaded.get(args[1]);
			u.placeAt(Integer.parseInt(args[2]), Integer.parseInt(args[3]));
			u.prepare();

			if (Boolean.parseBoolean(args[5])) {
				u.unitColor.setW(1);
			}

			if (Boolean.parseBoolean(args[4])) {
				if (!Inventory.units.contains(u)) {
					Inventory.units.add(u);
				}
				if (!Inventory.active.contains(u)) {
					Inventory.active.add(u);
				}
			} else {
				if (StateManager.currentState == StateManager.getState(StateDungeon.STATE_ID)) {
					if (!StateDungeon.getCurrentContext().enemies.contains(u)) {
						StateDungeon.getCurrentContext().enemies.add(u);
					}
				} else if (StateManager.currentState == StateManager.getState(StateHub.STATE_ID)) {
					if (!StateHub.getCurrentContext().npcs.contains(u)) {
						StateHub.getCurrentContext().npcs.add(u);
					}
				}
			}
		}
	},

	// Highlights a units range and selects them
	selectunit(false) {
		@Override
		public void run(String[] args, Event event) {
			Inventory.loaded.get(args[1]).select();
			AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
		}
	},

	// Shows the weapon range of a unit
	showrange(false) {
		@Override
		public void run(String[] args, Event event) {
			StateDungeon.getCurrentContext().highlightTiles((Inventory.loaded.get(args[1]).path.size() > 0) ? Inventory.loaded.get(args[1]).path.get(0).x : Inventory.loaded.get(args[1]).locX,
					(Inventory.loaded.get(args[1]).path.size() > 0) ? Inventory.loaded.get(args[1]).path.get(0).y : Inventory.loaded.get(args[1]).locY, 1, Inventory.loaded.get(args[1]).weapon.range + 1, "");
		}
	},

	// Starts combat between two units
	attack(false) {
		@Override
		public void run(String[] args, Event event) {
			StateDungeon.getCurrentContext().setCombat(Inventory.loaded.get(args[1]), Inventory.loaded.get(args[2]));
			StateDungeon.getCurrentContext().combatMode = false;
			StateDungeon.getCurrentContext().clearSelectedTiles();
			StateDungeon.getCurrentContext().curRotate = true;
			AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
		}
	},

	// Loads a dungeon map
	loadmap(false) {
		@Override
		public void run(String[] args, Event event) {
			StateDungeon.loadMap(args[1]);
		}
	},

	// Loads a hub map
	loadhub(false) {
		@Override
		public void run(String[] args, Event event) {
			StateHub.loadMap(args[1]);
		}
	},

	// Changes the current phase
	setphase(false) {
		@Override
		public void run(String[] args, Event event) {
			StateDungeon.getCurrentContext().phase = Integer.parseInt(args[1]);
			StateDungeon.getCurrentContext().swap = false;
		}
	},

	// Brings up the action menu
	actionmenu(true) {
		@Override
		public void run(String[] args, Event event) {
			StateManager.currentState.menus.add(new MenuDungeonAction(new Vector3f(0, 0, -80), Inventory.loaded.get(args[1]), StateDungeon.getCurrentContext()));
		}
	},

	// Selects the current menu item
	menuselect(true) {
		@Override
		public void run(String[] args, Event event) {
			StateManager.currentState.menus.get(0).action("", Inventory.loaded.get(args[1]));
		}
	},

	// Gives a choice menu to the player based on args given
	choice(false) {
		@Override
		public void run(String[] args, Event event) {
			String[] options = new String[args.length - 2];
			for (int i = 0; i < options.length; i++) {
				options[i] = args[i + 2];
			}
			StateManager.currentState.menus.add(new MenuChoice(new Vector3f(0, 0, -80), args[1], options));
		}
	},

	// Plays a sound effect
	playsfx(true) {
		@Override
		public void run(String[] args, Event event) {
			AudioManager.getSound(args[1]).play(AudioManager.defaultMainSFXGain, false);
		}
	},

	// If the unit can carry the item, it will add it and display a message
	// If it cant, a message will be displayed
	additem(false) {
		@Override
		public void run(String[] args, Event event) {
			Unit u = Inventory.loaded.get(args[2]);
			ItemProperty item = ItemProperty.get(args[1]);
			if (u.canAddItem(item)) {
				u.addItem(item);
				StateManager.currentState.effects.add(new EffectItemShow(u, item));
				StateManager.currentState.messages.add(new Message(new String[] { "You found " + item.name + "!" }));
			} else {
				StateManager.currentState.messages.add(new Message(new String[] { "You cant carry any more" }));
			}
		}
	},

	addpauseeffect(false) {
		@Override
		public void run(String[] args, Event event) {
			Unit u = Inventory.loaded.get(args[2]);
			StateManager.currentState.effects.add(new EffectPause(u, Integer.parseInt(args[1])));
		}
	};

	public abstract void run(String[] args, Event event);

	// Determines if a command can be ran while a menu is active
	public boolean menuOp;

	EventCommand(boolean menuOp) {
		this.menuOp = menuOp;
	}

}