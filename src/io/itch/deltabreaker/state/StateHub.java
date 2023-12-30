package io.itch.deltabreaker.state;

import java.awt.Point;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;

import io.itch.deltabreaker.builder.dungeon.DungeonGenerator;
import io.itch.deltabreaker.builder.dungeon.DungeonGeneratorVillage;
import io.itch.deltabreaker.core.FileManager;
import io.itch.deltabreaker.core.InputManager;
import io.itch.deltabreaker.core.InputMapping;
import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.effect.Effect;
import io.itch.deltabreaker.effect.EffectHealAura;
import io.itch.deltabreaker.effect.EffectLava;
import io.itch.deltabreaker.effect.EffectWater;
import io.itch.deltabreaker.effect.dungeon.EffectDungeonLavaSFX;
import io.itch.deltabreaker.effect.dungeon.EffectDungeonRain;
import io.itch.deltabreaker.effect.dungeon.EffectDungeonResidue;
import io.itch.deltabreaker.effect.dungeon.EffectDungeonSnow;
import io.itch.deltabreaker.event.Event;
import io.itch.deltabreaker.event.EventScript;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Light;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Cursor;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.tile.Tile;
import io.itch.deltabreaker.ui.menu.Menu;

public class StateHub extends State {

	public static final String STATE_ID = "state.hub";

	public static final String ACTION_LOAD = "hub.action.load";
	
	public String fadeOption = "";
	public int loadFolder;
	
	public HashMap<String, EventScript> eventList = new HashMap<>();
	public ArrayList<Event> events = new ArrayList<>();
	public ArrayList<Unit> npcs = new ArrayList<>();
	public DungeonGenerator dungeon;
	public Tile filler;
	public Light overheadLight;
	
	public StateHub() {
		super(STATE_ID);
	}

	public void tick() {
		if (Inventory.units.size() > 0) {
			camX = Inventory.units.get(0).x / 2.0;
			camY = Inventory.units.get(0).y / 2.0 + 24;
			rcamX = Math.floorDiv((int) camX, 8) - 15;
			rcamY = Math.floorDiv((int) camY, 8) - 17;
			Startup.camera.targetPosition.setX((float) camX);
			Startup.camera.targetPosition.setZ((float) camY);
			Startup.camera.targetPosition.setY(42 + (tiles[Inventory.units.get(0).locX][Inventory.units.get(0).locY].getPosition().getY() / 2));
			Startup.shadowCamera.setPosition(Startup.camera.position.getX(), Startup.shadowCamera.position.getY(), Startup.camera.position.getZ());
			Startup.shadowCamera.targetPosition.setY(128 + tiles[Inventory.units.get(0).locX][Inventory.units.get(0).locY].getPosition().getY() / 2);
			Startup.shadowCamera.setRotation(new Vector3f(-60, 0, 0));

			// Updates each tile in the camera range
			for (int x = 0; x < DungeonGenerator.xActiveSpace; x++) {
				for (int y = 0; y < DungeonGenerator.yActiveSpace; y++) {
					if (rcamX + x < tiles.length && rcamY + y < tiles[0].length && rcamX + x > -1 && rcamY + y > -1) {
						tiles[x + rcamX][y + rcamY].tick();
					}
				}
			}

			Inventory.units.get(0).tick();
		}
		for (Unit u : npcs) {
			u.tick();
		}

		for (int e = 0; e < effects.size(); e++) {
			effects.get(e).tick();
			if (effects.get(e).remove) {
				effects.get(e).cleanUp();
				effects.remove(e);
				e--;
			}
		}

		if (text.size() > 0) {
			text.get(0).tick();
			if (text.get(0).close && text.get(0).openInt == 0) {
				text.remove(0);
			}
		}

		if (menus.size() > 0) {
			menus.get(0).tick();
			if (!menus.get(0).open && menus.get(0).height <= 16) {
				menus.remove(0);
			}
		}

		cursor.tick();

		// This sorts the array of effects so that the ones closest get rendered first
		ArrayList<Effect> sortedEffects = new ArrayList<>();
		int size = effects.size();
		for (int e = 0; e < size; e++) {
			Effect closest = effects.get(0);

			for (int i = 0; i < effects.size(); i++) {
				float distA = Vector3f.distance(effects.get(i).position, Vector3f.mul(Startup.camera.position, 2, 2, 2));
				float distB = Vector3f.distance(closest.position, Vector3f.mul(Startup.camera.position, 2, 2, 2));

				if (distA < distB) {
					closest = effects.get(i);
				} else if (distA == distB) {
					if (effects.get(i).priority > closest.priority) {
						closest = effects.get(i);
					}
				}
			}
			sortedEffects.add(closest);
			effects.remove(closest);
		}
		effects = sortedEffects;

		if (events.size() > 0) {
			boolean unitMoving = false;
			for (Unit u : Inventory.loaded.values()) {
				if (u.locX * 16 != u.x || u.locY * 16 != u.y) {
					unitMoving = true;
					break;
				}
			}
			if ((noUIOnScreen() || events.get(0).canProcessDuringMenu()) && (!unitMoving || events.get(0).getActivator().startsWith(EventScript.ACTIVATOR_WAIT))) {
				events.get(0).tick();
				if (events.get(0).finished) {
					events.remove(0);
				}
			}
		}
				
		if(Startup.screenColor.equals(Startup.screenColorTarget) && Startup.screenColor.getW() >= 1) {
			switch(fadeOption) {
			
			case ACTION_LOAD:
				Inventory.loadGame(loadFolder);
				loadMap(Inventory.loadMap);
				break;
			
			}
		}
	}

