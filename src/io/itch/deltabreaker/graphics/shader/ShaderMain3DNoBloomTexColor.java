package io.itch.deltabreaker.graphics.shader;

import io.itch.deltabreaker.core.ResourceManager;
import io.itch.deltabreaker.core.SettingsManager;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.Matrix4f;

public class ShaderMain3DNoBloomTexColor extends Shader {

	public ShaderMain3DNoBloomTexColor(String file) {
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
		setUniform("corruption", Startup.corruption);
		setUniform("seed", Startup.seed);
		setUniform("bias", Startup.shadowBias);
		setUniform("shadowAmount", SettingsManager.shadowIntensity);
		setUniform("fogPos", Startup.fog);
		setUniform("depthMultiplier", Startup.depthMultiplier);
	}

}
