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

uniform float ambiance_intensity;
uniform float diffuse_intensity;
uniform float specular_intensity;
uniform float shininess;
uniform float bias;
uniform float shadowAmount;
uniform float fogPos;
uniform float attenuationLimit;
uniform float depthMultiplier;
uniform float seed;

uniform float fadeDist;
uniform float transition;

float rand(vec2 co) {
	return fract(sin(dot(co.xy, vec2(12.9898, 78.233))) * 43758.5453);
}

float ShadowCalculation(vec4 fragPosLightSpace)
{
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

void main() {		
	float shadow = ShadowCalculation(fs_in.FragPosLightSpace) * fs_in.shade.w;
	vec4 tex_color = texture2D(sampler, fs_in.TexCoords);
	tex_color = 0.667 * tex_color + 0.333 * texture2D(fog, fs_in.TexCoords + fogPos) * tex_color;
		
	BaseColor = tex_color * fs_in.shade;
	BaseColor.rgb = vec3(dot(BaseColor.rgb, vec3(0.299, 0.587, 0.114))) * rand(vec2(seed) * fs_in.FragPos.xz);
    float dist = gl_FragCoord.z / gl_FragCoord.w;
    float amt = min(dist - fadeDist, transition);
    float fadeAmt = clamp((1 - amt / transition) * fs_in.shade.w, 0, 1);
    if(dist > fadeDist) {
    	BaseColor.w *= fadeAmt;
    }
    Normals = vec4(fs_in.Normal, fadeAmt);
	FragPos = vec4(fs_in.FragPos, fadeAmt);
	Material = vec4(ambiance_intensity, diffuse_intensity, specular_intensity, fadeAmt);
	Misc = vec4(shadow, BaseColor.a, shininess, fadeAmt);
    	
    Depth = vec4(vec3(1 - ((gl_FragCoord.z / gl_FragCoord.w) * depthMultiplier)), fs_in.shade.w);
}
