package io.itch.deltabreaker.state;

import java.util.ArrayList;
import java.util.Random;

import io.itch.deltabreaker.builder.dungeon.DungeonGenerator;
import io.itch.deltabreaker.core.InputMapping;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.effect.Effect;
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
import io.itch.deltabreaker.multiplayer.client.MatchPreviewThread;
import io.itch.deltabreaker.multiprocessing.TaskThread;
import io.itch.deltabreaker.multiprocessing.WorkerTask;
import io.itch.deltabreaker.object.Cursor;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.tile.Tile;
import io.itch.deltabreaker.ui.RoomInfo;
import io.itch.deltabreaker.ui.menu.MenuMatch;

public class StateMatchLobby extends State {

	public static final String STATE_ID = "state.match.lobby";

	private Tile filler;
	private DungeonGenerator dungeon;
	private Light overheadLight;

	private double tileRot = 0;
	private int tileRadius;
	private double rotSpeed;

	private boolean matchReady = false;
	private String map;
	private int floor;
	private long seed;
	private boolean startingPlayer;
	private Unit[] enemies;

	public MatchPreviewThread thread;

	public RoomInfo details;

	private WorkerTask task = new WorkerTask() {
		@Override
		public void tick() {
			// Moves the shadow camera along with the normal camera
			Startup.shadowCamera.targetPosition.set(Startup.camera.position.getX(), 80 + tiles[(int) (camX / 16)][(int) (camX / 16)].getPosition().getY() / 2, Startup.camera.position.getZ());
			overheadLight.position.set(Startup.shadowCamera.position.getX(), Startup.shadowCamera.position.getY() + 48, Startup.shadowCamera.position.getZ());

			// Updates each tile in the camera range
			for (int x = 0; x < DungeonGenerator.xActiveSpace; x++) {
				for (int y = 0; y < DungeonGenerator.yActiveSpace; y++) {
					if (rcamX + x < tiles.length && rcamY + y < tiles[0].length && rcamX + x > -1 && rcamY + y > -1) {
						tiles[x + rcamX][y + rcamY].tick();
					}
				}
			}

			if (tileRot < 360) {
				tileRot += rotSpeed;
			} else {
				tileRot = 0;
			}
		}
	};

	public StateMatchLobby() {
		super(STATE_ID);
	}

