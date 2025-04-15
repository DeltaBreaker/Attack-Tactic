package io.itch.deltabreaker.object.item;

import java.util.Random;

import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.state.StateManager;

public class Item {

	public Vector3f position;
	public Vector3f rotation;
	public int locX, locY;
	public ItemProperty item;

	private int bob = 0;
	private int rot = 0;

	private int rotTimer = 0;
	private int rotTime;
	public boolean rotNow = false;

	public Item(Vector3f position, ItemProperty item) {
		this.position = position;
		locX = (int) position.getX() / 16;
		locY = (int) position.getZ() / 16;
		this.item = item;
		rotTime = new Random().nextInt(1200) + 1200;
		rotation = new Vector3f(-Startup.camera.getRotation().getX() + 360 * AdvMath.sin[rot / 4], -Startup.camera.getRotation().getY() + 360 * AdvMath.sin[rot / 4], -Startup.camera.getRotation().getZ());
	}

	public void tick() {
		position = new Vector3f(position.getX(), 16, position.getZ());
		if (bob < 359) {
			bob++;
		} else {
			bob = 0;
		}
		if (rotNow) {
			if (rot < 359) {
				rot++;
			} else {
				rot = 0;
				rotNow = false;
			}
		} else {
			if (rotTimer < rotTime) {
				rotTimer++;
			} else {
				rotTimer = 0;
				rotNow = true;
				rotTime = new Random().nextInt(1200) + 1200;
			}
		}
		rotation.set(-Startup.camera.getRotation().getX() + 360 * AdvMath.sin[rot / 4], -Startup.camera.getRotation().getY() + 360 * AdvMath.sin[rot / 4], -Startup.camera.getRotation().getZ());
	}

	public void render() {
		if (item.type.equals(ItemProperty.TYPE_GEM_ABILITY)) {
			BatchSorter.add("u", item.model, item.texture, "main_3d", item.material.toString(),
					Vector3f.add(Vector3f.add(position, 0, (float) (Math.sin(Math.toRadians(bob)) * 1.5), 0), 0, StateManager.currentState.tiles[locX][locY].getPosition().getY(), 0), rotation, Vector3f.SCALE_HALF,
					(item.type.equals(ItemProperty.TYPE_GEM_ABILITY)) ? ItemAbility.valueOf(item.abilities[0]).getColor() : Vector4f.COLOR_BASE, true, false);
			ItemAbility.valueOf(item.abilities[0]).getColor().printValues();
		}
		if (item.stack > 1) {
			TextRenderer.render("" + item.stack, Vector3f.add(Vector3f.add(position, 6, (float) (Math.sin(Math.toRadians(bob)) * 1.5 - 1.75), 4.75f), 0, StateManager.currentState.tiles[locX][locY].getPosition().getY(), 0),
					Vector3f.DEFAULT_CAMERA_ROTATION, Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, false);
		}
	}

}
