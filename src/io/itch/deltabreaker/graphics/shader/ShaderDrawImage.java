package io.itch.deltabreaker.graphics.shader;

import io.itch.deltabreaker.core.ResourceManager;
import io.itch.deltabreaker.core.SettingsManager;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.graphics.Material;

public class ShaderDrawImage extends Shader {

	public ShaderDrawImage(String file) {
		super(file);
	}

	@Override
	public void use(int sample, Material material) {
		if (setStaticUniforms) {
			setStaticUniforms();
			setStaticUniforms = false;
		}
	}

	@Override
	public void setStaticUniforms() {
		setUniform("corruption", Startup.corruption);
		setUniform("resolution", Startup.width, Startup.height);
		setUniform("radius", SettingsManager.vignetteRadius);
		setUniform("softness", SettingsManager.vignetteSoftness);
		setUniform("rt_w", (float) Startup.width);
		setUniform("rt_h", (float) Startup.height);
		setUniform("enableFXAA", SettingsManager.enableFXAA);
		setUniform("sampler", 0);
		setUniform("depth", 1);
		ResourceManager.textures.get("haze.png").bind(2);
		setUniform("haze", 2);
		setUniform("enableHaze", Startup.enableHaze);
		setUniform("time", (float) Startup.universalAge * 0.0001f);
		setUniform("size", 0.07f);
		setUniform("strength", 0.006f);
		setUniform("outlineTolerance", SettingsManager.outlineTolerance);
		setUniform("width", 1f / Startup.width * SettingsManager.outlineThickness);
		setUniform("height", 1f / Startup.height * SettingsManager.outlineThickness);
		setUniform("enableOutline", SettingsManager.enableOutline);
	}

}
