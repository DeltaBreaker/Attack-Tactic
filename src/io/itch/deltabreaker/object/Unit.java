package io.itch.deltabreaker.object;

import java.awt.Point;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import io.itch.deltabreaker.ai.AIHandler;
import io.itch.deltabreaker.ai.AIType;
import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.effect.EffectDebuff;
import io.itch.deltabreaker.effect.EffectEnergize;
import io.itch.deltabreaker.effect.EffectHeated;
import io.itch.deltabreaker.effect.EffectLavaSplash;
import io.itch.deltabreaker.effect.EffectText;
import io.itch.deltabreaker.effect.EffectWaterSplash;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.item.ItemAbility;
import io.itch.deltabreaker.object.item.ItemProperty;
import io.itch.deltabreaker.state.StateDungeon;
import io.itch.deltabreaker.state.StateHub;
import io.itch.deltabreaker.state.StateManager;

public class Unit {

	public static final int HAIR_STYLE_COUNT = 12;

	public static final int TYPE_OWNED = 0;
	public static final int TYPE_HOSTILE = 1;
	public static final int TYPE_FRIENDLY = 2;

	public static final String STATUS_POISON = "unit.status.poison";
	public static final String STATUS_SLEEP = "unit.status.sleep";

	public static ArrayList<String> names = new ArrayList<>();
	public static HashMap<String, float[]> GROWTH_PROFILES = new HashMap<>();

	public static float movementSpeed = 1f;

	public Vector3f position = new Vector3f(0, 0, 0);
	public Vector3f hairPosition = new Vector3f(0, 0, 0);
	public Vector3f armorPosition = new Vector3f(0, 0, 0);
	public Vector3f weaponPosition = new Vector3f(0, 0, 0);
	public Vector3f rotation = new Vector3f(0, 0, 0);

	public int memX = 0;
	public int memY = 0;
	public float memHeight = 0;
	public int locX;
	public int locY;
	public float x;
	public float y;
	public int dir = 0;
	public boolean poseLock = false;
	public float height = 0;
	public float fallSpeed = 0.5f;

	public int frame = 0;
	public int frames = 4;
	public int frameTimer = 0;
	public int frameTime = 23;

	public int waitTime = 80;
	public int waitTimer = 0;
	public boolean wait = false;

	public boolean hasTurn = true;
	public Vector4f unitColor;

	public ArrayList<Point> path = new ArrayList<Point>();

	public boolean inCombat = false;
	public boolean cRetract = false;
	public int cMovement = 0;
	public int cWaitTimer = 0;
	public int cWaitTime = 1;

	public boolean dead = false;

	private String status = "";
	private int statusTimer = 0;
	private EffectHeated statusEffect = new EffectHeated(new Vector3f(0, 0, 0), Vector4f.COLOR_BASE);

	private boolean playFootsteps = true;
	private int footstepFile = 0;
	private int footstepSounds = 4;

	// Customs

	public String name = "";

	public int race = 0;
	public int body = 0;
	public Vector4f bodyColor = new Vector4f(1, 1, 1, 1);
	public static Vector4f[] randBodyColors = { new Vector4f(1, 1, 1, 1), new Vector4f(0.819f, 0.819f, 0.819f, 1), new Vector4f(0.603f, 0.603f, 0.603f, 1), new Vector4f(0.235f, 0.098f, 0, 1) };

	public int hair = 0;
	public Vector4f hairColor = new Vector4f(1, 1, 1, 1);

	public AIType AIPattern;

	// Stats

	public static final int MAX_LEVEL = 50;
	public int level = 1;
	public int exp = 0;

	public int baseMovement = 14;
	public int movement = baseMovement;

	public int baseHp = 20;
	public int baseAtk = 5;
	public int baseMag = 5;
	public int baseSpd = 5;
	public int baseDef = 5;
	public int baseRes = 5;

	public int hp = baseHp;
	public int atk = baseAtk;
	public int mag = baseMag;
	public int spd = baseSpd;
	public int def = baseDef;
	public int res = baseMag;

	public int offsetHp = 0;
	public int offsetAtk = 0;
	public int offsetMag = 0;
	public int offsetSpd = 0;
	public int offsetDef = 0;
	public int offsetRes = 0;

	public int[] affinityXp = { 0, 0, 0, 0, 0 };

	public int currentHp = hp;

	private ArrayList<ItemProperty> items = new ArrayList<ItemProperty>();
	public ItemProperty weapon = ItemProperty.empty;
	public ItemProperty armor = ItemProperty.empty;
	public ItemProperty accessory = ItemProperty.empty;
	public String lastWeapon = weapon.id;

	public String uuid;

	public boolean generated = false;

	public Unit(int x, int y, Vector4f unitColor, String uuid) {
		locX = x;
		locY = y;
		this.x = locX * 16;
		this.y = locY * 16;
		this.unitColor = unitColor;
		unitColor.copy().setW(0);
		rotation = new Vector3f(-Startup.camera.getRotation().getX(), -Startup.camera.getRotation().getY(), -Startup.camera.getRotation().getZ());

		this.uuid = uuid;

		Inventory.loaded.put(uuid, this);
		
		AIPattern = AIType.getDefault();
	}

