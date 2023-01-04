package io.itch.deltabreaker.graphics.shader;

import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.Matrix4f;

public class ShaderShadow3D extends Shader {

	public ShaderShadow3D(String file) {
		super(file);
	}

	@Override
	public void use(int sample, Material material) {
		if(setStaticUniforms) {
			setStaticUniforms();
			setStaticUniforms = false;
		}
	}

	@Override
	public void setStaticUniforms() {
		setUniform("proView", Matrix4f.multiply(Startup.shadowCamera.getView(), Startup.shadowCamera.projection));
	}

}
