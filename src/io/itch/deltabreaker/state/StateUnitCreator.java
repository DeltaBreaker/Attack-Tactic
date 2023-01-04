package io.itch.deltabreaker.state;

import javax.swing.JOptionPane;

import io.itch.deltabreaker.builder.dungeon.DungeonGenerator;
import io.itch.deltabreaker.core.InputMapping;
import io.itch.deltabreaker.core.PerformanceManager;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.effect.Effect;
import io.itch.deltabreaker.effect.dungeon.EffectDungeonSnow;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Light;
import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Cursor;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.tile.Tile;
import io.itch.deltabreaker.ui.UIBox;
import io.itch.deltabreaker.ui.menu.Menu;

public class StateUnitCreator extends State {

	public static final String STATE_ID = "state.unit_creator";
	private static final long DUNGEON_SEED = 868533227004067064L;

	private static String returnState;

	private Tile filler;
	private static Unit unit;
	private int returnX, returnY, returnHeight;
	private DungeonGenerator dungeon;
	private int selected = 0;
	public int points = 0;

	public UIBox optionsPane1, optionsPane2;

	public Light overheadLight;

	public StateUnitCreator() {
		super(STATE_ID);
	}

	@Override
	public void tick() {
		overheadLight.position = new Vector3f(Startup.camera.position.getX(), 128 + tiles[(int) (camX / 16)][(int) (camX / 16)].getPosition().getY() / 2, Startup.camera.position.getZ());
		Startup.shadowCamera.setPosition(overheadLight.position);

		if (Startup.screenColorTarget.getW() == 1 && Startup.screenColor.getW() == 1) {
			StateManager.swapState(returnState);
		}
		for (int e = 0; e < effects.size(); e++) {
			effects.get(e).tick();
			if (effects.get(e).remove) {
				effects.get(e).cleanUp();
				effects.remove(e);
				e--;
			}
		}
		unit.tick();
		cursor.tick();
		if (menus.size() > 0) {
			menus.get(0).tick();
			if (!menus.get(0).open && menus.get(0).height <= 16) {
				menus.remove(0);
			}
		}

		if (menus.size() == 0) {
			Startup.staticView.position.set(0, 0, 0);
			Startup.staticView.targetPosition.set(0, 0, 0);
			if (selected < 8) {
				cursor.setLocation(Vector3f.add(optionsPane1.position, -8, -9 - selected * 8, 2));
			} else {
				cursor.setLocation(Vector3f.add(optionsPane2.position, -8, -9 - (selected - 8) * 8, 2));
			}
		}

		Startup.camera.position.setX((float) camX);
		Startup.camera.position.setZ((float) camY);
		Startup.camera.targetPosition.setY(42 + (tiles[25][24].getPosition().getY() / 2));
	}

	@Override
	public void render() {
		for (int x = 0; x < DungeonGenerator.xActiveSpace; x++) {
			for (int y = 0; y < DungeonGenerator.yActiveSpace; y++) {
				if (rcamX + x < tiles.length && rcamY + y < tiles[0].length && rcamX + x > -1 && rcamY + y > -1) {
					tiles[rcamX + x][rcamY + y].render(false);
				} else {
					BatchSorter.add(filler.getModel(), filler.getTexture(), filler.getShader(), filler.getMaterial().toString(),
							Vector3f.add(filler.getOffset(), (rcamX + x) * 16,
									(float) Math.round(dungeon.noise.getValue(((float) (rcamX + x) / (tiles.length + DungeonGenerator.xActiveSpace)), ((float) (rcamY + y) / (tiles[0].length + DungeonGenerator.yActiveSpace))) * 10) * 4f,
									(rcamY + y) * 16),
							new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f), new Vector4f(1, 1, 1, 1), true, false);
				}
			}
		}
		for (Effect e : effects) {
			e.render();
		}
		unit.render();

		cursor.render();

