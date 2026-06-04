package io.itch.deltabreaker.ui.menu;

import org.lwjgl.glfw.GLFW;

import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.event.Event;
import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Cursor;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.state.StateHub;
import io.itch.deltabreaker.state.StateManager;
import io.itch.deltabreaker.state.StateTitle;

public class MenuHubTitle extends Menu {

	public static final String[] OPTIONS = { "new", "load", "options", "quit" };

	private Vector3f optionsPosition = new Vector3f(100, -23, -90);
	private float uiOffset = 0;
	private float uiOffsetLimit = 20;
	private float uiOffsetSpeed = 0.75f;
	public boolean hideMenu = false;
	private Vector3f titlePosition;

	private int selected = 0;
	private double optionsRot = 0;
	private int optionsRadius = 35;
	private double optionsRotSpeed = 6;
	private double optionGlow = 0;
	private double optionGlowSpeed = 1;

	public MenuHubTitle() {
		super();
		titlePosition = new Vector3f(-(StateTitle.title.length() - 1) * 3f, 20, -45);

		StateManager.currentState.cursor = new Cursor(Vector3f.add(optionsPosition, -optionsRadius, 0, 0));
		StateManager.currentState.cursor.staticView = true;
		height = 100;
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("back")) {
				switch (OPTIONS[selected]) {

				case "new":
					open = false;
					StateHub.getCurrentContext().events.add(new Event(StateHub.getCurrentContext().eventList.get("on_new_game.json")));
					break;

				case "load":
					
					break;

				case "options":
					hideMenu = true;
					subMenu.add(new MenuOptions(new Vector3f(0, 0, -80)));
					break;

				case "quit":
					GLFW.glfwSetWindowShouldClose(Startup.thread.window, true);
					break;

				}
			}
		} else {
			subMenu.get(0).action(command, unit);
		}
	}

	@Override
	public void move(int amt) {
		if (subMenu.size() == 0) {
			selected += amt;
			if (selected < 0) {
				selected = 0;
				return;
			}
			if (selected > OPTIONS.length - 1) {
				selected = OPTIONS.length - 1;
				return;
			}
			AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
		} else {
			subMenu.get(0).move(amt);
		}
	}

	@Override
	public void tick() {
		if (hideMenu || !open) {
			if (uiOffset < uiOffsetLimit) {
				uiOffset = Math.min(uiOffsetLimit, uiOffset + uiOffsetSpeed);
			} else if (!open) {
				height = 0;
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

		if (optionGlow < 360) {
			optionGlow += optionGlowSpeed;
		} else {
			optionGlow = 0;
		}

		StateManager.currentState.cursor.targetPosition = Vector3f.add(optionsPosition, -optionsRadius - (OPTIONS[selected % OPTIONS.length].length() - 1) * 3 - 18, -2, 0);
	
		if (subMenu.size() > 0) {
			subMenu.get(0).tick();
			if (!subMenu.get(0).open && subMenu.get(0).height <= 16) {
				subMenu.remove(0);
			}
		}
	}

	@Override
	public void render() {
		if (uiOffset < uiOffsetLimit) {
			TextRenderer.render(StateTitle.title, Vector3f.add(titlePosition, 0, uiOffset, 0), Vector3f.EMPTY, Vector3f.SCALE_TRIPLE, new Vector4f(0.39216f, 0.44314f, 0.53333f, 1), true);
			for (int x = 0; x < 3; x++) {
				for (int y = 0; y < 3; y++) {
					TextRenderer.render(StateTitle.title, Vector3f.add(titlePosition, -1 + x, 1 + uiOffset - y, -1), Vector3f.EMPTY, Vector3f.SCALE_TRIPLE, new Vector4f(0.15686f, 0.15686f, 0.15686f, 1), true);
				}
			}
			double distance = 360.0 / OPTIONS.length;
			for (int i = 0; i < OPTIONS.length; i++) {
				Vector4f mult = new Vector4f(1, 1, 1, 1);
				if (selected % OPTIONS.length == i) {
					float glow = ((float) Math.sin(Math.toRadians(optionGlow)) + 1) / 3;
					mult.add(new Vector4f(glow, glow, glow, 0));
				}
				if (i * distance < selected * distance + 180 && i * distance > selected * distance - 180) {
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
		}
		
		if(subMenu.size() > 0) {
			subMenu.get(0).render();
		}
	}

}
