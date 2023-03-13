package io.itch.deltabreaker.ui.menu;

import org.lwjgl.glfw.GLFW;

import io.itch.deltabreaker.core.InputMapping;
import io.itch.deltabreaker.core.SettingsManager;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.state.StateManager;
import io.itch.deltabreaker.state.StateTitle;

public class MenuOptions extends Menu {

	public MenuOptions(Vector3f position) {
		super(position, new String[] { "Graphics", "Audio" });
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() != 0) {
			subMenu.get(0).action(command, unit);
			return;
		}
		if (command.equals("return")) {
			close();
			AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
			return;
		}
		if (command.equals("left") || command.equals("right")) {
			return;
		}

		switch (options[selected]) {

		case "Graphics":
			subMenu.add(new MenuOptionsVideo(Vector3f.add(position, width + 5, 0, 0)));
			break;

		case "Audio":
			subMenu.add(new MenuOptionsAudio(Vector3f.add(position, width + 5, 0, 0)));
			break;

		case "Controls":
			subMenu.add(new MenuOptionsControls(Vector3f.add(position, width + 5, 0, 0)));
			break;

		}
	}

	public void close() {
		super.close();

		if (StateManager.currentState == StateManager.getState(StateTitle.STATE_ID)) {
			StateTitle.getCurrentContext().hideMenu = false;
			Startup.staticView.setTargetPosition(0, 0, 0);
		}

		SettingsManager.saveSettingsFile(Startup.CFG_PATH, Startup.CFG_FILE);
	}

}

class MenuOptionsVideo extends Menu {

	public MenuOptionsVideo(Vector3f position) {
		super(position, getStrings());
	}

	public static String[] getStrings() {
		return new String[] { (SettingsManager.fullscreen) ? "fullscreen" : "windowed", "scale - " + (int) (SettingsManager.resScaling * 100) + "%", "vignette - " + (int) Math.round((SettingsManager.vignetteRadius - 0.75) / 0.05),
				"FXAA - " + ((SettingsManager.enableFXAA) ? "on" : "off"), "Bloom - " + ((SettingsManager.enableBloom) ? "on" : "off"), "Bloom Fidelity - " + (SettingsManager.bloomFidelity - 1),
				"Fancy Water - " + ((SettingsManager.enableFancyWater) ? "on" : "off"), "Fancy Rain - " + ((SettingsManager.enableFancyRain) ? "on" : "off") };
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() != 0) {
			subMenu.get(0).action(command, unit);
			return;
		}
		if (command.equals("return")) {
			close();
			AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
			return;
		}

		if (command.equals("left") || command.equals("right")) {
			switch (selected) {

			case 1:
				if (command.equals("left") && SettingsManager.resScaling > 0.25) {
					SettingsManager.resScaling = (float) (Math.round(Math.max(SettingsManager.resScaling - 0.05, 0.25) * 100.0) / 100.0);
					update();
					AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
				}
				if (command.equals("right") && SettingsManager.resScaling < 1) {
					SettingsManager.resScaling = (float) (Math.round(Math.min(SettingsManager.resScaling + 0.05, 1) * 100.0) / 100.0);
					update();
					AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
				}
				break;

			case 2:
				if (command.equals("left") && SettingsManager.vignetteRadius > 0.75) {
					SettingsManager.vignetteRadius = (float) Math.max(SettingsManager.vignetteRadius - 0.05, 0.75);
					update();
					AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
				}
				if (command.equals("right") && SettingsManager.vignetteRadius < 1.75) {
					SettingsManager.vignetteRadius = (float) Math.min(SettingsManager.vignetteRadius + 0.05, 1.75);
					update();
					AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
				}
				break;

			case 5:
				if (command.equals("left") && SettingsManager.bloomFidelity > 2) {
					SettingsManager.bloomFidelity--;
					update();
					AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
				}
				if (command.equals("right") && SettingsManager.bloomFidelity < 10) {
					SettingsManager.bloomFidelity++;
					update();
					AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
				}
				break;

			}
			return;
		}

		switch (selected) {

		case 0:
			SettingsManager.setFullscreen(!SettingsManager.fullscreen);
			update();
			AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
			break;

		case 3:
			SettingsManager.enableFXAA = !SettingsManager.enableFXAA;
			update();
			AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
			break;

		case 4:
			SettingsManager.enableBloom = !SettingsManager.enableBloom;
			update();
			AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
			break;

		case 6:
			SettingsManager.enableFancyWater = !SettingsManager.enableFancyWater;
			update();
			AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
			break;

		case 7:
			SettingsManager.enableFancyRain = !SettingsManager.enableFancyRain;
			update();
			AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
			break;

		}
	}

	public void update() {
		options = getStrings();
		super.update();
	}

}

