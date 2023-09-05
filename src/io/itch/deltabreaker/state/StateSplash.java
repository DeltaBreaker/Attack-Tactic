package io.itch.deltabreaker.state;

import io.itch.deltabreaker.core.InputMapping;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.effect.Effect;
import io.itch.deltabreaker.effect.EffectPoofMenu;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;

public class StateSplash extends State {

	public static final String STATE_ID = "splash";

	public static String splashIcon;

	public boolean open = true;
	public int waitTimer = 0;
	public final int waitTime = 200;
	public int charsRevealed = 0;
	public int charTime = 16;
	public int charTimer = -100;
	public static String text;
	public float fadeSpeed = 0.005f;

	public StateSplash() {
		super(STATE_ID);
	}

	public void tick() {
		for (int i = 0; i < effects.size(); i++) {
			effects.get(i).tick();
			if(effects.get(i).remove) {
				effects.remove(i);
				i--;
			}
		}
		if (open) {
			if (Startup.screenColor.getW() == 0) {
				if (charTimer == 0 && charsRevealed == 0) {
					effects.add(new EffectPoofMenu(new Vector3f(0, 2, -20)));
				}
				if (charTimer < charTime) {
					charTimer++;
				} else if (charsRevealed < text.length()) {
					charTimer = 0;
					charsRevealed++;
					AudioManager.getSound("text.ogg").play(AudioManager.defaultMainSFXGain, false);
				} else if (waitTimer < waitTime) {
					waitTimer++;
				} else {
					open = false;
				}
			}
		} else {
			if (Startup.screenColor.getW() != 1) {
				Startup.screenColorTarget.setW(1);
				Startup.transitionSpeed = fadeSpeed;
			} else {
				StateManager.swapState(StateTitle.STATE_ID);
			}
		}
	}

	public void render() {
		if (charsRevealed > 0) {
			BatchSorter.add(splashIcon + ".dae", splashIcon + ".png", "static_3d", Material.DEFAULT.toString(), new Vector3f(0, 3, -40), Vector3f.EMPTY, Vector3f.SCALE_FULL, Vector4f.COLOR_BASE, false, true);
		}
		Vector3f rotation = new Vector3f(0, 0, 0);
		Vector3f scale = new Vector3f(1, 1, 2);
		TextRenderer.render(text.substring(0, charsRevealed), new Vector3f(-text.length() * 2.75f, -26, -45), rotation, scale, Vector4f.COLOR_SPLASH_MAIN, true);
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				TextRenderer.render(text.substring(0, charsRevealed), new Vector3f(x - 1 - text.length() * 2.75f, -25 - y, -45), rotation, scale, Vector4f.COLOR_SPLASH_ALT, true);
			}
		}
		for (Effect e : effects) {
			e.render();
		}
	}

	public void onEnter() {
		Startup.staticView.setPosition(new Vector3f(0, 0, 0));
		Startup.screenColor.set(0.243f, 0.153f, 0.192f, 1);
		Startup.screenColorTarget.set(0.243f, 0.153f, 0.192f, 0);
	}

	public void onExit() {
		StateManager.initState(new StateSplash());
	}

	@SuppressWarnings("incomplete-switch")
	public void onKeyPress(InputMapping key) {
		switch (key) {

		case CONFIRM:
			if (Startup.screenColor.getW() == 0 && charsRevealed == text.length()) {
				try {
					open = false;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			break;

		}
	}

}
