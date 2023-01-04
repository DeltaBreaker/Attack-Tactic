#version 400

layout (location = 0) in vec3 vertices;
layout (location = 1) in vec2 texCoords;
layout (location = 2) in vec3 normals;
layout (location = 3) in vec4 shadePass;
layout (location = 4) in mat4 model;

out VS_OUT {
    vec3 FragPos;
    vec3 Normal;
    vec2 TexCoords;
    vec4 FragPosLightSpace;
    vec4 shade;
} vs_out;

flat out int instance;

uniform mat4 proView;
uniform mat4 lightProView;
uniform float seed;

float rand(vec2 co) {
    return fract(sin(dot(co.xy, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
	instance = gl_InstanceID;
	vs_out.shade = shadePass;
	vs_out.FragPos = vec3(transpose(model) * vec4(vertices, 1.0));
	vs_out.Normal = transpose(inverse(mat3(transpose(model)))) * normals;
	vs_out.TexCoords = texCoords;
	vs_out.FragPosLightSpace = lightProView * vec4(vs_out.FragPos, 1.0);
	gl_Position = proView * vec4(vs_out.FragPos, 1.0);
}