	public void tick() {
		cursor.tick();
		if (menus.size() > 0) {
			menus.get(0).tick();
			if (!menus.get(0).open && menus.get(0).height <= 16) {
				menus.remove(0);
			}
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

		camX = tiles.length * 4 + Math.cos(Math.toRadians(tileRot)) * tileRadius;
		camY = tiles[0].length * 4 + Math.sin(Math.toRadians(tileRot)) * tileRadius;
		rcamX = Math.floorDiv((int) camX, 8) - 15;
		rcamY = Math.floorDiv((int) camY, 8) - 17;
		Startup.camera.position.setX((float) camX);
		Startup.camera.position.setZ((float) camY);
		Startup.camera.targetPosition.setY(42 + (tiles[(int) (camX / 8)][(int) (camY / 8)].getPosition().getY() / 2));

		if (matchReady && Math.abs(Startup.screenColor.getW() - 1) < 0.00001) {
			StateDungeon.startMultiplayerDungeon(map, floor, seed, startingPlayer, enemies, thread.socket, thread.in, thread.out, thread.name, thread.opponentName);
		}
	}

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
							Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, true, false);
				}
			}
		}
		for (Effect e : effects) {
			e.render();
		}
		if (!hideCursor) {
			cursor.render();
		}
		if (menus.size() > 0) {
			menus.get(0).render();
		}
		if (details != null) {			
			details.render();
		}
		if(thread != null && thread.waiting) {
			//System.out.println(Startup.staticView.position.getX());
			TextRenderer.render(thread.name, new Vector3f(Startup.staticView.position.getX() * 2 - (thread.name.length() + 2) * 6, -60, -80), Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_GREEN, true);
			TextRenderer.render("vs", new Vector3f(Startup.staticView.position.getX() * 2 - 5.5f, -60, -80), Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, true);
			String opponentText = (thread.opponentName.length() > 0) ? thread.opponentName : "none";
			TextRenderer.render(opponentText, new Vector3f(Startup.staticView.position.getX() * 2 + 12, -60, -80), Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_RED, true);

			String icon = (thread.ready) ? "check" : "cross";
			BatchSorter.add(icon + ".dae", icon + ".png", "static_3d", Material.DEFAULT.toString(), new Vector3f(Startup.staticView.position.getX() * 2 - 16 - thread.name.length() * 3, -70, -80), Vector3f.EMPTY,
					Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, false, true);

			if (thread.opponentName.length() > 0) {
				icon = (thread.opponentReady) ? "check" : "cross";
				BatchSorter.add(icon + ".dae", icon + ".png", "static_3d", Material.DEFAULT.toString(), new Vector3f(Startup.staticView.position.getX() * 2 + 9 + (thread.opponentName.length()) * 3, -70, -80), Vector3f.EMPTY,
						Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, false, true);
			}
		}
	}

	public static void swapWithSetup() {
		StateMatchLobby state = new StateMatchLobby();
		StateManager.initState(state);
		StateManager.swapState(STATE_ID);
		state.menus.add(new MenuMatch(state, new Vector3f(0, 0, -80)));
	}

	public static void returnFromMatch() {
		StateMatchLobby state = new StateMatchLobby();
		StateManager.initState(state);
		StateManager.swapState(STATE_ID);
	}

	public void onEnter() {
		Startup.screenColor.setW(1);
		Startup.screenColorTarget.setW(0);

		matchReady = false;
		cursor = new Cursor(new Vector3f(0, 0, -79));
		cursor.staticView = true;

		String[] tagArray = DungeonGenerator.getPalletTags();
		String palletTag = tagArray[new Random().nextInt(tagArray.length)];

		dungeon = new DungeonGenerator(palletTag, 0).startWithoutUnits();
		tiles = dungeon.tiles;
		filler = Tile.getTile(new String[] { dungeon.getPalletTag(), Tile.TAG_FILLER }, new Vector3f(-1, -1, -1));

		tileRadius = Math.min(tiles.length, tiles[0].length) * 2;
		rotSpeed = 2 * Math.PI * tileRadius / 8640;
		camX = tiles.length * 4 + Math.cos(Math.toRadians(tileRot)) * tileRadius;
		camY = tiles[0].length * 4 + Math.sin(Math.toRadians(tileRot)) * tileRadius;

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

		TaskThread.process(task);
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

	public void readyUp(String map, int floor, long seed, boolean startingPlayer, Unit[] enemies, String name, String opponent) {
		this.map = map;
		this.floor = floor;
		this.seed = seed;
		this.startingPlayer = startingPlayer;
		this.enemies = enemies;
		matchReady = true;
		Startup.screenColorTarget.setW(1);
	};

	@SuppressWarnings("incomplete-switch")
	public void onKeyPress(InputMapping key) {
		switch (key) {

		case CONFIRM:
			if (menus.size() > 0) {
				menus.get(0).action("", null);
			}
			break;

		case BACK:
			if (menus.size() > 0) {
				menus.get(0).action("back", null);
			}
			break;

		case LEFT:
			if (menus.size() > 0) {
				menus.get(0).action("left", null);
			}
			break;

		case RIGHT:
			if (menus.size() > 0) {
				menus.get(0).action("right", null);
			}
			break;

		case UP:
			if (menus.size() > 0) {
				menus.get(0).move(-1);
			}
			break;

		case DOWN:
			if (menus.size() > 0) {
				menus.get(0).move(1);
			}
			break;

		}
	}

	public void onExit() {
		task.finish();
		StateManager.initState(new StateMatchLobby());
		for (Effect e : effects) {
			e.cleanUp();
		}
	}

}
