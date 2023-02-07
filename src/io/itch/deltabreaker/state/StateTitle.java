package io.itch.deltabreaker.state;

import java.util.ArrayList;
import java.util.Random;

import org.lwjgl.glfw.GLFW;

import io.itch.deltabreaker.builder.dungeon.DungeonGenerator;
import io.itch.deltabreaker.core.InputMapping;
import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.effect.Effect;
import io.itch.deltabreaker.effect.dungeon.EffectDungeonLavaSFX;
import io.itch.deltabreaker.effect.dungeon.EffectDungeonRain;
import io.itch.deltabreaker.effect.dungeon.EffectDungeonResidue;
import io.itch.deltabreaker.effect.dungeon.EffectDungeonSnow;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Light;
import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Cursor;
import io.itch.deltabreaker.object.tile.Tile;
import io.itch.deltabreaker.ui.Message;
import io.itch.deltabreaker.ui.menu.Menu;
import io.itch.deltabreaker.ui.menu.MenuOptions;
import io.itch.deltabreaker.ui.menu.MenuTitle;

public class StateTitle extends State {

	public static final String OPTION_NEW = "title.option.new";
	public static final String OPTION_LOAD = "title.option.load";

	public static final String STATE_ID = "state.title";
	public static final String[] OPTIONS = { "start", "options", "quit" };

	public static String title;
	public static String titleIcon;

	private Vector3f titlePosition = new Vector3f(-(title.length() - 1) * 3f, 20, -45);
	public ArrayList<Message> messages = new ArrayList<>();

	private Tile filler;
	private DungeonGenerator dungeon;
	private Light overheadLight;

	private double tileRot = 0;
	private int tileRadius;
	private double rotSpeed;

	private Vector3f optionsPosition = new Vector3f(100, -23, -90);
	private float uiOffset = 0;
	private float uiOffsetLimit = 20;
	private float uiOffsetSpeed = 0.75f;
	public boolean hideMenu = false;

	private int selected = 0;
	private double optionsRot = 0;
	private int optionsRadius = 35;
	private double optionsRotSpeed = 6;
	private double optionGlow = 0;
	private double optionGlowSpeed = 1;

	public String fadeOption;
	public int loadMap;

	public StateTitle() {
		super(STATE_ID);
	}

