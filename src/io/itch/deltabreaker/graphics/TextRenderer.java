package io.itch.deltabreaker.graphics;

import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;

public class TextRenderer {

	public static final String ALLOWED = "abcdefghijklmnopqrstuvwxyz0123456789+-_@.%!&";

	public static void render(String text, Vector3f position, Vector3f rotation, Vector3f scale, Vector4f color, boolean staticView) {
		char[] string = text.toLowerCase().toCharArray();
		for (int i = 0; i < string.length; i++) {
			if (ALLOWED.contains("" + string[i])) {
				BatchSorter.add(string[i] + ".dae", string[i] + ".png", (staticView) ? "static_3d" : "main_3d_nobloom_texcolor", Material.DEFAULT.toString(), Vector3f.add(position, i * 6, 0, 0), Vector3f.add(rotation, 0, 180, 0), scale, color,
						false, staticView);
			}
		}
	}

	public static void render(String priority, String text, Vector3f position, Vector3f rotation, Vector3f scale, Vector4f color, boolean staticView) {
		char[] string = text.toLowerCase().toCharArray();
		for (int i = 0; i < string.length; i++) {
			if (ALLOWED.contains("" + string[i])) {
				BatchSorter.add(priority, string[i] + ".dae", string[i] + ".png", (staticView) ? "static_3d" : "main_3d_nobloom_texcolor", Material.DEFAULT.toString(), Vector3f.add(position, i * 6, 0, 0), Vector3f.add(rotation, 0, 180, 0), scale,
						color, false, staticView);
			}
		}
	}

}
