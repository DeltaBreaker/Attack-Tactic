package io.itch.deltabreaker.state;

import java.awt.Point;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import io.itch.deltabreaker.ai.AIHandler;
import io.itch.deltabreaker.builder.dungeon.DungeonGenerator;
import io.itch.deltabreaker.builder.dungeon.DungeonGeneratorMultiplayer;
import io.itch.deltabreaker.builder.dungeon.DungeonGeneratorRestArea;
import io.itch.deltabreaker.builder.dungeon.DungeonGeneratorVillage;
import io.itch.deltabreaker.core.FileManager;
import io.itch.deltabreaker.core.InputManager;
import io.itch.deltabreaker.core.InputMapping;
import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.core.PerformanceManager;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.effect.Effect;
import io.itch.deltabreaker.effect.EffectBurst;
import io.itch.deltabreaker.effect.EffectEnergize;
import io.itch.deltabreaker.effect.EffectHealAura;
import io.itch.deltabreaker.effect.EffectHealthBarDeplete;
import io.itch.deltabreaker.effect.EffectLava;
import io.itch.deltabreaker.effect.EffectPoof;
import io.itch.deltabreaker.effect.EffectText;
import io.itch.deltabreaker.effect.EffectWater;
import io.itch.deltabreaker.effect.EffectWaterSplash;
import io.itch.deltabreaker.effect.battle.EffectBattle;
import io.itch.deltabreaker.effect.dungeon.EffectDungeonLavaSFX;
import io.itch.deltabreaker.effect.dungeon.EffectDungeonRain;
import io.itch.deltabreaker.effect.dungeon.EffectDungeonResidue;
import io.itch.deltabreaker.effect.dungeon.EffectDungeonSnow;
import io.itch.deltabreaker.event.Event;
import io.itch.deltabreaker.event.EventScript;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Light;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.multiplayer.GameInputStream;
import io.itch.deltabreaker.multiplayer.GameOutputStream;
import io.itch.deltabreaker.multiplayer.client.MatchComThread;
import io.itch.deltabreaker.multiprocessing.TaskThread;
import io.itch.deltabreaker.multiprocessing.WorkerTask;
import io.itch.deltabreaker.object.Cursor;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.Item;
import io.itch.deltabreaker.object.item.ItemAbility;
import io.itch.deltabreaker.object.item.ItemProperty;
import io.itch.deltabreaker.object.item.ItemUse;
import io.itch.deltabreaker.object.tile.Tile;
import io.itch.deltabreaker.ui.CombatCard;
import io.itch.deltabreaker.ui.HealingCard;
import io.itch.deltabreaker.ui.Message;
import io.itch.deltabreaker.ui.TextBox;
import io.itch.deltabreaker.ui.UIBox;
import io.itch.deltabreaker.ui.menu.Menu;
import io.itch.deltabreaker.ui.menu.MenuDungeonAction;
import io.itch.deltabreaker.ui.menu.MenuDungeonMain;
import io.itch.deltabreaker.ui.menu.MenuStatusCard;
import io.itch.deltabreaker.ui.menu.MenuUnitLevel;

public class StateDungeon extends State {

	public static final String STATE_ID = "state.dungeon";

	public static final int ACTION_PROGRESS = 0;
	public static final int ACTION_RETURN = 1;

	public String title = "";
	public String subTitle = "";

	public boolean curRotate = false;
	public int curRotateInt = 0;

	public double shadeX = 0;
	public int shadeXLimit = 256;
	public Light overheadLight;

	public Tile filler;
	public ArrayList<Unit> enemies = new ArrayList<>();
	public ArrayList<Item> items = new ArrayList<>();
	public ArrayList<Point> path = new ArrayList<Point>();
	public Point[] startPositions;

	public Unit selectedUnit;
	public Unit shellUnit;
	public ItemAbility selectedAbility = ItemAbility.ITEM_ABILITY_ATTACK;
	public ItemUse selectedItemUse;
	public ItemProperty selectedItem;

	public int phase = 0;

	public boolean showingRange = false;
	public boolean inCombat = false;
	public Unit attacker;
	public Unit defender;
	public int atkRounds = 0;
	public int defRounds = 0;
	public boolean lastRound = false;
	public int comWaitTimer = 0;
	public int comWaitTime = 100;
	public boolean combatMode = false;
	public boolean usingItem = false;
	public float healthAlpha = 0;
	public float healthAlphaSpeed = 0.05f;
	public float attackerDisplayHealth;
	public float defenderDisplayHealth;
	public float healthDrainSpeed = 0.5f;
	public String xpGainUnit = "";
	public float xpAlpha = 0;
	public float xpAlphaSpeed = 0.05f;
	public float displayXP = 1;
	public float displayXPTarget = 1;
	public float xpGainSpeed = 0.5f;
	public int xpFadeWaitTimer = 0;
	public int xpFadeWaitTime = 72;
	private Unit levelUpUnit;
	private boolean cursorFloat = false;
	public boolean hideInfo = false;

	public float alpha = 1;
	public float alphaTo = 0;
	public int action = ACTION_PROGRESS;
	public int titleWaitTimer = 0;
	public int titleWaitTime = 144;

	// Used for drawing the phase swap text animation
	public String[] phases = { "Player Phase", "Enemy Phase" };
	public Vector4f[] phaseColor = { Vector4f.COLOR_BLUE, Vector4f.COLOR_RED };
	public float baseSwapTextSpeed = 4f;
	public float swapTextSpeed = baseSwapTextSpeed;
	public float swapTimer = 0;
	public int swapTime = 144;
	public int swapWaitTimer = 0;
	public int swapWaitTime = 144;
	public int swapWaitGap = 18;
	public float swapWaitSpeed = 0.25f;
	public boolean swap = false;

	// Used for displaying in-world text
	public String info = "";
	public int infoLength = 0;

	// Keeps track of how many turns has passed and how fast the corruption
	// progresses
	public int currentTurn = 0;
	public float corruptionRate = 0;

	public DungeonGenerator dungeon;
	public AIHandler aiHandler;

	public HashMap<String, EventScript> eventList = new HashMap<>();
	public ArrayList<Event> events = new ArrayList<>();

	// Variables used for free roam
	public boolean freeRoamMode = false;
	public boolean caught = false;
	public int caughtTimer = 0;
	public int caughtTime = 144;

	public int roamUnit = 0;

	public boolean multiplayerMode = false;
	public MatchComThread comThread;

	public StateDungeon() {
		super(STATE_ID);
	}

	private WorkerTask task = new WorkerTask() {
		@Override
		public void tick() {
			// Moves the shadow camera along with the normal camera
			Startup.shadowCamera.targetPosition.set(Startup.camera.position.getX(), 80 + tiles[cursorPos.x][cursorPos.y].getPosition().getY() / 2, Startup.camera.position.getZ());
			overheadLight.position.set(Startup.shadowCamera.position.getX(), Startup.shadowCamera.position.getY() + 48, Startup.shadowCamera.position.getZ());

			// Controlls the fade timer for the board highlights
			if (shading < 360) {
				shading += 0.75f;
			} else {
				shading = 0;
			}

			// Updates each tile in the camera range
			for (int x = 0; x < DungeonGenerator.xActiveSpace; x++) {
				for (int y = 0; y < DungeonGenerator.yActiveSpace; y++) {
					if (rcamX + x < tiles.length && rcamY + y < tiles[0].length && rcamX + x > -1 && rcamY + y > -1) {
						tiles[x + rcamX][y + rcamY].tick();
					}
				}
			}

			if (shadeX < shadeXLimit) {
				shadeX += 0.6;
			} else {
				shadeX = 0;
			}
			if (curRotate) {
				if (curRotateInt < 90) {
					curRotateInt += 5;
				} else {
					curRotateInt = 0;
					curRotate = false;
				}
			}

			if (menus.size() == 0 && !cursorFloat) {
				cursor.targetPosition.set((int) cursorPos.getX() * 16 + 3, 34 + tiles[cursorPos.x][cursorPos.y].getPosition().getY(), (int) cursorPos.getY() * 16 - 8);
			}
			cursor.tick();

			if (alphaTo <= 0 && Startup.screenColor.getW() == 0) {
				if (alpha > alphaTo) {
					if (titleWaitTimer < titleWaitTime) {
						titleWaitTimer++;
					} else {
						alpha = (float) Math.max(alpha - 0.005, alphaTo);
					}
				} else {
					titleWaitTimer = 0;
				}
			}
		}
	};

