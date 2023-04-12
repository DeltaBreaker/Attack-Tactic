package io.itch.deltabreaker.state;

import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.multiplayer.client.MatchPreviewThread;
import io.itch.deltabreaker.object.Unit;

public class StateMatchLobby extends State {

	public static final String STATE_ID = "state.match.lobby";
	
	private boolean matchReady = false;
	private String map;
	private int floor;
	private long seed;
	private boolean startingPlayer;
	private Unit[] enemies;

	private MatchPreviewThread thread;
	
	private int timer = 0;
	private int time = 72;
	private int dots = 0;
	
	public StateMatchLobby() {
		super(STATE_ID);
	}

	public void tick() {
		if(timer < time) {
			timer++;
		} else {
			timer = 0;
			if(dots < 3) { 
				dots++;
			} else {
				dots = 0;
			}
		}
		if (matchReady) {
			StateDungeon.startMultiplayerDungeon(map, floor, seed, startingPlayer, enemies, thread.socket, thread.in, thread.out);
		}
	}

	public void render() {
		BatchSorter.add(StateSplash.splashIcon + ".dae", StateSplash.splashIcon + ".png", "static_3d", Material.DEFAULT.toString(), new Vector3f(0, 5, -35), Vector3f.EMPTY, Vector3f.SCALE_FULL, Vector4f.COLOR_BASE, false, true);
		String dot = "";
		for(int i = 0; i < dots; i++) {
			dot += ".";
		}
		TextRenderer.render("waiting" + dot, new Vector3f(-("waiting" + dot).length() * 2.75f, -24, -45), Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_SPLASH_MAIN, true);
	}
	
	public void onEnter() {
		matchReady = false;
		thread = new MatchPreviewThread(this, "localhost", 36676);
		new Thread(thread).start();
	}

	public void readyUp(String map, int floor, long seed, boolean startingPlayer, Unit[] enemies) {
		this.map = map;
		this.floor = floor;
		this.seed = seed;
		this.startingPlayer = startingPlayer;
		this.enemies = enemies;
		matchReady = true;
	};
	
}
