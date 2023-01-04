#version 400

layout (location = 0) in vec3 vertices;
layout (location = 1) in vec2 texCoords;
layout (location = 2) in vec3 normals;
layout (location = 3) in vec4 shadePass;

out VS_OUT {
    vec3 FragPos;
    vec3 Normal;
    vec2 TexCoords;
    vec4 FragPosLightSpace;
    vec4 shadePass;
} vs_out;

flat out int instance;

uniform mat4 proView;
uniform mat4 lightProView;
uniform float corruption;
uniform float seed;

mat4 model = mat4(0.5, 0.0, 0.0, 0.0,
				  0.0, 0.5, 0.0, 0.0,
				  0.0, 0.0, 0.5, 0.0,
				  0.0, 0.0, 0.0, 0.0);

float rand(vec2 co) {
	return fract(sin(dot(co.xy, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
	instance = gl_InstanceID;
	model[0].w = shadePass.x;
	model[1].w = shadePass.y;
	model[2].w = shadePass.z;
	model[3].w = shadePass.w;
	vs_out.shadePass = shadePass;
	
	vs_out.FragPos = vec3(transpose(model) * vec4(vertices, 1.0));
	vs_out.FragPos.x = corruption * 2 * rand(vs_out.FragPos.xz * vec2(seed)) + vs_out.FragPos.x;
	vs_out.FragPos.z = corruption * 2 * rand(vs_out.FragPos.xz * vec2(seed)) + vs_out.FragPos.z;
	vs_out.Normal = transpose(inverse(mat3(transpose(model)))) * normals;
	vs_out.TexCoords = texCoords;
	vs_out.FragPosLightSpace = lightProView * vec4(vs_out.FragPos, 1.0);
	gl_Position = proView * vec4(vs_out.FragPos, 1.0);
}
