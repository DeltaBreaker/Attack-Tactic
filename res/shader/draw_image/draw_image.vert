#version 400

// FXAA code used from:
// https://www.geeks3d.com/20110405/fxaa-fast-approximate-anti-aliasing-demo-glsl-opengl-test-radeon-geforce/3/

layout (location = 0) in vec3 vertices;
layout (location = 1) in vec2 texCoords;

out vec2 TexCoords;

void main() {
	TexCoords = texCoords;
	gl_Position = vec4(vertices, 1);
}
