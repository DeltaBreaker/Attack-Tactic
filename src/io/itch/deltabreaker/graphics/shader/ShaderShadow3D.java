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
		Matrix4f view = Startup.shadowCamera.getView();
		Matrix4f proView = Matrix4f.multiply(view, Startup.shadowCamera.projection);
		Matrix4f.release(view);
		setUniform("proView", proView);
		Matrix4f.release(proView);
	}

}
