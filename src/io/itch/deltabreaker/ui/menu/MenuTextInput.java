package io.itch.deltabreaker.ui.menu;

import java.awt.Dimension;

import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.state.StateManager;
import io.itch.deltabreaker.ui.UIBox;

public abstract class MenuTextInput extends Menu {

	private byte x = 0, y = 0;

	public static final String[][] INPUT = new String[][] { new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i" }, new String[] { "j", "k", "l", "m", "n", "o", "p", "q", "r" },
			new String[] { "s", "t", "u", "v", "w", "x", "y", "z", "." }, new String[] { "+", "-", "_", "&", "!", "@", "%", "ok", "del" } };

	public MenuTextInput(Vector3f position) {
		super(position, new String[] { "" }, getDimensions().width, getDimensions().height);
	}

	public void tick() {
		super.tick();
		width = getDimensions().width;
		openTo = getDimensions().height;
		StateManager.currentState.cursor.setLocation(Vector3f.add(position, x * 18 - 8, y * -12 - 22, 2));
	}

	public void render() {
		UIBox.render(position, width, height);
		for (int y = 0; y < INPUT.length; y++) {
			for (int x = 0; x < INPUT[y].length; x++) {
				if (height > y * 12 + 19) {
					TextRenderer.render(INPUT[y][x], Vector3f.add(position, x * 18 + 4, y * -12 - 19, 1), Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, true);
				}
			}
		}
		TextRenderer.render(options[0], Vector3f.add(position, 4, -3, 1), Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, true);
	}

	public void move(int dir) {
		if(dir == 1) {
			y = (byte) ((y + 1) % INPUT.length);
			AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
		} else {
			y = (byte) ((y + INPUT.length - 1) % INPUT.length);
			AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
		}
	}
	
	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			switch (command) {

			case "left":
				x = (byte) ((x + INPUT[y].length - 1) % INPUT[y].length);
				AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
				break;

			case "right":
				x = (byte) ((x + 1) % INPUT[y].length);
				AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
				break;

			case "back":
				if (options[0].length() == 0) {
					close();
					AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
				} else {
					options[0] = options[0].substring(0, options[0].length() - 1);
					AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
				}
				break;

			case "":
				switch (INPUT[y][x]) {

				default:
					options[0] += INPUT[y][x];
					AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
					break;

				case "ok":
					output(options[0]);
					close();
					AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
					break;

				case "del":
					if (options[0].length() > 0) {
						options[0] = options[0].substring(0, options[0].length() - 1);
						AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
					}
					break;

				}
				break;

			}
		} else {
			subMenu.get(0).action(command, unit);
		}
	}

	public abstract void output(String output);

	protected static Dimension getDimensions() {
		int longest = 0;
		int height = 10;
		for (String[] list : INPUT) {
			if (list.length > longest) {
				longest = list.length;
			}
		}
		return new Dimension(longest * 18 + 9, height + (INPUT.length + 3) * 8);
	}

}
