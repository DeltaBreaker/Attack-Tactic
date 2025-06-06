package io.itch.deltabreaker.graphics.shader;

import io.itch.deltabreaker.core.ResourceManager;
import io.itch.deltabreaker.core.SettingsManager;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.Matrix4f;

public class ShaderMain3DMedial extends Shader {

	public static float fadeDist = 96f;
	public static float transition = 32f;
	
	public ShaderMain3DMedial(String file) {
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
		
		if(setStaticUniforms) {
			setStaticUniforms();
			setStaticUniforms = false;
		}
	}

	@Override
	public void setStaticUniforms() {
		Matrix4f view = Startup.camera.getView();
		Matrix4f proView = Matrix4f.multiply(view, Startup.camera.projection);
		Matrix4f.release(view);
		setUniform("proView", proView);
		Matrix4f.release(proView);
		
		view = Startup.shadowCamera.getView();
		proView = Matrix4f.multiply(view, Startup.shadowCamera.projection);
		Matrix4f.release(view);
		setUniform("lightProView", proView);
		Matrix4f.release(proView);
		
		setUniform("bias", Startup.shadowBias);
		setUniform("seed", Startup.seed);
		setUniform("shadowAmount", SettingsManager.shadowIntensity);
		setUniform("fogPos", Startup.fog);
		setUniform("fadeDist", fadeDist);
		setUniform("transition", transition);
		setUniform("depthMultiplier", Startup.depthMultiplier);		
	}

}
