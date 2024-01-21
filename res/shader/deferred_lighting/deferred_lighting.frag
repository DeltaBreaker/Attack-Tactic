#version 400

out vec4 FragColor;
out vec4 BrightColor;

in vec2 TexCoords;

struct PointLight {
	vec4 position;
	vec4 color;
	vec4 direction;
};

uniform sampler2D baseImage;
uniform sampler2D normalImage;
uniform sampler2D positionImage;
uniform sampler2D materialImage;
uniform sampler2D miscImage;

uniform PointLight lights[300];
uniform int lightAmt;
uniform float gamma;
uniform float corruption;
uniform float seed;
uniform vec3 camera_pos;

// These are calculated once and then used for every light calculation
vec3 view_dir;
vec3 norm;
float kPi = 3.14159265;
float kEnergyConservation;
vec3 material;
float shininess;

float rand(vec2 co) {
	return fract(sin(dot(co.xy, vec2(12.9898, 78.233))) * 43758.5453);
}

vec3 getLighting(PointLight light, vec3 fragPos) {
	vec3 lightMinusFrag = light.position.xyz - fragPos;
	float distance = length(lightMinusFrag);
	float attenuation = 1.0
			/ (light.position.w + light.color.w * distance     
					+ light.direction.w * (distance * distance));
					
	vec3 ambiant_light = light.color.rgb * material.r;
					
	float mul = 1 - light.direction.g;
	vec3 light_dir = normalize(lightMinusFrag) * mul + light.direction.rgb;
					
	float diff = dot(norm, light_dir);
	vec3 diffuse_light = diff * material.g * light.color.rgb;
					
	vec3 reflect_dir = normalize(light_dir + view_dir);
	float spec = kEnergyConservation * pow(max(dot(norm, reflect_dir), 0.0), shininess);
					
	spec = diff != 0 ? spec : 0.0;
	vec3 specular_light = material.b * spec * light.color.rgb;
	
	vec3 result = (ambiant_light + diffuse_light + specular_light) * attenuation;
		
	return result;
}

void main() {
	// Take in normal from texture
	vec3 fragPos = texture(positionImage, TexCoords).rgb;
	vec3 normal = texture(normalImage, TexCoords).rgb;
	vec4 baseColor = texture(baseImage, TexCoords);
	material = texture(materialImage, TexCoords).rgb;
	float shadow = texture(miscImage, TexCoords).r;
	float bloomCalc = texture(miscImage, TexCoords).g;
	shininess = texture(miscImage, TexCoords).b;

	// Calculate these once for all lighting calcs
	view_dir = normalize(camera_pos - fragPos);
	norm = normalize(normal);
	kEnergyConservation = (8.0 + shininess) / (8.0 * kPi);

	// Get total light
	vec3 lighting = vec3(0);
	for (int i = 0; i < lightAmt; i++) {
		lighting += getLighting(lights[i], fragPos);
	}
	lighting *= (1 - shadow);

	FragColor = baseColor * vec4(lighting, 1);
	FragColor.rgb = pow(FragColor.rgb, vec3(1.0 / gamma));

	// Calculate brightness value
	float brightness = dot(FragColor.rgb, vec3(0.2126, 0.7152, 0.0722));

	// Turn gray scale based on corruption
	FragColor.rgb = abs(corruption - 1) * FragColor.rgb + corruption * vec3(dot(FragColor.rgb, vec3(0.299, 0.587, 0.114))) * rand(vec2(seed) * fragPos.xz);
	if (brightness > 1 || bloomCalc > 1) {
		BrightColor = FragColor;
		BrightColor.rgba = clamp(BrightColor.rgba, 0, 1);
		BrightColor *= (bloomCalc > 1) ? bloomCalc - 1 : bloomCalc;
	} else {
		BrightColor = vec4(0, 0, 0, 1);
	}
}