	@Override
	public void tick() {
		info = "";

		// This handles how long the exclamation mark appears when being caught
		if (caught) {
			if (caughtTimer < caughtTime) {
				caughtTimer++;
			} else {
				caughtTimer = 0;
				caught = false;
				enterGridMode();
			}
		}
		// Handles rotating the text when transitioning phases
		if (events.size() == 0 && swap && noUIOnScreen() && alpha == alphaTo && Startup.screenColor.equals(Startup.screenColorTarget) && xpAlpha == 0 && displayXP == displayXPTarget) {
			if (swapTimer == 0) {
				AudioManager.getSound("phase_" + phase + ".ogg").play(AudioManager.defaultMainSFXGain, false);
			}
			if (swapTimer < swapTime * 2) {
				swapTimer = Math.min(swapTime * 2, swapTimer + swapTextSpeed);
				if (swapTimer > swapTime - swapWaitGap && swapTimer < swapTime + swapWaitGap) {
					swapTextSpeed = swapWaitSpeed;
				} else {
					swapTextSpeed = baseSwapTextSpeed;
				}
			} else {
				swapTimer = 0;
				swap = false;
			}
		}
		if (!multiplayerMode) {
			aiHandler.tick();
		}
		if (inCombat) {
			// This handles the logic of the health bars that appear at the top of the
			// screen when in combat
			// Opacity / reduction / effects
			if (attacker.currentHp < attackerDisplayHealth) {
				int currentLength = (int) Math.round(40.0 / (double) attacker.hp * attackerDisplayHealth);
				int nextLength = (int) Math.round(40.0 / (double) attacker.hp * Math.max(attackerDisplayHealth - healthDrainSpeed, attacker.currentHp));
				if (currentLength != nextLength) {
					effects.add(new EffectHealthBarDeplete(new Vector3f(-38 + (int) Math.round(40.0 / (double) attacker.hp * attackerDisplayHealth) - 21.5f, 36, -79),
							(Inventory.active.contains(attacker)) ? new Vector4f(0, 0.580f, 1, 1) : new Vector4f(1, 0.317f, 0.341f, 1)));
				}
				attackerDisplayHealth = Math.max(attackerDisplayHealth - healthDrainSpeed, attacker.currentHp);
			}
			if (defender.currentHp < defenderDisplayHealth) {
				int currentLength = (int) Math.round(40.0 / (double) defender.hp * defenderDisplayHealth);
				int nextLength = (int) Math.round(40.0 / (double) defender.hp * Math.max(defenderDisplayHealth - healthDrainSpeed, defender.currentHp));
				if (currentLength != nextLength) {
					effects.add(new EffectHealthBarDeplete(new Vector3f(38 + Math.round(40.0 / (double) defender.hp * defenderDisplayHealth) - 21.5f, 36, -79),
							(Inventory.active.contains(defender)) ? new Vector4f(0, 0.580f, 1, 1) : new Vector4f(1, 0.317f, 0.341f, 1)));
				}
				defenderDisplayHealth = Math.max(defenderDisplayHealth - healthDrainSpeed, defender.currentHp);
			}
			if (healthAlpha < 1) {
				healthAlpha = Math.min(1, healthAlpha + healthAlphaSpeed);
			}
			// ----------------------------------------------------------------------

			if (attacker.path.size() == 0 && defender.path.size() == 0) {
				if (!attacker.inCombat && !defender.inCombat) {
					// Makes the units change direction to face each other when in combat
					if (attacker.x < defender.x) {
						if (Math.abs(attacker.y - defender.y) > Math.abs(attacker.x - defender.x)) {
							if (attacker.y < defender.y) {
								attacker.dir = 4;
								defender.dir = 3;
							} else {
								attacker.dir = 3;
								defender.dir = 4;
							}
						} else {
							attacker.dir = 2;
							defender.dir = 1;
						}
					} else {
						if (Math.abs(attacker.y - defender.y) > Math.abs(attacker.x - defender.x)) {
							if (attacker.y < defender.y) {
								attacker.dir = 4;
								defender.dir = 3;
							} else {
								attacker.dir = 3;
								defender.dir = 4;
							}
						} else {
							attacker.dir = 1;
							defender.dir = 2;
						}
					}
					if (comWaitTimer < comWaitTime) {
						comWaitTimer++;
					} else {
						comWaitTimer = 0;
						if (atkRounds + defRounds > 0) {
							if (lastRound) {
								if (defRounds > 0) {
									defender.inCombat = true;
									defRounds--;
									lastRound = false;
								} else {
									lastRound = false;
									comWaitTimer = comWaitTime;
								}
							} else {
								if (atkRounds > 0) {
									attacker.inCombat = true;
									atkRounds--;
									lastRound = true;
								} else {
									lastRound = true;
									comWaitTimer = comWaitTime;
								}
							}
						} else {
							inCombat = false;
							combatMode = false;

							// Activate abilities after combat ends
							for (String s : attacker.weapon.abilities) {
								ItemAbility a = ItemAbility.valueOf(s);
								if (a.isUnlocked(attacker.weapon)) {
									a.onCombatEnd(attacker, this);
								}
							}
							for (String s : defender.weapon.abilities) {
								ItemAbility a = ItemAbility.valueOf(s);
								if (a.isUnlocked(defender.weapon)) {
									a.onCombatEnd(defender, this);
								}
							}

							attacker.setTurn(false);

							// Distributes xp if a unit dies
							if (!multiplayerMode) {
								if (Inventory.active.contains(attacker)) {
									if (!attacker.dead) {
										xpGainUnit = attacker.name;
										if (displayXP == 0) {
											displayXP = attacker.exp;
										}
										int exp = AdvMath.inRange(defender.level - attacker.level + 5, 1, 10) * 2;
										if (attacker.accessory.hasAbility(ItemAbility.ITEM_ABILITY_XPGAIN_10)) {
											exp = (int) Math.round(exp * 1.1);
										}
										attacker.exp += exp;
										effects.add(new EffectText("+" + exp + " Exp", new Vector3f(attacker.x - ("+" + exp + " Exp").length(), 20 + tiles[attacker.locX][attacker.locY].getPosition().getY(), attacker.y),
												new Vector4f(ItemProperty.colorList[1], 1)));
										if (displayXPTarget == 0) {
											displayXPTarget = Math.min(displayXP + exp, 100);
										} else {
											displayXPTarget = Math.min(displayXPTarget + exp, 100);
										}
										if (attacker.exp >= 100) {
											attacker.exp -= 100;
											if (attacker.exp >= 100) {
												attacker.exp = 99;
											}
											levelUpUnit = attacker;
										}
									}
								} else if (defender != null) {
									if (!defender.dead) {
										xpGainUnit = defender.name;
										if (displayXP == 0) {
											displayXP = defender.exp;
										}
										int exp = AdvMath.inRange(attacker.level - defender.level + 5, 1, 10) * 2;
										if (defender.accessory.hasAbility(ItemAbility.ITEM_ABILITY_XPGAIN_10)) {
											exp = (int) Math.round(exp * 1.1);
										}
										defender.exp += exp;
										effects.add(new EffectText("+" + exp + " Exp", new Vector3f(defender.x - ("+" + exp + " Exp").length(), 20 + tiles[defender.locX][defender.locY].getPosition().getY(), defender.y),
												new Vector4f(ItemProperty.colorList[1], 1)));
										if (displayXPTarget == 0) {
											displayXPTarget = Math.min(displayXP + exp, 100);
										} else {
											displayXPTarget = Math.min(displayXPTarget + exp, 100);
										}
										if (defender.exp >= 100) {
											defender.exp -= 100;
											if (defender.exp >= 100) {
												defender.exp = 99;
											}
											levelUpUnit = defender;
										}
									}
								}
							}

							clearUnit();
						}
					}
				}
			}
		} else {
			// This handles the logic of the health bars that appear at the top of the
			// screen when in combat
			// Opacity / reduction / effects
			if (displayXP < displayXPTarget) {
				if (xpAlpha < 1) {
					if (noUIOnScreen() && (!attacker.dead || (attacker.dead && attacker.unitColor.getW() <= 0)) && (!defender.dead || (defender.dead && defender.unitColor.getW() <= 0))) {
						xpAlpha = Math.min(xpAlpha + xpAlphaSpeed, 1);
					}
				} else {
					if (xpFadeWaitTimer < xpFadeWaitTime) {
						xpFadeWaitTimer++;
					} else {
						int currentLength = (int) displayXP;
						int nextLength = (int) Math.min(displayXP + xpGainSpeed, displayXPTarget);
						if (currentLength != nextLength) {
							effects.add(new EffectHealthBarDeplete(new Vector3f(displayXP - 47.5f, -45, -79), new Vector4f(ItemProperty.colorList[1], 1)));
							AudioManager.getSound("xp.ogg").playWithoutReset(AudioManager.defaultSubSFXGain, false);
						}
						displayXP = Math.min(displayXP + xpGainSpeed, displayXPTarget);
						if (displayXP == displayXPTarget) {
							xpFadeWaitTimer = 0;
						}
					}
				}
			} else if (xpAlpha > 0) {
				if (xpFadeWaitTimer < xpFadeWaitTime) {
					xpFadeWaitTimer++;
				} else {
					xpAlpha = Math.max(xpAlpha - xpAlphaSpeed, 0);
					if (xpAlpha == 0) {
						if (displayXPTarget == 100) {
							menus.add(new MenuUnitLevel(new Vector3f(0, 0, -80), levelUpUnit, this));
						}
						displayXP = 0;
						displayXPTarget = 0;
						xpFadeWaitTimer = 0;
					}
				}
			}
			// ----------------------------------------------------------------------

			if (healthAlpha > 0) {
				healthAlpha = Math.max(0, healthAlpha - healthAlphaSpeed);
			}
		}
		if (menus.size() > 0) {
			menus.get(0).tick();
			if (!menus.get(0).open && menus.get(0).height <= 16) {
				menus.remove(0);
			}
		}
		if (messages.size() > 0) {
			messages.get(0).tick();
			if (messages.get(0).remove) {
				messages.remove(0);
			}
		}
		if (text.size() > 0) {
			text.get(0).tick();
			if (text.get(0).close && text.get(0).openInt == 0) {
				text.remove(0);
			}
		}

		// Updates all units, updates the info tag when hovered over, and removes dead
		// units
		boolean changePhase = true;
		for (int u = 0; u < Inventory.active.size(); u++) {
			if (!freeRoamMode && cursorPos.x == Inventory.active.get(u).locX && cursorPos.y == Inventory.active.get(u).locY) {
				info = Inventory.active.get(u).name + " " + Inventory.active.get(u).currentHp + "_" + Inventory.active.get(u).hp;
			}
			Inventory.active.get(u).tick();
			if (Inventory.active.get(u).unitColor.getW() <= 0) {
				effects.add(new EffectPoof(new Vector3f(Inventory.active.get(u).x, Inventory.active.get(u).height + 13, Inventory.active.get(u).y)));

				if (multiplayerMode) {
					for (ItemProperty e : Inventory.active.get(u).getItemList()) {
						if (Inventory.active.contains(attacker)) {
							if (defender.addItem(e) != 0) {
								findLocationForItem(Inventory.active.get(u), e);
							}
						} else if (Inventory.active.contains(defender)) {
							if (attacker.addItem(e) != 0) {
								findLocationForItem(Inventory.active.get(u), e);
							}
						} else {
							findLocationForItem(Inventory.active.get(u), e);
						}
					}
				} else {
					Inventory.active.get(u).clearItems();
				}

				for (EventScript e : eventList.values()) {
					if (e.activator.equals(EventScript.ACTIVATOR_DEATH + " " + Inventory.active.get(u).uuid)) {
						events.add(new Event(e));
						changePhase = false;
					}
				}

				Inventory.active.remove(u);
			} else if (Inventory.active.get(u).unitColor.getW() < 1) {
				changePhase = false;
			}
		}

		for (int u = 0; u < enemies.size(); u++) {
			if (cursorPos.x == enemies.get(u).locX && cursorPos.y == enemies.get(u).locY && enemies.get(u).currentHp > 0) {
				info = enemies.get(u).name + " " + enemies.get(u).currentHp + "_" + enemies.get(u).hp;
			}
			enemies.get(u).tick();
			if (enemies.get(u).hasTurn) {
				changePhase = false;
			}
			if (enemies.get(u).unitColor.getW() <= 0) {
				if (!multiplayerMode) {
					if (aiHandler.processing) {
						aiHandler.gettingPath = true;
						aiHandler.hasAttacked = false;
						if (aiHandler.currentUnit == enemies.size() - 1) {
							aiHandler.processing = false;
						}
					}
					if (Inventory.active.contains(attacker)) {
						if (!attacker.dead) {
							xpGainUnit = attacker.name;
							if (displayXP == 0) {
								displayXP = attacker.exp;
							}
							int exp = AdvMath.inRange(defender.level - attacker.level + 6, 1, 16) * 5;
							if (attacker.accessory.hasAbility(ItemAbility.ITEM_ABILITY_XPGAIN_10)) {
								exp = (int) Math.round(exp * 1.1);
							}
							attacker.exp += exp;
							effects.add(new EffectText("+" + exp + " Exp", new Vector3f(attacker.x - ("+" + exp + " Exp").length(), 20 + tiles[attacker.locX][attacker.locY].getPosition().getY(), attacker.y),
									new Vector4f(ItemProperty.colorList[1], 1)));
							if (displayXPTarget == 0) {
								displayXPTarget = Math.min(displayXP + exp, 100);
							} else {
								displayXPTarget = Math.min(displayXPTarget + exp, 100);
							}
							if (attacker.exp >= 100) {
								attacker.exp -= 100;
								if (attacker.exp >= 100) {
									attacker.exp = 99;
								}
								levelUpUnit = attacker;
							}
						}
					} else if (defender != null) {
						if (!defender.dead) {
							xpGainUnit = defender.name;
							if (displayXP == 0) {
								displayXP = defender.exp;
							}
							int exp = AdvMath.inRange(attacker.level - defender.level + 6, 1, 16) * 5;
							if (defender.accessory.hasAbility(ItemAbility.ITEM_ABILITY_XPGAIN_10)) {
								exp = (int) Math.round(exp * 1.1);
							}
							defender.exp += exp;
							effects.add(new EffectText("+" + exp + " Exp", new Vector3f(defender.x - ("+" + exp + " Exp").length(), 20 + tiles[defender.locX][defender.locY].getPosition().getY(), defender.y),
									new Vector4f(ItemProperty.colorList[1], 1)));
							if (displayXPTarget == 0) {
								displayXPTarget = Math.min(displayXP + exp, 100);
							} else {
								displayXPTarget = Math.min(displayXPTarget + exp, 100);
							}
							if (defender.exp >= 100) {
								defender.exp -= 100;
								if (defender.exp >= 100) {
									defender.exp = 99;
								}
								levelUpUnit = defender;
							}
						}
					}
				}
				for (ItemProperty e : enemies.get(u).getItemList()) {
					if (Inventory.active.contains(attacker)) {
						if (attacker.addItem(e) != 0) {
							findLocationForItem(enemies.get(u), e);
						}
					} else if (Inventory.active.contains(defender)) {
						if (defender.addItem(e) != 0) {
							findLocationForItem(enemies.get(u), e);
						}
					} else {
						findLocationForItem(enemies.get(u), e);
					}
				}
				effects.add(new EffectPoof(new Vector3f(enemies.get(u).x, enemies.get(u).height + 13, enemies.get(u).y)));

				for (EventScript e : eventList.values()) {
					if (e.activator.equals(EventScript.ACTIVATOR_DEATH + " " + enemies.get(u).uuid)) {
						events.add(new Event(e));
						changePhase = false;
					}
				}

				if (enemies.get(u).generated) {
					Inventory.loaded.remove(enemies.get(u).uuid);
				}
				enemies.remove(u);

				if (enemies.size() == 0) {
					for (EventScript e : eventList.values()) {
						if (e.activator.equals(EventScript.ACTIVATOR_MAP_ROUTE)) {
							events.add(new Event(e));
						}
					}
				}
			} else if (enemies.get(u).unitColor.getW() < 1) {
				changePhase = false;
			}
		}

		for (Item i : items) {
			if (i.position.getX() > (int) camX * 2 - 208 && i.position.getX() < (int) camX * 2 + 192) {
				if (i.position.getZ() > (int) camY * 2 - DungeonGenerator.yActiveSpace * 16 && i.position.getZ() < (int) camY * 2 + 96) {
					i.tick();
					if (cursorPos.x == i.locX && cursorPos.y == i.locY) {
						if (info.length() == 0) {
							info = i.item.name;
						} else {
							info += " + " + i.item.name;
						}
					}
				}
			}
		}

		for (int e = 0; e < effects.size(); e++) {
			effects.get(e).tick();
			if (effects.get(e).remove) {
				effects.get(e).cleanUp();
				effects.remove(e);
				e--;
			}
		}

		// This sorts the array of effects so that the ones closest get rendered first
		ArrayList<Effect> sortedEffects = new ArrayList<>();
		int size = effects.size();
		for (int e = 0; e < size; e++) {
			Effect closest = effects.get(0);

			for (int i = 0; i < effects.size(); i++) {
				float distA = Vector3f.distance(effects.get(i).position, Vector3f.mul(Startup.camera.position, 2, 2, 2));
				float distB = Vector3f.distance(closest.position, Vector3f.mul(Startup.camera.position, 2, 2, 2));

				if (distA < distB) {
					closest = effects.get(i);
				} else if (distA == distB) {
					if (effects.get(i).priority > closest.priority) {
						closest = effects.get(i);
					}
				}
			}
			sortedEffects.add(closest);
			effects.remove(closest);
		}
		effects = sortedEffects;

		if (Inventory.active.contains(selectedUnit) || enemies.contains(selectedUnit)) {
			path.clear();
			path = getPath(cursorPos.x, cursorPos.y);
		}
		if (alphaTo >= 1) {
			if (alpha < alphaTo) {
				alpha = (float) Math.min(alpha + 0.005, alphaTo);
			} else if (titleWaitTimer < titleWaitTime) {
				titleWaitTimer++;
			} else {
				titleWaitTimer = 0;
				switch (action) {

				case ACTION_PROGRESS:
					progressDungeon();
					break;

				case ACTION_RETURN:
					StateTitle.swapToMenu();
					break;

				}
			}
		}

		if (tiles[cursorPos.x][cursorPos.y].defense != 0) {
			if (info.length() > 0) {
				info += " + ";
			}
			info += tiles[cursorPos.x][cursorPos.y].defense + " Def";
		}

		if (PerformanceManager.developerMode) {
			info += " " + tiles[cursorPos.x][cursorPos.y].getProperty() + " " + cursorPos.x + "x " + cursorPos.y + "y" + " status " + tiles[cursorPos.x][cursorPos.y].status + " mp " + tiles[cursorPos.x][cursorPos.y].movementPenalty;
		}
		int lengthTarget = info.length() * 6 + 10;
		if (info.length() > 0 && phase == 0 && menus.size() == 0 && !combatMode && !inCombat && alpha < 0.1 && alpha == alphaTo && alphaTo != 1 && !hideCursor && !swap) {
			Startup.staticView.setPosition(0, 0, 0);
			if (infoLength < lengthTarget) {
				infoLength = Math.min(lengthTarget, infoLength + 10);
			} else if (infoLength > lengthTarget) {
				infoLength = Math.max(0, infoLength - 10);
			}
		} else if (infoLength > 0) {
			infoLength = Math.max(0, infoLength - 10);
		}

		// Manages the event system
		if (events.size() > 0) {
			boolean unitMoving = false;
			for (Unit u : Inventory.loaded.values()) {
				if (u.locX * 16 != u.x || u.locY * 16 != u.y) {
					unitMoving = true;
					break;
				}
			}
			if (!inCombat && (noUIOnScreen() || events.get(0).canProcessDuringMenu()) && !unitMoving && xpAlpha == 0 && displayXP == displayXPTarget) {
				events.get(0).tick();
				if (events.get(0).finished) {
					events.remove(0);
				}
			}
		}

		if (!aiHandler.processing && changePhase && noUIOnScreen() && phase == 1 && !usingItem && xpAlpha == 0 && this.displayXP == displayXPTarget && !multiplayerMode) {
			changePhase(0);
		}

		if (!(usingItem && phase == 1)) {
			if (camX < tcamX) {
				camX += Unit.movementSpeed / 2;
			}
			if (camX > tcamX) {
				camX -= Unit.movementSpeed / 2;
			}
			if (camY < tcamY) {
				camY += Unit.movementSpeed / 2;
			}
			if (camY > tcamY) {
				camY -= Unit.movementSpeed / 2;
			}
			rcamX = Math.floorDiv((int) camX, 8) - 18;
			rcamY = Math.floorDiv((int) camY, 8) - 17;
			if (!aiHandler.processing) {
				Startup.camera.position.setX((float) camX);
				Startup.camera.position.setZ((float) camY);
				Startup.camera.targetPosition.setX((float) camX);
				Startup.camera.targetPosition.setZ((float) camY);
				Startup.camera.targetPosition.setY(42 + (tiles[cursorPos.x][cursorPos.y].getPosition().getY() / 2));
			}
		}

		if (hideInfo) {
			info = "";
		}
	}

