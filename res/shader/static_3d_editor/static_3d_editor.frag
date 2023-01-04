#version 400

out vec4 FragColor;
out vec4 BrightColor;

in VS_OUT {
    vec3 FragPos;
    vec3 Normal;
    vec2 TexCoords;
    vec4 FragPosLightSpace;
    vec4 shade;
} fs_in;

flat in int instance;

uniform sampler2D sampler;

uniform vec3 camera_pos;
uniform float shininess;
uniform float gamma;
uniform vec2 resolution;
uniform float radius;
uniform float softness;

vec3 getLighting() {
		// Ambient light
		vec3 ambiant_light = vec3(1) * 0.125;
		
		// Diffuse light
		vec3 norm = normalize(fs_in.Normal);
		float diff = max(dot(norm, vec3(0, 0, 1)), 0.0) * 0.75;
		
		return (ambiant_light + diff);
}

void main() {
		// Vignette
		vec2 position = (gl_FragCoord.xy / resolution.xy) - vec2(0.5);
		position.x *= resolution.x / resolution.y;
		float length = length(position);
		length = smoothstep(radius, radius - softness, length);
		
		vec4 tex_color = texture2D(sampler, fs_in.TexCoords);
		
		// Get total light
		vec3 lighting = vec3(1);
		
		FragColor = tex_color * fs_in.shade * vec4(lighting, 1);
		FragColor.rgb = pow(FragColor.rgb, vec3(1.0/gamma));

		float brightness = dot(FragColor.rgb, vec3(0.2126, 0.7152, 0.0722));
    	if(brightness > 1) {
        	BrightColor = FragColor;
   		}
        
        FragColor.rgb = mix(FragColor.rgb, FragColor.rgb * length, 0.5);
      	BrightColor = vec4(0);
}
