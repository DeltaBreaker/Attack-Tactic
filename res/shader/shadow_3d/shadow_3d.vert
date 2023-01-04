#version 400

layout (location = 0) in vec3 vertices;
layout (location = 1) in vec2 texCoords;
layout (location = 2) in vec3 normals;
layout (location = 3) in vec4 shadePass;
layout (location = 4) in mat4 model;

out VS_OUT {
    vec3 Normal;
    vec2 TexCoords;
    vec4 shade;
} vs_out;

uniform mat4 proView;

void main() {
	vs_out.shade = shadePass;
	vs_out.Normal = normals;
	vs_out.TexCoords = texCoords;
	gl_Position = proView * transpose(model) * vec4(vertices, 1.0);
}
