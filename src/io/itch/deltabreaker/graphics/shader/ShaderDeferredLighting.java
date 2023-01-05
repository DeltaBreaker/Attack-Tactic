package io.itch.deltabreaker.graphics.shader;

import io.itch.deltabreaker.core.SettingsManager;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.state.StateManager;

public class ShaderDeferredLighting extends Shader {
	
	public ShaderDeferredLighting(String file) {
		super(file);
	}

	@Override
	public void use(int sample, Material material) {
		setUniform("baseImage", sample);
		setUniform("normalImage", sample + 1);
		setUniform("positionImage", sample + 2);
		setUniform("materialImage", sample + 3);
		setUniform("miscImage", sample + 4);

		if (setStaticUniforms) {
			setStaticUniforms();
			setStaticUniforms = false;
		}
	}

	@Override
	public void setStaticUniforms() {
		setUniform("camera_pos", Startup.camera.position);
		setUniform("gamma", SettingsManager.gamma);
		setUniform("corruption", Startup.corruption);
		setUniform("seed", Startup.seed);

		int lightCount = Math.min(StateManager.currentState.lights.size(), 128);
		for (int i = 0; i < lightCount; i++) {
			setUniform("lights[" + i + "].position", StateManager.currentState.lights.get(i).position);
			setUniform("lights[" + i + "].color", StateManager.currentState.lights.get(i).color);
			if (StateManager.currentState.lights.get(i).direction != null) {
				setUniform("lights[" + i + "].direction", StateManager.currentState.lights.get(i).direction);
			}
			setUniform("lights[" + i + "].constant", StateManager.currentState.lights.get(i).constant);
			setUniform("lights[" + i + "].linear", StateManager.currentState.lights.get(i).linear);
			setUniform("lights[" + i + "].quadratic", StateManager.currentState.lights.get(i).quadratic);
			setUniform("lights[" + i + "].directional", StateManager.currentState.lights.get(i).direction != null);
		}
		setUniform("lightAmt", lightCount);
	}

}
