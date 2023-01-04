package io.itch.deltabreaker.graphics;

public enum Material {

	DEFAULT(1f, 0.5f, 0.55f, 2),
	
	BRONZE(0.8f, 0.85f, 1.45f, 16),
	
	SILVER(0.8f, 0.65f, 1.45f, 16),
	
	GOLD(0.8f, 0.65f, 1.45f, 16),
	
	MATTE(1, 0, 0, 1),
	
	GLOW(2, 0, 0, 1),
	
	WATER(1f, 0, 1.25f, 24),
	
	NO_SPEC(1, 1, 0, 1);
	
	public float ambianceIntensity;
	public float diffuseIntensity;
	public float specularIntensity;
	public float shininess;
	
	private Material(float ambianceIntensity, float diffuseIntensity, float specularIntensity, int shininess) {
		this.ambianceIntensity = ambianceIntensity;
		this.diffuseIntensity = diffuseIntensity;
		this.specularIntensity = specularIntensity;
		this.shininess = shininess;
	}
	
}