package io.itch.deltabreaker.state;

import java.awt.Point;
import java.util.ArrayList;

import io.itch.deltabreaker.core.InputMapping;
import io.itch.deltabreaker.effect.Effect;
import io.itch.deltabreaker.effect.EffectWaterSplash;
import io.itch.deltabreaker.graphics.Light;
import io.itch.deltabreaker.object.Cursor;
import io.itch.deltabreaker.object.tile.Tile;
import io.itch.deltabreaker.ui.ItemInfoCard;
import io.itch.deltabreaker.ui.Message;
import io.itch.deltabreaker.ui.StatusCard;
import io.itch.deltabreaker.ui.TextBox;
import io.itch.deltabreaker.ui.menu.Menu;

public class State {

	public final String STATE_ID;

	// These are used so that each state has its own version of this data to work
	// with or for other classes to access a generalized variable
	public Cursor cursor;
	public Point cursorPos = new Point(0, 0);
	public Tile[][] tiles = new Tile[0][0];
	public ArrayList<Light> lights = new ArrayList<>();
	public ArrayList<Effect> effects = new ArrayList<>();
	public ArrayList<Message> messages = new ArrayList<>();
	public ArrayList<ItemInfoCard> itemInfo = new ArrayList<>();
	public ArrayList<StatusCard> status = new ArrayList<>();
	public ArrayList<Menu> menus = new ArrayList<>();
	public ArrayList<TextBox> text = new ArrayList<>();
	public double camX = 0;
	public double camY = 0;
	public int rcamX = 0;
	public int rcamY = 0;
	public double tcamX = 0;
	public double tcamY = 0;
	public float shading = 0;
	public boolean controlLock = false;
	public boolean menuLock = false;
	public boolean hideCursor = false;

	public State(String stateID) {
		STATE_ID = stateID;
	}

	public void tick() {

	};

	public void render() {

	};

	public void onEnter() {

	};

	public void onExit() {

	};

	public void onKeyPress(InputMapping key) {

	};

	public void onKeyRelease(InputMapping key) {

	};

	public void onKeyRepeat(InputMapping key) {

	};

	public void onCreate() {

	}

	public void onDestroy() {

	};

	public void clearSelectedTiles() {
		for (int x = 0; x < tiles.length; x++) {
			for (int y = 0; y < tiles[0].length; y++) {
				tiles[x][y].status = 0;
				tiles[x][y].range = 0;
			}
		}
		EffectWaterSplash.playSfx = true;
	}

}
