package io.itch.deltabreaker.effect.battle;

import java.util.ArrayList;

import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Light;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.state.StateDungeon;
import io.itch.deltabreaker.state.StateManager;

public class EffectCoupDeGrace extends EffectBattle {

	public Light light;
	public ArrayList<EffectBattleSpark> sparks = new ArrayList<>();

	public int sparkAmount = 50;

	public boolean dir;

	private Unit unit;

	private int delayTimer = 0;
	private int delayTime = 144;

	public EffectCoupDeGrace(boolean dir, Unit unit) {
		super(new Vector3f(unit.x, 16 + unit.height, unit.y), new Vector3f(60, 0, 0));
		frames = 13;
		frameTime = 5;
		light = new Light(Vector3f.mul(this.position.add(new Vector3f(0, 1, 0)), new Vector3f(0.5f, 0.5f, 0.5f)), new Vector3f(6f, 2.5f, 0f), 1f, 0.002f, 3f, null);
		StateManager.currentState.lights.add(light);
		this.dir = dir;
		this.unit = unit;
		Startup.camera.shake(0, 0, 0);
		unit.poseLock = true;
		unit.dir = 0;
		unit.frame = 1;
	}

	public void tick() {
		if (delayTimer < delayTime) {
			delayTimer++;
		} else {
			if (frameTimer == 0 && frame == 0) {
				unit.poseLock = true;
				unit.dir = 0;
				unit.frame = 2;
				Startup.camera.shake(5f, 0, 5f);
				AudioManager.getSound("battle_fire_medium.ogg").play(AudioManager.defaultBattleSFXGain, false);
				
				int damage = unit.currentHp - 1;
				unit.hurt(damage);
				
				for(Unit u : StateDungeon.getCurrentContext().enemies) {
					if(Math.abs(u.locX - unit.locX) + Math.abs(u.locY - unit.locY) <= 4) {
						u.hurt(damage);
					}
				}
				for(Unit u : Inventory.active) {
					if(u != unit && Math.abs(u.locX - unit.locX) + Math.abs(u.locY - unit.locY) <= 4) {
						u.hurt(damage);
					}
				}
			}
			if (frameTimer < frameTime) {
				frameTimer++;
			} else {
				frameTimer = 0;
				if (frame < frames) {
					frame++;
					onFrame();
				}
			}
		}
		if (frame >= 4) {
			light.color.setX(Math.max(0, light.color.getX() - 0.1f));
			light.color.setY(Math.max(0, light.color.getY() - 0.05f));
		} else {
			if (light.quadratic > 0.02f) {
				light.quadratic -= 0.02f;
			}
		}
		for (int i = 0; i < sparks.size(); i++) {
			sparks.get(i).tick();
			if (sparks.get(i).remove) {
				sparks.remove(i);
				i--;
			}
		}
		if (frame == frames && sparks.size() == 0) {
			remove = true;
			unit.poseLock = false;
			unit.setTurn(false);
			StateManager.currentState.controlLock = false;
			StateManager.currentState.hideCursor = false;
			StateDungeon.getCurrentContext().hideInfo = false;
		}
	}

	public void onFrame() {
		if (frame == 3 && StateManager.currentState.effects.size() < 100) {
			for (int i = 0; i < sparkAmount; i++) {
				sparks.add(new EffectBattleSpark(Vector3f.mul(Vector3f.add(position, 0, -12, 2), 0.5f, 0.5f, 0.5f), new Vector3f(3f, 1f, 0f), 0.005f));
			}
		}
	}

	@Override
	public void render() {
		if (frame < frames - 1 && delayTimer == delayTime) {
			BatchSorter.add("z", "effect_battle_fire_1_" + frame + ".dae", "effect_battle_fire_1_" + frame + ".png", "main_3d", "DEFAULT", Vector3f.add(position, -1, 8, -16).mul(0.5f), rotation, new Vector3f(1, 1, 1), new Vector4f(1, 1, 1, 1), true, false);
		
		}
		for (int i = 0; i < sparks.size(); i++) {
			sparks.get(i).render();
		}
	}

	@Override
	public void cleanUp() {
		StateManager.currentState.lights.remove(light);
		sparks.clear();
	}

}