	public void tick() {
		rotation.set(-Startup.camera.getRotation().getX(), -Startup.camera.getRotation().getY(), -Startup.camera.getRotation().getZ());

		updateStats();

		statusEffect.position.set(x / 2, (StateManager.currentState.tiles[locX][locY].getPosition().getY() - 10) / 2, y / 2);

		if (currentHp > hp) {
			currentHp = hp;
		}
		if (!weapon.id.equals(lastWeapon) && !StateDungeon.getCurrentContext().freeRoamMode) {
			if (StateManager.currentState.STATE_ID.equals(StateDungeon.STATE_ID)) {
				StateDungeon.getCurrentContext().clearSelectedTiles();
				StateDungeon.getCurrentContext().highlightTiles(locX, locY, 1, weapon.range + 1, "");
			}
			lastWeapon = weapon.id;
		}
		if (StateManager.currentState.STATE_ID.equals(StateDungeon.STATE_ID)) {
			if (!StateDungeon.getCurrentContext().inCombat) {
				if (dead) {
					if (unitColor.getW() > 0) {
						unitColor.setW(Math.max(unitColor.getW() - 0.005f, 0));
					}
				} else {
					if (unitColor.getW() < 1) {
						unitColor.setW(Math.min(unitColor.getW() + 0.005f, 1));
					}
				}
			}
		} else {
			if (dead) {
				if (unitColor.getW() > 0) {
					unitColor.setW(Math.max(unitColor.getW() - 0.005f, 0));
				}
			} else {
				if (unitColor.getW() < 1) {
					unitColor.setW(Math.min(unitColor.getW() + 0.005f, 1));
				}
			}
		}
		if (inCombat) {
			if (cWaitTimer < cWaitTime) {
				cWaitTimer++;
			} else {
				cWaitTimer = 0;
				if (cMovement < 8) {
					if (!cRetract) {
						if (dir == 1) {
							x--;
						}
						if (dir == 2) {
							x++;
						}
						if (dir == 3) {
							y--;
						}
						if (dir == 4) {
							y++;
						}
					} else {
						if (dir == 1) {
							x++;
						}
						if (dir == 2) {
							x--;
						}
						if (dir == 3) {
							y++;
						}
						if (dir == 4) {
							y--;
						}
					}
					cMovement++;
				} else {
					cMovement = 0;
					if (cRetract) {
						StateDungeon.getCurrentContext().attack();
						inCombat = false;
					}
					cRetract = !cRetract;
				}
			}
		}
		if (path.size() > 1) {
			if ((int) x / 16 == path.get(path.size() - 1).x && (int) y / 16 == path.get(path.size() - 1).y) {
				path.remove(path.size() - 1);
				locX = path.get(path.size() - 1).x;
				locY = path.get(path.size() - 1).y;
			}
		} else {
			if (!poseLock && !StateDungeon.getCurrentContext().inCombat) {
				waitTime = 80;
				dir = 0;
			}
			if (!path.isEmpty()) {
				path.clear();
				playFootsteps = true;
			}
		}
		if (!inCombat && unitColor.getW() >= 1) {
			boolean updated = false;
			if (x < locX * 16) {
				x = Math.min(x + movementSpeed, locX * 16);
				dir = 2;
				waitTime = 0;
				updated = true;
			}
			if (x > locX * 16) {
				x = Math.max(x - movementSpeed, locX * 16);
				dir = 1;
				waitTime = 0;
				updated = true;
			}
			if (y < locY * 16) {
				y = Math.min(y + movementSpeed, locY * 16);
				dir = 4;
				waitTime = 0;
				updated = true;
			}
			if (y > locY * 16) {
				y = Math.max(y - movementSpeed, locY * 16);
				dir = 3;
				waitTime = 0;
				updated = true;
			}
			if (updated && x % 16 == 0 && y % 16 == 0) {
				if (!StateDungeon.getCurrentContext().freeRoamMode || Inventory.active.contains(this)) {
					if (playFootsteps || StateDungeon.getCurrentContext().freeRoamMode || StateManager.currentState.STATE_ID.equals(StateHub.STATE_ID)) {
						AudioManager.getSound("footsteps_" + footstepFile + ".ogg").play(AudioManager.defaultSubSFXGain, false);
						footstepFile += (new Random().nextInt(2) + 1);
						footstepFile %= footstepSounds;
					}
					playFootsteps = !playFootsteps;
					if (StateManager.currentState.tiles[locX][locY].isWaterLogged()) {
						StateManager.currentState.effects.add(new EffectWaterSplash(new Vector3f(x, 10 + StateManager.currentState.tiles[locX][locY].getPosition().getY(), y),
								(StateDungeon.getCurrentContext().freeRoamMode && Inventory.active.contains(this)) || !StateDungeon.getCurrentContext().freeRoamMode));
					}
					if (StateManager.currentState.tiles[locX][locY].isLavaLogged()) {
						StateManager.currentState.effects.add(new EffectLavaSplash(new Vector3f(x, 10 + StateManager.currentState.tiles[locX][locY].getPosition().getY(), y),
								(StateDungeon.getCurrentContext().freeRoamMode && Inventory.active.contains(this)) || !StateDungeon.getCurrentContext().freeRoamMode));
					}
				}
			}
		}
		if (wait)

		{
			if (waitTimer < waitTime) {
				waitTimer++;
			} else {
				waitTimer = 0;
				wait = false;
			}
		} else if (hasTurn && !poseLock) {
			if (frameTimer < frameTime) {
				frameTimer++;
			} else {
				frameTimer = 0;
				if (frame < frames - 1) {
					frame++;
					if (frame == frames - 2) {
						wait = true;
					}
				} else {
					frame = 0;
					wait = true;
				}
			}
		}

		float height = StateManager.currentState.tiles[(int) Math.round(x / 16.0)][(int) Math.round(y / 16.0)].getPosition().getY();
		if (this.height < height) {
			this.height = Math.min(this.height + fallSpeed, height);
		}
		if (this.height > height) {
			this.height = Math.max(this.height - fallSpeed, height);
		}
		position.set(x, 13 + this.height, y);
		int offset = 0;
		if (body == 3) {
			offset = 2;
		} else if (body > 0) {
			offset = 1;
		}

		hairPosition.set(x, 13 + offset * 0.5f + this.height, y - offset * 0.9f);
		armorPosition.set(x, 13 + this.height + 0.3f, y + 0.2f);
		weaponPosition.set(x, 13 + this.height + 0.65f, y + 0.3f);
	}

