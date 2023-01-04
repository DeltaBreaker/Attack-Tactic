#version 400

out vec4 FragColor;
out vec4 BrightColor;
out vec4 Depth;

in VS_OUT {
    vec3 FragPos;
    vec3 Normal;
    vec2 TexCoords;
    vec4 FragPosLightSpace;
    vec4 shade;
} fs_in;

flat in int instance;

struct PointLight {
	vec3 position;
	vec3 color;
	vec3 direction;
	
	float constant;
	float linear;
	float quadratic;
	
	bool directional;
};

uniform PointLight lights[128];
uniform int lightAmt;

uniform sampler2D sampler;
uniform sampler2D shadowMap;
uniform sampler2D fog;

uniform vec3 camera_pos;
uniform float ambiance_intensity;
uniform float diffuse_intensity;
uniform float specular_intensity;
uniform float shininess;
uniform float gamma;
uniform float bias;
uniform float shadowAmount;
uniform float corruption;
uniform float seed;
uniform float fogPos;
uniform float attenuationLimit;
uniform float depthMultiplier;

float kPi = 3.14159265;
float kEnergyConservation;
vec3 view_dir;
vec3 norm;

float rand(vec2 co) {
	return fract(sin(dot(co.xy, vec2(12.9898, 78.233))) * 43758.5453);
}

float ShadowCalculation(vec4 fragPosLightSpace) {
    vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
    projCoords = projCoords * 0.5 + 0.5;
    float currentDepth = projCoords.z;
    
    float shadow = 0;
    vec2 texelSize = 2.0 / textureSize(shadowMap, 0);
    for(int x = -1; x <= 1; x++) {
    	for(int y = -1; y <= 1; y++) {
    		float pcfDepth = texture2D(shadowMap, projCoords.xy + vec2(x, y) * texelSize).r;
    		shadow += currentDepth - bias > pcfDepth ? shadowAmount : 0.0;
    	}
    }

    return shadow;
}  

vec3 getLighting(PointLight light) {
		vec3 result = vec3(0);
		float distance = length(light.position - fs_in.FragPos);
		float attenuation = 1.0 / (light.constant + light.linear * distance + light.quadratic * (distance * distance));
		
		// Ambient light
		vec3 ambiant_light = light.color * ambiance_intensity;
			
		// Diffuse light
		vec3 light_dir = (light.directional) ? normalize(-light.direction) : normalize(light.position - fs_in.FragPos);
		float diff = max(dot(norm, light_dir), 0.0);
		float diffuse = diff * diffuse_intensity;
		vec3 diffuse_light = diffuse * light.color;
			
		// Specular light
		vec3 reflect_dir = normalize(light_dir + view_dir);
		float spec = kEnergyConservation * pow(max(dot(norm, reflect_dir), 0.0), shininess);
			
		spec = diff != 0 ? spec : 0.0;
		vec3 specular_light = specular_intensity * spec * light.color;
		result = (ambiant_light + diffuse_light + specular_light) * attenuation;
		
		return result;
}

void main() {
		kEnergyConservation = (8.0 + shininess) / (8.0 * kPi);
		view_dir = normalize(camera_pos - fs_in.FragPos);
		norm = normalize(fs_in.Normal);
		
		float shadow = ShadowCalculation(fs_in.FragPosLightSpace) * fs_in.shade.w;
		vec4 tex_color = texture2D(sampler, fs_in.TexCoords);
		//tex_color = abs(corruption / 3 - 1) * tex_color + corruption / 3 * texture2D(fog, fs_in.TexCoords + fogPos) * tex_color;
		
		// Get total light
		vec3 lighting = vec3(0);
		for(int i = 0; i < lightAmt; i++) {
			lighting += getLighting(lights[i]);
		}
		lighting *= (1 - shadow);
		
		FragColor = tex_color * fs_in.shade * vec4(lighting, 1);
		FragColor.rgb = pow(FragColor.rgb, vec3(1.0/gamma));
    	FragColor.rgb = abs(corruption - 1) * FragColor.rgb + corruption * vec3(dot(FragColor.rgb, vec3(0.299, 0.587, 0.114))) * rand(vec2(seed) * fs_in.FragPos.xz);
        BrightColor = FragColor;
        Depth = vec4(vec3(1 - ((gl_FragCoord.z / gl_FragCoord.w) * depthMultiplier)), fs_in.shade.w);
}
