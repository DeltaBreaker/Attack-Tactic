package io.itch.deltabreaker.state;

import io.itch.deltabreaker.core.InputMapping;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.multiplayer.client.MatchPreviewThread;
import io.itch.deltabreaker.object.Cursor;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.ui.RoomInfo;
import io.itch.deltabreaker.ui.menu.MenuMatch;

public class StateMatchLobby extends State {

	public static final String STATE_ID = "state.match.lobby";

	private boolean matchReady = false;
	private String map;
	private int floor;
	private long seed;
	private boolean startingPlayer;
	private Unit[] enemies;
	private String name, opponent;
	
	public MatchPreviewThread thread;

	private int timer = 0;
	private int time = 72;
	private int dots = 0;

	public RoomInfo details;
	
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
		if (timer < time) {
			timer++;
		} else {
			timer = 0;
			if (dots < 3) {
				dots++;
			} else {
				dots = 0;
			}
		}
		if (matchReady) {
			StateDungeon.startMultiplayerDungeon(map, floor, seed, startingPlayer, enemies, thread.socket, thread.in, thread.out, name, opponent);
		}
	}

	public void render() {
		if (!hideCursor) {
			cursor.render();
		}
		if (menus.size() > 0) {
			menus.get(0).render();
		}
		if(details != null) {
			details.render();
			String dot = "";
			for (int i = 0; i < dots; i++) {
				dot += ".";
			}
			TextRenderer.render("waiting" + dot, Vector3f.add(Vector3f.mul(Startup.staticView.position, 2), new Vector3f(-("waiting" + dot).length() * 2.75f, -24, -45)), Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_SPLASH_MAIN, true);
		}
	}

	public void onEnter() {
		matchReady = false;
		cursor = new Cursor(new Vector3f(0, 0, 0));
		cursor.staticView = true;
		menus.add(new MenuMatch(this, new Vector3f(0, 0, -80)));
	}

	public void readyUp(String map, int floor, long seed, boolean startingPlayer, Unit[] enemies, String name, String opponent) {
		this.map = map;
		this.floor = floor;
		this.seed = seed;
		this.startingPlayer = startingPlayer;
		this.enemies = enemies;
		this.name = name;
		this.opponent = opponent;
		matchReady = true;
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

}