	public void updateStats() {
		// Get all the bonus stats from different equipment
		int[] abilityStats = new int[7];
		for (String s : weapon.abilities) {
			ItemAbility ability = ItemAbility.valueOf(s);
			if (ability.hasStats && ability.isUnlocked(weapon)) {
				int[] abilityBonus = ability.getStats();
				for (int i = 0; i < abilityBonus.length; i++) {
					abilityStats[i] += abilityBonus[i];
				}
			}
		}

		hp = Math.min(99, Math.max(1, baseHp + weapon.hp + armor.hp + offsetHp + accessory.hp + abilityStats[0]));
		atk = Math.min(99, Math.max(0, Math.min(60, baseAtk) + weapon.atk + armor.atk + offsetAtk + accessory.atk + abilityStats[1]));
		mag = Math.min(99, Math.max(0, Math.min(60, baseMag) + weapon.mag + armor.mag + offsetMag + accessory.mag + abilityStats[2]));
		spd = Math.min(99, Math.max(0, Math.min(60, baseSpd) + weapon.spd + armor.spd + offsetSpd + accessory.spd + abilityStats[3]));
		def = Math.min(99, Math.max(0, Math.min(60, baseDef) + weapon.def + armor.def + offsetDef + accessory.def + abilityStats[4]));
		res = Math.min(99, Math.max(0, Math.min(60, baseRes) + weapon.res + armor.res + offsetRes + accessory.res + abilityStats[5]));
		movement = Math.min(20, Math.max(1, baseMovement + weapon.mov + armor.mov + accessory.mov + abilityStats[6]));
	}

	public void render() {
		if (unitColor.getW() > 0) {
			StateDungeon context = StateDungeon.getCurrentContext();
			String shader = (StateManager.currentState.STATE_ID == StateDungeon.STATE_ID && context.freeRoamMode && context.enemies.contains(this)) ? "main_3d_enemy" : "main_3d";
			boolean ignoreDepth = (unitColor.getW() < 1) ? true : false;

			// Change color if unit has acted
			Vector4f bodyColor = this.bodyColor;
			Vector4f hairColor = this.hairColor;
			float equipFade = (hasTurn) ? 1 : 0.5f;

			if (!hasTurn) {
				bodyColor = Vector4f.mul(this.bodyColor, 0.5f);
				hairColor = Vector4f.mul(this.hairColor, 0.5f);
			}

			BatchSorter.add((shader.equals("main_3d_enemy")) ? "zz" : "", "unit_body_" + race + "_" + body + "_" + dir + "_" + frame + ".dae", "unit_body_" + race + "_" + body + "_" + dir + "_" + frame + ".png", shader,
					Material.DEFAULT.toString(), position, rotation, Vector3f.SCALE_HALF, new Vector4f(bodyColor.getX(), bodyColor.getY(), bodyColor.getZ(), unitColor.getW()), !context.freeRoamMode && unitColor.getW() > 0, false,
					ignoreDepth);
			if (hair > -1) {
				BatchSorter.add((shader.equals("main_3d_enemy")) ? "zz" : "", "unit_hair_" + hair + "_" + dir + "_" + frame + ".dae", "unit_hair_" + hair + "_" + dir + "_" + frame + ".png", shader, Material.DEFAULT.toString(), hairPosition,
						rotation, Vector3f.SCALE_HALF, new Vector4f(hairColor.getX(), hairColor.getY(), hairColor.getZ(), unitColor.getW()), !context.freeRoamMode && unitColor.getW() > 0, false, ignoreDepth);
			}
			if (!armor.type.equals(ItemProperty.TYPE_EMPTY)) {
				BatchSorter.add((shader.equals("main_3d_enemy")) ? "zz" : "", armor.wearableModelID + "_" + body + "_" + dir + "_" + frame + ".dae", armor.wearableTextureID + "_" + body + "_" + dir + "_" + frame + ".png", shader,
						armor.material.toString(), armorPosition, rotation, Vector3f.SCALE_HALF, new Vector4f(equipFade, equipFade, equipFade, unitColor.getW()), !context.freeRoamMode && unitColor.getW() > 0, false, ignoreDepth);
			}
			if (!weapon.type.equals(ItemProperty.TYPE_EMPTY)) {
				BatchSorter.add((shader.equals("main_3d_enemy")) ? "zz" : "", weapon.wearableModelID + "_" + body + "_" + dir + "_" + frame + ".dae", weapon.wearableTextureID + "_" + body + "_" + dir + "_" + frame + ".png", shader,
						weapon.material.toString(), weaponPosition, rotation, Vector3f.SCALE_HALF, new Vector4f(equipFade, equipFade, equipFade, unitColor.getW()), !context.freeRoamMode && unitColor.getW() > 0, false, ignoreDepth);
			}

			String buffStatus = "";
			int[] stats = { offsetHp, offsetAtk, offsetMag, offsetSpd, offsetDef, offsetRes };
			for (int i : stats) {
				if (i > 0) {
					buffStatus += "buff";
					break;
				}
			}
			for (int i : stats) {
				if (i < 0) {
					buffStatus += "debuff";
					break;
				}
			}
			if (buffStatus.length() > 0) {
				BatchSorter.add(buffStatus + ".dae", buffStatus + ".png", "main_3d", Material.DEFAULT.toString(), new Vector3f(x + 7, 17 + height, y - 5), rotation, Vector3f.SCALE_HALF, new Vector4f(1, 1, 1, unitColor.getW()), false,
						false);
			}
		}
	}

