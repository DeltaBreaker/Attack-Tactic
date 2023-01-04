package io.itch.deltabreaker.graphics.shader;

import io.itch.deltabreaker.graphics.Material;

public class ShaderGaussianBlur extends Shader {

	public ShaderGaussianBlur(String file) {
		super(file);
	}

	@Override
	public void use(int sample, Material material) {
		// Empty
	}

	@Override
	public void setStaticUniforms() {
		// Empty
	}

}