	public void render() {
		for (int x = 0; x < DungeonGenerator.xActiveSpace; x++) {
			for (int y = 0; y < DungeonGenerator.yActiveSpace; y++) {
				if (rcamX + x < tiles.length && rcamY + y < tiles[0].length && rcamX + x > -1 && rcamY + y > -1) {
					if (Startup.camera.isInsideView(tiles[rcamX + x][rcamY + y].getPosition(), 0.5f, 0.5f, 0.5f, 10)) {
						tiles[rcamX + x][rcamY + y].render(false);
					}
				} else {
					Vector3f position = Vector3f.add(filler.getOffset(), (rcamX + x) * 16,
							(float) Math.round(dungeon.noise.getValue(((float) (rcamX + x) / (tiles.length + DungeonGenerator.xActiveSpace)), ((float) (rcamY + y) / (tiles[0].length + DungeonGenerator.yActiveSpace))) * 10) * 4f,
							(rcamY + y) * 16);
					if (Startup.camera.isInsideView(position, 0.5f, 0.5f, 0.5f, 8)) {
						BatchSorter.add(filler.getModel(), filler.getTexture(), filler.getShader(), filler.getMaterial().toString(), position, Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, true, false);
					}
				}
			}
		}
		if (Inventory.units.size() > 0) {
			Unit control = Inventory.units.get(0);
			control.render();
		}
		boolean showIcon = false;
		for (Unit u : npcs) {
			u.render();
			if (!showIcon && hasEvent(u) && noUIOnScreen() && events.size() == 0) {
				BatchSorter.add("gui_action.dae", "gui_action.png", "main_3d_nobloom_texcolor", Material.DEFAULT.toString(),
						Vector3f.add(new Vector3f(u.x, 13 + u.height, u.y), 2, 8 + AdvMath.sin[(int) Startup.universalAge % 360] * 1.5f, -12), Inventory.units.get(0).rotation, Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, false, false);
			}
		}
		for (EventScript e : eventList.values()) {
			String[] args = e.activator.split(" ");
			if (args[0].equals(EventScript.ACTIVATOR_ACTION) && args.length == 3) {
				Vector3f position = Vector3f.add(tiles[Integer.parseInt(args[1])][Integer.parseInt(args[2])].getPosition(), 0, 20, 0);
				BatchSorter.add("gui_action.dae", "gui_action.png", "main_3d_nobloom_texcolor", Material.DEFAULT.toString(), Vector3f.add(position, 0, 8 + AdvMath.sin[(int) Startup.universalAge % 360] * 1.5f, -1),
						Vector3f.DEFAULT_INVERSE_CAMERA_ROTATION, Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, false, false);
			}
		}
		for (Effect e : effects) {
			e.render();
		}
		if (text.size() > 0) {
			text.get(0).render();
		}
		for (Menu m : menus) {
			m.render();
		}
		if (!hideCursor || (menus.size() > 0 && menus.get(0).open)) {
			cursor.render();
		}
	}

