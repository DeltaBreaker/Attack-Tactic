package io.itch.deltabreaker.effect;

import java.awt.Point;
import java.util.ArrayList;

import io.itch.deltabreaker.core.SettingsManager;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Light;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.tile.Tile;
import io.itch.deltabreaker.state.StateManager;

public class EffectLava extends EffectWater {

	private static Vector3f color = new Vector3f(Vector4f.COLOR_LAVA.getX(), Vector4f.COLOR_LAVA.getY(), Vector4f.COLOR_LAVA.getZ());
	private Effect heated;
	private Light light;

	public EffectLava(Tile t, Tile[][] tiles, ArrayList<Point> points) {
		super(t, color, tiles, points);
		this.t = t;

		heated = new EffectHeated(Vector3f.add(Vector3f.div(position, 2, 2, 2), 0, -5, 0), Vector4f.COLOR_LAVA_HEAT);
		StateManager.currentState.effects.add(heated);

		t.setWaterLogged(false);
		t.setLavaLogged(true);
		
		if ((int) position.getX() % 2 == 0 && (int) position.getZ() % 2 == 0) {
			light = new Light(Vector3f.add(Vector3f.div(position, 2, 2, 2), 0, 4, 0), new Vector3f(2f, 1.25f, 0f), 1.5f, 1f, 0.075f, null);
			StateManager.currentState.lights.add(light);
		}
	}

	@Override
	public void render() {
		if (canRender) {
			if (SettingsManager.enableFancyWater) {
				boolean renderOrder = Startup.camera.position.getX() * 2 < position.getX();
				positions.clear();
				for (int y = water[0].length - 1; y >= 0; y--) {
					if (renderOrder) {
						for (int x = 0; x < water.length; x++) {
							positions.add(water[x][y].renderPosition);
						}
					} else {
						for (int x = water.length - 1; x >= 0; x--) {
							positions.add(water[x][y].renderPosition);
						}
					}
				}
				BatchSorter.addLiquidBatch("z", "pixel.dae", "pixel.png", "main_3d_lava", Material.DEFAULT.toString(), positions);
			} else {
				Vector3f position = Vector3f.add(this.position, 0,
						heights[0][0] + noise[0][0] * height * AdvMath.sin[(int) (Startup.universalAge + (this.position.getX()) * spacingDevisor + (this.position.getZ()) * spacingDevisor) % AdvMath.values], 0);
				BatchSorter.add("z", "pixel.dae", "pixel.png", "main_3d_bloom", Material.DEFAULT.toString(), Vector3f.div(Vector3f.add(position, 0, 6, 0), Vector3f.SCALE_16X), Vector3f.EMPTY, Vector3f.SCALE_8X, Vector4f.mul(Vector4f.COLOR_LAVA, new Vector4f(0.5f, 0.5f, 0.5f, 1)), false, false);
			}
		}
	}

	@Override
	public void cleanUp() {
		StateManager.currentState.lights.remove(light);
		t.setLavaLogged(false);
		t.movementPenalty = Math.max(t.movementPenalty - 1, 1);
		heated.remove = true;
		task.finish();
	}

}