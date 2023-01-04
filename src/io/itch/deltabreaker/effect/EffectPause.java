package io.itch.deltabreaker.effect;

import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.state.StateManager;

public class EffectPause extends Effect {

	private int timer = 0;
	private int time;
	private int dots = 1;
	
	public EffectPause(Unit u, int time) {
		super(new Vector3f(u.x, 20 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y - 8), Vector3f.EMPTY, Vector3f.SCALE_HALF);
		this.time = time;
	}

	@Override
	public void tick() {
		if (timer < time) {
			timer++;
		} else if(dots < 3) {
			dots++;
			timer = 0;
		} else {
			remove = true;
		}
	}

	@Override
	public void render() {
		String text = "";
		for(int i = 0; i < dots; i++) {
			text += ".";
		}
		TextRenderer.render(text, Vector3f.add(position, -text.length() * 3 + 5.5f, 0, 0), rotation, scale, Vector4f.COLOR_BASE, false);
	}

	@Override
	public void cleanUp() {
		
	}

}
