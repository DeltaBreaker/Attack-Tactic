package io.itch.deltabreaker.ui;

import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;

public class UIBox {

	public Vector3f position;
	public static Vector3f rotation = new Vector3f(0, 0, 0);
	public static Vector3f scale = new Vector3f(0.5f, 0.5f, 0.5f);
	public int width, height;

	public UIBox(Vector3f position, int width, int height) {
		this.position = position;
		this.width = width;
		this.height = height;
	}

	public void render() {
		if (width >= 16 && height >= 16) {
			BatchSorter.add("gui_top_left.dae", "gui_top_left.png", "static_3d", Material.DEFAULT.toString(), position, rotation, scale, new Vector4f(1, 1, 1, 1), false, true);
			BatchSorter.add("a", "gui_top_right.dae", "gui_top_right.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, width - 8, 0, 0), rotation, scale, new Vector4f(1, 1, 1, 1), false, true);
			BatchSorter.add("gui_bottom_left.dae", "gui_bottom_left.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, 0, -height + 8, 0), rotation, scale, new Vector4f(1, 1, 1, 1), false, true);
			BatchSorter.add("a", "gui_bottom_right.dae", "gui_bottom_right.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, width - 8, -height + 8, 0), rotation, scale, new Vector4f(1, 1, 1, 1), false, true);
			for (int x = 0; x < width / 8 - 1; x++) {
				BatchSorter.add("gui_top.dae", "gui_top.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, 8 + 8 * x, 0, 0), rotation, scale, new Vector4f(1, 1, 1, 1), false, true);
				BatchSorter.add("gui_bottom.dae", "gui_bottom.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, 8 + 8 * x, -height + 8, 0), rotation, scale, new Vector4f(1, 1, 1, 1), false, true);
			}
			for (int y = 0; y < height / 8 - 1; y++) {
				BatchSorter.add("gui_left.dae", "gui_left.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, 0, -y * 8 - 8, 0), rotation, scale, new Vector4f(1, 1, 1, 1), false, true);
				BatchSorter.add("a", "gui_right.dae", "gui_right.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, width - 8, -y * 8 - 8, 0), rotation, scale, new Vector4f(1, 1, 1, 1), false, true);
			}
			for (int x = 0; x < width / 8 - 1; x++) {
				for (int y = 0; y < height / 8 - 1; y++) {
					BatchSorter.add("gui_middle.dae", "gui_middle.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, 8 + 8 * x, -y * 8 - 8, 0), rotation, scale, new Vector4f(1, 1, 1, 1), false, true);
				}
			}
		}
	}

	public static void render(Vector3f position, int width, int height) {
		if (width >= 16 && height >= 16) {
			BatchSorter.add("gui_top_left.dae", "gui_top_left.png", "static_3d", Material.DEFAULT.toString(), position, rotation, scale, new Vector4f(1, 1, 1, 1), false, true);
			BatchSorter.add("a", "gui_top_right.dae", "gui_top_right.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, width - 8, 0, 0), rotation, scale, new Vector4f(1, 1, 1, 1), false, true);
			BatchSorter.add("gui_bottom_left.dae", "gui_bottom_left.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, 0, -height + 8, 0), rotation, scale, new Vector4f(1, 1, 1, 1), false, true);
			BatchSorter.add("a", "gui_bottom_right.dae", "gui_bottom_right.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, width - 8, -height + 8, 0), rotation, scale, new Vector4f(1, 1, 1, 1), false, true);
			for (int x = 0; x < width / 8 - 1; x++) {
				BatchSorter.add("gui_top.dae", "gui_top.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, 8 + 8 * x, 0, 0), rotation, scale, new Vector4f(1, 1, 1, 1), false, true);
				BatchSorter.add("gui_bottom.dae", "gui_bottom.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, 8 + 8 * x, -height + 8, 0), rotation, scale, new Vector4f(1, 1, 1, 1), false, true);
			}
			for (int y = 0; y < height / 8 - 1; y++) {
				BatchSorter.add("gui_left.dae", "gui_left.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, 0, -y * 8 - 8, 0), rotation, scale, new Vector4f(1, 1, 1, 1), false, true);
				BatchSorter.add("a", "gui_right.dae", "gui_right.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, width - 8, -y * 8 - 8, 0), rotation, scale, new Vector4f(1, 1, 1, 1), false, true);
			}
			for (int x = 0; x < width / 8 - 1; x++) {
				for (int y = 0; y < height / 8 - 1; y++) {
					BatchSorter.add("gui_middle.dae", "gui_middle.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, 8 + 8 * x, -y * 8 - 8, 0), rotation, scale, new Vector4f(1, 1, 1, 1), false, true);
				}
			}
		}
	}

	public static void render(Vector3f position, int width, int height, Vector4f color) {
		if (width >= 16 && height >= 16) {
			BatchSorter.add("gui_top_left.dae", "gui_top_left.png", "static_3d", Material.DEFAULT.toString(), position, rotation, scale, color, true, true);
			BatchSorter.add("a", "gui_top_right.dae", "gui_top_right.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, width - 8, 0, 0), rotation, scale, color, true, true);
			BatchSorter.add("gui_bottom_left.dae", "gui_bottom_left.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, 0, -height + 8, 0), rotation, scale, color, true, true);
			BatchSorter.add("a", "gui_bottom_right.dae", "gui_bottom_right.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, width - 8, -height + 8, 0), rotation, scale, color, true, true);
			for (int x = 0; x < width / 8 - 1; x++) {
				BatchSorter.add("gui_top.dae", "gui_top.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, 8 + 8 * x, 0, 0), rotation, scale, color, true, true);
				BatchSorter.add("gui_bottom.dae", "gui_bottom.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, 8 + 8 * x, -height + 8, 0), rotation, scale, color, true, true);
			}
			for (int y = 0; y < height / 8 - 1; y++) {
				BatchSorter.add("gui_left.dae", "gui_left.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, 0, -y * 8 - 8, 0), rotation, scale, color, true, true);
				BatchSorter.add("a", "gui_right.dae", "gui_right.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, width - 8, -y * 8 - 8, 0), rotation, scale, color, true, true);
			}
			for (int x = 0; x < width / 8 - 1; x++) {
				for (int y = 0; y < height / 8 - 1; y++) {
					BatchSorter.add("gui_middle.dae", "gui_middle.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, 8 + 8 * x, -y * 8 - 8, 0), rotation, scale, color, true, true);
				}
			}
		}
	}

}
