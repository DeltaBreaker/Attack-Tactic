package io.itch.deltabreaker.ui.menu;

import java.awt.Dimension;
import java.util.ArrayList;

import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.state.StateManager;
import io.itch.deltabreaker.ui.UIBox;

public abstract class Menu extends UIBox {

	public String[] options;
	public int selected = 0;

	public int openTo = 0;
	public boolean open = true;

	public Menu parent;
	public ArrayList<Menu> subMenu = new ArrayList<Menu>();

	public Menu(Vector3f position, String[] options) {
		super(position, getDimensions(options).width, 8);
		this.options = options;
		openTo = getDimensions(options).height;
		AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
	}

	public Menu(Vector3f position, String[] options, int width, int height) {
		super(position, width, 8);
		this.options = options;
		openTo = height;
		AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
	}

	protected static Dimension getDimensions(String[] text) {
		String longest = "";
		int height = 10;
		int times = 0;
		for (String s : text) {
			if (s.length() > longest.length()) {
				longest = s;
			}
			if (times < 5) {
				height += 8;
				times++;
			}
		}
		return new Dimension(longest.length() * 6 + 9, height);
	}

	public void tick() {
		if (selected > options.length - 1) {
			selected = options.length - 1;
		}
		if (options.length == 0) {
			open = false;
		}
		if (selected > options.length - 1) {
			selected = options.length - 1;
		}
		if (open) {
			if (height < openTo) {
				height = Math.min(height + 3, openTo);
			}
			if (height > openTo) {
				height = Math.max(height - 3, openTo);
			}
		} else {
			if (height > 0) {
				height = Math.max(height - 3, 16);
			}
		}
		if (subMenu.size() > 0) {
			subMenu.get(0).tick();
			if (!subMenu.get(0).open && subMenu.get(0).height <= 16) {
				subMenu.remove(0);
			}
		}
		if (selected > options.length - 1) {
			selected = options.length - 1;
		}
		if (selected < 0) {
			selected = 0;
		}
		if (subMenu.size() == 0 && open && StateManager.currentState.status.size() == 0 && StateManager.currentState.itemInfo.size() == 0) {
			Startup.staticView.targetPosition = new Vector3f(position.getX() / 2 - 1 + width / 4, position.getY() / 2 - openTo / 4, Startup.staticView.position.getZ());
			StateManager.currentState.cursor.setLocation(new Vector3f(position.getX() - 10, position.getY() - 9 - 8 * Math.min(4, selected), position.getZ() + 4));
		}
	}

	public void update() {
		width = getDimensions(options).width;
		openTo = getDimensions(options).height;
	}

	public void move(int amt) {
		if (subMenu.size() == 0) {
			AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
			selected += amt;
			if (selected < 0) {
				selected = options.length - 1;
			}
			if (selected > options.length - 1) {
				selected = 0;
			}
		} else {
			subMenu.get(0).move(amt);
		}
	}

	// Used for simplicity. May also need to add extra steps later.
	public void close() {
		open = false;
	}

	// Closes all open menus in a chain
	public void closeAll() {
		open = false;
		if (parent != null) {
			parent.closeAll();
		}
	}

	// Used when creating sub-menus instead of using an entirely new constructor
	// just to set the parent variable
	public Menu setParent(Menu parent) {
		this.parent = parent;
		return this;
	}

	public void render() {
		super.render();
		for (int i = 0; i < Math.min(5, options.length); i++) {
			if (i * 8 + 12 < height) {
				TextRenderer.render(options[i + Math.max(0, selected - 4)], Vector3f.add(position, 4, (-i - Math.max(0, selected - options.length)) * 8 - 6, 1), new Vector3f(0, 0, 0), scale, new Vector4f(1, 1, 1, 1), true);
			}
		}
		if (subMenu.size() > 0) {
			subMenu.get(0).render();
		}
	}

	public abstract void action(String command, Unit unit);

}