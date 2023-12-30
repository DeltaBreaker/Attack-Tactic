package io.itch.deltabreaker.state;

import java.awt.Point;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.UUID;

import io.itch.deltabreaker.builder.dungeon.DungeonGenerator;
import io.itch.deltabreaker.builder.dungeon.DungeonGeneratorVillage;
import io.itch.deltabreaker.core.InputMapping;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.effect.Effect;
import io.itch.deltabreaker.effect.EffectHealAura;
import io.itch.deltabreaker.effect.EffectLava;
import io.itch.deltabreaker.effect.EffectWater;
import io.itch.deltabreaker.effect.dungeon.EffectDungeonLavaSFX;
import io.itch.deltabreaker.effect.dungeon.EffectDungeonRain;
import io.itch.deltabreaker.effect.dungeon.EffectDungeonResidue;
import io.itch.deltabreaker.effect.dungeon.EffectDungeonSnow;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Light;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Cursor;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.tile.Tile;
import io.itch.deltabreaker.object.tile.TileBrazier;
import io.itch.deltabreaker.object.tile.TileCompound;
import io.itch.deltabreaker.ui.menu.MenuCreatorSystem;
import io.itch.deltabreaker.ui.menu.MenuCreatorUnit;

public class StateCreatorHub extends State {

	public static final String STATE_ID = "state.creator.hub";
	public static final String SAVE_PATH = "save/custom/map";
	public static final String LOAD_PATH = "res/data/map";

	public static int staticRenderWidth = 9;

	public Tile filler;
	public ArrayList<Tile> palletTiles = new ArrayList<>();
	public ArrayList<Unit> npcs = new ArrayList<>();
	public boolean selectingTile = false;
	public int selectionDestination = 32;
	public int selectionHeight = 0;
	public String[] modes = { "Tile Mode", "Adjust Mode", "Prop Mode", "Unit Mode", "System Mode" };
	public int editMode = 0;
	public int selectedTile = 0;
	public Light overheadLight;

	public DungeonGenerator dungeon;

	public boolean placingLava = false;
	public boolean placingWater = false;

	// Used to keep track of the new points
	public ArrayList<Point> waterPoints = new ArrayList<>();
	public ArrayList<Point> lavaPoints = new ArrayList<>();

	// Gives complete list of all points to keep the form consistent
	public ArrayList<Point> waterHistory = new ArrayList<>();
	public ArrayList<Point> lavaHistory = new ArrayList<>();
	public ArrayList<EffectWater> waterArray = new ArrayList<>();
	public ArrayList<EffectLava> lavaArray = new ArrayList<>();
	public ArrayList<EffectHealAura> healArray = new ArrayList<>();
	public ArrayList<Point> positions = new ArrayList<>();

	public StateCreatorHub() {
		super(STATE_ID);
	}

	public void tick() {
		// Moves the shadow camera along with the normal camera
		Startup.shadowCamera.targetPosition.set(Startup.camera.position.getX(), 128 + tiles[cursorPos.x][cursorPos.y].getPosition().getY() / 2, Startup.camera.position.getZ());
		Startup.shadowCamera.setRotation(new Vector3f(-60, 0, 0));
		if (editMode == 0) {
			Startup.staticView.setTargetPosition(new Vector3f(selectedTile * 16, 0, 0));
		} else if (menus.size() == 0) {
			Startup.staticView.setTargetPosition(new Vector3f(0, 0, 0));
		}

		for (int x = 0; x < DungeonGenerator.xActiveSpace; x++) {
			for (int y = 0; y < DungeonGenerator.yActiveSpace; y++) {
				if (rcamX + x < tiles.length && rcamY + y < tiles[0].length && rcamX + x > -1 && rcamY + y > -1) {
					tiles[x + rcamX][y + rcamY].tick();
				}
			}
		}

		if (menus.size() == 0) {
			cursor.setLocation(new Vector3f((int) cursorPos.getX() * 16 + 3, 34 + tiles[cursorPos.x][cursorPos.y].getPosition().getY(), (int) cursorPos.getY() * 16 - 8));
		}
		cursor.tick();
		for (int e = 0; e < effects.size(); e++) {
			effects.get(e).tick();
			if (effects.get(e).remove) {
				effects.get(e).cleanUp();
				effects.remove(e);
				e--;
			}
		}

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

		for (Unit u : npcs) {
			u.tick();
		}

		if (menus.size() > 0) {
			menus.get(0).tick();
			if (!menus.get(0).open && menus.get(0).height <= 16) {
				menus.remove(0);
			}
		}

		if (selectingTile) {
			if (selectionHeight < selectionDestination) {
				selectionHeight += 2;
			}
		} else {
			if (selectionHeight > 0) {
				selectionHeight -= 2;
			}
		}
		for (int i = 0; i < palletTiles.size(); i++) {
			palletTiles.get(i).setPosition(i * 32, -50 + selectionHeight - selectionDestination, -75);
		}
		if (camX < tcamX) {
			camX += 0.5;
		}
		if (camX > tcamX) {
			camX -= 0.5;
		}
		if (camY < tcamY) {
			camY += 0.5;
		}
		if (camY > tcamY) {
			camY -= 0.5;
		}
		rcamX = Math.floorDiv((int) camX, 8) - 21;
		rcamY = Math.floorDiv((int) camY, 8) - 20;
		Startup.camera.position.setX((float) camX);
		Startup.camera.position.setZ((float) camY);
		Startup.camera.targetPosition.setX((float) camX);
		Startup.camera.targetPosition.setZ((float) camY);
		Startup.camera.targetPosition.setY(42 + (tiles[cursorPos.x][cursorPos.y].getPosition().getY() / 2));
	}