	public void onEnter() {
		InputManager.repeatDelay = 0;
		Startup.screenColor.setW(1);
		Startup.screenColorTarget.setW(0);

		InputManager.repeatDelay = 0;
		InputManager.keyTime = 30;
		Unit.movementSpeed = 0.5f;

		Startup.camera.speedY = 0.125f;
		Startup.shadowCamera.speedY = 0.125f;

		cursor = new Cursor(Vector3f.EMPTY.copy());
	}

	public void onExit() {
		InputManager.repeatDelay = 10;
		for (Effect e : effects) {
			e.cleanUp();
		}
		InputManager.repeatDelay = 10;
		InputManager.keyTime = 15;
		Unit.movementSpeed = 1f;
	}

	public boolean canProcessEvent() {
		boolean unitMoving = false;
		for (Unit u : Inventory.loaded.values()) {
			if (u.locX * 16 != u.x || u.locY * 16 != u.y) {
				unitMoving = true;
				break;
			}
		}
		return noUIOnScreen() && !unitMoving;
	}

	public boolean noUIOnScreen() {
		return (text.size() == 0 && menus.size() == 0 && messages.size() == 0);
	}

	public boolean hasEvent(Unit u) {
		for (EventScript e : eventList.values()) {
			String[] activator = e.activator.split(" ");
			if (activator[0].equals(EventScript.ACTIVATOR_ACTION) && activator[1].equals(u.uuid)) {
				return true;
			}
		}
		return false;
	}

