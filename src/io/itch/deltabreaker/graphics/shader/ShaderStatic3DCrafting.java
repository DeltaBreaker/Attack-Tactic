package io.itch.deltabreaker.graphics.shader;

import io.itch.deltabreaker.core.SettingsManager;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.ui.CraftingHUD;

public class ShaderStatic3DCrafting extends Shader {

	public ShaderStatic3DCrafting(String file) {
		super(file);
	}

	@Override
	public void use(int sample, Material material) {
		setUniform("sampler", sample);
		setUniform("shininess", material.shininess);
		
		if(setStaticUniforms) {
			setStaticUniforms();
			setStaticUniforms = false;
		}
	}

	@Override
	public void setStaticUniforms() {
		setUniform("view", Startup.staticView.getView());
		setUniform("projection", Startup.staticView.projection);
		setUniform("camera_pos", Startup.staticView.position);
		setUniform("lightView", Startup.shadowCamera.getView());
		setUniform("lightProjection", Startup.shadowCamera.projection);
		setUniform("gamma", SettingsManager.gamma);
		setUniform("resolution", Startup.width, Startup.height);
		setUniform("radius", SettingsManager.vignetteRadius);
		setUniform("softness", SettingsManager.vignetteSoftness);
		setUniform("seed", CraftingHUD.seed);
		setUniform("xOffset", CraftingHUD.xOffset);
		setUniform("yOffset", CraftingHUD.yOffset);
	}

}