	public void renderFlat(Vector3f position, Vector3f scale, Vector4f color) {
		int offset = 0;
		if (body == 3) {
			offset = 2;
		} else if (body > 0) {
			offset = 1;
		}

		// Change color if unit has acted
		Vector4f bodyColor = this.bodyColor;
		Vector4f hairColor = this.hairColor;
		if (!hasTurn) {
			bodyColor = Vector4f.mul(this.bodyColor, 0.5f);
			hairColor = Vector4f.mul(this.hairColor, 0.5f);
		}
		BatchSorter.add("unit_body_" + race + "_" + body + "_" + dir + "_" + frame + ".dae", "unit_body_" + race + "_" + body + "_" + dir + "_" + frame + ".png", "static_3d", "DEFAULT", position, Vector3f.EMPTY, scale,
				Vector4f.mul(color, new Vector4f(bodyColor.getX(), bodyColor.getY(), bodyColor.getZ(), unitColor.getW())), false, true);
		if (hair > -1) {
			BatchSorter.add("unit_hair_" + hair + "_" + dir + "_" + frame + ".dae", "unit_hair_" + hair + "_" + dir + "_" + frame + ".png", "static_3d", "DEFAULT", Vector3f.add(position, 0, offset * 1, 0), Vector3f.EMPTY, scale,
					Vector4f.mul(color, new Vector4f(hairColor.getX(), hairColor.getY(), hairColor.getZ(), unitColor.getW())), false, true);
		}
		if (!armor.type.equals(ItemProperty.TYPE_EMPTY)) {
			BatchSorter.add(armor.wearableModelID + "_" + body + "_" + dir + "_" + frame + ".dae", armor.wearableTextureID + "_" + body + "_" + dir + "_" + frame + ".png", "static_3d", armor.material.toString(),
					Vector3f.add(position, 0, 0, 0.25f), Vector3f.EMPTY, scale, color, false, true);
		}
		if (!weapon.type.equals(ItemProperty.TYPE_EMPTY)) {
			BatchSorter.add(weapon.wearableModelID + "_" + body + "_" + dir + "_" + frame + ".dae", weapon.wearableTextureID + "_" + body + "_" + dir + "_" + frame + ".png", "static_3d", armor.material.toString(),
					Vector3f.add(position, 0, 0, 0.75f), Vector3f.EMPTY, scale, color, false, true);
		}
	}

	public void renderFlatPose(Vector3f position, Vector3f scale, Vector4f color, int dir, int frame) {
		int offset = 0;
		if (body == 3) {
			offset = 2;
		} else if (body > 0) {
			offset = 1;
		}

		// Change color if unit has acted
		Vector4f bodyColor = this.bodyColor;
		Vector4f hairColor = this.hairColor;
		if (!hasTurn) {
			bodyColor = Vector4f.mul(this.bodyColor, 0.5f);
			hairColor = Vector4f.mul(this.hairColor, 0.5f);
		}
		BatchSorter.add("unit_body_" + race + "_" + body + "_" + dir + "_" + frame + ".dae", "unit_body_" + race + "_" + body + "_" + dir + "_" + frame + ".png", "static_3d", "DEFAULT", position, Vector3f.EMPTY, scale,
				Vector4f.mul(color, new Vector4f(bodyColor.getX(), bodyColor.getY(), bodyColor.getZ(), unitColor.getW())), false, true);
		if (hair > -1) {
			BatchSorter.add("unit_hair_" + hair + "_" + dir + "_" + frame + ".dae", "unit_hair_" + hair + "_" + dir + "_" + frame + ".png", "static_3d", "DEFAULT", Vector3f.add(position, 0, offset * 1, 0), Vector3f.EMPTY, scale,
					Vector4f.mul(color, new Vector4f(hairColor.getX(), hairColor.getY(), hairColor.getZ(), unitColor.getW())), false, true);
		}
		if (!armor.type.equals(ItemProperty.TYPE_EMPTY)) {
			BatchSorter.add(armor.wearableModelID + "_" + body + "_" + dir + "_" + frame + ".dae", armor.wearableTextureID + "_" + body + "_" + dir + "_" + frame + ".png", "static_3d", armor.material.toString(),
					Vector3f.add(position, 0, 0, 0.25f), Vector3f.EMPTY, scale, color, false, true);
		}
		if (!weapon.type.equals(ItemProperty.TYPE_EMPTY)) {
			BatchSorter.add(weapon.wearableModelID + "_" + body + "_" + dir + "_" + frame + ".dae", weapon.wearableTextureID + "_" + body + "_" + dir + "_" + frame + ".png", "static_3d", armor.material.toString(),
					Vector3f.add(position, 0, 0, 0.75f), Vector3f.EMPTY, scale, color, false, true);
		}
	}