	public void render() {
		if (selectionHeight != 0) {
			for (int t = 0; t < staticRenderWidth; t++) {
				int i = t + selectedTile - 4;
				if (i >= 0 && i < palletTiles.size()) {
					palletTiles.get(i).renderEditor();
				}
			}
		}
		for (int x = 0; x < DungeonGenerator.xActiveSpace; x++) {
			for (int y = 0; y < DungeonGenerator.yActiveSpace; y++) {
				if (rcamX + x < tiles.length && rcamY + y < tiles[0].length && rcamX + x > -1 && rcamY + y > -1) {
					if (Startup.camera.isInsideView(Vector3f.mul(tiles[rcamX + x][rcamY + y].getPosition(), Vector3f.SCALE_HALF), 8)) {
						tiles[rcamX + x][rcamY + y].render(false);
					}
				} else {
					Vector3f position = Vector3f.add(filler.getOffset(), (rcamX + x) * 16,
							(float) Math.round(dungeon.noise.getValue(((float) (rcamX + x) / (tiles.length + DungeonGenerator.xActiveSpace)), ((float) (rcamY + y) / (tiles[0].length + DungeonGenerator.yActiveSpace))) * 10) * 4f,
							(rcamY + y) * 16);
					if (Startup.camera.isInsideView(Vector3f.mul(position, Vector3f.SCALE_HALF), 8)) {
						BatchSorter.add(filler.getModel(), filler.getTexture(), filler.getShader(), filler.getMaterial().toString(), position, Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, true, false);
					}
				}
			}
		}
		for (Effect e : effects) {
			e.render();
		}
		if (menus.size() > 0) {
			menus.get(0).render();
		}
		for (Unit u : npcs) {
			float height = tiles[u.locX][u.locY].getPosition().getY();
			Vector3f position = new Vector3f(u.x, 13 + height, u.y);
			if (Startup.camera.isInsideView(position, 0.5f, 0.5f, 0.5f, 8)) {
				u.render();
			}
		}
		for (Point p : positions) {
			Vector3f pos = new Vector3f(p.x * 16, tiles[p.x][p.y].getPosition().getY() + 6, p.y * 16 + 8);
			TextRenderer.render("p", Vector3f.div(Vector3f.add(pos, new Vector3f(0, 4, -8)), Vector3f.SCALE_HALF), new Vector3f(cursor.rotation.getY(), 0, 0), Vector3f.SCALE_QUARTER, Vector4f.COLOR_BASE, false);
			TextRenderer.render("p", Vector3f.div(Vector3f.add(pos, new Vector3f(0 - 0.5f, 4, -0.25f - 8)), Vector3f.SCALE_HALF), new Vector3f(cursor.rotation.getY(), 0, 0), Vector3f.SCALE_QUARTER, Vector4f.COLOR_BLACK, false);
			TextRenderer.render("p", Vector3f.div(Vector3f.add(pos, new Vector3f(0 + 0.5f, 4, -0.25f - 8)), Vector3f.SCALE_HALF), new Vector3f(cursor.rotation.getY(), 0, 0), Vector3f.SCALE_QUARTER, Vector4f.COLOR_BLACK, false);
			TextRenderer.render("p", Vector3f.div(Vector3f.add(pos, new Vector3f(0, 0.25f + 4, -0.55f - 8)), Vector3f.SCALE_HALF), new Vector3f(cursor.rotation.getY(), 0, 0), Vector3f.SCALE_QUARTER, Vector4f.COLOR_BLACK, false);
			TextRenderer.render("p", Vector3f.div(Vector3f.add(pos, new Vector3f(0, -0.35f + 4, 0.35f - 8)), Vector3f.SCALE_HALF), new Vector3f(cursor.rotation.getY(), 0, 0), Vector3f.SCALE_QUARTER, Vector4f.COLOR_BLACK, false);
			BatchSorter.add("marker.dae", "marker.png", "main_3d", Material.DEFAULT.toString(), new Vector3f(cursorPos.x * 16, 8.5f + tiles[cursorPos.x][cursorPos.y].getPosition().getY(), cursorPos.y * 16), new Vector3f(0, 0, 0),
					Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, true, false);
		}
		cursor.render();
		TextRenderer.render(modes[editMode], Vector3f.div(Vector3f.add(cursor.position, new Vector3f(-2 - (modes[editMode].length() - 1) * 1.5f, 4, -8)), Vector3f.SCALE_HALF), new Vector3f(cursor.rotation.getY(), 0, 0),
				Vector3f.SCALE_QUARTER, Vector4f.COLOR_BASE, false);
		TextRenderer.render(modes[editMode], Vector3f.div(Vector3f.add(cursor.position, new Vector3f(-2 - (modes[editMode].length() - 1) * 1.5f - 0.5f, 4, -0.25f - 8)), Vector3f.SCALE_HALF), new Vector3f(cursor.rotation.getY(), 0, 0),
				Vector3f.SCALE_QUARTER, Vector4f.COLOR_BLACK, false);
		TextRenderer.render(modes[editMode], Vector3f.div(Vector3f.add(cursor.position, new Vector3f(-2 - (modes[editMode].length() - 1) * 1.5f + 0.5f, 4, -0.25f - 8)), Vector3f.SCALE_HALF), new Vector3f(cursor.rotation.getY(), 0, 0),
				Vector3f.SCALE_QUARTER, Vector4f.COLOR_BLACK, false);
		TextRenderer.render(modes[editMode], Vector3f.div(Vector3f.add(cursor.position, new Vector3f(-2 - (modes[editMode].length() - 1) * 1.5f, 0.25f + 4, -0.55f - 8)), Vector3f.SCALE_HALF), new Vector3f(cursor.rotation.getY(), 0, 0),
				Vector3f.SCALE_QUARTER, Vector4f.COLOR_BLACK, false);
		TextRenderer.render(modes[editMode], Vector3f.div(Vector3f.add(cursor.position, new Vector3f(-2 - (modes[editMode].length() - 1) * 1.5f, -0.35f + 4, 0.35f - 8)), Vector3f.SCALE_HALF), new Vector3f(cursor.rotation.getY(), 0, 0),
				Vector3f.SCALE_QUARTER, Vector4f.COLOR_BLACK, false);
		BatchSorter.add("marker.dae", "marker.png", "main_3d", Material.DEFAULT.toString(), new Vector3f(cursorPos.x * 16, 8.5f + tiles[cursorPos.x][cursorPos.y].getPosition().getY(), cursorPos.y * 16), new Vector3f(0, 0, 0),
				Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, true, false);

		TextRenderer.render(cursorPos.x + " - " + cursorPos.y, Vector3f.add(Vector3f.div(Startup.staticView.position, 1, 1, 1), -160, -85, -140), Vector3f.EMPTY, Vector3f.SCALE_FULL, Vector4f.COLOR_BASE, true);
	}

