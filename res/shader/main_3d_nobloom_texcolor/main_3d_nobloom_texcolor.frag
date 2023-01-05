#version 400

out vec4 BaseColor;
out vec4 Normals;
out vec4 FragPos;
out vec4 Material;
out vec4 Misc;
out vec4 Depth;

in VS_OUT {
	vec3 FragPos;
	vec3 Normal;
	vec2 TexCoords;
	vec4 FragPosLightSpace;
	vec4 shade;
}fs_in;

flat in int instance;

uniform sampler2D sampler;
uniform sampler2D shadowMap;
uniform sampler2D fog;

uniform float bias;
uniform float shadowAmount;
uniform float fogPos;
uniform float depthMultiplier;
uniform float corruption;
uniform float ambiance_intensity;
uniform float diffuse_intensity;
uniform float specular_intensity;
uniform float shininess;

float ShadowCalculation(vec4 fragPosLightSpace) {
	vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
	projCoords = projCoords * 0.5 + 0.5;
	float currentDepth = projCoords.z;

	float shadow = 0;
	vec2 texelSize = 2.0 / textureSize(shadowMap, 0);
	for (int x = -1; x <= 1; x++) {
		for (int y = -1; y <= 1; y++) {
			float pcfDepth = texture2D(shadowMap,
					projCoords.xy + vec2(x, y) * texelSize).r;
			shadow += currentDepth - bias > pcfDepth ? shadowAmount : 0.0;
		}
	}

	return shadow;
}

void main() {
	float shadow = ShadowCalculation(fs_in.FragPosLightSpace) * fs_in.shade.w;
	vec4 tex_color = texture2D(sampler, fs_in.TexCoords);
	tex_color = abs(corruption / 3 - 1) * tex_color + corruption / 3 * texture2D(fog, fs_in.TexCoords + fogPos) * tex_color;

	BaseColor = tex_color * fs_in.shade;
	Normals = vec4(fs_in.Normal, tex_color.a);
	FragPos = vec4(fs_in.FragPos, tex_color.a);
	Material = vec4(ambiance_intensity, diffuse_intensity, specular_intensity, tex_color.a);
	Misc = vec4(shadow, 0, shininess, tex_color.a);

	Depth = vec4(
			vec3(1 - ((gl_FragCoord.z / gl_FragCoord.w) * depthMultiplier)),
			fs_in.shade.w);
}
