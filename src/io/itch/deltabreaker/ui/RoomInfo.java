package io.itch.deltabreaker.ui;

import java.awt.Dimension;

import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;

public class RoomInfo extends UIBox {

	private String[] details;

	public RoomInfo(Vector3f position, String[] details) {
		super(position, getDimensions(details).width, getDimensions(details).height);
		this.details = details;
	}

	protected static Dimension getDimensions(String[] text) {
		String longest = "";
		int height = 10;
		for (String s : text) {
			if (s.length() > longest.length()) {
				longest = s;
			}
			height += 8;
		}
		return new Dimension(longest.length() * 6 + 9, height);
	}

	public void render() {
		Startup.staticView.targetPosition = new Vector3f(position.getX() / 2 - 1 + width / 4, position.getY() / 2 - height / 4, Startup.staticView.position.getZ());
		super.render();
		for (int i = 0; i < details.length; i++) {
			TextRenderer.render(details[i], Vector3f.add(position, 4, -i * 8 - 6, 1), new Vector3f(0, 0, 0), scale, new Vector4f(1, 1, 1, 1), true);
		}
	}

}