	@Override
	public void tick() {
		Startup.shadowCamera.targetPosition.set(Startup.camera.position.getX(), 80 + tiles[(int) (camX / 16)][(int) (camX / 16)].getPosition().getY() / 2, Startup.camera.position.getZ());
		overheadLight.position.set(Startup.shadowCamera.position.getX(), Startup.shadowCamera.position.getY() + 48, Startup.shadowCamera.position.getZ());
		if (Startup.screenColor.getW() == 1 && Startup.screenColorTarget.getW() == 1) {
			switch (fadeOption) {

			case OPTION_NEW:
				StateHub.loadMap(Inventory.loadMap);
				break;

			case OPTION_LOAD:
				Inventory.loadGame(loadMap);
				StateHub.loadMap(Inventory.loadMap);
				break;

			}
		}
		if (hideMenu) {
			if (uiOffset < uiOffsetLimit) {
				uiOffset = Math.min(uiOffsetLimit, uiOffset + uiOffsetSpeed);
			}
		} else {
			if (uiOffset > 0) {
				uiOffset = Math.max(0, uiOffset - uiOffsetSpeed);
			}
		}
		double location = -selected * 360.0 / OPTIONS.length;
		if (optionsRot < location) {
			optionsRot = Math.min(optionsRot + optionsRotSpeed, location);
		}
		if (optionsRot > location) {
			optionsRot = Math.max(optionsRot - optionsRotSpeed, location);
		}
		if (tileRot < 360) {
			tileRot += rotSpeed;
		} else {
			tileRot = 0;
		}
		if (optionGlow < 360) {
			optionGlow += optionGlowSpeed;
		} else {
			optionGlow = 0;
		}
		for (int i = 0; i < effects.size(); i++) {
			effects.get(i).tick();
			if (effects.get(i).remove) {
				effects.remove(i);
				i--;
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

		for (int i = 0; i < menus.size(); i++) {
			menus.get(i).tick();
			if (menus.get(i).open == false && menus.get(i).height <= 16) {
				menus.remove(i);
				i--;
			}
		}
		if (messages.size() > 0) {
			messages.get(0).tick();
			if (messages.get(0).remove) {
				messages.remove(0);
			}
		}
		if (menus.size() == 0) {
			cursor.targetPosition = Vector3f.add(optionsPosition, -optionsRadius - (OPTIONS[selected % OPTIONS.length].length() - 1) * 3 - 18, -2, 0);
		}
		cursor.tick();

		camX = tiles.length * 4 + Math.cos(Math.toRadians(tileRot)) * tileRadius;
		camY = tiles[0].length * 4 + Math.sin(Math.toRadians(tileRot)) * tileRadius;
		rcamX = Math.floorDiv((int) camX, 8) - 15;
		rcamY = Math.floorDiv((int) camY, 8) - 17;
		Startup.camera.position.setX((float) camX);
		Startup.camera.position.setZ((float) camY);
		Startup.camera.targetPosition.setY(42 + (tiles[(int) (camX / 8)][(int) (camY / 8)].getPosition().getY() / 2));
	}

	@Override
	public void render() {
		for (Menu m : menus) {
			m.render();
		}
		if (messages.size() > 0) {
			messages.get(0).render();
		}
		for (Effect e : effects) {
			e.render();
		}
		for (int x = 0; x < DungeonGenerator.xActiveSpace; x++) {
			for (int y = 0; y < DungeonGenerator.yActiveSpace; y++) {
				if (rcamX + x < tiles.length && rcamY + y < tiles[0].length && rcamX + x > -1 && rcamY + y > -1) {
					tiles[rcamX + x][rcamY + y].render(false);
				} else {
					BatchSorter.add(filler.getModel(), filler.getTexture(), filler.getShader(), filler.getMaterial().toString(),
							Vector3f.add(filler.getOffset(), (rcamX + x) * 16,
									(float) Math.round(dungeon.noise.getValue(((float) (rcamX + x) / (tiles.length + DungeonGenerator.xActiveSpace)), ((float) (rcamY + y) / (tiles[0].length + DungeonGenerator.yActiveSpace))) * 10) * 4f,
									(rcamY + y) * 16),
							Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, true, false);
				}
			}
		}
		if (uiOffset < uiOffsetLimit) {
			TextRenderer.render(title, Vector3f.add(titlePosition, 0, uiOffset, 0), Vector3f.EMPTY, Vector3f.SCALE_TRIPLE, new Vector4f(0.39216f, 0.44314f, 0.53333f, 1), true);
			for (int x = 0; x < 3; x++) {
				for (int y = 0; y < 3; y++) {
					TextRenderer.render(title, Vector3f.add(titlePosition, -1 + x, 1 + uiOffset - y, -1), Vector3f.EMPTY, Vector3f.SCALE_TRIPLE, new Vector4f(0.15686f, 0.15686f, 0.15686f, 1), true);
				}
			}
			double distance = 360.0 / OPTIONS.length;
			for (int i = 0; i < OPTIONS.length; i++) {
				Vector4f mult = new Vector4f(1, 1, 1, 1);
				if (selected % OPTIONS.length == i) {
					float glow = ((float) Math.sin(Math.toRadians(optionGlow)) + 1) / 3;
					mult.add(new Vector4f(glow, glow, glow, 0));
				}
				TextRenderer.render(OPTIONS[i], Vector3f.add(optionsPosition, (float) -Math.cos(Math.toRadians(optionsRot + distance * i)) * optionsRadius - (OPTIONS[i].length() - 1) * 3 + uiOffset * 4,
						(float) -Math.sin(Math.toRadians(optionsRot + distance * i)) * optionsRadius, 0), Vector3f.EMPTY, Vector3f.SCALE_FULL, Vector4f.mul(new Vector4f(0.39216f, 0.44314f, 0.53333f, 1), mult), true);
				for (int x = 0; x < 3; x++) {
					for (int y = 0; y < 3; y++) {
						TextRenderer.render(OPTIONS[i], Vector3f.add(optionsPosition, (float) -Math.cos(Math.toRadians(optionsRot + distance * i)) * optionsRadius + x - 1 - (OPTIONS[i].length() - 1) * 3 + uiOffset * 4,
								(float) -Math.sin(Math.toRadians(optionsRot + distance * i)) * optionsRadius - y + 1, -1), Vector3f.EMPTY, Vector3f.SCALE_FULL, new Vector4f(0.15686f, 0.15686f, 0.15686f, 1), true);
					}
				}
			}
		}

		cursor.render();

	}

	@Override
	public void onEnter() {
		Inventory.loadHeaderData();
		String[] tagArray = DungeonGenerator.getPalletTags();
		String palletTag = tagArray[new Random().nextInt(tagArray.length)];

		dungeon = new DungeonGenerator(palletTag, 0).startWithoutUnits();
		tiles = dungeon.tiles;
		filler = Tile.getTile(new String[] { dungeon.getPalletTag(), Tile.TAG_FILLER }, new Vector3f(-1, -1, -1));

		tileRadius = Math.min(tiles.length, tiles[0].length) * 2;
		rotSpeed = 2 * Math.PI * tileRadius / 8640;
		camX = tiles.length * 4 + Math.cos(Math.toRadians(tileRot)) * tileRadius;
		camY = tiles[0].length * 4 + Math.sin(Math.toRadians(tileRot)) * tileRadius;

		if (menus.size() == 0) {
			cursor = new Cursor(Vector3f.add(optionsPosition, -optionsRadius, 0, 0));
		} else {
			cursor = new Cursor(Vector3f.add(menus.get(0).position, 0, 0, 0));
		}
		cursor.staticView = true;

		Startup.camera.position = new Vector3f(0, 0, 0);
		camX = tiles.length * 4 + Math.cos(Math.toRadians(tileRot)) * tileRadius;
		camY = tiles[0].length * 4 + Math.sin(Math.toRadians(tileRot)) * tileRadius;
		rcamX = Math.floorDiv((int) camX, 8) - 15;
		rcamY = Math.floorDiv((int) camY, 8) - 17;
		Startup.camera.position.setX((float) camX);
		Startup.camera.position.setZ((float) camY);
		Startup.camera.position.setY(42 + (tiles[(int) (camX / 8)][(int) (camY / 8)].getPosition().getY() / 2));
		Startup.camera.speedY = 0.005f;
		Startup.staticView.setPosition(new Vector3f(0, 0, 0));
		Startup.camera.setRotation(new Vector3f(-60, 0, 0));
		Startup.shadowCamera.setPosition(new Vector3f(Startup.camera.position.getX(), 64 + tiles[(int) (camX / 16)][(int) (camX / 16)].getPosition().getY() / 2, Startup.camera.position.getZ()));
		Startup.shadowCamera.setRotation(new Vector3f(-60, 0, 0));
		Startup.shadowCamera.speedY = 1f;

		setEffects(dungeon.getEffectTags(), dungeon.getEffectVars());

		float[] screen = dungeon.getScreenColor();
		Startup.screenColor.set(screen[0], screen[0], screen[0], Startup.screenColor.getW());
		Startup.screenColorTarget.set(screen[0], screen[0], screen[0], Startup.screenColorTarget.getW());

		Startup.screenColor.setW(1);
		Startup.screenColorTarget.setW(0);
	}

	public void onExit() {
		StateManager.initState(new StateTitle());
		for (Effect e : effects) {
			e.cleanUp();
		}
	}

	public static void swapToMenu() {
		StateTitle state = new StateTitle();
		state.hideMenu = true;
		state.uiOffset = state.uiOffsetLimit;
		StateManager.initState(state);
		StateManager.swapState(STATE_ID);
	}

	private void setEffects(String[] tags, double[][] vars) {
		Startup.enableHaze = false;
		for (int i = 0; i < tags.length; i++) {
			switch (tags[i]) {

			case DungeonGenerator.TAG_LIGHT_MAIN:
				overheadLight = new Light(Startup.shadowCamera.position.copy(), new Vector3f((float) vars[i][0], (float) vars[i][1], (float) vars[i][2]), (float) vars[i][3], (float) vars[i][4], (float) vars[i][5], Vector3f.DOWN);
				lights.add(overheadLight);
				break;

			case DungeonGenerator.TAG_EFFECT_SNOW:
				effects.add(new EffectDungeonSnow());
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

			case DungeonGenerator.TAG_EFFECT_LAVA:
				effects.add(new EffectDungeonLavaSFX());
				break;

			}
		}
	}

	@SuppressWarnings("incomplete-switch")
	public void onKeyPress(InputMapping key) {
		switch (key) {

		case UP:
			if (selected > 0 && canOperateMainUI()) {
				selected--;
				AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
			}
			if (canOperateSubUI()) {
				menus.get(0).move(-1);
			}
			break;

		case DOWN:
			if (selected < OPTIONS.length - 1 && canOperateMainUI()) {
				selected++;
				AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
			}
			if (canOperateSubUI()) {
				menus.get(0).move(1);
			}
			break;

		case CONFIRM:
			if (canOperateSubUI()) {
				menus.get(0).action("", null);
			} else if (messages.size() > 0) {
				messages.get(0).close();
			}
			if (canOperateMainUI()) {
				switch (OPTIONS[selected]) {

				case "start":
					hideMenu = true;
					menus.add(new MenuTitle(new Vector3f(0, 0, -80)));
					break;

				case "options":
					hideMenu = true;
					menus.add(new MenuOptions(new Vector3f(0, 0, -80)));
					break;
					
				case "quit":
					GLFW.glfwSetWindowShouldClose(Startup.thread.window, true);
					break;

				}
			}
			break;

		case BACK:
			if (canOperateSubUI()) {
				menus.get(0).action("return", null);
			} else if (messages.size() > 0) {
				messages.get(0).close();
			}
			break;

		}

	}

	@SuppressWarnings("incomplete-switch")
	public void onKeyRepeat(InputMapping key) {
		switch (key) {

		case LEFT:
			if (canOperateSubUI()) {
				menus.get(0).action("left", null);
			}
			break;

		case RIGHT:
			if (canOperateSubUI()) {
				menus.get(0).action("right", null);
			}
			break;

		}
	}

	private boolean canOperateMainUI() {
		return Startup.screenColor.getW() == 0 && !hideMenu && menus.size() == 0;
	}

	private boolean canOperateSubUI() {
		return Startup.screenColor.getW() == 0 && menus.size() > 0 && messages.size() == 0;
	}

	public static StateTitle getCurrentContext() {
		return (StateTitle) StateManager.currentState;
	}

}
