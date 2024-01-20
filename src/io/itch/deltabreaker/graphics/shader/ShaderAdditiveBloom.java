package io.itch.deltabreaker.graphics.shader;

import io.itch.deltabreaker.core.ResourceManager;
import io.itch.deltabreaker.core.SettingsManager;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.graphics.Material;

public class ShaderAdditiveBloom extends Shader {

	public ShaderAdditiveBloom(String file) {
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
		setUniform("sampler", 0);
		setUniform("blur", 1);
		ResourceManager.textures.get("haze.png").bind(2);
		setUniform("haze", 2);
		setUniform("depth", 3);
		setUniform("rt_w", (float) Startup.width);
		setUniform("rt_h", (float) Startup.height);
		setUniform("enableFXAA", SettingsManager.enableFXAA);
		setUniform("enableHaze", Startup.enableHaze);
		setUniform("time", (float) Startup.universalAge * 0.0001f);
		setUniform("size", 0.07f);
		setUniform("strength", 0.006f);
	}

}

//	Can be used for crt scan line effect
//	if(int(mod(TexCoords.y * resolution.y, spacing)) < thickness) {
//		hdrColor *= vec3(darkness);
//	}