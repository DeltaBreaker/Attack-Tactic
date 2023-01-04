package io.itch.deltabreaker.ui;

import java.awt.Dimension;

import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;

public class Message extends UIBox {

	private String[] text;
	private boolean seen = false;
	public boolean remove = false;

	public float endPosition = -46;
	public float startPosition = -80;
	public float y;

	public Message(String[] text) {
		super(Vector3f.add(Vector3f.mul(Startup.staticView.position, 2, 2, 2), 4 - getDimensions(text).width / 2, getDimensions(text).height + 2, -80),
				getDimensions(text).width, getDimensions(text).height);
		this.text = text;
		y = startPosition;
		position.setY(Startup.staticView.position.getY() * 2 + y + height - 4);
	}

	public void tick() {
		if (!seen) {
			if (y < endPosition) {
				y = Math.min(y + 1f, endPosition);
			}
		} else {
			if (y > startPosition) {
				y = Math.max(y - 1f, startPosition);
			} else {
				remove = true;
			}
		}
		position.setX(Startup.staticView.position.getX() * 2 + 4 - width / 2);
		position.setY(Startup.staticView.position.getY() * 2 + y + height - 4);
	}

	public void render() {
		super.render();
		for(int i = 0; i < text.length; i++) {
			TextRenderer.render(text[i], Vector3f.add(position, 4, -6 - i * 8, 1), Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, true);
		}
	}

	public void close() {
		seen = true;
	}

	protected static Dimension getDimensions(String[] text) {
		String longest = "";
		int height = 10;
		int times = 0;
		for (String s : text) {
			if (s.length() > longest.length()) {
				longest = s;
			}
			if (times < 5) {
				height += 8;
				times++;
			}
		}
		return new Dimension(longest.length() * 6 + 9, height);
	}

}
