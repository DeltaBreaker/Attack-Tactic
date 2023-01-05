#version 400

out vec4 FragColor;
out vec4 BrightColor;

in vec2 TexCoords;

struct PointLight {
	vec3 position;
	vec3 color;
	vec3 direction;

	float constant;
	float linear;
	float quadratic;

	bool directional;
};

uniform sampler2D baseImage;
uniform sampler2D normalImage;
uniform sampler2D positionImage;
uniform PointLight lights[128];
uniform int lightAmt;
uniform float gamma;
uniform float ambiance_intensity;
uniform float diffuse_intensity;
uniform float specular_intensity;
uniform float shininess;
uniform float corruption;
uniform float seed;
uniform vec3 camera_pos;

// These are calculated once and then used for every light calculation
vec3 view_dir;
vec3 norm;
float kPi = 3.14159265;
float kEnergyConservation;

float rand(vec2 co) {
	return fract(sin(dot(co.xy, vec2(12.9898, 78.233))) * 43758.5453);
}

vec3 getLighting(PointLight light, vec3 fragPos) {
	vec3 result = vec3(0);
	float distance = length(light.position - fragPos);
	float attenuation = 1.0
			/ (light.constant + light.linear * distance
					+ light.quadratic * (distance * distance));

	// Ambient light
	vec3 ambiant_light = light.color * ambiance_intensity;

	// Diffuse light
	vec3 light_dir =
			(light.directional) ?
					normalize(-light.direction) :
					normalize(light.position - fragPos);
	float diff = max(dot(norm, light_dir), 0.0);
	float diffuse = diff * diffuse_intensity;
	vec3 diffuse_light = diffuse * light.color;

	// Specular light
	vec3 reflect_dir = normalize(light_dir + view_dir);
	float spec = kEnergyConservation
			* pow(max(dot(norm, reflect_dir), 0.0), shininess);

	spec = diff != 0 ? spec : 0.0;
	vec3 specular_light = specular_intensity * spec * light.color;
	result = (ambiant_light + diffuse_light + specular_light) * attenuation;

	return result;
}

void main() {
	// Take in normal from texture
	vec3 fragPos = texture(positionImage, TexCoords).rgb;
	vec3 normal = texture(normalImage, TexCoords).rgb;
	vec4 baseColor = texture(baseImage, TexCoords);
	float shadow = texture(normalImage, TexCoords).a;
	int bloomCalc = int(texture(positionImage, TexCoords).a);

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
	FragColor.rgb = abs(corruption - 1) * FragColor.rgb
			+ corruption * vec3(dot(FragColor.rgb, vec3(0.299, 0.587, 0.114)))
					* rand(vec2(seed) * fragPos.xz);
	if ((brightness > 1 && bloomCalc != 2) || bloomCalc == 1) {
		BrightColor = FragColor;
	} else {
		BrightColor = vec4(0, 0, 0, 1);
	}
}
