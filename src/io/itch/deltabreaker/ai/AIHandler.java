package io.itch.deltabreaker.ai;

import java.awt.Point;
import java.util.ArrayList;

import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.effect.EffectPoof;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.ItemAbility;
import io.itch.deltabreaker.object.item.ItemProperty;
import io.itch.deltabreaker.object.item.ItemUse;
import io.itch.deltabreaker.state.StateDungeon;
import io.itch.deltabreaker.state.StateManager;

public class AIHandler {

	public int currentUnit = 0;
	public boolean gettingPath = true;
	public boolean processing = false;
	public ArrayList<Point> currentPath = new ArrayList<Point>();
	public String currentOption = "";
	public boolean hasAttacked = false;
	public Unit attackTarget;
	public ItemAbility attackAbility = ItemAbility.ITEM_ABILITY_ATTACK;
	public ItemProperty itemUse;
	public int memCamX = 0;
	public int memCamY = 0;
	public boolean resetCamera = true;
	public int waitTime = 216;
	public int waitTimer = waitTime / 2;
	private boolean doneProcessing = false;
	private StateDungeon context;

	public AIHandler(StateDungeon context) {
		this.context = context;
	}

	public void tick() {
		boolean canProcess = true;
		for (Unit u : context.enemies) {
			if (u.dead) {
				canProcess = false;
			}
		}
		for (Unit u : Inventory.active) {
			if (u.dead) {
				canProcess = false;
			}
		}
		if (!context.noUIOnScreen() || context.swap || Startup.screenColor.getW() != context.alphaTo || context.usingItem || context.xpAlpha > 0 || context.displayXP != context.displayXPTarget) {
			canProcess = false;
		}
		if (currentUnit == context.enemies.size() - 1 && !processing && !resetCamera) {
			currentUnit = 0;
			context.tcamX = memCamX;
			context.tcamY = memCamY;
			resetCamera = true;
		}
		if ((context.enemies.size() == 0 || doneProcessing) && context.xpAlpha == 0 && waitTimer >= waitTime) {
			processing = false;
		}
		if (processing && canProcess) {
			if (waitTimer >= waitTime && !doneProcessing) {
				if (gettingPath) {
					// Runs code associated with the units AI type
					context.enemies.get(currentUnit).runAI(this, context);
				} else {
					if (!context.enemies.get(currentUnit).inCombat) {
						// Set the camX and camY to make sure the tiles are drawn correctly
						context.camX = context.enemies.get(currentUnit).x / 2;
						context.camY = context.enemies.get(currentUnit).y / 2 + 16;
						context.tcamX = (int) context.enemies.get(currentUnit).x / 2;
						context.tcamY = (int) context.enemies.get(currentUnit).y / 2 + 16;

						// Manually set the camera and shadow camera trained on the current unit
						if (!context.inCombat) {
							Startup.camera.setTargetPosition(context.enemies.get(currentUnit).x / 2.0f, 42 + (context.tiles[context.enemies.get(currentUnit).locX][context.enemies.get(currentUnit).locY].getPosition().getY() / 2),
									context.enemies.get(currentUnit).y / 2.0f + 16);
						} else {
							float xDist = context.attacker.locX * 8 - context.defender.locX * 8;
							float yDist = context.attacker.locY * 8 - context.defender.locY * 8;
							Startup.camera.setTargetPosition(context.enemies.get(currentUnit).x / 2.0f - xDist / 2, 42 + (context.tiles[context.enemies.get(currentUnit).locX][context.enemies.get(currentUnit).locY].getPosition().getY() / 2),
									context.enemies.get(currentUnit).y / 2.0f + 16 - yDist / 2);
						}
						Startup.shadowCamera.setTargetPosition(Startup.camera.position.getX(), 80 + StateManager.currentState.tiles[context.enemies.get(currentUnit).locX][context.enemies.get(currentUnit).locY].getPosition().getY() / 2,
								Startup.camera.position.getZ());
						context.overheadLight.position.set(Startup.shadowCamera.position.getX(), Startup.shadowCamera.position.getY() + 48, Startup.shadowCamera.position.getZ());
					}
					if (!context.inCombat) {
						if (context.enemies.get(currentUnit).path.isEmpty() && !currentPath.isEmpty()) {
							context.enemies.get(currentUnit).move(currentPath);
							currentPath.clear();
							context.clearSelectedTiles();
						}
						if (context.enemies.get(currentUnit).x == context.enemies.get(currentUnit).locX * 16 && context.enemies.get(currentUnit).y == context.enemies.get(currentUnit).locY * 16
								&& context.enemies.get(currentUnit).path.isEmpty()) {
							switch (currentOption) {

							case "wait":
								// Makes the unit pick up an item if on one
								if (context.enemies.get(currentUnit).hasTurn) {
									for (int i = 0; i < context.items.size(); i++) {
										if (context.items.get(i).locX == context.enemies.get(currentUnit).locX && context.items.get(i).locY == context.enemies.get(currentUnit).locY) {
											int overflow = context.enemies.get(currentUnit).addItem(context.items.get(i).item);
											if (overflow == 0) {
												AudioManager.getSound("loot.ogg").play(AudioManager.defaultMainSFXGain, false);
												StateManager.currentState.effects.add(new EffectPoof(Vector3f.add(new Vector3f(context.enemies.get(currentUnit).locX * 16, 20, context.enemies.get(currentUnit).locY * 16), 0,
														StateManager.currentState.tiles[context.enemies.get(currentUnit).locX][context.enemies.get(currentUnit).locY].getPosition().getY(), 0)));
												context.items.remove(i);
											}
											break;
										}
									}
								}

								context.enemies.get(currentUnit).setTurn(false);

								if (currentUnit < context.enemies.size() - 1) {
									currentUnit++;
									gettingPath = true;
								} else {
									doneProcessing = true;
								}
								waitTimer = 0;
								break;

							case "attack":
								if (!hasAttacked) {
									context.selectedAbility = attackAbility;
									context.setCombat(context.enemies.get(currentUnit), attackTarget);
									hasAttacked = true;
								} else if (!context.inCombat) {
									if (currentUnit < context.enemies.size() - 1) {
										currentUnit++;
										gettingPath = true;
									} else {
										doneProcessing = true;
									}
									hasAttacked = false;
									if (!context.defender.dead) {
										waitTimer = waitTime / 2;
									}
								}
								break;

							case "item_use":
								context.selectedItem = itemUse;
								context.selectedUnit = context.enemies.get(currentUnit);
								ItemUse.valueOf(itemUse.use).use(attackTarget, context);

								if (currentUnit < context.enemies.size() - 1) {
									currentUnit++;
									gettingPath = true;
									waitTimer = 0;
								} else {
									doneProcessing = true;
								}
								break;
								
							case "item_followup":
								context.selectedItem = itemUse;
								context.selectedUnit = context.enemies.get(currentUnit);
								ItemUse.valueOf(itemUse.use).use(attackTarget, context);
								ItemUse.valueOf(itemUse.use).followUp(attackTarget, context);

								if (currentUnit < context.enemies.size() - 1) {
									currentUnit++;
									gettingPath = true;
									waitTimer = 0;
								} else {
									doneProcessing = true;
								}
								break;

							case "ability":
								attackAbility.use(context.enemies.get(currentUnit), context);

								if (currentUnit < context.enemies.size() - 1) {
									currentUnit++;
									gettingPath = true;
								} else {
									doneProcessing = true;
								}
								waitTimer = 0;
								break;

							}
						}
					}
				}
			} else {
				waitTimer++;
			}
		}
	}

	public void startProcessing() {
		currentUnit = 0;
		gettingPath = true;
		processing = true;
		currentOption = "";
		memCamX = (int) context.camX;
		memCamY = (int) context.camY;
		resetCamera = false;
		waitTimer = waitTime / 2;
		doneProcessing = false;
	}

}