	public void resetAnimation() {
		dir = 0;
		frame = 0;
		frameTimer = 0;
		wait = false;
		waitTimer = 0;
	}

	public void runAI(AIHandler parent, StateDungeon context) {
		AIPattern.run(this, parent, context);
	}

	public void select() {
		StateDungeon.getCurrentContext().selectedUnit = this;
		StateDungeon.getCurrentContext().highlightTiles(locX, locY, movement, weapon.range, Inventory.active.contains(this) ? "unit" : "enemy");
		if (StateDungeon.getCurrentContext().multiplayerMode && StateDungeon.getCurrentContext().phase == 0) {
			StateDungeon.getCurrentContext().comThread.eventQueue.add(new String[] { "HIGHLIGHT_UNIT", uuid });
		}
	}

	@SuppressWarnings("unchecked")
	public void move(ArrayList<Point> path) {
		memX = locX;
		memY = locY;
		memHeight = StateManager.currentState.tiles[(int) Math.round(x / 16.0)][(int) Math.round(y / 16.0)].getPosition().getY();
		this.path = (ArrayList<Point>) path.clone();
	}

	public void hurt(int damage) {
		currentHp = Math.max(0, currentHp - damage);
		if (currentHp == 0) {
			dead = true;
			inCombat = false;
		}
		StateManager.currentState.effects
				.add(new EffectText("-" + damage + " Hp", new Vector3f(x - ("-" + damage + " Hp").length() * 1.5f, 20 + StateManager.currentState.tiles[locX][locY].getPosition().getY(), y - 8), new Vector4f(0.75f, 0.25f, 0.25f, 1)));
	}

	public void heal(int damage) {
		int healing = Math.min(hp, currentHp + damage) - currentHp;
		StateManager.currentState.effects.add(
				new EffectText("+" + healing + " Hp", new Vector3f(x - ("+" + healing + " Hp").length() * 1.5f, 20 + StateManager.currentState.tiles[locX][locY].getPosition().getY(), y - 8), new Vector4f(ItemProperty.colorList[1], 1)));
		StateManager.currentState.effects.add(new EffectEnergize(new Vector3f(x, 10 + StateManager.currentState.tiles[locX][locY].getPosition().getY(), y), new Vector4f(0.5f, 1f, 0.5f, 1f)));
		currentHp = Math.min(hp, currentHp + damage);
	}

	public void applyStatus(String status) {
		switch (status) {

		case STATUS_POISON:
			statusTimer = 5;
			this.status = STATUS_POISON;
			statusEffect.die = true;
			statusEffect = new EffectHeated(new Vector3f(x, 10 + StateManager.currentState.tiles[locX][locY].getPosition().getY(), y), Vector4f.COLOR_POISON);
			StateManager.currentState.effects.add(statusEffect);
			StateManager.currentState.effects.add(new EffectText("+Poison", new Vector3f(x - ("+Poison").length() * 1.5f, 20 + StateManager.currentState.tiles[locX][locY].getPosition().getY(), y - 8), Vector4f.COLOR_RED));
			StateManager.currentState.effects.add(new EffectDebuff(new Vector3f(x, 10 + StateManager.currentState.tiles[locX][locY].getPosition().getY(), y)));
			break;

		case STATUS_SLEEP:
			setTurn(false);
			statusTimer = 3;
			this.status = STATUS_SLEEP;
			statusEffect.die = true;
			statusEffect = new EffectHeated(new Vector3f(x, 10 + StateManager.currentState.tiles[locX][locY].getPosition().getY(), y), Vector4f.COLOR_BASE);
			StateManager.currentState.effects.add(statusEffect);
			StateManager.currentState.effects.add(new EffectText("+Sleep", new Vector3f(x - ("+Sleep").length() * 1.5f, 20 + StateManager.currentState.tiles[locX][locY].getPosition().getY(), y - 8), Vector4f.COLOR_RED));
			StateManager.currentState.effects.add(new EffectDebuff(new Vector3f(x, 10 + StateManager.currentState.tiles[locX][locY].getPosition().getY(), y)));
			break;

		}
	}

	public String getStatus() {
		return status;
	}

	public void clearStatus() {
		if (!status.equals("")) {
			switch (status) {

			case STATUS_POISON:
				StateManager.currentState.effects.add(new EffectText("-Poison", new Vector3f(x - ("-Poison").length() * 1.5f, 20 + StateManager.currentState.tiles[locX][locY].getPosition().getY(), y - 8), Vector4f.COLOR_GREEN));
				break;

			case STATUS_SLEEP:
				StateManager.currentState.effects.add(new EffectText("-Sleep", new Vector3f(x - ("-Sleep").length() * 1.5f, 20 + StateManager.currentState.tiles[locX][locY].getPosition().getY(), y - 8), Vector4f.COLOR_GREEN));
				break;

			}

			statusEffect.die = true;
			status = "";
		}
	}

