#version 400

// FXAA code used from:
// https://www.geeks3d.com/20110405/fxaa-fast-approximate-anti-aliasing-demo-glsl-opengl-test-radeon-geforce/3/

out vec4 FragColor;
  
in vec2 TexCoords;

uniform sampler2D sampler;
uniform sampler2D blur;
uniform sampler2D haze;
uniform sampler2D depth;
uniform vec2 resolution;
uniform float radius;
uniform float softness;
uniform float corruption;
uniform float spacing;
uniform float thickness;
uniform float darkness;
uniform bool enableFXAA;
uniform bool enableHaze;
uniform float time;
uniform float size;
uniform float strength;
uniform float outlineTolerance;
uniform float width;
uniform float height;
uniform bool enableOutline;

uniform float vx_offset;
uniform float rt_w; // GeeXLab built-in
uniform float rt_h; // GeeXLab built-in
uniform float FXAA_SPAN_MAX = 8.0;
uniform float FXAA_REDUCE_MUL = 1.0/8.0;
uniform float FXAA_SUBPIX_SHIFT = 1.0/4.0;
vec4 posPos;

#define FxaaInt2 ivec2
#define FxaaFloat2 vec2
#define FxaaTexLod0(t, p) textureLod(t, p, 0.0)
#define FxaaTexOff(t, p, o, r) textureLodOffset(t, p, 0.0, o)

vec3 FxaaPixelShader( 
  vec4 posPos, // Output of FxaaVertexShader interpolated across screen.
  sampler2D tex, // Input texture.
  vec2 rcpFrame) // Constant {1.0/frameWidth, 1.0/frameHeight}.
{   
/*---------------------------------------------------------*/
    #define FXAA_REDUCE_MIN   (1.0/128.0)
    //#define FXAA_REDUCE_MUL   (1.0/8.0)
    //#define FXAA_SPAN_MAX     8.0
/*---------------------------------------------------------*/
    vec3 rgbNW = FxaaTexLod0(tex, posPos.zw).xyz;
    vec3 rgbNE = FxaaTexOff(tex, posPos.zw, FxaaInt2(1,0), rcpFrame.xy).xyz;
    vec3 rgbSW = FxaaTexOff(tex, posPos.zw, FxaaInt2(0,1), rcpFrame.xy).xyz;
    vec3 rgbSE = FxaaTexOff(tex, posPos.zw, FxaaInt2(1,1), rcpFrame.xy).xyz;
    vec3 rgbM  = FxaaTexLod0(tex, posPos.xy).xyz;
/*---------------------------------------------------------*/
    vec3 luma = vec3(0.299, 0.587, 0.114);
    float lumaNW = dot(rgbNW, luma);
    float lumaNE = dot(rgbNE, luma);
    float lumaSW = dot(rgbSW, luma);
    float lumaSE = dot(rgbSE, luma);
    float lumaM  = dot(rgbM,  luma);
/*---------------------------------------------------------*/
    float lumaMin = min(lumaM, min(min(lumaNW, lumaNE), min(lumaSW, lumaSE)));
    float lumaMax = max(lumaM, max(max(lumaNW, lumaNE), max(lumaSW, lumaSE)));
/*---------------------------------------------------------*/
    vec2 dir; 
    dir.x = -((lumaNW + lumaNE) - (lumaSW + lumaSE));
    dir.y =  ((lumaNW + lumaSW) - (lumaNE + lumaSE));
/*---------------------------------------------------------*/
    float dirReduce = max(
        (lumaNW + lumaNE + lumaSW + lumaSE) * (0.25 * FXAA_REDUCE_MUL),
        FXAA_REDUCE_MIN);
    float rcpDirMin = 1.0/(min(abs(dir.x), abs(dir.y)) + dirReduce);
    dir = min(FxaaFloat2( FXAA_SPAN_MAX,  FXAA_SPAN_MAX), 
          max(FxaaFloat2(-FXAA_SPAN_MAX, -FXAA_SPAN_MAX), 
          dir * rcpDirMin)) * rcpFrame.xy;
/*--------------------------------------------------------*/
    vec3 rgbA = (1.0/2.0) * (
        FxaaTexLod0(tex, posPos.xy + dir * (1.0/3.0 - 0.5)).xyz +
        FxaaTexLod0(tex, posPos.xy + dir * (2.0/3.0 - 0.5)).xyz);
    vec3 rgbB = rgbA * (1.0/2.0) + (1.0/4.0) * (
        FxaaTexLod0(tex, posPos.xy + dir * (0.0/3.0 - 0.5)).xyz +
        FxaaTexLod0(tex, posPos.xy + dir * (3.0/3.0 - 0.5)).xyz);
    float lumaB = dot(rgbB, luma);
    if((lumaB < lumaMin) || (lumaB > lumaMax)) return rgbA;
    return rgbB;
}

vec4 PostFX(sampler2D tex, float time) {
  vec4 c = vec4(0.0);
  vec2 rcpFrame = vec2(1.0/rt_w, 1.0/rt_h);
  c.rgb = FxaaPixelShader(posPos, tex, rcpFrame);
  c.a = 1.0;
  return c;
}

void main() {
	// Heat haze distortion
	vec2 distort;
	if(enableHaze) {
		distort.x = texture2D(haze, fract(TexCoords * size + vec2(0.0, time))).r * strength;
		distort.y = texture2D(haze, fract(TexCoords * size * 3.4 + vec2(0.0, time * 1.6))).g * strength * 1.3;
	}

	// FXAA Coords
	vec2 rcpFrame = vec2(1.0/rt_w, 1.0/rt_h);
  	posPos.xy = TexCoords.xy + distort;
  	posPos.zw = (TexCoords.xy + distort) - (rcpFrame * (0.5 + FXAA_SUBPIX_SHIFT));

	// Vignette
	vec2 position = (gl_FragCoord.xy / resolution.xy) - vec2(0.5);
	position.x *= resolution.x / resolution.y;
	float length = length(position);
	length = smoothstep(radius, radius - (softness + corruption), length);

    vec3 hdrColor = (enableFXAA) ? PostFX(sampler, 0.0).rgb : texture(sampler, TexCoords + distort).rgb;
    vec3 bloomColor = texture(blur, TexCoords + distort).rgb;

	hdrColor += bloomColor;

    FragColor = vec4(hdrColor, 1.0);
    
    // Apply vignette
    FragColor.rgb = mix(FragColor.rgb, FragColor.rgb * length, 0.5);
    
    if(enableOutline) {
	    // Edge detection
		float currentDepth = texture(depth, TexCoords).r;
		float total = 0;
		total += abs(ceil(currentDepth - texture(depth, TexCoords + vec2(width, 0)).r + outlineTolerance));
		total += abs(ceil(currentDepth - texture(depth, TexCoords + vec2(-width, 0)).r + outlineTolerance));
		total += abs(ceil(currentDepth - texture(depth, TexCoords + vec2(0, height)).r + outlineTolerance));
		total += abs(ceil(currentDepth - texture(depth, TexCoords + vec2(0, -height)).r + outlineTolerance));
		
		if(total > 0) {
			vec3 outline = vec3(1 - total);
			FragColor.rgb *= clamp(outline, 0, 1) * texture(depth, TexCoords).a;
		}
	}
}