		optionsPane1.render();
		TextRenderer.render("Hair " + (unit.hair + 1), Vector3f.add(optionsPane1.position, 6, -6, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f), new Vector4f(1, 1, 1, 1), true);
		for (int i = 0; i < 38; i++) {
			BatchSorter.add("pixel.dae", "pixel.png", "static_3d", "DEFAULT", Vector3f.add(optionsPane1.position, 5 + i, -14, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f), new Vector4f((1.0f / 38f * i), 0, 0, 1), true, true);
			BatchSorter.add("pixel.dae", "pixel.png", "static_3d", "DEFAULT", Vector3f.add(optionsPane1.position, 5 + i, -22, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f), new Vector4f(0, (1.0f / 38f * i), 0, 1), true, true);
			BatchSorter.add("pixel.dae", "pixel.png", "static_3d", "DEFAULT", Vector3f.add(optionsPane1.position, 5 + i, -30, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f), new Vector4f(0, 0, (1.0f / 38f * i), 1), true, true);
		}
		BatchSorter.add("pixel.dae", "pixel.png", "static_3d", "DEFAULT", Vector3f.add(optionsPane1.position, 5 + Math.min((unit.hairColor.getX() * 38), 37f), -13, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f),
				new Vector4f(1, 1, 1, 1), true, true);
		BatchSorter.add("pixel.dae", "pixel.png", "static_3d", "DEFAULT", Vector3f.add(optionsPane1.position, 5 + Math.min((unit.hairColor.getX() * 38), 37f), -15, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f),
				new Vector4f(1, 1, 1, 1), true, true);
		BatchSorter.add("pixel.dae", "pixel.png", "static_3d", "DEFAULT", Vector3f.add(optionsPane1.position, 5 + Math.min((unit.hairColor.getY() * 38), 37f), -21, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f),
				new Vector4f(1, 1, 1, 1), true, true);
		BatchSorter.add("pixel.dae", "pixel.png", "static_3d", "DEFAULT", Vector3f.add(optionsPane1.position, 5 + Math.min((unit.hairColor.getY() * 38), 37f), -23, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f),
				new Vector4f(1, 1, 1, 1), true, true);
		BatchSorter.add("pixel.dae", "pixel.png", "static_3d", "DEFAULT", Vector3f.add(optionsPane1.position, 5 + Math.min((unit.hairColor.getZ() * 38), 37f), -29, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f),
				new Vector4f(1, 1, 1, 1), true, true);
		BatchSorter.add("pixel.dae", "pixel.png", "static_3d", "DEFAULT", Vector3f.add(optionsPane1.position, 5 + Math.min((unit.hairColor.getZ() * 38), 37f), -31, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f),
				new Vector4f(1, 1, 1, 1), true, true);

		TextRenderer.render("Body " + (unit.body + 1), Vector3f.add(optionsPane1.position, 6, -38, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f), new Vector4f(1, 1, 1, 1), true);
		for (int i = 0; i < 38; i++) {
			BatchSorter.add("pixel.dae", "pixel.png", "static_3d", "DEFAULT", Vector3f.add(optionsPane1.position, 5 + i, -46, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f), new Vector4f((1.0f / 38f * i), 0, 0, 1), true, true);
			BatchSorter.add("pixel.dae", "pixel.png", "static_3d", "DEFAULT", Vector3f.add(optionsPane1.position, 5 + i, -54, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f), new Vector4f(0, (1.0f / 38f * i), 0, 1), true, true);
			BatchSorter.add("pixel.dae", "pixel.png", "static_3d", "DEFAULT", Vector3f.add(optionsPane1.position, 5 + i, -62, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f), new Vector4f(0, 0, (1.0f / 38f * i), 1), true, true);
		}
		BatchSorter.add("pixel.dae", "pixel.png", "static_3d", "DEFAULT", Vector3f.add(optionsPane1.position, 5 + Math.min((unit.bodyColor.getX() * 38), 37f), -45, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f),
				new Vector4f(1, 1, 1, 1), true, true);
		BatchSorter.add("pixel.dae", "pixel.png", "static_3d", "DEFAULT", Vector3f.add(optionsPane1.position, 5 + Math.min((unit.bodyColor.getX() * 38), 37f), -47, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f),
				new Vector4f(1, 1, 1, 1), true, true);
		BatchSorter.add("pixel.dae", "pixel.png", "static_3d", "DEFAULT", Vector3f.add(optionsPane1.position, 5 + Math.min((unit.bodyColor.getY() * 38), 37f), -53, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f),
				new Vector4f(1, 1, 1, 1), true, true);
		BatchSorter.add("pixel.dae", "pixel.png", "static_3d", "DEFAULT", Vector3f.add(optionsPane1.position, 5 + Math.min((unit.bodyColor.getY() * 38), 37f), -55, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f),
				new Vector4f(1, 1, 1, 1), true, true);
		BatchSorter.add("pixel.dae", "pixel.png", "static_3d", "DEFAULT", Vector3f.add(optionsPane1.position, 5 + Math.min((unit.bodyColor.getZ() * 38), 37f), -61, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f),
				new Vector4f(1, 1, 1, 1), true, true);
		BatchSorter.add("pixel.dae", "pixel.png", "static_3d", "DEFAULT", Vector3f.add(optionsPane1.position, 5 + Math.min((unit.bodyColor.getZ() * 38), 37f), -63, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f),
				new Vector4f(1, 1, 1, 1), true, true);