	public void setTurn(boolean ready) {
		if (!ready && status.equals(STATUS_POISON)) {
			hurt((int) Math.round(hp / 10.0));
			statusTimer--;
		}
		if (ready && status.equals(STATUS_SLEEP)) {
			statusTimer--;
		}
		if (statusTimer <= 0) {
			clearStatus();
		}
		if (!ready || !status.equals(STATUS_SLEEP)) {
			hasTurn = ready;
			resetAnimation();
		}
	}

	public void clearStatChanges() {
		offsetHp = 0;
		offsetAtk = 0;
		offsetMag = 0;
		offsetSpd = 0;
		offsetDef = 0;
		offsetRes = 0;
	}

	public void reset() {
		x = memX * 16;
		y = memY * 16;
		locX = memX;
		locY = memY;
		height = memHeight;
		dir = 0;
		waitTime = 80;
		EffectWaterSplash.playSfx = true;
	}

	public void setLocation(int locX, int locY) {
		this.locX = locX;
		this.locY = locY;
	}

	public void placeAt(int x, int y) {
		locX = x;
		locY = y;
		this.x = locX * 16;
		this.y = locY * 16;
	}

	public void setBody(int race, int body, Vector4f bodyColor) {
		this.race = race;
		this.body = body;
		this.bodyColor = bodyColor;
	}

	public void setHair(int hair, Vector4f hairColor) {
		this.hair = hair;
		this.hairColor = hairColor;
	}

	public void initStats(int baseHp, int baseAtk, int baseMag, int baseSpd, int baseDef, int baseRes) {
		this.baseHp = baseHp + 15;
		this.baseAtk = baseAtk;
		this.baseMag = baseMag;
		this.baseSpd = baseSpd;
		this.baseDef = baseDef;
		this.baseRes = baseRes;
	}

	public void randLevels(int level, int levelVariance, float[] growths) {
		int levels = level + new Random().nextInt(Math.max(1, levelVariance));

		for (int i = 0; i < levels; i++) {
			this.level++;
			for (int j = 0; j < 3; j++) {
				int stat = new Random().nextInt(6);
				while (new Random().nextFloat() > growths[stat]) {
					stat = new Random().nextInt(6);
				}
				switch (stat) {

				case 0:
					if (baseHp < 99) {
						baseHp++;
					} else {
						j--;
					}
					break;

				case 1:
					if (baseAtk < 60) {
						baseAtk++;
					} else {
						j--;
					}
					break;

				case 2:
					if (baseMag < 60) {
						baseMag++;
					} else {
						j--;
					}
					break;

				case 3:
					if (baseSpd < 60) {
						baseSpd++;
					} else {
						j--;
					}
					break;

				case 4:
					if (baseDef < 60) {
						baseDef++;
					} else {
						j--;
					}
					break;

				case 5:
					if (baseRes < 60) {
						baseRes++;
					} else {
						j--;
					}
					break;

				}
			}
		}
	}

	public int getStatTotal() {
		return baseHp + baseAtk + baseMag + baseSpd + baseDef + baseRes;
	}

	public int levelByStatTotal() {
		return (int) (getStatTotal() / 3.84);
	}

	// Used to initiallize units when being loaded into a map
	public void prepare() {
		// Update stats
		// Get all the bonus stats from different equipment
		int[] abilityStats = new int[7];
		for (String s : weapon.abilities) {
			ItemAbility ability = ItemAbility.valueOf(s);
			if (ability.hasStats && ability.isUnlocked(weapon)) {
				int[] abilityBonus = ability.getStats();
				for (int i = 0; i < abilityBonus.length; i++) {
					abilityStats[i] += abilityBonus[i];
				}
			}
		}

		offsetHp = 0;
		offsetAtk = 0;
		offsetMag = 0;
		offsetSpd = 0;
		offsetDef = 0;
		offsetRes = 0;

		hp = Math.max(1, baseHp + weapon.hp + armor.hp + offsetHp + accessory.hp + abilityStats[0]);
		atk = Math.max(0, baseAtk + weapon.atk + armor.atk + offsetAtk + accessory.atk + abilityStats[1]);
		mag = Math.max(0, baseMag + weapon.mag + armor.mag + offsetMag + accessory.mag + abilityStats[2]);
		spd = Math.max(0, baseSpd + weapon.spd + armor.spd + offsetSpd + accessory.spd + abilityStats[3]);
		def = Math.max(0, baseDef + weapon.def + armor.def + offsetDef + accessory.def + abilityStats[4]);
		res = Math.max(0, baseRes + weapon.res + armor.res + offsetRes + accessory.res + abilityStats[5]);
		movement = Math.max(1, baseMovement + abilityStats[6]);

		// Fully heal
		currentHp = hp;
		dead = false;

		lastWeapon = weapon.id;
		unitColor = new Vector4f(1, 1, 1, 1);
		setTurn(true);

		// Set alpha to fade in
		unitColor.setW(0);
	}

