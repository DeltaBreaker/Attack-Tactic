package io.itch.deltabreaker.effect.dungeon;

import java.util.ArrayList;
import java.util.Random;

import io.itch.deltabreaker.core.SettingsManager;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.effect.Effect;
import io.itch.deltabreaker.effect.EffectThunder;
import io.itch.deltabreaker.effect.EffectWaterSplash;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Light;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Matrix4f;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.tile.Tile;
import io.itch.deltabreaker.state.StateManager;

public class EffectDungeonRain extends Effect {

	private static final int SPAWN_LIMIT = 1000;

	private Light mainLight;
	private Vector3f color = new Vector3f(20, 20, 20);

	private ArrayList<RainDrop> snowflakes = new ArrayList<>();

	private int spawnTimer = 0;
	private int spawnRate = 0;

	private int flashTimer = 0;
	private int baseFlashRate = 720;
	private int flashRateVariance = 2880;
	private int flashRate = baseFlashRate;
	private float doubleFlashChance = 0.1f;
	private int doubleFlashRate = 72;
	private int doubleFlashRateVariance = 144;
	private int thunderSfx = 0;
	private int thunderSfxMax = 1;

	private ArrayList<Matrix4f> matrices = new ArrayList<>();
	private ArrayList<Vector4f> colors = new ArrayList<>();

	public EffectDungeonRain(Light mainLight) {
		super(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(0, 0, 0));
		this.mainLight = mainLight;
		if (!AudioManager.getSound("rain.ogg").isPlaying()) {
			AudioManager.getSound("rain.ogg").play(0, true);
		}
		AudioManager.getSound("rain.ogg").fade(AudioManager.defaultSubSFXGain, 144, true);
	}

	public void tick() {
		if (spawnTimer < spawnRate) {
			spawnTimer++;
		} else if (snowflakes.size() < SPAWN_LIMIT) {
			spawnTimer = 0;
			snowflakes.add(new RainDrop());
			snowflakes.add(new RainDrop());
			snowflakes.add(new RainDrop());
			snowflakes.add(new RainDrop());
		}
		for (int i = 0; i < snowflakes.size(); i++) {
			snowflakes.get(i).tick();
			if (snowflakes.get(i).remove) {
				Matrix4f.release(snowflakes.get(i).precalc);
				snowflakes.remove(i);
				i--;
			}
		}
		if (flashTimer < flashRate) {
			flashTimer++;
		} else {
			flashTimer = 0;
			StateManager.currentState.effects.add(new EffectThunder(new Light(mainLight.position, color.copy(), mainLight.linear, mainLight.constant, mainLight.quadratic, null), 0.009f));

			AudioManager.getSound("thunder_" + thunderSfx + ".ogg").play(AudioManager.defaultSubSFXGain, false);
			if (thunderSfx < thunderSfxMax) {
				thunderSfx++;
			} else {
				thunderSfx = 0;
			}

			if (new Random().nextFloat() > doubleFlashChance) {
				flashRate = baseFlashRate + new Random().nextInt(flashRateVariance);
			} else {
				flashRate = doubleFlashRate + new Random().nextInt(doubleFlashRateVariance);
			}
		}
	}

	public void render() {
		matrices.clear();
		colors.clear();
		for (RainDrop s : snowflakes) {
			if (s.position.getZ() - 16 < Startup.camera.position.getZ() * 2 && Vector3f.distance(s.position, Startup.camera.position) < 96) {
				matrices.add(s.precalc);
				colors.add(Vector4f.COLOR_WATER);
			}
		}
		BatchSorter.addBatch("pixel.dae", "pixel.png", "main_3d_bloom", Material.WATER.toString(), matrices, colors, true, false, false);
	}

	@Override
	public void cleanUp() {
		snowflakes.clear();
		AudioManager.getSound("rain.ogg").fade(0, 144, true);
	}

}

class RainDrop {

	public boolean remove = false;

	public Vector3f position;
	public Vector3f scale;
	public Vector3f speed = new Vector3f(0, -2, 0);

	public Matrix4f precalc;

	public float alpha = 0.5f;

	public RainDrop() {
		scale = new Vector3f(0.5f, 0.5f, 0.5f).mul(new Random().nextFloat() + 0.5f);
		position = new Vector3f(new Random().nextInt(StateManager.currentState.tiles.length * 16) - StateManager.currentState.tiles.length * 4, Startup.camera.position.getY() + new Random().nextInt(64),
				new Random().nextInt(StateManager.currentState.tiles[0].length * 16));
		speed.add(0, new Random().nextFloat(), 0);

		precalc = Matrix4f.transform(position, Vector3f.EMPTY, scale);
	}

	public void tick() {
		Tile t = StateManager.currentState.tiles[AdvMath.inRange((int) (position.getX()) / 8, 0, StateManager.currentState.tiles.length - 1)][AdvMath.inRange((int) (position.getZ()) / 8, 0, StateManager.currentState.tiles[0].length - 1)];
		if (position.getY() > (t.getPosition().getY() - t.getOffset().getY()) / 2 + 9f) {
			position.add(speed);
		} else {
			remove = true;
			float distance = Vector3f.distance(position, Startup.camera.position);
			if (distance < 96 && position.getZ() < Startup.camera.position.getZ() && SettingsManager.enableFancyRain) {
				StateManager.currentState.effects.add(new EffectWaterSplash(Vector3f.add(Vector3f.mul(position, 2, 2, 2), 0, -9, 0), false));
			}
		}

		precalc.set(3, position.getX());
		precalc.set(7, position.getY());
		precalc.set(11, position.getZ());

		precalc.set(0, scale.getX());
		precalc.set(5, scale.getY());
		precalc.set(10, scale.getZ());
	}

}