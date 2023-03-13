package io.itch.deltabreaker.graphics.shader;

import io.itch.deltabreaker.core.SettingsManager;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.Matrix4f;

public class ShaderStatic3D extends Shader {

	public ShaderStatic3D(String file) {
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
		Matrix4f view = Startup.staticView.getView();
		setUniform("view", view);
		Matrix4f.release(view);
		
		view = Startup.shadowCamera.getView();
		setUniform("lightView", view);
		Matrix4f.release(view);
		
		setUniform("projection", Startup.staticView.projection);
		setUniform("camera_pos", Startup.staticView.position);
		setUniform("lightProjection", Startup.shadowCamera.projection);
		setUniform("gamma", SettingsManager.gamma);
		setUniform("resolution", Startup.width, Startup.height);
		setUniform("radius", SettingsManager.vignetteRadius);
		setUniform("softness", SettingsManager.vignetteSoftness);
	}

}