	public int addItem(ItemProperty item) {
		if (item.type.equals(ItemProperty.TYPE_USABLE) || item.type.equals(ItemProperty.TYPE_OTHER)) {
			for (ItemProperty i : items) {
				if (i.id.equals(item.id)) {
					if (i.stack < ItemProperty.STACK_CAP) {
						int overflow = Math.max(i.stack + item.stack, ItemProperty.STACK_CAP) % ItemProperty.STACK_CAP;
						i.stack += (item.stack - overflow);
						item.stack = overflow;
						return overflow;
					} else {
						return item.stack;
					}
				}
			}
		}
		if (items.size() < 5) {
			items.add(item);
			return 0;
		}
		return item.stack;
	}

	public int addItem(ItemProperty item, int amt) {
		if (item.type.equals(ItemProperty.TYPE_USABLE) || item.type.equals(ItemProperty.TYPE_OTHER)) {
			for (ItemProperty i : items) {
				if (i.id.equals(item.id)) {
					if (i.stack < ItemProperty.STACK_CAP) {
						int overflow = Math.max(i.stack + amt, ItemProperty.STACK_CAP) % ItemProperty.STACK_CAP;
						i.stack += amt - overflow;
						item.stack -= amt - overflow;
						return overflow;
					} else {
						return item.stack;
					}
				}
			}
		}
		if (items.size() < 5) {
			ItemProperty copy = item.copy();
			copy.stack = amt;
			items.add(copy);
			item.stack -= amt;
			return 0;
		}
		return amt;
	}

	public int addItemInFront(ItemProperty item) {
		if (item.type.equals(ItemProperty.TYPE_USABLE) || item.type.equals(ItemProperty.TYPE_OTHER)) {
			for (ItemProperty i : items) {
				if (i.id.equals(item.id)) {
					if (i.stack < ItemProperty.STACK_CAP) {
						int overflow = Math.max(i.stack + item.stack, ItemProperty.STACK_CAP) % ItemProperty.STACK_CAP;
						i.stack += (item.stack - overflow);
						item.stack = overflow;
						return overflow;
					} else {
						return item.stack;
					}
				}
			}
		}
		if (items.size() < 5) {
			items.add(0, item);
			return 0;
		}
		return item.stack;
	}

	public boolean canAddItem(ItemProperty item) {
		if (item.type.equals(ItemProperty.TYPE_USABLE) || item.type.equals(ItemProperty.TYPE_OTHER)) {
			for (ItemProperty i : items) {
				if (i.id.equals(item.id) && i.stack < ItemProperty.STACK_CAP) {
					return true;
				}
			}
		}
		if (items.size() < 5) {
			return true;
		}
		return false;
	}

