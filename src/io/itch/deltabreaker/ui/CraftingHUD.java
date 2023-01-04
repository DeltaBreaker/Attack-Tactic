package io.itch.deltabreaker.ui;

import java.util.Random;

import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.item.ItemProperty;

public class CraftingHUD {

	public static float xOffset = 0;
	public static float yOffset = 0;
	public static float tollerance = 0.15f;
	public static float seed = 0;

	public ItemProperty item;
	public Vector3f position = new Vector3f(0, 0, -40);
	public Vector3f boxPosition = new Vector3f(-6, 6, -41);
	public Vector3f ballPosition = new Vector3f(0, -7, -15);

	public Vector3f offset1;

	public float xSpeed = 0;
	public float ySpeed = 0;

	public boolean complete = false;

	public CraftingHUD(ItemProperty item) {
		this.item = item;
		seed = new Random().nextFloat();

		offset1 = new Vector3f(new Random().nextInt(360), new Random().nextInt(360), 0);

		updateOffsets();
	}

	public void tick() {
		if (!complete) {
			rotate(xSpeed, ySpeed);
			if (Math.abs(xOffset) + Math.abs(yOffset) < tollerance) {
				complete = true;
				offset1.set(0, 0, 0);
			}
		}
	}

	public void render() {
		boxPosition.set(-8, 8, -41);
		UIBox.render(boxPosition, 24, 24);
		BatchSorter.add(item.model, item.texture, "static_3d_crafting", item.material, position, Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, false, true, false);
	}

	private void rotate(float x1, float y1) {
		offset1.add(x1, y1, 0);

		if (offset1.getX() < 0) {
			offset1.setX(offset1.getX() + 360);
		}
		if (offset1.getX() > 359) {
			offset1.setX(offset1.getX() - 360);
		}
		if (offset1.getY() < 0) {
			offset1.setY(offset1.getY() + 360);
		}
		if (offset1.getY() > 359) {
			offset1.setY(offset1.getY() - 360);
		}

		updateOffsets();
	}

	private void updateOffsets() {
		xOffset = 2 * AdvMath.sin[(int) offset1.getX()];
		yOffset = 2 * AdvMath.sin[(int) offset1.getY()];
	}

}