	@Override
	public void render() {
		for (int x = 0; x < DungeonGenerator.xActiveSpace; x++) {
			for (int y = 0; y < DungeonGenerator.yActiveSpace; y++) {
				if (rcamX + x < tiles.length && rcamY + y < tiles[0].length && rcamX + x > -1 && rcamY + y > -1) {
					if (Startup.camera.isInsideView(tiles[rcamX + x][rcamY + y].getPosition(), 0.5f, 0.5f, 0.5f, 10)) {
						tiles[rcamX + x][rcamY + y].render(false);
					}
				} else {
					Vector3f position = Vector3f.add(filler.getOffset(), (rcamX + x) * 16,
							(float) Math.round(dungeon.noise.getValue(((float) (rcamX + x) / (tiles.length + DungeonGenerator.xActiveSpace)), ((float) (rcamY + y) / (tiles[0].length + DungeonGenerator.yActiveSpace))) * 10) * 4f,
							(rcamY + y) * 16);
					if (Startup.camera.isInsideView(position, 0.5f, 0.5f, 0.5f, 8)) {
						BatchSorter.add(filler.getModel(), filler.getTexture(), filler.getShader(), filler.getMaterial().toString(), position, Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, true, false);
					}
				}
			}
		}
		for (Item i : items) {
			if (Startup.camera.isInsideView(i.position, 0.5f, 0.5f, 0.5f, 32)) {
				i.render();
			}
		}
		if (!freeRoamMode) {
			for (Unit u : Inventory.active) {
				float height = tiles[u.locX][u.locY].getPosition().getY();
				Vector3f position = new Vector3f(u.x, 13 + height, u.y);
				u.render();
				if (Startup.camera.isInsideView(position, 0.5f, 0.5f, 0.5f, 8) && u.unitColor.getW() > 0) {
					for (int i = 0; i < Math.round(12.0 / (double) u.hp * u.currentHp); i++) {
						BatchSorter.add("pixel.dae", "pixel.png", "main_3d", Material.DEFAULT.toString(), Vector3f.add(position, i - 6, -3, 8), Vector3f.DEFAULT_CAMERA_ROTATION, Vector3f.SCALE_HALF, new Vector4f(0, 0.580f, 1, 1), true,
								false);
					}
					BatchSorter.add("pixel.dae", "pixel.png", "main_3d", Material.DEFAULT.toString(), Vector3f.add(position, -7, -3, 8), Vector3f.DEFAULT_CAMERA_ROTATION, Vector3f.SCALE_HALF, Vector4f.COLOR_BLACK, true, false);
					BatchSorter.add("pixel.dae", "pixel.png", "main_3d", Material.DEFAULT.toString(), Vector3f.add(position, 6, -3, 8), Vector3f.DEFAULT_CAMERA_ROTATION, Vector3f.SCALE_HALF, Vector4f.COLOR_BLACK, true, false);
				}
			}
		} else {
			Inventory.active.get(roamUnit).render();
			if (caught) {
				Vector3f position = new Vector3f(Inventory.active.get(roamUnit).x, 13 + Inventory.active.get(roamUnit).height, Inventory.active.get(roamUnit).y);
				BatchSorter.add("exclamation.dae", "exclamation.png", "main_3d", Material.DEFAULT.toString(), Vector3f.add(position, 9, 14, -4), Vector3f.DEFAULT_INVERSE_CAMERA_ROTATION, Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, false,
						false);
			}
		}
		for (Unit u : enemies) {
			float height = tiles[u.locX][u.locY].getPosition().getY();
			Vector3f position = new Vector3f(u.x, 13 + height, u.y);
			u.render();
			if (Startup.camera.isInsideView(position, 0.5f, 0.5f, 0.5f, 8) && !freeRoamMode && u.unitColor.getW() > 0) {
				for (int i = 0; i < Math.round(12.0 / (double) u.hp * u.currentHp); i++) {
					BatchSorter.add("pixel.dae", "pixel.png", "main_3d", Material.DEFAULT.toString(), Vector3f.add(position, i - 6, -3, 8), Vector3f.DEFAULT_CAMERA_ROTATION, Vector3f.SCALE_HALF, new Vector4f(1, 0.317f, 0.341f, 1), true,
							false);
				}
				BatchSorter.add("pixel.dae", "pixel.png", "main_3d", Material.DEFAULT.toString(), Vector3f.add(position, -7, -3, 8), Vector3f.DEFAULT_CAMERA_ROTATION, Vector3f.SCALE_HALF, Vector4f.COLOR_BLACK, true, false);
				BatchSorter.add("pixel.dae", "pixel.png", "main_3d", Material.DEFAULT.toString(), Vector3f.add(position, 6, -3, 8), Vector3f.DEFAULT_CAMERA_ROTATION, Vector3f.SCALE_HALF, Vector4f.COLOR_BLACK, true, false);
			}
		}
		if (inCombat || healthAlpha > 0) {
			Startup.staticView.setPosition(0, 0, 0);
			Vector3f position = new Vector3f(-38, 42, -80);
			Vector4f color = (Inventory.active.contains(attacker)) ? new Vector4f(0, 0.580f, 1, healthAlpha) : new Vector4f(1, 0.317f, 0.341f, healthAlpha);
			Vector4f fadeColor = new Vector4f(1, 1, 1, healthAlpha);
			for (int i = (int) Math.round(40.0 / (double) attacker.hp * attackerDisplayHealth); i > 0; i--) {
				BatchSorter.add("pixel.dae", "pixel.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, i - 21.5f, -6, 1), Vector3f.EMPTY, Vector3f.SCALE_HALF, color, false, true);
			}
			BatchSorter.add("pixel.dae", "pixel.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, -22.5f, -6, 1), Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_BLACK, false, true);
			BatchSorter.add("pixel.dae", "pixel.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, 19.5f, -6, 1), Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_BLACK, false, true);
			TextRenderer.render(attacker.name, Vector3f.add(position, 3 - attacker.name.length() * 3, 0, 0), Vector3f.EMPTY, Vector3f.SCALE_HALF, fadeColor, true);
			UIBox.render(Vector3f.add(position, -Math.max(attacker.name.length() * 6 + 9, 54) / 2 + 3.5f, 2, -1), Math.max(attacker.name.length() * 6 + 9, 54), 16);

			position = new Vector3f(38, 42, -80);
			color = (Inventory.active.contains(defender)) ? new Vector4f(0, 0.580f, 1, healthAlpha) : new Vector4f(1, 0.317f, 0.341f, healthAlpha);
			for (int i = (int) Math.round(40.0 / (double) defender.hp * defenderDisplayHealth); i > 0; i--) {
				BatchSorter.add("pixel.dae", "pixel.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, i - 21.5f, -6, 1), Vector3f.EMPTY, Vector3f.SCALE_HALF, color, false, true);
			}
			BatchSorter.add("pixel.dae", "pixel.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, -21.5f, -6, 1), Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_BLACK, false, true);
			BatchSorter.add("pixel.dae", "pixel.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, 19.6f, -6, 1), Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_BLACK, false, true);
			TextRenderer.render(defender.name, Vector3f.add(position, 3 - defender.name.length() * 3, 0, 0), Vector3f.EMPTY, Vector3f.SCALE_HALF, fadeColor, true);
			UIBox.render(Vector3f.add(position, -Math.max(defender.name.length() * 6 + 9, 54) / 2 + 3.5f, 2, -1), Math.max(defender.name.length() * 6 + 9, 54), 16);
		}
		if (xpAlpha > 0) {
			Startup.staticView.setPosition(0, 0, 0);
			Vector3f position = new Vector3f(0, -45, -80);
			for (int i = 0; i < displayXP; i++) {
				BatchSorter.add("pixel.dae", "pixel.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, i - 50.5f, 0, 1), Vector3f.EMPTY, Vector3f.SCALE_HALF, new Vector4f(ItemProperty.colorList[1], xpAlpha), false,
						true);
			}
			BatchSorter.add("pixel.dae", "pixel.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, -51.5f, 0, 1), Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_BLACK, false, true);
			BatchSorter.add("pixel.dae", "pixel.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, 49.5f, 0, 1), Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_BLACK, false, true);
			TextRenderer.render(xpGainUnit, Vector3f.add(position, -xpGainUnit.length() * 3 + 3, 4, 1), Vector3f.EMPTY, Vector3f.SCALE_HALF, new Vector4f(1, 1, 1, xpAlpha), true);
			UIBox.render(Vector3f.add(position, -52, 7, -1), 110, 16, new Vector4f(1, 1, 1, xpAlpha));
		}
		for (Effect e : effects) {
			e.render();
		}
		for (Menu m : menus) {
			m.render();
		}
		for (Message m : messages) {
			m.render();
		}
		for (TextBox t : text) {
			t.render();
		}
		if (((phase == 0 || multiplayerMode) || (menus.size() > 0 && menus.get(0).open)) && (!freeRoamMode || (menus.size() > 0 && menus.get(0).open)) && (!hideCursor || (menus.size() > 0 && menus.get(0).open))) {
			cursor.render();
		}
		if ((phase == 0 || multiplayerMode) && !hideCursor && !freeRoamMode) {
			BatchSorter.add("marker.dae", "marker.png", "main_3d_nobloom_texcolor", Material.DEFAULT.toString(), new Vector3f(cursorPos.x * 16, 8.5f + tiles[cursorPos.x][cursorPos.y].getPosition().getY(), cursorPos.y * 16),
					new Vector3f(0, curRotateInt, 0), Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, false, false);
		}

		// Draw movement arrow
		if (Inventory.active.contains(selectedUnit)) {
			for (int i = 0; i < path.size() - 1; i++) {
				int direction = getArrowDirection(i);
				BatchSorter.add("d", "arrow_" + direction + ".dae", "arrow_" + direction + ".png", "main_3d_nobloom_texcolor", Material.DEFAULT.toString(),
						new Vector3f(path.get(i).x * 16, 8.5f + tiles[path.get(i).x][path.get(i).y].getPosition().getY(), path.get(i).y * 16), new Vector3f(0, curRotateInt, 0), Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, false, false);
			}
		}

		if (combatMode && messages.size() == 0) {
			if (selectedAbility.target.equals(ItemAbility.TARGET_ENEMY)) {
				for (Unit u : enemies) {
					if (u.locX == cursorPos.x && u.locY == cursorPos.y && tiles[u.locX][u.locY].status > 0) {
						if (selectedAbility.showCombat) {
							Startup.staticView.setPosition(0, 0, 0);
							CombatCard.renderRightToLeft(new Vector3f(2, 48, -80), selectedUnit, selectedAbility.calculateAttackingDamage(selectedUnit, u, false));
							CombatCard.renderLeftToRight(new Vector3f(4, 48, -80), u, selectedAbility.calculateDefendingDamage(selectedUnit, u, false));
						} else if (selectedAbility.showHealing) {
							Startup.staticView.setPosition(0, 0, 0);
							HealingCard.render(new Vector3f(2, 48, -80), selectedUnit, u, selectedAbility);
						}
					}
				}
			} else if (selectedAbility.target.equals(ItemAbility.TARGET_UNIT)) {
				for (Unit u : Inventory.active) {
					if (u != selectedUnit) {
						if (u.locX == cursorPos.x && u.locY == cursorPos.y && tiles[u.locX][u.locY].status > 0) {
							if (selectedAbility.showCombat) {
								Startup.staticView.setPosition(0, 0, 0);
								CombatCard.renderRightToLeft(new Vector3f(2, 48, -80), selectedUnit, selectedAbility.calculateAttackingDamage(selectedUnit, u, false));
								CombatCard.renderLeftToRight(new Vector3f(4, 48, -80), u, selectedAbility.calculateDefendingDamage(selectedUnit, u, false));
							} else if (selectedAbility.showHealing) {
								Startup.staticView.setPosition(0, 0, 0);
								HealingCard.render(new Vector3f(2, 48, -80), selectedUnit, u, selectedAbility);
							}
						}
					}
				}
			}
		}
		if (phase == 0 && menus.size() == 0 && !combatMode) {
			UIBox.render(new Vector3f(-infoLength / 2 + 1.5f, 93, -160), infoLength + 1, 17);
			TextRenderer.render(info.substring(0, AdvMath.inRange((infoLength - 10) / 6, 0, info.length())), new Vector3f(-infoLength / 2 + 6.5f, 88, -159), Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, true);
		}

		if (action == ACTION_PROGRESS && alpha > 0) {
			BatchSorter.add("fade.dae", "fade.png", "static_3d", Material.MATTE.toString(), Vector3f.add(Vector3f.div(Startup.staticView.position, 14, 8, 1), 0, 0, -81), Vector3f.mul(Startup.staticView.getRotation(), -1, -1, -1),
					new Vector3f(14, 8, 1), new Vector4f(0, 0, 0, alpha), false, true);
			TextRenderer.render(title, Vector3f.add(Startup.staticView.position, -(title.length() - 1) * 3, 4, -80), Vector3f.EMPTY, Vector3f.SCALE_FULL, new Vector4f(1, 1, 1, alpha), true);
			TextRenderer.render(subTitle, Vector3f.add(Startup.staticView.position, -(subTitle.length() - 1) * 3, -4, -80), Vector3f.EMPTY, Vector3f.SCALE_FULL, new Vector4f(1, 1, 1, alpha), true);
		}
		if (swap && noUIOnScreen()) {
			Startup.staticView.position.set(0, 0, 0);
			Vector3f pos = new Vector3f(swapTime - swapTimer, 0, -80).add(phases[phase].length() * -3f, 0, 0);
			TextRenderer.render(phases[phase], pos, Vector3f.EMPTY, Vector3f.SCALE_HALF, phaseColor[phase], true);
			TextRenderer.render(phases[phase], pos.add(-1, 0, -1), Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_BLACK, true);
			TextRenderer.render(phases[phase], pos.add(2, 0, 0), Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_BLACK, true);
			TextRenderer.render(phases[phase], pos.add(-1, -1, 0), Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_BLACK, true);
			TextRenderer.render(phases[phase], pos.add(0, 2, 0), Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_BLACK, true);

			float percent = swapTimer / swapTime;
			Vector3f fadePos = new Vector3f(15 - 15 * percent, 9, -5);
			BatchSorter.add("fade.dae", "fade.png", "static_3d", Material.MATTE.toString(), fadePos, Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_BLACK.copy().setW(0.5f), false, true);
			BatchSorter.add("fade.dae", "fade.png", "static_3d", Material.MATTE.toString(), Vector3f.add(fadePos, -30 + 15 * percent * 2, -18, 0), Vector3f.EMPTY, Vector3f.SCALE_HALF, Vector4f.COLOR_BLACK.copy().setW(0.5f), false, true);
		}
	}

	@Override
	public void onEnter() {
		Startup.screenColorTarget.setW(0);
		aiHandler = new AIHandler(this);
		Startup.camera.speedY = 0.125f;
		Startup.shadowCamera.speedY = 0.125f;
		Startup.shadowCamera.setRotation(new Vector3f(-60, 0, 0));
		InputManager.repeatDelay = 10;
		InputManager.keyTime = 15;
		Unit.movementSpeed = 1f;
	}

	@Override
	public void onExit() {
		task.finish();
		for (Effect e : effects) {
			e.cleanUp();
		}
	}

	private int getArrowDirection(int i) {
		int arrow = 0;
		if (i < path.size() - 1) {
			if (path.get(i + 1).x > path.get(i).x) {
				if (i == 0) {
					arrow = 8;
				} else {
					if (i > 0) {
						if (path.get(i - 1).y > path.get(i).y) {
							arrow = 2;
						} else if (path.get(i - 1).y < path.get(i).y) {
							arrow = 5;
						} else {
							arrow = 1;
						}
					}
				}
			}
			if (path.get(i + 1).x < path.get(i).x) {
				if (i == 0) {
					arrow = 6;
				} else {
					if (i > 0) {
						if (path.get(i - 1).y > path.get(i).y) {
							arrow = 3;
						} else if (path.get(i - 1).y < path.get(i).y) {
							arrow = 4;
						} else {
							arrow = 1;
						}
					}
				}
			}
			if (path.get(i + 1).y > path.get(i).y) {
				if (i == 0) {
					arrow = 9;
				} else {
					if (i > 0) {
						if (path.get(i - 1).x > path.get(i).x) {
							arrow = 2;
						} else if (path.get(i - 1).x < path.get(i).x) {
							arrow = 3;
						} else {
							arrow = 0;
						}
					}
				}
			}
			if (path.get(i + 1).y < path.get(i).y) {
				if (i == 0) {
					arrow = 7;
				} else {
					if (i > 0) {
						if (path.get(i - 1).x > path.get(i).x) {
							arrow = 5;
						} else if (path.get(i - 1).x < path.get(i).x) {
							arrow = 4;
						} else {
							arrow = 0;
						}
					}
				}
			}
		}
		return arrow;
	}

	public void clearUnit() {
		selectedUnit = shellUnit;
		combatMode = false;
		EffectWaterSplash.playSfx = true;
	}

	public void clearAtkDefUnit() {
		attacker = null;
		defender = null;
		EffectWaterSplash.playSfx = true;
	}

	public ArrayList<Point> getPath(int x, int y) {
		ArrayList<Point> path = new ArrayList<Point>();

		if (x < tiles.length * 16 && y < tiles[0].length * 16) {
			if (tiles[x][y].status > 1) {
				path.add(new Point(x, y));
				for (int i = 0; i < path.size(); i++) {
					if (path.get(i).x > 0) {
						if (tiles[path.get(i).x - 1][path.get(i).y].status > tiles[path.get(i).x][path.get(i).y].status && !tiles[path.get(i).x - 1][path.get(i).y].isSolid()) {
							path.add(new Point(path.get(i).x - 1, path.get(i).y));
							continue;
						}
					}
					if (path.get(i).x < tiles.length - 1) {
						if (tiles[path.get(i).x + 1][path.get(i).y].status > tiles[path.get(i).x][path.get(i).y].status && !tiles[path.get(i).x + 1][path.get(i).y].isSolid()) {
							path.add(new Point(path.get(i).x + 1, path.get(i).y));
							continue;
						}
					}
					if (path.get(i).y > 0) {
						if (tiles[path.get(i).x][path.get(i).y - 1].status > tiles[path.get(i).x][path.get(i).y].status && !tiles[path.get(i).x][path.get(i).y - 1].isSolid()) {
							path.add(new Point(path.get(i).x, path.get(i).y - 1));
							continue;
						}
					}
					if (path.get(i).y < tiles[0].length - 1) {
						if (tiles[path.get(i).x][path.get(i).y + 1].status > tiles[path.get(i).x][path.get(i).y].status && !tiles[path.get(i).x][path.get(i).y + 1].isSolid()) {
							path.add(new Point(path.get(i).x, path.get(i).y + 1));
							continue;
						}
					}
				}
			}
		}

		return path;
	}

	public void changePhase(int phase) {
		if (this.phase != phase) {
			this.phase = phase;
			for (Unit u : Inventory.active) {
				u.setTurn(true);
			}
			for (Unit u : enemies) {
				if (!u.getStatus().equals(Unit.STATUS_SLEEP) || phase == 1) {
					u.setTurn(true);
				}
			}
			if (phase == 0) {
				currentTurn++;
				for (Unit u : Inventory.active) {
					u.clearStatChanges();
					if (tiles[u.locX][u.locY].isLavaLogged()) {
						u.hurt((int) Math.ceil(u.hp / 10.0));
					}
					if (tiles[u.locX][u.locY].healLogged) {
						u.heal((int) Math.ceil(u.hp / 10.0));
					}
				}
				Startup.corruptionTarget = Math.min(1, Startup.corruptionTarget + corruptionRate);
			}
			if (phase == 1) {
				if (!multiplayerMode) {
					aiHandler.startProcessing();
				} else {
					comThread.eventQueue.add(new String[] { "CHANGE_PHASE", "0" });
				}
				for (Unit u : enemies) {
					u.clearStatChanges();
					if (tiles[u.locX][u.locY].isLavaLogged()) {
						u.hurt((int) Math.ceil(u.hp / 10.0));
					}
					if (tiles[u.locX][u.locY].healLogged) {
						u.heal((int) Math.ceil(u.hp / 10.0));
					}
				}
			}
			swap = true;
		}
	}

	public void setCombat(Unit atker, Unit defder) {
		attacker = atker;
		defender = defder;

		atkRounds = 0;
		defRounds = 0;

		atkRounds = selectedAbility.calculateAttackingDamage(attacker, defender, false)[1];
		defRounds = selectedAbility.calculateDefendingDamage(attacker, defender, false)[1];

		attacker.waitTimer = attacker.waitTime;
		defender.waitTimer = defender.waitTime;

		attackerDisplayHealth = attacker.currentHp;
		defenderDisplayHealth = defender.currentHp;

		inCombat = true;
		lastRound = false;

		if (selectedAbility != ItemAbility.ITEM_ABILITY_ATTACK) {
			StateManager.currentState.effects.add(
					new EffectText(selectedAbility.toString(), new Vector3f(attacker.x - selectedAbility.toString().length() * 1.5f, 20 + StateManager.currentState.tiles[attacker.locX][attacker.locY].getPosition().getY(), attacker.y - 8),
							new Vector4f(ItemProperty.colorList[0], 1)));
			StateManager.currentState.effects.add(new EffectEnergize(new Vector3f(atker.x, 10 + StateManager.currentState.tiles[atker.locX][atker.locY].getPosition().getY(), atker.y), Vector4f.COLOR_BASE));
		}
	}

	public void attack() {
		if (attacker.inCombat) {
			effects.add(EffectBattle.getEffect(attacker.weapon.animation, new Vector3f(defender.x, 16 + tiles[defender.locX][defender.locY].getPosition().getY(), defender.y), defender.dir == 1 ? true : false));
			defender.hurt(selectedAbility.calculateAttackingDamage(attacker, defender, false)[0]);
			selectedAbility.onHit(this);
			if (defender.dead) {
				atkRounds = 0;
				defRounds = 0;
			}
		} else {
			effects.add(EffectBattle.getEffect(defender.weapon.animation, new Vector3f(attacker.x, 16 + tiles[attacker.locX][attacker.locY].getPosition().getY(), attacker.y), attacker.dir == 1 ? true : false));
			attacker.hurt(selectedAbility.calculateDefendingDamage(attacker, defender, false)[0]);
			selectedAbility.onRetaliation(this);
			if (attacker.dead) {
				atkRounds = 0;
				defRounds = 0;
			}
		}
	}

	public void setCamera(int x, int y) {
		tcamX = x;
		tcamY = y;
		camX = tcamX;
		camY = tcamY;
	}

	// This recursively calls itself for surrounding tiles instead of iterating over
	// the entire map
	public void highlightTiles(int x, int y, int status, int range, String type) {
		if (status > 1 || range > 0) {

			boolean onUnit = false;
			switch (type) {

			case "unit":
				for (Unit w : enemies) {
					if (x == w.locX && y == w.locY) {
						onUnit = true;
					}
				}
				break;

			case "enemy":
				for (Unit w : Inventory.active) {
					if (x == w.locX && y == w.locY) {
						onUnit = true;
					}
				}
				break;

			}

			if (tiles[x][y].isSolid() || onUnit) {
				status = 1;
			}

			tiles[x][y].status = status;
			tiles[x][y].range = range;

			if (x > 0) {
				if (tiles[x - 1][y].status < status - tiles[x - 1][y].movementPenalty || (tiles[x - 1][y].status <= 1 && tiles[x - 1][y].range < range)) {
					highlightTiles(x - 1, y, (status > 1) ? Math.max(status - tiles[x - 1][y].movementPenalty, 1) : 1, (status > 1) ? range : range - 1, type);
				}
			}
			if (x < tiles.length - 1) {
				if (tiles[x + 1][y].status < status - tiles[x + 1][y].movementPenalty || (tiles[x + 1][y].status <= 1 && tiles[x + 1][y].range < range)) {
					highlightTiles(x + 1, y, (status > 1) ? Math.max(status - tiles[x + 1][y].movementPenalty, 1) : 1, (status > 1) ? range : range - 1, type);
				}
			}
			if (y > 0) {
				if (tiles[x][y - 1].status < status - tiles[x][y - 1].movementPenalty || (tiles[x][y - 1].status <= 1 && tiles[x][y - 1].range < range)) {
					highlightTiles(x, y - 1, (status > 1) ? Math.max(status - tiles[x][y - 1].movementPenalty, 1) : 1, (status > 1) ? range : range - 1, type);
				}
			}
			if (y < tiles[0].length - 1) {
				if (tiles[x][y + 1].status < status - tiles[x][y + 1].movementPenalty || (tiles[x][y + 1].status <= 1 && tiles[x][y + 1].range < range)) {
					highlightTiles(x, y + 1, (status > 1) ? Math.max(status - tiles[x][y + 1].movementPenalty, 1) : 1, (status > 1) ? range : range - 1, type);
				}
			}
		}
	}

	public void showEnemyRange() {
		if (!Inventory.active.contains(selectedUnit)) {
			if (!showingRange) {
				showingRange = true;
				AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
				for (Unit u : enemies) {
					highlightTiles(u.locX, u.locY, u.movement, u.weapon.range, "enemy");
				}
			} else if (!Inventory.active.contains(selectedUnit)) {
				showingRange = false;
				clearSelectedTiles();
				clearUnit();
				AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
			}
		}
	}

	public static void initUnits() {
		for (Unit u : Inventory.active) {
			u.prepare();
		}
	}

	public void refreshUnits() {
		for (Unit u : Inventory.active) {
			if (!u.dead) {
				u.lastWeapon = u.weapon.id;
				u.unitColor = new Vector4f(1, 1, 1, 1);
				u.setTurn(true);

				u.currentHp = Math.min(u.hp, u.currentHp + u.hp / 2);
			}
		}
	}

	public static void startDungeon(String pattern, int level, long seed) {
		setUpDungeon(new DungeonGenerator(pattern, level, seed));
		initUnits();
	}

	public static void startMultiplayerDungeon(String pattern, int level, long seed, boolean starting, Unit[] enemies, Socket socket, GameInputStream in, GameOutputStream out, String name, String opponent) {
		setUpMultiplayerDungeon(pattern, level, seed, starting, enemies.clone(), socket, in, out, name, opponent);
		initUnits();
	}

	public void progressDungeon() {
		setUpDungeon((dungeon.baseLevel + 2 < dungeon.getBottomFloor()) ? new DungeonGenerator(dungeon.getPattern(), dungeon.baseLevel + 1, dungeon.seed).start()
				: new DungeonGeneratorRestArea(dungeon.getPattern(), dungeon.baseLevel + 1, dungeon.seed));
		refreshUnits();
	}

	private static void setUpDungeon(DungeonGenerator dungeon) {
		StateDungeon state = new StateDungeon();
		StateManager.initState(state);
		StateManager.swapState(STATE_ID);

		state.dungeon = dungeon.start();
		state.tiles = state.dungeon.tiles;
		state.filler = Tile.getTile(new String[] { state.dungeon.getPalletTag(), Tile.TAG_FILLER }, new Vector3f(-1, -1, -1));
		state.items = state.dungeon.items;

		state.enemies = state.dungeon.enemyPlacements;
		for (Unit u : state.enemies) {
			u.prepare();
		}

		state.cursorPos = new Point(Inventory.active.get(Inventory.active.size() - 1).locX, Inventory.active.get(Inventory.active.size() - 1).locY);
		state.cursor = new Cursor(new Vector3f(state.cursorPos.x * 16, 32, state.cursorPos.y * 16));
		state.setCamera((int) state.cursor.position.getX() / 2, (int) state.cursor.position.getZ() / 2 + 16);
		Startup.camera.setPosition(new Vector3f((float) state.camX, 32, (float) state.camY));
		Startup.camera.setRotation(new Vector3f(-60, 0, 0));
		Startup.shadowCamera.setPosition(new Vector3f(Startup.camera.position.getX(), 80 + state.tiles[state.cursorPos.x][state.cursorPos.y].getPosition().getY() / 2, Startup.camera.position.getZ() + 5));

		state.setEffects(dungeon.getEffectTags(), dungeon.getEffectVars());

		state.title = state.dungeon.getName();
		state.subTitle = (state.dungeon.baseLevel + 1) + "f";

		float[] screen = state.dungeon.getScreenColor();
		Startup.screenColor.set(screen[0], screen[0], screen[0], Startup.screenColor.getW());
		Startup.screenColorTarget.set(screen[0], screen[0], screen[0], Startup.screenColorTarget.getW());

		TaskThread.process(state.task);
	}

	public static void loadMap(String map) {
		File f = new File(StateCreatorHub.LOAD_PATH + "/" + map + "/map.dat");

		if (f.exists()) {
			try {
				StateDungeon state = new StateDungeon();
				StateManager.initState(state);
				StateManager.swapState(STATE_ID);

				DataInputStream in = new DataInputStream(new FileInputStream(f));

				long seed = in.readLong();
				String pallet = in.readUTF();
				int width = in.readInt();
				int height = in.readInt();

				state.dungeon = new DungeonGeneratorVillage(width, height, pallet, seed).start();
				state.filler = Tile.getTile(new String[] { state.dungeon.getPalletTag(), Tile.TAG_FILLER }, new Vector3f(0, 0, 0));

				ArrayList<Point> waterPoints = new ArrayList<>();
				ArrayList<Point> lavaPoints = new ArrayList<>();
				state.tiles = new Tile[width][height];
				for (int x = 0; x < width; x++) {
					for (int y = 0; y < height; y++) {
						state.tiles[x][y] = Tile.getTile(Tile.getProperty(in.readUTF()), new Vector3f(in.readFloat() / 16, in.readFloat() / 16, in.readFloat() / 16));
						state.tiles[x][y].setRotation(in.readFloat(), in.readFloat(), in.readFloat());

						if (in.readBoolean()) {
							waterPoints.add(new Point(x, y));
						}
						if (in.readBoolean()) {
							lavaPoints.add(new Point(x, y));
						}
						if (in.readBoolean()) {
							EffectHealAura effect = new EffectHealAura(state.tiles[x][y]);
							state.effects.add(effect);
						}
					}
				}
				for (Point p : waterPoints) {
					EffectWater e = new EffectWater(state.tiles[p.x][p.y], new Vector3f(0.427f, 0.765f, 0.9f), state.tiles, waterPoints);
					state.effects.add(e);
				}
				for (Point p : lavaPoints) {
					EffectLava e = new EffectLava(state.tiles[p.x][p.y], state.tiles, lavaPoints);
					state.effects.add(e);
				}

				int length = in.readInt();
				for (int i = 0; i < length; i++) {
					String uuid = in.readUTF();
					File unit = new File(StateCreatorHub.LOAD_PATH + "/" + map + "/unit/" + uuid + ".dat");
					if (unit.exists()) {
						state.enemies.add(Unit.loadUnit(in.readInt(), in.readInt(), Vector4f.COLOR_BASE.copy(), StateCreatorHub.LOAD_PATH + "/" + map + "/unit/" + uuid + ".dat"));
					} else {
						System.out.println("[StsteDungeon]: The unit file for " + StateCreatorHub.LOAD_PATH + "/" + map + "/" + uuid + " is missing");
					}
				}
				for (Unit u : state.enemies) {
					u.prepare();
				}

				state.startPositions = new Point[in.readInt()];
				for (int i = 0; i < state.startPositions.length; i++) {
					state.startPositions[i] = new Point(in.readInt(), in.readInt());
				}
				for (int i = 0; i < Math.min(state.startPositions.length, Inventory.active.size()); i++) {
					Inventory.active.get(i).placeAt(state.startPositions[i].x, state.startPositions[i].y);
				}

				in.close();

				if (Inventory.active.size() > 0) {
					state.cursorPos = new Point(Inventory.active.get(Inventory.active.size() - 1).locX, Inventory.active.get(Inventory.active.size() - 1).locY);
				} else {
					state.cursorPos = new Point(state.startPositions[0].x, state.startPositions[0].y);
				}
				state.cursor = new Cursor(new Vector3f(state.cursorPos.x * 16, 32, state.cursorPos.y * 16));
				state.setCamera((int) state.cursor.position.getX() / 2, (int) state.cursor.position.getZ() / 2 + 16);
				Startup.camera.setPosition(new Vector3f((float) state.camX, 32, (float) state.camY));
				Startup.camera.setRotation(new Vector3f(-60, 0, 0));
				Startup.shadowCamera.setPosition(new Vector3f(Startup.camera.position.getX(), 80 + state.tiles[state.cursorPos.x][state.cursorPos.y].getPosition().getY() / 2, Startup.camera.position.getZ() + 5));

				state.setEffects(state.dungeon.getEffectTags(), state.dungeon.getEffectVars());

				for (File events : FileManager.getFiles(StateCreatorHub.LOAD_PATH + "/" + map + "/event")) {
					if (events.getName().endsWith(".json")) {
						state.eventList.put(events.getName(), EventScript.loadScript(events));
					}
				}

				for (EventScript e : state.eventList.values()) {
					if (e.activator.equals(EventScript.ACTIVATOR_MAP_LOAD)) {
						state.events.add(new Event(e));
					}
				}

				float[] screen = state.dungeon.getScreenColor();
				Startup.screenColor.set(screen[0], screen[0], screen[0], Startup.screenColor.getW());
				Startup.screenColorTarget.set(screen[0], screen[0], screen[0], Startup.screenColorTarget.getW());

				TaskThread.process(state.task);
			} catch (Exception e) {
				e.printStackTrace();
			}

			initUnits();
		}
	}

	private static void setUpMultiplayerDungeon(String map, int floor, long seed, boolean starting, Unit[] enemies, Socket socket, GameInputStream in, GameOutputStream out, String name, String opponent) {
		StateDungeon state = new StateDungeon();
		StateManager.initState(state);
		StateManager.swapState(STATE_ID);

		state.multiplayerMode = true;
		state.comThread = new MatchComThread(socket, in, out, state);
		if (!starting) {
			state.phase = 1;
		}

		DungeonGeneratorMultiplayer dungeon = new DungeonGeneratorMultiplayer(map, floor, seed);

		state.dungeon = dungeon.start();
		state.tiles = state.dungeon.tiles;
		state.filler = Tile.getTile(new String[] { state.dungeon.getPalletTag(), Tile.TAG_FILLER }, new Vector3f(-1, -1, -1));
		state.items = state.dungeon.items;

		int offset = (starting) ? 0 : enemies.length;
		Point[] placements = dungeon.generatePlacements(enemies.length);

		for (int i = 0; i < Inventory.active.size(); i++) {
			Inventory.active.get(i).placeAt(placements[i + offset].x, placements[i + offset].y);
			Inventory.active.get(i).prepare();
		}

		for (int i = 0; i < enemies.length; i++) {
			state.enemies.add(enemies[i]);
			enemies[i].placeAt(placements[i - offset + enemies.length].x, placements[i - offset + enemies.length].y);
			enemies[i].prepare();
			Inventory.loaded.put(enemies[i].uuid, enemies[i]);
		}

		Unit focalUnit = (starting) ? Inventory.active.get(Inventory.active.size() - 1) : enemies[enemies.length - 1];
		state.cursorPos = new Point(focalUnit.locX, focalUnit.locY);
		state.cursor = new Cursor(new Vector3f(state.cursorPos.x * 16, 32, state.cursorPos.y * 16));
		state.setCamera((int) state.cursor.position.getX() / 2, (int) state.cursor.position.getZ() / 2 + 16);
		Startup.camera.setPosition(new Vector3f((float) state.camX, 32, (float) state.camY));
		Startup.camera.setRotation(new Vector3f(-60, 0, 0));
		Startup.shadowCamera.setPosition(new Vector3f(Startup.camera.position.getX(), 80 + state.tiles[state.cursorPos.x][state.cursorPos.y].getPosition().getY() / 2, Startup.camera.position.getZ() + 5));

		state.setEffects(state.dungeon.getEffectTags(), state.dungeon.getEffectVars());

		state.title = state.dungeon.getName();
		state.subTitle = name + " vs " + opponent;

		float[] screen = state.dungeon.getScreenColor();
		Startup.screenColor.set(screen[0], screen[0], screen[0], Startup.screenColor.getW());
		Startup.screenColorTarget.set(screen[0], screen[0], screen[0], Startup.screenColorTarget.getW());

		TaskThread.process(state.task);
		new Thread(state.comThread).start();
	}

	private void setEffects(String[] tags, double[][] vars) {
		Startup.enableHaze = false;
		for (int i = 0; i < tags.length; i++) {
			switch (tags[i]) {

			case DungeonGenerator.TAG_LIGHT_MAIN:
				overheadLight = new Light(Startup.shadowCamera.position.copy(), new Vector3f((float) vars[i][0], (float) vars[i][1], (float) vars[i][2]), (float) vars[i][3], (float) vars[i][4], (float) vars[i][5], Vector3f.DOWN);
				lights.add(overheadLight);
				break;

			case DungeonGenerator.TAG_EFFECT_SNOW:
				effects.add(new EffectDungeonSnow());
				break;

			case DungeonGenerator.TAG_EFFECT_LAVA:
				effects.add(new EffectDungeonLavaSFX());
				break;

			case DungeonGenerator.TAG_EFFECT_RAIN:
				effects.add(new EffectDungeonRain(overheadLight));
				break;

			case DungeonGenerator.TAG_EFFECT_CORRUPTION:
				Startup.corruptionTarget = (float) vars[i][0];
				corruptionRate = (float) vars[i][1];
				break;

			case DungeonGenerator.TAG_EFFECT_RESIDUE:
				effects.add(new EffectDungeonResidue());
				break;

			case DungeonGenerator.TAG_EFFECT_HEAT:
				Startup.enableHaze = true;
				break;

			}
		}
	}

	public boolean noUIOnScreen() {
		return (text.size() == 0 && menus.size() == 0 && messages.size() == 0);
	}

	public void enterGridMode() {
		cursorPos = new Point(Inventory.active.get(roamUnit).locX, Inventory.active.get(roamUnit).locY);
		cursor.warpLocation((float) cursorPos.getX() * 16, cursor.position.getY(), (float) cursorPos.getY() * 16);
		cursorFloat = false;
		InputManager.repeatDelay = 10;
		InputManager.keyTime = 15;
		Unit.movementSpeed = 1f;
		clearSelectedTiles();
		clearUnit();

		freeRoamMode = false;

		effects.add(new EffectPoof(new Vector3f(Inventory.active.get(roamUnit).x, 13 + Inventory.active.get(roamUnit).height, Inventory.active.get(roamUnit).y + 2)));

		// Get and set unit positions
		// Get loop count based on distance from the center of the map
		Unit location = Inventory.active.get(roamUnit);
		int widthCenter = tiles.length / 2;
		int heightCenter = tiles[0].length / 2;
		int loopCount = Math.max(widthCenter + Math.abs(widthCenter - location.locX), heightCenter + Math.abs(heightCenter - location.locY));

		// Loop through "rings" of tiles around the main unit, search for valid
		// positions, then place the units
		int currentUnit = 0;
		mainLoop: // Label to break out of nested for loops when the last unit is placed
		for (int i = 0; i < loopCount; i++) {
			for (int x = 0; x < 3 + i * 2; x++) {
				for (int y = 0; y < 3 + i * 2; y++) {
					if (currentUnit >= Inventory.active.size()) {
						break mainLoop; // Break out of every loop
					}
					if (currentUnit == roamUnit) {
						currentUnit++;
						continue;
					}
					int locX = location.locX - i + x - 1;
					int locY = location.locY - i + y - 1;
					boolean a = locX % 2 != location.locX % 2;
					boolean b = locY % 2 != location.locY % 2;
					if ((a && b) || (!a && !b)) {
						if (locX > -1 && locX < tiles.length && locY > -1 && locY < tiles[0].length) {
							if (isTileAvailable(locX, locY)) {
								Inventory.active.get(currentUnit).placeAt(locX, locY);
								currentUnit++;
							}
						}
					}
				}
			}
		}

		// Add effects
		for (Unit u : Inventory.active) {
			effects.add(new EffectBurst(new Vector3f(Inventory.active.get(roamUnit).x, 13 + Inventory.active.get(roamUnit).height, Inventory.active.get(roamUnit).y), u));
		}
		for (Unit u : enemies) {
			effects.add(new EffectPoof(new Vector3f(u.x, 13 + u.height, u.y + 2)));
		}
	}

	public void enterFreeRoam() {
		// This is to make walking smoother
		// The normal values for these is 10 and 15
		InputManager.repeatDelay = 0;
		InputManager.keyTime = 30;

		Unit.movementSpeed = 0.5f;

		freeRoamMode = true;
		Unit focus = Inventory.active.get(roamUnit);

		tcamX = (int) focus.x / 2;
		tcamY = (int) focus.y / 2 + 24;
		cursorPos.setLocation(focus.locX, focus.locY);

		for (Unit u : Inventory.active) {
			if (u != focus) {
				effects.add(new EffectPoof(new Vector3f(u.x, 13 + u.height, u.y + 2)));
				u.placeAt(0, 0);
			}
		}
		for (Unit u : enemies) {
			effects.add(new EffectPoof(new Vector3f(u.x, 13 + u.height, u.y + 2)));
		}
	}

	public void processFreeRoamMovement() {
		for (Unit u : enemies) {
			Point p = u.AIPattern.getDefaultRoamLocation(u, this);
			u.locX = p.x;
			u.locY = p.y;

			Unit target = Inventory.active.get(roamUnit);
			if (Math.abs(u.locX - target.locX) + Math.abs(u.locY - target.locY) <= 1) {
				caught = true;
			}
		}
	}

	@Override
	@SuppressWarnings("incomplete-switch")
	public void onKeyPress(InputMapping key) {
		switch (key) {

		case JOYSTICK:
			if (noUIOnScreen() && !controlLock && phase == 0 && Startup.screenColor.getW() == alphaTo) {
				cursorFloat = true;
				float[] axes = key.getAxes();
				if (!freeRoamMode) {
					int cursorXNow = (int) ((cursor.position.getX() + 4) / 16);
					int cursorXNext = (int) ((cursor.position.getX() + 4 + axes[0]) / 16);
					int cursorYNow = (int) ((cursor.position.getZ() + 16) / 16);
					int cursorYNext = (int) ((cursor.position.getZ() + 16 + axes[1]) / 16);

					if (cursorXNow != cursorXNext || cursorYNow != cursorYNext) {
						if (cursorXNext > -1 && cursorYNext > -1 && cursorXNext < tiles.length && cursorYNext < tiles[0].length) {
							AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
							cursorPos.x = cursorXNext;
							cursorPos.y = cursorYNext;
							if (multiplayerMode) {
								comThread.eventQueue.add(new String[] { "MOVE_CURSOR" });
							}
						}
					}
					if ((cursor.targetPosition.getX() > -16 || axes[0] > 0) && (cursor.targetPosition.getX() < tiles.length * 16 - 16 || axes[0] < 0)) {
						cursor.targetPosition.add(axes[0] * cursor.speed, 0, 0);
						cursor.position.add(axes[0] * cursor.speed, 0, 0);
					}

					if ((cursor.targetPosition.getZ() > -16 || axes[1] > 0) && (cursor.targetPosition.getZ() < tiles[0].length * 16 - 24 || axes[1] < 0)) {
						cursor.targetPosition.add(0, 0, axes[1] * cursor.speed);
						cursor.position.add(0, 0, axes[1] * cursor.speed);
					}
					cursor.targetPosition.setY(34 + tiles[cursorPos.x][cursorPos.y].getPosition().getY());

					if (axes[0] < 0 && cursor.position.getX() / 2 - tcamX < -24) {
						tcamX += axes[0] / 2;
					}
					if (axes[0] > 0 && cursor.position.getX() / 2 - tcamX > 24) {
						tcamX += axes[0] / 2;
					}
					if (axes[1] < 0 && cursor.position.getZ() / 2 - tcamY < -48) {
						tcamY += axes[1] / 2;
					}
					if (axes[1] > 0 && cursor.position.getZ() / 2 - tcamY > -8) {
						tcamY += axes[1] / 2;
					}
				} else if (!caught) {
					Unit u = Inventory.active.get(roamUnit);
					if (u.x == u.locX * 16 && u.y == u.locY * 16) {
						if (axes[0] > 0.25) {
							if (isTileWalkable(u.locX + 1, u.locY)) {
								u.locX++;
								cursorPos.x = u.locX;
								tcamX += 8;
								processFreeRoamMovement();
							}
						}
						if (axes[0] < -0.25) {
							if (isTileWalkable(u.locX - 1, u.locY)) {
								u.locX--;
								cursorPos.x = u.locX;
								tcamX -= 8;
								processFreeRoamMovement();
							}
						}
						if (axes[1] > 0.25) {
							if (isTileWalkable(u.locX, u.locY + 1)) {
								u.locY++;
								cursorPos.y = u.locY;
								tcamY += 8;
								processFreeRoamMovement();
							}
						}
						if (axes[1] < -0.25) {
							if (isTileWalkable(u.locX, u.locY - 1)) {
								u.locY--;
								cursorPos.y = u.locY;
								tcamY -= 8;
								processFreeRoamMovement();
							}
						}
					}
				}
			}
			break;

		case MISC:
			for (Unit u : Inventory.active) {
				u.addItem(ItemProperty.get("item.material.gem.ruby"));
				u.addItem(ItemProperty.get("item.material.gem.onyx"));
				u.addItem(ItemProperty.get("item.material.gem.diamond"));
				u.addItem(ItemProperty.get("item.material.bar.steel"));
				u.addItem(ItemProperty.get("item.material.bar.gold"));
				u.accessory = ItemProperty.get("item.accessory.mirror.medal");
			}
			for (Unit u : enemies) {
				u.addItem(ItemProperty.get("item.sword.gold"));
			}
//			enterFreeRoam();
			break;

		case HIGHLIGHT:
			if (noUIOnScreen() && !controlLock && phase == 0 && Startup.screenColor.getW() == alphaTo) {
				showEnemyRange();
			}
			break;

		case WEAPON_LEFT:
			if (combatMode && messages.size() == 0) {
				if (selectedAbility.target.equals(ItemAbility.TARGET_ENEMY)) {
					for (Unit u : enemies) {
						if (u.locX == cursorPos.x && u.locY == cursorPos.y && tiles[u.locX][u.locY].status > 0) {
							if (selectedAbility.showCombat) {
								do {
									for (ItemProperty i : selectedUnit.getItemList()) {
										if (i.type.equals(ItemProperty.TYPE_WEAPON)) {
											ItemProperty weapon = selectedUnit.weapon;
											selectedUnit.weapon = i;
											selectedUnit.removeItem(i);
											selectedUnit.addItem(weapon);
											AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);

											clearSelectedTiles();
											highlightTiles(selectedUnit.locX, selectedUnit.locY, 1, i.range + 1, "");
											break;
										}
									}
								} while (u.locX == cursorPos.x && u.locY == cursorPos.y && tiles[u.locX][u.locY].status == 0);
								break;
							}
						}
					}
				}
			}
			if (freeRoamMode) {
				if (!noUIOnScreen()) {
					break;
				}
				effects.add(new EffectPoof(new Vector3f(Inventory.active.get(roamUnit).x, 13 + Inventory.active.get(roamUnit).height, Inventory.active.get(roamUnit).y + 2)));
				int nextUnit = roamUnit;
				if (roamUnit > 0) {
					nextUnit--;
				} else {
					nextUnit = Inventory.active.size() - 1;
				}
				Inventory.active.get(nextUnit).placeAt(Inventory.active.get(roamUnit).locX, Inventory.active.get(roamUnit).locY);
				Inventory.active.get(nextUnit).height = Inventory.active.get(roamUnit).height;
				roamUnit = nextUnit;
				AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
			}

			break;

		case WEAPON_RIGHT:
			if (combatMode && messages.size() == 0) {
				if (selectedAbility.target.equals(ItemAbility.TARGET_ENEMY)) {
					for (Unit u : enemies) {
						if (u.locX == cursorPos.x && u.locY == cursorPos.y && tiles[u.locX][u.locY].status > 0) {
							if (selectedAbility.showCombat) {
								do {
									for (int i = selectedUnit.getItemList().size() - 1; i >= 0; i--) {
										ItemProperty item = selectedUnit.getItemList().get(i);
										if (item.type.equals(ItemProperty.TYPE_WEAPON)) {
											ItemProperty weapon = selectedUnit.weapon;
											selectedUnit.weapon = item;
											selectedUnit.removeItem(item);
											selectedUnit.addItemInFront(weapon);
											AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);

											clearSelectedTiles();
											highlightTiles(selectedUnit.locX, selectedUnit.locY, 1, item.range + 1, "");
											break;
										}
									}
								} while (u.locX == cursorPos.x && u.locY == cursorPos.y && tiles[u.locX][u.locY].status == 0);
								break;
							}
						}
					}
				}
			}
			if (freeRoamMode) {
				if (!noUIOnScreen()) {
					break;
				}
				effects.add(new EffectPoof(new Vector3f(Inventory.active.get(roamUnit).x, 13 + Inventory.active.get(roamUnit).height, Inventory.active.get(roamUnit).y + 2)));
				int nextUnit = roamUnit;
				if (roamUnit < Inventory.active.size() - 1) {
					nextUnit++;
				} else {
					nextUnit = 0;
				}
				Inventory.active.get(nextUnit).placeAt(Inventory.active.get(roamUnit).locX, Inventory.active.get(roamUnit).locY);
				Inventory.active.get(nextUnit).height = Inventory.active.get(roamUnit).height;
				roamUnit = nextUnit;
				AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
			}
			break;

		case UP:
			if (menuLock) {
				break;
			}
			if (menus.size() > 0) {
				menus.get(0).move(-1);
			}
			break;

		case DOWN:
			if (menuLock) {
				break;
			}
			if (menus.size() > 0) {
				menus.get(0).move(1);
			}
			break;

		case CONFIRM:
			if (alpha < 0.95 && swapTimer == 0 && displayXP == displayXPTarget && xpAlpha == 0) {
				if (messages.size() == 0) {
					if (text.size() == 0) {
						if (menus.size() == 0) {
							if (phase == 0 && !controlLock) {
								if (!freeRoamMode) {
									if (!Inventory.active.contains(selectedUnit)) {
										if (!inCombat) {
											for (Unit u : Inventory.active) {
												if (cursorPos.x == u.locX && cursorPos.y == u.locY) {
													if (u.hasTurn) {
														clearSelectedTiles();
														u.select();
														curRotate = true;
														AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
														break;
													}
												}
											}
											for (Unit u : enemies) {
												if (cursorPos.x == u.locX && cursorPos.y == u.locY) {
													if (u != selectedUnit) {
														u.select();
														curRotate = true;
														AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
														break;
													} else {
														menus.add(new MenuStatusCard(new Vector3f(0, 0, -80), selectedUnit, true));
														Startup.staticView.position = new Vector3f(126 / 4, -90 / 4 + 1, 0);
														AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
													}
												}
											}
											if (!Inventory.active.contains(selectedUnit) && !enemies.contains(selectedUnit)) {
												menus.add(new MenuDungeonMain(new Vector3f(0, 0, -80)));
											}
										}
									} else {
										if (!combatMode) {
											boolean notOnUnit = true;
											for (Unit u : Inventory.active) {
												if ((u.locX == cursorPos.getX() && u.locY == cursorPos.getY()) && u != selectedUnit) {
													notOnUnit = false;
													break;
												}
											}
											if (path.size() > 0 && notOnUnit) {
												selectedUnit.move(path);
												curRotate = true;
												clearSelectedTiles();
												highlightTiles(path.get(0).x, path.get(0).y, 1, selectedUnit.weapon.range + 1, "");
												menus.add(new MenuDungeonAction(new Vector3f(0, 0, -80), selectedUnit, this));
												if (multiplayerMode) {
													comThread.eventQueue.add(new String[] { "MOVE_UNIT_PATH", selectedUnit.uuid, "" + path.get(0).x, "" + path.get(0).y });
													comThread.eventQueue.add(new String[] { "CLEAR_TILE_HIGHLIGHT" });
													comThread.eventQueue.add(new String[] { "HIGHLIGHT_TILES", "" + path.get(0).x, "" + path.get(0).y, "1", "" + (selectedUnit.weapon.range + 1), "" });
												}
											}
										} else {
											switch (selectedAbility.target) {

											case ItemAbility.TARGET_ENEMY:
												for (Unit u : enemies) {
													if (cursorPos.x == u.locX && cursorPos.y == u.locY && tiles[cursorPos.x][cursorPos.y].range > 0 && selectedAbility.followUp(u, this)) {
														combatMode = false;
														clearSelectedTiles();
														curRotate = true;
														AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
													}
												}
												break;

											case ItemAbility.TARGET_UNIT:
												for (Unit u : Inventory.active) {
													if (cursorPos.x == u.locX && cursorPos.y == u.locY && u != selectedUnit && tiles[cursorPos.x][cursorPos.y].range > 0 && selectedAbility.followUp(u, this)) {
														combatMode = false;
														clearSelectedTiles();
														curRotate = true;
														AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
													}
												}
												break;

											default:
												if (selectedAbility.followUp(selectedUnit, this)) {
													combatMode = false;
													clearSelectedTiles();
													curRotate = true;
													AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
												}
												break;

											}
										}
									}
								} else if (!caught) {
									selectedUnit = Inventory.active.get(roamUnit);
									selectedUnit.path.add(new Point(selectedUnit.locX, selectedUnit.locY));
									menus.add(new MenuDungeonAction(new Vector3f(0, 0, -80), Inventory.active.get(roamUnit), this));
								}

							}
						} else if (!menuLock) {
							menus.get(0).action("", selectedUnit);
						}
					}
				} else {
					messages.get(0).close();
					AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
				}
			}
			break;

		case BACK:
			if (alpha < 0.95) {
				if (messages.size() == 0) {
					if (text.size() == 0) {
						if (!controlLock) {
							if (menus.size() == 0) {
								if (phase == 0 && !freeRoamMode) {
									if (!combatMode) {
										if (enemies.contains(selectedUnit) || Inventory.active.contains(selectedUnit) || showingRange) {
											showingRange = false;
											clearSelectedTiles();
											clearUnit();
											AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
											if (multiplayerMode) {
												comThread.eventQueue.add(new String[] { "CLEAR_SEL_UNIT" });
											}
										}
									} else {
										selectedUnit.reset();
										clearSelectedTiles();
										clearUnit();
										if (multiplayerMode) {
											comThread.eventQueue.add(new String[] { "RESET_UNIT" });
										}
										AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
									}
								}
							} else {
								menus.get(0).action("back", selectedUnit);
							}
						}
					} else {
						text.get(0).next();
					}
				} else {
					messages.get(0).close();
					AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
				}
			}
			break;
		}
	}

	public void onKeyRelease(int key) {
		switch (key) {

		}
	}

	@Override
	@SuppressWarnings("incomplete-switch")
	public void onKeyRepeat(InputMapping key) {
		switch (key) {

		case CONFIRM:
			if (alpha < 0.95) {
				if (messages.size() == 0) {
					if (text.size() > 0) {
						text.get(0).next();
					}
				}
			}
			break;

		case UP:
			cursorFloat = false;
			if (noUIOnScreen() && !controlLock && phase == 0 && Startup.screenColor.getW() == alphaTo) {
				if (cursorPos.y > 0) {
					boolean walkable = isTileWalkable(cursorPos.x, cursorPos.y - 1);
					if ((!freeRoamMode || walkable) && !caught) {
						cursorPos.setLocation(cursorPos.getX(), cursorPos.getY() - 1);
						if (multiplayerMode) {
							comThread.eventQueue.add(new String[] { "MOVE_CURSOR" });
						}
						if (!freeRoamMode) {
							AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
						}
					}
					if (((freeRoamMode && walkable) || (Math.floorDiv((int) tcamY, 8) - 12 > -12 && cursorPos.getY() == Math.floorDiv((int) tcamY, 8) - 12 + 6)) && !caught) {
						tcamY -= 8;
					}
					if (freeRoamMode && walkable && !caught) {
						Inventory.active.get(roamUnit).locY--;
						processFreeRoamMovement();
					}
				}
			}
			break;

		case DOWN:
			cursorFloat = false;
			if (noUIOnScreen() && !controlLock && phase == 0 && Startup.screenColor.getW() == alphaTo) {
				if (cursorPos.y < tiles[0].length - 1) {
					boolean walkable = isTileWalkable(cursorPos.x, cursorPos.y + 1);
					if ((!freeRoamMode || walkable) && !caught) {
						cursorPos.setLocation(cursorPos.getX(), cursorPos.getY() + 1);
						if (multiplayerMode) {
							comThread.eventQueue.add(new String[] { "MOVE_CURSOR" });
						}
						if (!freeRoamMode) {
							AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
						}
					}
					if (((freeRoamMode && walkable) || (Math.floorDiv((int) tcamY, 8) - 12 == cursorPos.getY() - 13)) && !caught) {
						tcamY += 8;
					}
					if (freeRoamMode && walkable && !caught) {
						Inventory.active.get(roamUnit).locY++;
						processFreeRoamMovement();
					}
				}
			}
			break;

		case LEFT:
			cursorFloat = false;
			if (menus.size() > 0) {
				menus.get(0).action("left", selectedUnit);
				break;
			}
			if (noUIOnScreen() && !controlLock && phase == 0 && Startup.screenColor.getW() == alphaTo) {
				if (cursorPos.x > 0) {
					boolean walkable = isTileWalkable(cursorPos.x - 1, cursorPos.y);
					if ((!freeRoamMode || walkable) && !caught) {
						cursorPos.setLocation(cursorPos.getX() - 1, cursorPos.getY());
						if (multiplayerMode) {
							comThread.eventQueue.add(new String[] { "MOVE_CURSOR" });
						}
						if (!freeRoamMode) {
							AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
						}
					}
					if (((freeRoamMode && walkable) || (Math.floorDiv((int) tcamX, 8) - 12 > -10 && cursorPos.getX() == Math.floorDiv((int) tcamX, 8) - 12 + 8)) && !caught) {
						tcamX -= 8;
					}
					if (freeRoamMode && walkable && !caught) {
						Inventory.active.get(roamUnit).locX--;
						processFreeRoamMovement();
					}
				}
			}
			break;

		case RIGHT:
			cursorFloat = false;
			if (menus.size() > 0) {
				menus.get(0).action("right", selectedUnit);
				break;
			}
			if (noUIOnScreen() && !controlLock && phase == 0 && Startup.screenColor.getW() == alphaTo) {
				if (cursorPos.x < tiles.length - 1) {
					boolean walkable = isTileWalkable(cursorPos.x + 1, cursorPos.y);
					if ((!freeRoamMode || walkable) && !caught) {
						cursorPos.setLocation(cursorPos.getX() + 1, cursorPos.getY());
						if (multiplayerMode) {
							comThread.eventQueue.add(new String[] { "MOVE_CURSOR" });
						}
						if (!freeRoamMode) {
							AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
						}
					}
					if (((freeRoamMode && walkable) || (Math.floorDiv((int) tcamX, 8) - 12 == cursorPos.getX() - 16)) && !caught) {
						tcamX += 8;
					}
					if (freeRoamMode && walkable && !caught) {
						Inventory.active.get(roamUnit).locX++;
						processFreeRoamMovement();
					}
				}
			}
			break;
		}
	}

	public void findLocationForItem(Unit location, ItemProperty e) {
		int widthCenter = tiles.length / 2;
		int heightCenter = tiles[0].length / 2;
		int loopCount = Math.max(widthCenter + Math.abs(widthCenter - location.locX), heightCenter + Math.abs(heightCenter - location.locY));

		// Loop through "rings" of tiles around the main unit, search for valid
		// positions, then place the units
		mainLoop: // Label to break out of nested for loops when the last unit is placed
		for (int i = 0; i < loopCount; i++) {
			for (int x = 0; x < 3 + i * 2; x++) {
				for (int y = 0; y < 3 + i * 2; y++) {
					int locX = location.locX - i + x - 1;
					int locY = location.locY - i + y - 1;
					if (locX > -1 && locX < tiles.length && locY > -1 && locY < tiles[0].length) {
						if (isTileAvailableForItem(locX, locY)) {
							items.add(new Item(new Vector3f(locX * 16, 24, locY * 16), e.copy()));
							effects.add(new EffectPoof(Vector3f.add(new Vector3f(locX * 16, 20, locY * 16), 0, StateManager.currentState.tiles[locX][locY].getPosition().getY(), 0)));
							break mainLoop;
						}
					}
				}
			}
		}
	}

	// This is used for walking in free mode
	// It only checks for enemies and solid tiles
	public boolean isTileWalkable(int x, int y) {
		for (Unit u : enemies) {
			if (u.locX == x && u.locY == y) {
				return false;
			}
		}
		return !tiles[x][y].isSolid();
	}

	// This should be used to generally check if any tile can be occupied by any
	// unit
	public boolean isTileAvailable(int x, int y) {
		for (Unit u : enemies) {
			if (u.locX == x && u.locY == y) {
				return false;
			}
		}
		for (Unit u : Inventory.active) {
			if (u.locX == x && u.locY == y) {
				return false;
			}
		}
		return !tiles[x][y].isSolid();
	}

	public boolean isTileAvailableForItem(int x, int y) {
		for (Item u : items) {
			if (u.locX == x && u.locY == y) {
				return false;
			}
		}
		return !tiles[x][y].isSolid();
	}

	public void onDestroy() {
		lights.clear();
		for (Tile[] x : tiles) {
			for (Tile y : x) {
				y.cleanUp();
			}
		}
		for (Effect e : effects) {
			e.cleanUp();
		}

		ArrayList<Unit> remove = new ArrayList<>();
		for (Unit u : Inventory.loaded.values()) {
			if (!Inventory.units.contains(u)) {
				remove.add(u);
			}
		}
		for (Unit u : remove) {
			Inventory.loaded.remove(u.uuid);
		}
	}

	public static StateDungeon getCurrentContext() {
		return (StateDungeon) StateManager.getState(STATE_ID);
	}

}