	public void removeItem(ItemProperty item) {
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).id.equals(item.id)) {
				if ((items.get(i).type.equals(ItemProperty.TYPE_USABLE) || items.get(i).type.equals(ItemProperty.TYPE_OTHER)) && items.get(i).stack > 1) {
					items.get(i).stack--;
				} else {
					items.remove(i);
				}
				return;
			}
		}
	}

	public void removeItem(ItemProperty item, int amt) {
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).id.equals(item.id)) {
				if ((items.get(i).type.equals(ItemProperty.TYPE_USABLE) || items.get(i).type.equals(ItemProperty.TYPE_OTHER)) && items.get(i).stack > amt) {
					items.get(i).stack -= amt;
				} else {
					items.remove(i);
				}
				return;
			}
		}
	}

	public ArrayList<ItemProperty> getItemList() {
		return items;
	}

	public boolean hasItem(ItemProperty item) {
		for (ItemProperty i : items) {
			if (i.type.equals(item.type)) {
				return true;
			}
		}
		return false;
	}

	public void clearItems() {
		items.clear();
	}

	public static Unit randomCombatUnit(int x, int y, Vector4f color, int level, int levelVariance, float[] growths, AIType ai) {
		Unit u = new Unit(x, y, color, UUID.randomUUID().toString());
		u.generated = true;
		u.name = names.get(new Random().nextInt(names.size()));
		u.setHair(new Random().nextInt(HAIR_STYLE_COUNT), new Vector4f(new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 1));
		u.setBody(new Random().nextInt(1), new Random().nextInt(4), randBodyColors[new Random().nextInt(randBodyColors.length)]);
		u.randLevels(level, levelVariance, growths);

		ItemProperty[] tierGear = ItemProperty.searchForTier(u.levelByStatTotal() / 10 - 1, ItemProperty.getItemList(), false);

		ItemProperty[] weapons = ItemProperty.searchForType(ItemProperty.TYPE_WEAPON, tierGear, true);
		weapons = (u.baseAtk > u.baseMag) ? ItemProperty.searchForAttackStat(weapons) : ItemProperty.searchForMagicStat(weapons);
		if (weapons.length > 0) {
			u.weapon = weapons[new Random().nextInt(weapons.length)].copy();
		}

		ItemProperty[] armor = ItemProperty.searchForType(ItemProperty.TYPE_ARMOR, tierGear, true);
		if (armor.length > 0) {
			u.armor = armor[new Random().nextInt(armor.length)].copy();
		}
		ItemProperty[] accessories = ItemProperty.searchForType(ItemProperty.TYPE_ACCESSORY, tierGear, true);
		if (accessories.length > 0) {
			u.accessory = accessories[new Random().nextInt(accessories.length)].copy();
		}

		u.AIPattern = ai;

		return u;
	}

	public void saveUnit(String file) {
		File f = new File(file);

		try {
			DataOutputStream out = new DataOutputStream(new FileOutputStream(f));

			out.writeUTF(uuid);
			out.writeUTF(name);
			out.writeInt(hair);
			out.writeFloat(hairColor.getX());
			out.writeFloat(hairColor.getY());
			out.writeFloat(hairColor.getZ());
			out.writeInt(race);
			out.writeInt(body);
			out.writeFloat(bodyColor.getX());
			out.writeFloat(bodyColor.getY());
			out.writeFloat(bodyColor.getZ());

			out.writeInt(baseHp);
			out.writeInt(baseAtk);
			out.writeInt(baseMag);
			out.writeInt(baseSpd);
			out.writeInt(baseDef);
			out.writeInt(baseRes);
			out.writeInt(level);
			out.writeInt(baseMovement);

			out.writeUTF(weapon.id);
			out.writeUTF(weapon.uuid);
			out.writeInt(weapon.abilities.length);
			for (String s : weapon.abilities) {
				out.writeUTF(s);
			}

			out.writeInt(items.size());
			for (ItemProperty i : items) {
				out.writeUTF(i.id);
				out.writeUTF(i.uuid);
				out.writeInt(i.abilities.length);
				for (String s : i.abilities) {
					out.writeUTF(s);
				}
			}

			// Output weapon ability unlock stats when added

			out.writeUTF(armor.id);
			out.writeUTF(armor.uuid);
			out.writeUTF(accessory.id);
			out.writeUTF(accessory.uuid);

			out.writeUTF(AIPattern.getFile());

			out.flush();
			out.close();

			System.out.println("[Unit]: Saved unit " + file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Unit loadUnit(int x, int y, Vector4f color, String file) throws Exception {
		File f = new File(file);
		if (f.exists()) {
			DataInputStream in = new DataInputStream(new FileInputStream(f));
			Unit u = new Unit(x, y, color, in.readUTF());

			u.name = in.readUTF();
			u.setHair(in.readInt(), new Vector4f(in.readFloat(), in.readFloat(), in.readFloat(), 1));
			u.setBody(in.readInt(), in.readInt(), new Vector4f(in.readFloat(), in.readFloat(), in.readFloat(), 1));
			u.baseHp = in.readInt();
			u.baseAtk = in.readInt();
			u.baseMag = in.readInt();
			u.baseSpd = in.readInt();
			u.baseDef = in.readInt();
			u.baseRes = in.readInt();
			u.level = in.readInt();
			u.baseMovement = in.readInt();

			u.weapon = ItemProperty.get(in.readUTF()).copy();
			u.weapon.uuid = in.readUTF();
			String[] abilities = new String[in.readInt()];
			for (int i = 0; i < abilities.length; i++) {
				abilities[i] = in.readUTF();
			}
			u.weapon.abilities = abilities;

			int length = in.readInt();
			for (int i = 0; i < length; i++) {
				ItemProperty item = ItemProperty.get(in.readUTF());
				item.uuid = in.readUTF();
				String[] itemAbilities = new String[in.readInt()];
				for (int j = 0; j < itemAbilities.length; j++) {
					itemAbilities[j] = in.readUTF();
				}
				u.addItem(item);
			}

			u.armor = ItemProperty.get(in.readUTF()).copy();
			u.armor.uuid = in.readUTF();
			u.accessory = ItemProperty.get(in.readUTF()).copy();
			u.accessory.uuid = in.readUTF();

			// Set and clean up
			u.currentHp = u.baseHp + u.weapon.hp + u.armor.hp + u.offsetHp + u.accessory.hp;
			u.lastWeapon = u.weapon.id;

			u.AIPattern = AIType.get(in.readUTF());

			in.close();

			return u;
		} else {
			System.out.println("[Unit]: File " + file + " was not found");
		}
		return randomCombatUnit(x, y, new Vector4f(1, 1, 1, 1), 1, 0, new float[] { 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f }, AIType.get("standard_dungeon.json"));
	}

	public static void loadNames(String file) {
		File f = new File(file);
		if (f.exists()) {
			try {
				JSONArray ja = (JSONArray) new JSONParser().parse(new FileReader(f));

				for (int i = 0; i < ja.size(); i++) {
					names.add((String) ja.get(i));
				}
				System.out.println("[Unit]: Loaded name data with " + names.size() + " names");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// Returns a String array with the names contained in an ArrayList of units
	public static String[] getUnitNames(Unit[] units) {
		String[] names = new String[units.length];
		for (int i = 0; i < units.length; i++) {
			names[i] = units[i].name;
		}
		return names;
	}

	public static void loadProfiles(String file) {
		File f = new File(file);
		if (f.exists()) {
			try {
				JSONArray ja = (JSONArray) new JSONParser().parse(new FileReader(f));

				for (int i = 0; i < ja.size(); i++) {
					JSONObject profile = (JSONObject) ja.get(i);

					JSONArray data = (JSONArray) profile.get("values");
					float[] values = new float[data.size()];
					for (int j = 0; j < data.size(); j++) {
						values[j] = (float) ((double) data.get(j));
					}

					GROWTH_PROFILES.put((String) profile.get("name"), values);
				}
				System.out.println("[Unit]: Loaded profile data with " + GROWTH_PROFILES.size() + " profiles");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}