package io.itch.deltabreaker.effect;

import java.awt.Point;
import java.util.ArrayList;

import com.flowpowered.noise.model.Plane;
import com.flowpowered.noise.module.source.Perlin;

import io.itch.deltabreaker.core.SettingsManager;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Matrix4f;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.multiprocessing.TaskThread;
import io.itch.deltabreaker.multiprocessing.WorkerTask;
import io.itch.deltabreaker.object.tile.Tile;

public class EffectWater extends Effect {

	public static final int BLOCK_SIZE = 16;
	public static final float MULTIPLIER = BLOCK_SIZE / 16f;

	public Vector4f color;
	public WaterBlock[][] water = new WaterBlock[BLOCK_SIZE][BLOCK_SIZE];
	public float[][] noise = new float[BLOCK_SIZE][BLOCK_SIZE];

	public static float spacing = 38;
	public static float height = 2;
	public static float spacingDevisor = 360 / spacing;

	public float[][] heights = new float[BLOCK_SIZE][BLOCK_SIZE];

	public boolean canRender = true;

	public Tile t;

	protected WorkerTask task = new WorkerTask() {
		@Override
		public void tick() {
			if (SettingsManager.enableFancyWater && canRender) {
				for (int x = 0; x < water.length; x++) {
					for (int y = 0; y < water[x].length; y++) {
						water[x][y].renderPosition.setY(
								(position.getY() + 14 + heights[x][y] + noise[x][y] * height * AdvMath.sin[(int) (Startup.universalAge + (x + position.getX()) * spacingDevisor + (y + position.getZ()) * spacingDevisor) % AdvMath.values])
										* 0.5f);
					}
				}
			}
		}
	};

	public EffectWater(Tile t, Vector3f color, Tile[][] tiles, ArrayList<Point> points) {
		super(t.getPosition(), Vector3f.EMPTY, Vector3f.SCALE_HALF);
		this.color = new Vector4f(color, 0.6f);
		this.t = t;
		t.setWaterLogged(true);
		t.movementPenalty += 1;

		Perlin perlin = new Perlin();
		perlin.setSeed((int) 1);
		perlin.setPersistence(0.8 - MULTIPLIER * 0.2);
		Plane noise = new Plane(perlin);

		for (int x = 0; x < water.length; x++) {
			for (int y = 0; y < water[x].length; y++) {
				water[x][y] = new WaterBlock(Vector3f.add(Vector3f.mul(position, MULTIPLIER, MULTIPLIER, MULTIPLIER), x + 0.5f - 8, MULTIPLIER + 12, y + 0.5f - 8), Vector3f.SCALE_HALF);
				this.noise[x][y] = (float) noise.getValue((position.getX() * MULTIPLIER + x) / (100.0 / MULTIPLIER), (position.getZ() * MULTIPLIER + y) / (100.0 / MULTIPLIER));
			}
		}

		for (int x = 0; x < heights.length; x++) {
			if (containsPoint(points, (int) position.getX() / 16, (int) position.getZ() / 16 - 1)) {
				heights[x][0] = Math.min(4, Math.max(heights[x][0], tiles[(int) position.getX() / 16][(int) position.getZ() / 16 - 1].getPosition().getY() - position.getY()));
			}
			if (containsPoint(points, (int) position.getX() / 16, (int) position.getZ() / 16 + 1)) {
				heights[x][heights[0].length - 1] = Math.min(4, Math.max(heights[x][heights[0].length - 1], tiles[(int) position.getX() / 16][(int) position.getZ() / 16 + 1].getPosition().getY() - position.getY()));
			}
		}
		for (int y = 0; y < heights[0].length; y++) {
			if (containsPoint(points, (int) position.getX() / 16 - 1, (int) position.getZ() / 16)) {
				heights[0][y] = Math.min(4, Math.max(heights[0][y], tiles[(int) position.getX() / 16 - 1][(int) position.getZ() / 16].getPosition().getY() - position.getY()));
			}
			if (containsPoint(points, (int) position.getX() / 16 + 1, (int) position.getZ() / 16)) {
				heights[heights.length - 1][y] = Math.min(4, Math.max(heights[heights.length - 1][y], tiles[(int) position.getX() / 16 + 1][(int) position.getZ() / 16].getPosition().getY() - position.getY()));
			}
		}
		if (containsPoint(points, (int) position.getX() / 16 - 1, (int) position.getZ() / 16 - 1)) {
			heights[0][0] = Math.min(4, Math.max(heights[0][0], tiles[(int) position.getX() / 16 - 1][(int) position.getZ() / 16 - 1].getPosition().getY() - position.getY()));
		}
		if (containsPoint(points, (int) position.getX() / 16 + 1, (int) position.getZ() / 16 - 1)) {
			heights[heights.length - 1][0] = Math.min(4, Math.max(heights[heights.length - 1][0], tiles[(int) position.getX() / 16 + 1][(int) position.getZ() / 16 - 1].getPosition().getY() - position.getY()));
		}
		if (containsPoint(points, (int) position.getX() / 16 - 1, (int) position.getZ() / 16 + 1)) {
			heights[0][heights[0].length - 1] = Math.min(4, Math.max(heights[0][heights[0].length - 1], tiles[(int) position.getX() / 16 - 1][(int) position.getZ() / 16 + 1].getPosition().getY() - position.getY()));
		}
		if (containsPoint(points, (int) position.getX() / 16 + 1, (int) position.getZ() / 16 + 1)) {
			heights[heights.length - 1][heights[0].length - 1] = Math.min(4,
					Math.max(heights[heights.length - 1][heights[0].length - 1], tiles[(int) position.getX() / 16 + 1][(int) position.getZ() / 16 + 1].getPosition().getY() - position.getY()));
		}

		for (int i = 0; i < BLOCK_SIZE * 2; i++) {
			for (int x = 0; x < heights.length; x++) {
				for (int y = 0; y < heights[0].length; y++) {
					float total = 0;
					int amt = 0;

					if (x != 0) {
						total += heights[x - 1][y];
						amt++;
					}
					if (y != 0) {
						total += heights[x][y - 1];
						amt++;
					}
					if (x < heights.length - 1) {
						total += heights[x + 1][y];
						amt++;
					}
					if (y < heights[0].length - 1) {
						total += heights[x][y + 1];
						amt++;
					}
					if (!(x == 0 && y == 0) && !(x == 0 && y == heights[0].length - 1) && !(x == heights.length - 1 && y == 0) && !(x == heights.length - 1 && y == heights[0].length - 1)) {
						heights[x][y] = Math.max(heights[x][y], total / amt);
					}
				}
			}
		}

		TaskThread.process(task);
	}

