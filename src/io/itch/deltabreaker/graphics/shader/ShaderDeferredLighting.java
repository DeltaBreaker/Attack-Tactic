package io.itch.deltabreaker.graphics.shader;

import io.itch.deltabreaker.core.SettingsManager;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.graphics.Light;
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

		int lightCount = Math.min(StateManager.currentState.lights.size(), 100);
		for (int i = 0; i < lightCount; i++) {
			Light light = StateManager.currentState.lights.get(i);
			setUniform("lights[" + i + "].position", light.position.getX(), light.position.getY(), light.position.getZ(), light.constant);
			setUniform("lights[" + i + "].color", light.color.getX(), light.color.getY(), light.color.getZ(), light.linear);
			setUniform("lights[" + i + "].direction", light.direction.getX(), light.direction.getY(), light.direction.getZ(), light.quadratic);
		}
		setUniform("lightAmt", lightCount);
	}

}
