package io.itch.deltabreaker.effect;

import io.itch.deltabreaker.graphics.Light;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.state.StateManager;

public class EffectThunder extends Effect {

	private float fadeSpeed;
	private Light light;
	private Vector3f baseLightColor;
	
	public EffectThunder(Light light, float fadeSpeed) {
		super(Vector3f.EMPTY, Vector3f.EMPTY, Vector3f.EMPTY);
		this.light = light;
		this.fadeSpeed = fadeSpeed;
		baseLightColor = light.color.copy();
		StateManager.currentState.lights.add(light);
	}

	@Override
	public void tick() {
		light.color.sub(baseLightColor.getX() * fadeSpeed, baseLightColor.getY() * fadeSpeed, baseLightColor.getZ() * fadeSpeed);
		if (light.color.trueTotal() <= 0) {
			remove = true;
		}
	}

	@Override
	public void render() {

	}

	@Override
	public void cleanUp() {
		StateManager.currentState.lights.remove(light);
	}

}