	@Override
	public void tick() {
		canRender = Startup.camera.isInsideView(position, 0.5f, 0.5f, 0.5f, 8);
	}

	@Override
	public void render() {
		if (canRender) {
			if (SettingsManager.enableFancyWater) {
				boolean renderOrder = Startup.camera.position.getX() * 2 < position.getX();
				if (renderOrder) {
					for (int y = water[0].length - 1; y >= 0; y--) {
						for (int x = 0; x < water.length; x++) {
							BatchSorter.addLiquidPosition("main_3d_water", water[x][y].renderPosition);
						}
					}
				} else {
					for (int y = water[0].length - 1; y >= 0; y--) {
						for (int x = water.length - 1; x >= 0; x--) {
							BatchSorter.addLiquidPosition("main_3d_water", water[x][y].renderPosition);
						}
					}
				}
			} else {
				Vector3f position = Vector3f.add(this.position, 0,
						heights[0][0] + noise[0][0] * height * AdvMath.sin[(int) (Startup.universalAge + (this.position.getX()) * spacingDevisor + (this.position.getZ()) * spacingDevisor) % AdvMath.values], 0);
				BatchSorter.add("z", "pixel.dae", "pixel.png", "main_3d_bloom", Material.WATER.toString(), Vector3f.div(Vector3f.add(position, 0, 6, 0), Vector3f.SCALE_16X), Vector3f.EMPTY, Vector3f.SCALE_8X, color, false, false);
			}
		}

	}

	@Override
	public void cleanUp() {
		t.setWaterLogged(false);
		t.movementPenalty = Math.max(t.movementPenalty - 1, 1);
		task.finish();
	}

	private static boolean containsPoint(ArrayList<Point> point, int x, int y) {
		boolean contains = false;
		for (Point p : point) {
			if (p.x == x && p.y == y) {
				contains = true;
				break;
			}
		}
		return contains;
	}

}

class WaterBlock {

	public Vector4f renderPosition;

	public WaterBlock(Vector3f position, Vector3f scale) {
		Matrix4f precalc = Matrix4f.transform(position, Vector3f.EMPTY, scale);
		renderPosition = new Vector4f(precalc.get(3, 0), precalc.get(3, 1), precalc.get(3, 2), precalc.get(3, 3));
		Matrix4f.release(precalc);
	}

}