	public static void loadMap(String map) {
		File f = new File(StateCreatorHub.LOAD_PATH + "/" + map + "/map.dat");

		if (f.exists()) {
			try {
				StateHub hub = new StateHub();
				StateManager.initState(hub);
				StateManager.swapState(STATE_ID);
				DataInputStream in = new DataInputStream(new FileInputStream(f));

				long seed = in.readLong();
				String pallet = in.readUTF();
				int width = in.readInt();
				int height = in.readInt();

				hub.dungeon = new DungeonGeneratorVillage(width, height, pallet, seed).start();
				hub.filler = Tile.getTile(new String[] { hub.dungeon.getPalletTag(), Tile.TAG_FILLER }, new Vector3f(-1, -1, -1));

				ArrayList<Point> waterPoints = new ArrayList<>();
				ArrayList<Point> lavaPoints = new ArrayList<>();
				hub.tiles = new Tile[width][height];
				for (int x = 0; x < width; x++) {
					for (int y = 0; y < height; y++) {
						hub.tiles[x][y] = Tile.getTile(Tile.getProperty(in.readUTF()), new Vector3f(in.readFloat() / 16, in.readFloat() / 16, in.readFloat() / 16));
						hub.tiles[x][y].setRotation(in.readFloat(), in.readFloat(), in.readFloat());

						if (in.readBoolean()) {
							waterPoints.add(new Point(x, y));
						}
						if (in.readBoolean()) {
							lavaPoints.add(new Point(x, y));
						}
						if (in.readBoolean()) {
							EffectHealAura effect = new EffectHealAura(hub.tiles[x][y]);
							hub.effects.add(effect);
						}
					}
				}
				for (Point p : waterPoints) {
					EffectWater e = new EffectWater(hub.tiles[p.x][p.y], new Vector3f(0.427f, 0.765f, 0.9f), hub.tiles, waterPoints);
					hub.effects.add(e);
				}
				for (Point p : lavaPoints) {
					EffectLava e = new EffectLava(hub.tiles[p.x][p.y], hub.tiles, lavaPoints);
					hub.effects.add(e);
				}

				int length = in.readInt();
				for (int i = 0; i < length; i++) {
					String uuid = in.readUTF();
					File unit = new File(StateCreatorHub.LOAD_PATH + "/" + map + "/unit/" + uuid + ".dat");
					if (unit.exists()) {
						hub.npcs.add(Unit.loadUnit(in.readInt(), in.readInt(), Vector4f.COLOR_BASE.copy(), StateCreatorHub.LOAD_PATH + "/" + map + "/unit/" + uuid + ".dat"));
					} else {
						System.out.println("[StsteCreatorHub]: The unit file for " + StateCreatorHub.LOAD_PATH + "/" + map + "/" + uuid + " is missing");
					}
				}

				length = in.readInt();
				Point[] p = new Point[length];
				for (int i = 0; i < length; i++) {
					p[i] = new Point(in.readInt(), in.readInt());
				}

				// Read start points and use the first to place the unit
				if (Inventory.units.size() > 0) {
					Inventory.units.get(0).prepare();

					Inventory.units.get(0).placeAt(p[0].x, p[0].y);
					hub.camX = Inventory.units.get(0).locX * 8;
					hub.camY = Inventory.units.get(0).locY * 8;
				}

				in.close();

				Startup.camera.setPosition(new Vector3f((float) hub.camX, 32, (float) hub.camY));
				Startup.camera.setRotation(new Vector3f(-60, 0, 0));
				if (Inventory.units.size() > 0) {
					Startup.shadowCamera.setPosition(new Vector3f(Startup.camera.position.getX(), 128 + hub.tiles[Inventory.units.get(0).locX][Inventory.units.get(0).locY].getPosition().getY() / 2, Startup.camera.position.getZ() + 5));
				}
				hub.setEffects(hub.dungeon.getEffectTags(), hub.dungeon.getEffectVars());

				for (File e : FileManager.getFiles(StateCreatorHub.LOAD_PATH + "/" + map + "/event")) {
					if (e.getName().endsWith(".json")) {
						hub.eventList.put(e.getName(), EventScript.loadScript(e));
					}
				}

				for (EventScript e : hub.eventList.values()) {
					if (e.activator.equals(EventScript.ACTIVATOR_MAP_LOAD)) {
						hub.events.add(new Event(e));
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			JOptionPane.showMessageDialog(null, "[StateHub]: File " + map + " not foud");
			System.out.println("[StateHub]: File " + map + " not foud");
			System.exit(0);
		}
	}

	private void setEffects(String[] tags, double[][] vars) {
		Startup.enableHaze = false;
		for (int i = 0; i < tags.length; i++) {
			switch (tags[i]) {

			case DungeonGenerator.TAG_LIGHT_MAIN:
				overheadLight = new Light(Startup.shadowCamera.position, new Vector3f((float) vars[i][0], (float) vars[i][1], (float) vars[i][2]), (float) vars[i][3], (float) vars[i][4], (float) vars[i][5], Vector3f.DOWN);
				lights.add(overheadLight);
				break;

			case DungeonGenerator.TAG_EFFECT_SNOW:
				effects.add(new EffectDungeonSnow());
				break;

			case DungeonGenerator.TAG_EFFECT_LAVA:
				effects.add(new EffectDungeonLavaSFX());
				break;

			case DungeonGenerator.TAG_EFFECT_RAIN:
				effects.add(new EffectDungeonRain(overheadLight));
				break;

			case DungeonGenerator.TAG_EFFECT_RESIDUE:
				effects.add(new EffectDungeonResidue());
				break;

			case DungeonGenerator.TAG_EFFECT_HEAT:
				Startup.enableHaze = true;
				break;

			}
		}
	}

	@SuppressWarnings("incomplete-switch")
	public void onKeyPress(InputMapping key) {
		if (Inventory.units.size() > 0) {
			switch (key) {

			case JOYSTICK:
				if (noUIOnScreen() && !controlLock) {
					float[] axes = key.getAxes();
					Unit u = Inventory.units.get(0);
					if (u.x == u.locX * 16 && u.y == u.locY * 16) {
						if (axes[0] > 0.25) {
							if (u.locY < tiles[0].length - 1 && isTileWalkable(u.locX + 1, u.locY)) {
								u.locX++;
								checkForMovementEvent();
							}
						}
						if (axes[0] < -0.25) {
							if (u.locY > 0 && isTileWalkable(u.locX - 1, u.locY)) {
								u.locX--;
								checkForMovementEvent();
							}
						}
						if (axes[1] > 0.25) {
							if (u.locX < tiles.length - 1 && isTileWalkable(u.locX, u.locY + 1)) {
								u.locY++;
								checkForMovementEvent();
							}
						}
						if (axes[1] < -0.25) {
							if (u.locX > 0 && isTileWalkable(u.locX, u.locY - 1)) {
								u.locY--;
								checkForMovementEvent();
							}
						}
					}
				}
				break;

			case UP:
				if (menus.size() > 0) {
					menus.get(0).move(-1);
					return;
				}
				break;

			case DOWN:
				if (menus.size() > 0) {
					menus.get(0).move(1);
					return;
				}
				break;

			case LEFT:
				if (menus.size() > 0) {
					return;
				}
				break;

			case RIGHT:
				if (menus.size() > 0) {
					return;
				}
				break;

			case CONFIRM:
				if (menus.size() != 0) {
					menus.get(0).action("", null);
					return;
				}
				if (text.size() == 0) {
					if (!controlLock) {
						for (Unit u : npcs) {
							if (Math.abs(u.locX - Inventory.units.get(0).locX) + Math.abs(u.locY - Inventory.units.get(0).locY) == 1) {
								for (EventScript e : eventList.values()) {
									String[] activator = e.activator.split(" ");
									if (activator[0].equals(EventScript.ACTIVATOR_ACTION) && activator[1].equals(u.uuid)) {
										events.add(new Event(e));
									}
								}
							}
						}
						for (EventScript e : eventList.values()) {
							String[] args = e.activator.split(" ");
							if (args[0].equals(EventScript.ACTIVATOR_ACTION) && args.length == 3) {
								if (Math.abs(Integer.parseInt(args[1]) - Inventory.units.get(0).locX) + Math.abs(Integer.parseInt(args[2]) - Inventory.units.get(0).locY) <= 1) {
									events.add(new Event(e));
								}
							}
						}
						Unit control = Inventory.units.get(0);
						if (control.locX > 0) {
							tiles[control.locX - 1][control.locY].action(control, new String[] {});
						}
						if (control.locX < tiles.length - 1) {
							tiles[control.locX + 1][control.locY].action(control, new String[] {});
						}
						if (control.locY > 0) {
							tiles[control.locX][control.locY - 1].action(control, new String[] {});
						}
						if (control.locY < tiles[0].length - 1) {
							tiles[control.locX][control.locY + 1].action(control, new String[] {});
						}
					}
				} else {
					text.get(0).next();
				}
				break;

			case BACK:
				if (menus.size() != 0) {
					menus.get(0).action("back", null);
					return;
				}

				if (text.size() != 0) {
					text.get(0).next();
					return;
				}
				break;

			}
		}
	}

	public void onKeyRelease(int key) {

	}

	@SuppressWarnings("incomplete-switch")
	public void onKeyRepeat(InputMapping key) {
		if (Inventory.units.size() > 0) {
			switch (key) {

			case UP:
				if (!controlLock && menus.size() == 0) {
					if (Inventory.units.get(0).locY > 0 && isTileWalkable(Inventory.units.get(0).locX, Inventory.units.get(0).locY - 1)) {
						Inventory.units.get(0).locY--;
						checkForMovementEvent();
					}
				}
				break;

			case DOWN:
				if (!controlLock && menus.size() == 0) {
					if (Inventory.units.get(0).locY < tiles[0].length - 1 && isTileWalkable(Inventory.units.get(0).locX, Inventory.units.get(0).locY + 1)) {
						Inventory.units.get(0).locY++;
						checkForMovementEvent();
					}
				}
				break;

			case LEFT:
				if (!controlLock && menus.size() == 0) {
					if (Inventory.units.get(0).locX > 0 && isTileWalkable(Inventory.units.get(0).locX - 1, Inventory.units.get(0).locY)) {
						Inventory.units.get(0).locX--;
						checkForMovementEvent();
					}
				}
				break;

			case RIGHT:
				if (!controlLock && menus.size() == 0) {
					if (Inventory.units.get(0).locX < tiles.length - 1 && isTileWalkable(Inventory.units.get(0).locX + 1, Inventory.units.get(0).locY)) {
						Inventory.units.get(0).locX++;
						checkForMovementEvent();
					}
				}
				break;

			}
		}
	}

	private void checkForMovementEvent() {
		for (EventScript e : eventList.values()) {
			String[] parts = e.activator.split(" ");
			if (parts[0].equals(EventScript.ACTIVATOR_WAIT) && Integer.parseInt(parts[1]) == Inventory.units.get(0).locX && Integer.parseInt(parts[2]) == Inventory.units.get(0).locY) {
				events.add(new Event(e));
			}
		}
	}

	private boolean isTileWalkable(int x, int y) {
		for (Unit u : npcs) {
			if (u.locX == x && u.locY == y) {
				return false;
			}
		}
		return !tiles[x][y].isSolid();
	}

	public static StateHub getCurrentContext() {
		return (StateHub) StateManager.currentState;
	}

}