		optionsPane2.render();
		String[] stats = { "hp " + unit.baseHp, "atk " + unit.baseAtk, "mag " + unit.baseMag, "spd " + unit.baseSpd, "def " + unit.baseDef, "res " + unit.baseRes, "mov " + unit.baseMovement, unit.name, "finish" };
		for (int i = 0; i < stats.length; i++) {
			TextRenderer.render(stats[i], Vector3f.add(optionsPane2.position, 6, -6 - i * 8, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f), new Vector4f(1, 1, 1, 1), true);
		}

		for (Menu m : menus) {
			m.render();
		}
	}

	@Override
	public void onEnter() {
		optionsPane1 = new UIBox(new Vector3f(-70, 34, -80), 54, 74);
		optionsPane2 = new UIBox(new Vector3f(24, 34, -80), 66, 83);

		returnX = unit.locX;
		returnY = unit.locY;
		returnHeight = (int) unit.height;
		unit.placeAt(24, 26);
		selected = 0;
		Startup.screenColor.setW(1);
		points = 0;
		menus.clear();
		cursor = new Cursor(new Vector3f(-70, 34, -80));
		cursor.staticView = true;

		dungeon = new DungeonGenerator("snow_forrest.json", 99, DUNGEON_SEED).startWithoutUnits();
		filler = Tile.getTile(new String[] { dungeon.getPalletTag(), Tile.TAG_FILLER }, new Vector3f(0, 0, 0));
		tiles = dungeon.tiles;
		tiles[24][24] = Tile.getTile(new String[] { "pallet.snow_forrest", "tile.campfire" }, new Vector3f(24, tiles[24][24].getPosition().getY() / 16, 24));
		tiles[24][26] = Tile.getTile(new String[] { "pallet.snow_forrest", "tile.floor.center" }, new Vector3f(24, tiles[24][26].getPosition().getY() / 16, 26));
		unit.height = tiles[24][26].getPosition().getY();

		camX = 200 - 8;
		camY = 208 + 16;
		rcamX = Math.floorDiv((int) camX, 8) - 15;
		rcamY = Math.floorDiv((int) camY, 8) - 17;

		Startup.staticView.position.set(0, 0, 0);
		Startup.camera.setPosition(new Vector3f((float) camX, 42, (float) camY));
		Startup.camera.position.setY(42 + (tiles[25][24].getPosition().getY() / 2));
		Startup.camera.targetPosition.setY(42 + (tiles[25][24].getPosition().getY() / 2));
		Startup.camera.setRotation(new Vector3f(-60, 0, 0));

		setEffects(dungeon.getEffectTags(), dungeon.getEffectVars());
	}

	@Override
	public void onExit() {
		for (Tile[] x : tiles) {
			for (Tile y : x) {
				y.cleanUp();
			}
		}
		for (Effect e : effects) {
			e.cleanUp();
		}
		StateManager.initState(new StateUnitCreator());
		unit.placeAt(returnX, returnY);
		unit.height = returnHeight;
	}

	public static void swap(Unit unit, String returnState) {
		StateUnitCreator.unit = unit;
		StateUnitCreator.returnState = returnState;
		StateManager.swapState(STATE_ID);
	}

	private void setEffects(String[] tags, double[][] vars) {
		for (int i = 0; i < tags.length; i++) {
			switch (tags[i]) {

			case DungeonGenerator.TAG_LIGHT_MAIN:
				overheadLight = new Light(Startup.shadowCamera.position, new Vector3f((float) vars[i][0], (float) vars[i][1], (float) vars[i][2]), (float) vars[i][3], (float) vars[i][4], (float) vars[i][5], Vector3f.DOWN);
				lights.add(overheadLight);
				break;

			case DungeonGenerator.TAG_EFFECT_SNOW:
				effects.add(new EffectDungeonSnow());
				break;

			}
		}
	}

	@Override
	public void onKeyPress(InputMapping key) {
		if (selected == 15) {
			if (key == InputMapping.CONFIRM) {
				PerformanceManager.checkForCrash = false;
				unit.name = JOptionPane.showInputDialog(null);
				PerformanceManager.checkForCrash = true;
			}
		}

		if (Startup.screenColorTarget.getW() < 0.1f && selected == 16 && key == InputMapping.CONFIRM) {
			if (menus.size() == 0) {
				menus.add(new Menu(new Vector3f(-10, 12, -80), new String[] { "yes", "no" }) {
					@Override
					public void action(String command, Unit unit) {
						switch (options[selected]) {

						case "yes":
							Startup.screenColorTarget.set(1, 1, 1, 1);
							close();
							break;

						case "no":
							close();
							break;

						}
					}
				});
			} else {
				menus.get(0).action("", unit);
			}
		}

		if (key == InputMapping.BACK && menus.size() > 0) {
			menus.get(0).close();
		}
	}

	@Override
	@SuppressWarnings("incomplete-switch")
	public void onKeyRepeat(InputMapping key) {
		if (Startup.screenColorTarget.getW() < 0.1f) {
			switch (key) {

			case DOWN:
				if (menus.size() == 0) {
					if (selected < 16) {
						selected++;
					}
				} else {
					menus.get(0).move(1);
				}
				break;

			case UP:
				if (menus.size() == 0) {
					if (selected > 0) {
						selected--;
					}
				} else {
					menus.get(0).move(-1);
				}
				break;

			case RIGHT:
				switch (selected) {

				case 0:
					if (unit.hair < Unit.HAIR_STYLE_COUNT - 1) {
						unit.hair++;
					}
					break;

				case 1:
					if (unit.hairColor.getX() < 1) {
						unit.hairColor.setX(Math.min(1, unit.hairColor.getX() + 0.05f));
					}
					break;

				case 2:
					if (unit.hairColor.getY() < 1) {
						unit.hairColor.setY(Math.min(1, unit.hairColor.getY() + 0.05f));
					}
					break;

				case 3:
					if (unit.hairColor.getZ() < 1) {
						unit.hairColor.setZ(Math.min(1, unit.hairColor.getZ() + 0.05f));
					}
					break;

				case 4:
					if (unit.body < 3) {
						unit.body++;
					}
					break;

				case 5:
					if (unit.bodyColor.getX() < 1) {
						unit.bodyColor.setX(Math.min(1, unit.bodyColor.getX() + 0.05f));
					}
					break;

				case 6:
					if (unit.bodyColor.getY() < 1) {
						unit.bodyColor.setY(Math.min(1, unit.bodyColor.getY() + 0.05f));
					}
					break;

				case 7:
					if (unit.bodyColor.getZ() < 1) {
						unit.bodyColor.setZ(Math.min(1, unit.bodyColor.getZ() + 0.05f));
					}
					break;

				case 8:
					unit.baseHp++;
					break;

				case 9:
					unit.baseAtk++;
					break;

				case 10:
					unit.baseMag++;
					break;

				case 11:
					unit.baseSpd++;
					break;

				case 12:
					unit.baseDef++;
					break;

				case 13:
					unit.baseRes++;
					break;

				case 14:
					unit.baseMovement++;
					break;

				}
				break;

			case LEFT:
				switch (selected) {

				case 0:
					if (unit.hair > 0) {
						unit.hair--;
					}
					break;

				case 1:
					if (unit.hairColor.getX() > 0) {
						unit.hairColor.setX(Math.max(0, unit.hairColor.getX() - 0.05f));
					}
					break;

				case 2:
					if (unit.hairColor.getY() > 0) {
						unit.hairColor.setY(Math.max(0, unit.hairColor.getY() - 0.05f));
					}
					break;

				case 3:
					if (unit.hairColor.getZ() > 0) {
						unit.hairColor.setZ(Math.max(0, unit.hairColor.getZ() - 0.05f));
					}
					break;

				case 4:
					if (unit.body > 0) {
						unit.body--;
					}
					break;

				case 5:
					if (unit.bodyColor.getX() > 0) {
						unit.bodyColor.setX(Math.max(0, unit.bodyColor.getX() - 0.05f));
					}
					break;

				case 6:
					if (unit.bodyColor.getY() > 0) {
						unit.bodyColor.setY(Math.max(0, unit.bodyColor.getY() - 0.05f));
					}
					break;

				case 7:
					if (unit.bodyColor.getZ() > 0) {
						unit.bodyColor.setZ(Math.max(0, unit.bodyColor.getZ() - 0.05f));
					}
					break;

				case 8:
					if (unit.baseHp > 1) {
						unit.baseHp--;
					}
					break;

				case 9:
					if (unit.baseAtk > 0) {
						unit.baseAtk--;
					}
					break;

				case 10:
					if (unit.baseMag > 0) {
						unit.baseMag--;
					}
					break;

				case 11:
					if (unit.baseSpd > 0) {
						unit.baseSpd--;
					}
					break;

				case 12:
					if (unit.baseDef > 0) {
						unit.baseDef--;
					}
					break;

				case 13:
					if (unit.baseRes > 0) {
						unit.baseRes--;
					}
					break;

				case 14:
					if (unit.baseMovement > 0) {
						unit.baseMovement--;
					}
					break;

				}
				break;

			}
		}
	}
}
