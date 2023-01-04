#version 400

layout (location = 0) in vec3 vertices;
layout (location = 1) in vec2 texCoords;

out vec2 TexCoords;

void main() {
	TexCoords = texCoords;
	gl_Position = vec4(vertices, 1);
}
