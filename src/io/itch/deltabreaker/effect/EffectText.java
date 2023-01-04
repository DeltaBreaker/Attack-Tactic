package io.itch.deltabreaker.effect;

import java.util.Random;

import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;

public class EffectText extends Effect {

	public static final Vector3f ROTATION = new Vector3f(-60, 0, 0);

	public String text;
	public Vector4f color;
	public float loop;

	public EffectText(String text, Vector3f position, Vector4f color) {
		super(position, new Vector3f(-60, 0, 0), new Vector3f(0.25f, 0.25f, 0.25f));
		this.text = text;
		this.color = color.copy();
		loop = new Random().nextInt(360);
	}

	@Override
	public void tick() {
		if (color.getW() <= 0) {
			remove = true;
		} else {
			color.setW(color.getW() - 0.005f);
		}
		loop += 1.5f;
		position.setY(position.getY() + 0.05f);
	}

	@Override
	public void render() {
		float alpha = color.getW();
		float angle = (float) Math.sin(Math.toRadians(loop)) * 3;
		TextRenderer.render("zz", text, Vector3f.add(Vector3f.mul(position, 2, 2, 2), angle, 0, 0), ROTATION, scale, color, false);
		TextRenderer.render("zz", text, Vector3f.add(Vector3f.mul(position, 2, 2, 2), -1 + angle, -0.75f, 0), ROTATION, scale, new Vector4f(0, 0, 0, alpha), false);
		TextRenderer.render("zz", text, Vector3f.add(Vector3f.mul(position, 2, 2, 2), 1 + angle, -0.75f, 0), ROTATION, scale, new Vector4f(0, 0, 0, alpha), false);
		TextRenderer.render("zz", text, Vector3f.add(Vector3f.mul(position, 2, 2, 2), angle, -0.25f, -0.75f), ROTATION, scale, new Vector4f(0, 0, 0, alpha), false);
		TextRenderer.render("zz", text, Vector3f.add(Vector3f.mul(position, 2, 2, 2), angle, -1.25f, 0.75f), ROTATION, scale, new Vector4f(0, 0, 0, alpha), false);
	}

	@Override
	public void cleanUp() {

	}

}
