package io.itch.deltabreaker.graphics.shader;

import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.core.ResourceManager;
import io.itch.deltabreaker.core.SettingsManager;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.Matrix4f;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.state.StateManager;

public class ShaderMain3DEnemy extends Shader {

	public static float fadeDist = 24f;
	public static float transition = 8f;

	public ShaderMain3DEnemy(String file) {
		super(file);
	}

	@Override
	public void use(int sample, Material material) {
		// Object-to-object uniforms
		setUniform("sampler", sample);
		setUniform("shadowMap", sample + 1);
		ResourceManager.textures.get("fog.png").bind(sample + 2);
		setUniform("fog", sample + 2);
		setUniform("ambiance_intensity", material.ambianceIntensity);
		setUniform("diffuse_intensity", material.diffuseIntensity);
		setUniform("specular_intensity", material.specularIntensity);
		setUniform("shininess", material.shininess);

		if (setStaticUniforms) {
			setStaticUniforms();
			setStaticUniforms = false;
		}
	}

	@Override
	public void setStaticUniforms() {
		setUniform("proView", Matrix4f.multiply(Startup.camera.getView(), Startup.camera.projection));
		setUniform("lightProView", Matrix4f.multiply(Startup.shadowCamera.getView(), Startup.shadowCamera.projection));
		setUniform("seed", Startup.seed);
		setUniform("camera_pos", Startup.camera.position);
		setUniform("gamma", SettingsManager.gamma);
		setUniform("bias", Startup.shadowBias);
		setUniform("shadowAmount", SettingsManager.shadowIntensity);
		setUniform("fogPos", Startup.fog);
		setUniform("fadeDist", fadeDist);
		setUniform("transition", transition);
		setUniform("spotlight", new Vector3f(Inventory.active.get(0).x / 2, (13 + Inventory.active.get(0).height) / 2, Inventory.active.get(0).y / 2));
		setUniform("depthMultiplier", Startup.depthMultiplier);

		// Light variables
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
		setUniform("lightAmt", StateManager.currentState.lights.size());
	}

}