class MenuOptionsAudio extends Menu {

	public MenuOptionsAudio(Vector3f position) {
		super(position, getStrings());
	}

	public static String[] getStrings() {
		return new String[] { "Music - " + (int) Math.round(AudioManager.defaultMusicGain / 0.1), "Main SFX - " + (int) Math.round(AudioManager.defaultMainSFXGain / 0.1),
				"Sub SFX - " + (int) Math.round(AudioManager.defaultSubSFXGain / 0.1), "Battle SFX - " + (int) Math.round(AudioManager.defaultBattleSFXGain / 0.1) };
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() != 0) {
			subMenu.get(0).action(command, unit);
			return;
		}
		if (command.equals("return")) {
			close();
			AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
			return;
		}

		if (command.equals("left") || command.equals("right")) {
			switch (selected) {

			case 0:
				if (command.equals("left") && AudioManager.defaultMusicGain > 0) {
					AudioManager.defaultMusicGain = (float) (Math.round(Math.max(AudioManager.defaultMusicGain - 0.1, 0) * 100.0) / 100.0);
					update();
					AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
				}
				if (command.equals("right") && AudioManager.defaultMusicGain < 1) {
					AudioManager.defaultMusicGain = (float) (Math.round(Math.min(AudioManager.defaultMusicGain + 0.1, 1) * 100.0) / 100.0);
					update();
					AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
				}
				break;

			case 1:
				if (command.equals("left") && AudioManager.defaultMainSFXGain > 0) {
					AudioManager.defaultMainSFXGain = (float) (Math.round(Math.max(AudioManager.defaultMainSFXGain - 0.1, 0) * 100.0) / 100.0);
					update();
					AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
				}
				if (command.equals("right") && AudioManager.defaultMainSFXGain < 1) {
					AudioManager.defaultMainSFXGain = (float) (Math.round(Math.min(AudioManager.defaultMainSFXGain + 0.1, 1) * 100.0) / 100.0);
					update();
					AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
				}
				break;

			case 2:
				if (command.equals("left") && AudioManager.defaultSubSFXGain > 0) {
					AudioManager.defaultSubSFXGain = (float) (Math.round(Math.max(AudioManager.defaultSubSFXGain - 0.1, 0) * 100.0) / 100.0);
					update();
					AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
				}
				if (command.equals("right") && AudioManager.defaultSubSFXGain < 1) {
					AudioManager.defaultSubSFXGain = (float) (Math.round(Math.min(AudioManager.defaultSubSFXGain + 0.1, 1) * 100.0) / 100.0);
					update();
					AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
				}
				break;

			case 3:
				if (command.equals("left") && AudioManager.defaultBattleSFXGain > 0) {
					AudioManager.defaultBattleSFXGain = (float) (Math.round(Math.max(AudioManager.defaultBattleSFXGain - 0.1, 0) * 100.0) / 100.0);
					update();
					AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
				}
				if (command.equals("right") && AudioManager.defaultBattleSFXGain < 1) {
					AudioManager.defaultBattleSFXGain = (float) (Math.round(Math.min(AudioManager.defaultBattleSFXGain + 0.1, 1) * 100.0) / 100.0);
					update();
					AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
				}
				break;

			}
			return;
		}
	}

	public void update() {
		options = getStrings();
		super.update();
	}

}

class MenuOptionsControls extends Menu {

	private boolean record = false;
	private static InputMapping[] mappings = InputMapping.values();
	
	public MenuOptionsControls(Vector3f position) {
		super(position, getStrings());
	}

	public static String[] getStrings() {
		String[] text = new String[mappings.length - 7];
		for(int i = 0; i < text.length; i++) {
			text[i] = mappings[i].toString() + " - " + GLFW.glfwGetKeyName(mappings[i].keyCode, GLFW.glfwGetKeyScancode(mappings[i].keyCode));
		}
		return text;
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() != 0) {
			subMenu.get(0).action(command, unit);
			return;
		}
		if (command.equals("return")) {
			close();
			AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
			return;
		}

		if(record) {
			
		}
		record = !record;
	}

	public void update() {
		options = getStrings();
		super.update();
	}

}