	public void onDestroy() {
		lights.clear();
		for (Tile[] x : tiles) {
			for (Tile y : x) {
				y.cleanUp();
			}
		}
		for (Effect e : effects) {
			e.cleanUp();
		}
	}

	public void onEnter() {
		Startup.shadowCamera.setPosition(new Vector3f(Startup.camera.position.getX(), 128, Startup.camera.position.getZ() + 5));
		Startup.screenColor.setW(1);
		Startup.screenColorTarget.setW(0);
	}

	public static void startCreating(int width, int height, String pallet) {
		StateCreatorHub state = new StateCreatorHub();
		StateManager.initState(state);
		StateManager.swapState(STATE_ID);

		state.lights.clear();
		state.effects.clear();

		state.dungeon = new DungeonGeneratorVillage(width, height, pallet).start();
		state.tiles = state.dungeon.tiles;

		state.filler = Tile.getTile(new String[] { state.dungeon.getPalletTag(), Tile.TAG_FILLER }, new Vector3f(0, 0, 0));
		state.createTileArray(state.dungeon.getPalletTag());
		
		state.cursorPos = new Point(width / 2, height / 2);
		state.cursor = new Cursor(new Vector3f(state.cursorPos.x * 16, 32, state.cursorPos.y * 16));
		state.setCamera((int) state.cursor.position.getX() / 2, (int) state.cursor.position.getZ() / 2 + 16);
		Startup.camera.setPosition(new Vector3f((float) state.camX, 32, (float) state.camY));
		Startup.camera.setRotation(new Vector3f(-60, 0, 0));
		Startup.shadowCamera.setPosition(new Vector3f(Startup.camera.position.getX(), 128 + state.tiles[state.cursorPos.x][state.cursorPos.y].getPosition().getY() / 2, Startup.camera.position.getZ() + 5));
		state.setEffects(state.dungeon.getEffectTags(), state.dungeon.getEffectVars());
		
		float[] screen = state.dungeon.getScreenColor();
		Startup.screenColor.set(screen[0], screen[0], screen[0], Startup.screenColor.getW());
		Startup.screenColorTarget.set(screen[0], screen[0], screen[0], Startup.screenColorTarget.getW());
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

	public void setCamera(int x, int y) {
		tcamX = x;
		tcamY = y;
		camX = tcamX;
		camY = tcamY;
	}

	@Override
	@SuppressWarnings("incomplete-switch")
	public void onKeyPress(InputMapping key) {
		switch (key) {

		case ADD:
			if (editMode == 2) {
				EffectHealAura effect = new EffectHealAura(tiles[cursorPos.x][cursorPos.y]);
				healArray.add(effect);
				effects.add(effect);
			}
			break;

		case REMOVE:
			if (editMode == 2) {
				for (int i = 0; i < waterArray.size(); i++) {
					Vector3f position = waterArray.get(i).position;
					if ((int) (position.getX() / 16) == cursorPos.x && (int) (position.getZ() / 16) == cursorPos.y) {
						for (int p = 0; p < waterHistory.size(); p++) {
							if (waterHistory.get(p).x == cursorPos.x && waterHistory.get(p).y == cursorPos.y) {
								waterHistory.remove(p);
								p--;
							}
						}

						waterArray.get(i).cleanUp();
						effects.remove(waterArray.get(i));
						waterArray.remove(i);
						i--;
					}
				}
				for (int i = 0; i < lavaArray.size(); i++) {
					Vector3f position = lavaArray.get(i).position;
					if ((int) (position.getX() / 16) == cursorPos.x && (int) (position.getZ() / 16) == cursorPos.y) {
						for (int p = 0; p < lavaHistory.size(); p++) {
							if (lavaHistory.get(p).x == cursorPos.x && lavaHistory.get(p).y == cursorPos.y) {
								lavaHistory.remove(p);
								p--;
							}
						}

						lavaArray.get(i).cleanUp();
						effects.remove(lavaArray.get(i));
						lavaArray.remove(i);
						i--;
					}
				}
				for (int i = 0; i < healArray.size(); i++) {
					Vector3f position = healArray.get(i).t.getPosition();
					if ((int) (position.getX() / 16) == cursorPos.x && (int) (position.getZ() / 16) == cursorPos.y) {
						healArray.get(i).cleanUp();
						effects.remove(healArray.get(i));
						healArray.remove(i);
						i--;
					}
				}
			}
			break;

		case BACK:
			if (menus.size() == 0) {
				switch (editMode) {

				case 0:
					selectingTile = !selectingTile;
					break;

				case 2:
					lavaPoints.add(new Point(cursorPos.x, cursorPos.y));
					tiles[cursorPos.x][cursorPos.y].status = 1;
					placingLava = true;
					break;

				case 3:
					for (int i = 0; i < npcs.size(); i++) {
						if (npcs.get(i).locX == cursorPos.x && npcs.get(i).locY == cursorPos.y) {
							npcs.remove(i);
							i--;
						}
					}
					break;

				}
				break;
			} else {
				menus.get(0).action("back", null);
			}
			break;

		case CONFIRM:
			if (menus.size() == 0) {
				switch (editMode) {

				case 0:
					if (!selectingTile) {
						Tile t = tiles[cursorPos.x][cursorPos.y];
						tiles[cursorPos.x][cursorPos.y].cleanUp();
						boolean hasTag = false;
						for (String s : palletTiles.get(selectedTile).getTags()) {
							if (s.equals(Tile.TAG_DECORATION_ILLUMINATION_WALL)) {
								hasTag = true;
								break;
							}
						}
						if (hasTag) {
							tiles[cursorPos.x][cursorPos.y] = new TileBrazier(Tile.getProperty(new String[] { dungeon.getPalletTag(), Tile.TAG_DECORATION_ILLUMINATION_WALL })[0],
									Vector3f.div(tiles[cursorPos.x][cursorPos.y].getPosition(), new Vector3f(16, 16, 16)), tiles[cursorPos.x][cursorPos.y]);
						} else {
							tiles[cursorPos.x][cursorPos.y] = Tile.getTile(palletTiles.get(selectedTile).property, Vector3f.div(tiles[cursorPos.x][cursorPos.y].getPosition(), 16, 16, 16));
						}

						tiles[cursorPos.x][cursorPos.y].setWaterLogged(t.isWaterLogged());
						tiles[cursorPos.x][cursorPos.y].setLavaLogged(t.isLavaLogged());
						tiles[cursorPos.x][cursorPos.y].healLogged = t.healLogged;
					}
					break;

				case 2:
					waterPoints.add(new Point(cursorPos.x, cursorPos.y));
					tiles[cursorPos.x][cursorPos.y].status = 2;
					placingWater = true;
					break;

				case 3:
					for (Unit u : npcs) {
						if (u.locX == cursorPos.x && u.locY == cursorPos.y) {
							menus.add(new MenuCreatorUnit(new Vector3f(0, 0, -80), u));
							break;
						}
					}
					if (menus.size() == 0) {
						npcs.add(new Unit(cursorPos.x, cursorPos.y, new Vector4f(1, 1, 1, 1), UUID.randomUUID().toString()));
					}
					break;

				case 4:
					Startup.staticView.setPosition(0, 0, 0);
					menus.add(new MenuCreatorSystem(this, new Vector3f(0, 0, -80)));
					break;

				}
			} else {
				menus.get(0).action("", null);
			}
			break;

		case WEAPON_LEFT:
			if (editMode > 0 && !selectingTile && menus.size() == 0) {
				editMode--;
			}
			break;

		case WEAPON_RIGHT:
			if (editMode < modes.length - 1 && !selectingTile && menus.size() == 0) {
				editMode++;
			}
			break;

		}
	}

	@Override
	@SuppressWarnings("incomplete-switch")
	public void onKeyRepeat(InputMapping key) {
		switch (key) {

		case CONFIRM:
			if (editMode == 1) {
				tiles[cursorPos.x][cursorPos.y].translate(0, 4, 0);
			}
			break;

		case BACK:
			switch (editMode) {

			case 1:
				tiles[cursorPos.x][cursorPos.y].translate(0, -4, 0);
				break;

			}
			break;

		case ADD:
			if (editMode == 1) {
				tiles[cursorPos.x][cursorPos.y].rotate(0, 90, 0);
			}
			if (editMode == 3) {
				boolean containsPoint = false;
				for (Point p : positions) {
					if (p.x == cursorPos.x && p.y == cursorPos.y) {
						containsPoint = true;
						break;
					}
				}
				if (!containsPoint) {
					positions.add(new Point(cursorPos.x, cursorPos.y));
				}
			}
			break;

		case REMOVE:
			if (editMode == 1) {
				tiles[cursorPos.x][cursorPos.y].rotate(0, -90, 0);
			}
			if (editMode == 3) {
				for (int i = 0; i < positions.size(); i++) {
					if (positions.get(i).x == cursorPos.x && positions.get(i).y == cursorPos.y) {
						positions.remove(i);
						i--;
					}
				}
			}
			break;

		case UP:
			if (menus.size() == 0) {
				if (cursorPos.y > 0 && !selectingTile) {
					cursorPos.setLocation(cursorPos.getX(), cursorPos.getY() - 1);
					if (Math.floorDiv((int) tcamY, 8) - 12 > -12 && cursorPos.getY() == Math.floorDiv((int) tcamY, 8) - 12 + 5) {
						tcamY -= 8;
					}
				}
				if (editMode == 2) {
					if (placingLava) {
						if (tiles[cursorPos.x][cursorPos.y].status != 1) {
							tiles[cursorPos.x][cursorPos.y].status = 1;
							lavaPoints.add(new Point(cursorPos.x, cursorPos.y));
							lavaHistory.add(new Point(cursorPos.x, cursorPos.y));
						}
					}
					if (placingWater) {
						if (tiles[cursorPos.x][cursorPos.y].status != 2) {
							tiles[cursorPos.x][cursorPos.y].status = 2;
							waterPoints.add(new Point(cursorPos.x, cursorPos.y));
							waterHistory.add(new Point(cursorPos.x, cursorPos.y));
						}
					}
				}
			} else {
				menus.get(0).move(-1);
			}
			break;

		case DOWN:
			if (menus.size() == 0) {
				if (cursorPos.y < tiles[0].length - 1 && !selectingTile) {
					cursorPos.setLocation(cursorPos.getX(), cursorPos.getY() + 1);
					if (Math.floorDiv((int) tcamY, 8) - 12 == cursorPos.getY() - 12) {
						tcamY += 8;
					}
				}
				if (editMode == 2) {
					if (placingLava) {
						if (tiles[cursorPos.x][cursorPos.y].status != 1) {
							tiles[cursorPos.x][cursorPos.y].status = 1;
							lavaPoints.add(new Point(cursorPos.x, cursorPos.y));
							lavaHistory.add(new Point(cursorPos.x, cursorPos.y));
						}
					}
					if (placingWater) {
						if (tiles[cursorPos.x][cursorPos.y].status != 2) {
							tiles[cursorPos.x][cursorPos.y].status = 2;
							waterPoints.add(new Point(cursorPos.x, cursorPos.y));
							waterHistory.add(new Point(cursorPos.x, cursorPos.y));
						}
					}
				}
			} else {
				menus.get(0).move(1);
			}
			break;

		case LEFT:
			if (menus.size() == 0) {
				if (editMode == 0 && selectedTile > 0 && selectingTile) {
					selectedTile--;
				}
				if (cursorPos.x > 0 && !selectingTile) {
					cursorPos.setLocation(cursorPos.getX() - 1, cursorPos.getY());
					if (Math.floorDiv((int) tcamX, 8) - 12 > -10 && cursorPos.getX() == Math.floorDiv((int) tcamX, 8) - 12 + 8) {
						tcamX -= 8;
					}
				}
				if (editMode == 2) {
					if (placingLava) {
						if (tiles[cursorPos.x][cursorPos.y].status != 1) {
							tiles[cursorPos.x][cursorPos.y].status = 1;
							lavaPoints.add(new Point(cursorPos.x, cursorPos.y));
							lavaHistory.add(new Point(cursorPos.x, cursorPos.y));
						}
					}
					if (placingWater) {
						if (tiles[cursorPos.x][cursorPos.y].status != 2) {
							tiles[cursorPos.x][cursorPos.y].status = 2;
							waterPoints.add(new Point(cursorPos.x, cursorPos.y));
							waterHistory.add(new Point(cursorPos.x, cursorPos.y));
						}
					}
				}
			} else {
				menus.get(0).action("left", null);
			}
			break;

		case RIGHT:
			if (menus.size() == 0) {
				if (editMode == 0 && selectedTile < palletTiles.size() - 1 && selectingTile) {
					selectedTile++;
				}
				if (cursorPos.x < tiles.length - 1 && !selectingTile) {
					cursorPos.setLocation(cursorPos.getX() + 1, cursorPos.getY());
					if (Math.floorDiv((int) tcamX, 8) - 12 == cursorPos.getX() - 16) {
						tcamX += 8;
					}
				}
				if (editMode == 2) {
					if (placingLava) {
						if (tiles[cursorPos.x][cursorPos.y].status != 1) {
							tiles[cursorPos.x][cursorPos.y].status = 1;
							lavaPoints.add(new Point(cursorPos.x, cursorPos.y));
							lavaHistory.add(new Point(cursorPos.x, cursorPos.y));
						}
					}
					if (placingWater) {
						if (tiles[cursorPos.x][cursorPos.y].status != 2) {
							tiles[cursorPos.x][cursorPos.y].status = 2;
							waterPoints.add(new Point(cursorPos.x, cursorPos.y));
							waterHistory.add(new Point(cursorPos.x, cursorPos.y));
						}
					}
				}
			} else {
				menus.get(0).action("right", null);
			}
			break;

		}
	}

	@SuppressWarnings("incomplete-switch")
	public void onKeyRelease(InputMapping key) {
		switch (key) {

		case CONFIRM:
			switch (editMode) {

			case 2:
				resetTileStatus();
				placingWater = false;
				break;

			}
			break;

		case BACK:
			switch (editMode) {

			case 2:
				resetTileStatus();
				placingLava = false;
				break;

			}
			break;

		}
	}

	public void resetTileStatus() {
		for (Tile[] x : tiles) {
			for (Tile y : x) {
				y.status = 0;
			}
		}
		for (Point p : waterPoints) {
			EffectWater e = new EffectWater(tiles[p.x][p.y], new Vector3f(0.427f, 0.765f, 0.9f), tiles, waterHistory);
			effects.add(e);
			waterArray.add(e);
		}
		waterPoints.clear();
		for (Point p : lavaPoints) {
			EffectLava e = new EffectLava(tiles[p.x][p.y], tiles, lavaHistory);
			effects.add(e);
			lavaArray.add(e);
		}
		lavaPoints.clear();
	}

	public void createTileArray(String palletTag) {
		Tile[] palletTiles = Tile.getTiles(new String[] { palletTag }, new Vector3f(0, 0, 0));
		for (int i = 0; i < palletTiles.length; i++) {
			palletTiles[i].setPosition(i * 32, -50, -75);
			this.palletTiles.add(palletTiles[i]);
		}
		for (Effect e : effects) {
			e.cleanUp();
		}
		effects.clear();
	}

	public void resize(int width, int height) {
		Tile[][] temp = tiles;
		tiles = new Tile[width][height];
		for (int x = 0; x < temp.length; x++) {
			for (int y = 0; y < temp[0].length; y++) {
				if (x < tiles.length && y < tiles[0].length) {
					tiles[x][y] = temp[x][y];
				} else {
					temp[x][y].cleanUp();
				}
			}
		}
		for (int x = 0; x < tiles.length; x++) {
			for (int y = 0; y < tiles[0].length; y++) {
				if (tiles[x][y] == null) {
					tiles[x][y] = Tile.getTile(new String[] { dungeon.getPalletTag(), Tile.TAG_FILLER },
							new Vector3f(x, (float) Math.round(dungeon.noise.getValue(((float) x / (tiles.length + DungeonGenerator.xActiveSpace)), ((float) y / (tiles[0].length + DungeonGenerator.yActiveSpace))) * 10) / 4f, y));
				}
			}
		}

		int cursorOffsetX = Math.max(cursorPos.x + 1 - tiles.length, 0);
		int cursorOffsetY = Math.max(cursorPos.y + 1 - tiles[0].length, 0);
		cursorPos.x -= cursorOffsetX;
		cursorPos.y -= cursorOffsetY;
		tcamX -= 8 * cursorOffsetX;
		tcamY -= 8 * cursorOffsetY;
		System.out.println("[StateCreatorHub]: Map resized to " + width + " x " + height);
	}

	public void save(String file) {
		File dir = new File(SAVE_PATH + "/" + file);
		if (!dir.exists()) {
			dir.mkdirs();
			System.out.println("[StateCreatorHub]: Save directory created");
		}

		File events = new File(SAVE_PATH + "/" + file + "/event");
		if (!events.exists()) {
			events.mkdirs();
		}

		File f = new File(dir + "/map.dat");
		try {
			DataOutputStream out = new DataOutputStream(new FileOutputStream(f));

			out.writeLong(dungeon.seed);
			out.writeUTF(dungeon.getPattern());

			out.writeInt(tiles.length);
			out.writeInt(tiles[0].length);
			for (int x = 0; x < tiles.length; x++) {
				for (int y = 0; y < tiles[0].length; y++) {
					boolean hasTag = false;
					String[] tags = tiles[x][y].getTags();
					for (String s : tags) {
						if (s.equals(Tile.TAG_DECORATION_ILLUMINATION_WALL)) {
							hasTag = true;
						}
					}
					out.writeUTF(tiles[x][y].getPropertyName());
					if (hasTag) {
						out.writeUTF(((TileCompound) tiles[x][y]).t.getPropertyName());
					}

					out.writeFloat(tiles[x][y].getPosition().getX());
					out.writeFloat(tiles[x][y].getPosition().getY());
					out.writeFloat(tiles[x][y].getPosition().getZ());

					out.writeFloat(tiles[x][y].getRotation().getX());
					out.writeFloat(tiles[x][y].getRotation().getY());
					out.writeFloat(tiles[x][y].getRotation().getZ());

					out.writeBoolean(tiles[x][y].isWaterLogged());
					out.writeBoolean(tiles[x][y].isLavaLogged());
					out.writeBoolean(tiles[x][y].healLogged);
				}
			}

			out.writeInt(npcs.size());
			for (Unit u : npcs) {
				out.writeUTF(u.uuid);
				out.writeInt(u.locX);
				out.writeInt(u.locY);
				File uDir = new File(SAVE_PATH + "/" + file + "/unit");
				if (!uDir.exists()) {
					uDir.mkdirs();
				}
				u.saveUnit(SAVE_PATH + "/" + file + "/unit/" + u.uuid + ".dat");
			}

			out.writeInt(positions.size());
			for (Point p : positions) {
				out.writeInt(p.x);
				out.writeInt(p.y);
			}

			out.flush();
			out.close();

			System.out.println("[StateCreatorHub]: " + file + " was saved");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void load(String file) {
		File f = new File(SAVE_PATH + "/" + file + "/map.dat");
		if (f.exists()) {
			try {
				DataInputStream in = new DataInputStream(new FileInputStream(f));

				long seed = in.readLong();
				String pallet = in.readUTF();
				
				int width = in.readInt();
				int height = in.readInt();

				StateCreatorHub state = new StateCreatorHub();
				StateManager.initState(state);
				StateManager.swapState(STATE_ID);

				state.lights.clear();
				state.effects.clear();

				state.dungeon = new DungeonGeneratorVillage(width, height, pallet, seed).start();
				state.createTileArray(state.dungeon.getPalletTag());
				state.filler = Tile.getTile(new String[] { state.dungeon.getPalletTag(), Tile.TAG_FILLER }, new Vector3f(0, 0, 0));
				
				state.tiles = new Tile[width][height];
				for (int x = 0; x < width; x++) {
					for (int y = 0; y < height; y++) {
						String prop = in.readUTF();
						boolean hasTag = false;
						String[] tags = Tile.getTagsFromString(prop);
						for (String s : tags) {
							if (s.equals(Tile.TAG_DECORATION_ILLUMINATION_WALL)) {
								hasTag = true;
							}
						}
						if (hasTag) {
							String tile = in.readUTF();
							Vector3f position = new Vector3f(in.readFloat() / 16, in.readFloat() / 16, in.readFloat() / 16);
							Tile base = Tile.getTile(Tile.getProperty(tile), position.copy());
							state.tiles[x][y] = new TileBrazier(Tile.getProperty(prop), position.copy(), base);
						} else {
							state.tiles[x][y] = Tile.getTile(Tile.getProperty(prop), new Vector3f(in.readFloat() / 16, in.readFloat() / 16, in.readFloat() / 16));
						}
						state.tiles[x][y].rotate(in.readFloat(), in.readFloat(), in.readFloat());

						if (in.readBoolean()) {
							Point p = new Point(x, y);
							state.waterPoints.add(p);
							state.waterHistory.add(p);
						}
						if (in.readBoolean()) {
							Point p = new Point(x, y);
							state.lavaPoints.add(p);
							state.lavaHistory.add(p);
						}
						if (in.readBoolean()) {
							EffectHealAura effect = new EffectHealAura(state.tiles[x][y]);
							state.effects.add(effect);
							state.healArray.add(effect);
						}
					}
				}
				int length = in.readInt();
				for (int i = 0; i < length; i++) {
					String uuid = in.readUTF();
					File unit = new File(SAVE_PATH + "/" + file + "/unit/" + uuid + ".dat");
					if (unit.exists()) {
						state.npcs.add(Unit.loadUnit(in.readInt(), in.readInt(), Vector4f.COLOR_BASE.copy(), SAVE_PATH + "/" + file + "/unit/" + uuid + ".dat"));
					} else {
						System.out.println("[StsteCreatorHub]: The unit file for " + file + "/" + uuid + " is missing");
					}
				}

				length = in.readInt();
				for (int i = 0; i < length; i++) {
					state.positions.add(new Point(in.readInt(), in.readInt()));
				}

				state.resetTileStatus();

				state.cursorPos = new Point(width / 2, height / 2);
				state.cursor = new Cursor(new Vector3f(state.cursorPos.x * 16, 32, state.cursorPos.y * 16));
				state.setCamera((int) state.cursor.position.getX() / 2, (int) state.cursor.position.getZ() / 2 + 16);
				Startup.camera.setPosition(new Vector3f((float) state.camX, 32, (float) state.camY));
				Startup.camera.setRotation(new Vector3f(-60, 0, 0));
				Startup.shadowCamera.setPosition(new Vector3f(Startup.camera.position.getX(), 128 + state.tiles[state.cursorPos.x][state.cursorPos.y].getPosition().getY() / 2, Startup.camera.position.getZ() + 5));
				state.setEffects(state.dungeon.getEffectTags(), state.dungeon.getEffectVars());

				in.close();

				System.out.println("[StateCreatorHub]: " + file + " was loaded");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("[StsteCreatorHub]: The map file for " + file + " is missing");
		}
	}

}