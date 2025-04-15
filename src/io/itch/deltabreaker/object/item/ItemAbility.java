package io.itch.deltabreaker.object.item;

import java.util.ArrayList;
import java.util.Random;

import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.effect.EffectEnergize;
import io.itch.deltabreaker.effect.EffectPoof;
import io.itch.deltabreaker.effect.EffectShield;
import io.itch.deltabreaker.effect.EffectText;
import io.itch.deltabreaker.effect.battle.EffectCoupDeGrace;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.state.StateDungeon;
import io.itch.deltabreaker.state.StateManager;
import io.itch.deltabreaker.ui.Message;
import io.itch.deltabreaker.ui.menu.MenuDungeonSteal;
import io.itch.deltabreaker.ui.menu.MenuTrade;

public enum ItemAbility {

	// The standard attack
	ITEM_ABILITY_ATTACK("Attack", "target.enemy", true, false, true, false, false, 99, -1) {

		@Override
		public boolean use(Unit u, StateDungeon context) {
			context.combatMode = true;
			return true;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			context.setCombat(context.selectedUnit, u);
			if (context.multiplayerMode) {
				context.comThread.eventQueue.add(new String[] { "USE_ATTACKING_ABILITY", "ITEM_ABILITY_ATTACK", context.selectedUnit.weapon.uuid, context.selectedUnit.uuid, u.uuid });
				context.comThread.eventQueue.add(new String[] { "CLEAR_TILE_HIGHLIGHT" });
			}
			return true;
		}

		@Override
		public int[] calculateAttackingDamage(Unit atk, Unit def, boolean ignoreRange) {
			int[] results = new int[2];

			int reduction = StateManager.currentState.tiles[def.locX][def.locY].defense;

			// Returns damage dealt
			if (atk.weapon.damageType.equals(ItemProperty.TYPE_DAMAGE_PHYSICAL)) {
				results[0] = Math.max(0, atk.atk - (def.def + reduction));
			} else if (atk.weapon.damageType.equals(ItemProperty.TYPE_DAMAGE_MAGIC)) {
				results[0] = Math.max(0, atk.mag - (def.res + reduction));
			} else {
				results[0] = Math.max(0, (atk.atk + atk.mag) - (def.def + def.res + reduction));
			}

			// Returns the number of attacks
			if (ignoreRange || atk.weapon.range >= (Math.abs(atk.locX - def.locX) + Math.abs(atk.locY - def.locY))) {
				results[1] = Math.max(1, 1 + Math.floorDiv((atk.spd - def.spd) + (int) (atk.height - def.height), 5));
			} else {
				results[1] = 0;
			}

			return results;
		}

		@Override
		public int[] calculateDefendingDamage(Unit atk, Unit def, boolean ignoreRange) {
			int[] results = new int[2];

			int reduction = StateManager.currentState.tiles[atk.locX][atk.locY].defense;

			// Returns damage dealt
			if (def.weapon.damageType.equals(ItemProperty.TYPE_DAMAGE_PHYSICAL)) {
				results[0] = Math.max(0, def.atk - (atk.def + reduction));
			} else if (def.weapon.damageType.equals(ItemProperty.TYPE_DAMAGE_MAGIC)) {
				results[0] = Math.max(0, def.mag - (atk.res + reduction));
			} else {
				results[0] = Math.max(0, (def.atk + def.mag) - (atk.def + atk.res + reduction));
			}

			// Returns the number of attacks
			if (ignoreRange || def.weapon.range >= (Math.abs(def.locX - atk.locX) + Math.abs(def.locY - atk.locY))) {
				results[1] = Math.max(1, 1 + Math.floorDiv((def.spd - atk.spd) + (int) (def.height - atk.height), 5));
			} else {
				results[1] = 0;
			}

			return results;
		}

		@Override
		public int calculateHealing(Unit healer, Unit healed) {
			return 0;
		}

		@Override
		public int[] getStats() {
			return null;
		}

		@Override
		public void onCombatEnd(Unit unit, StateDungeon context) {
			// Empty
		}

		@Override
		public void onHit(StateDungeon context) {
			// Empty
		}

		@Override
		public void onRetaliation(StateDungeon context) {
			// Empty
		}

		@Override
		public String[] getInfo() {
			return null;
		}
	},

	ITEM_ABILITY_TRADE("Trade", "target.unit", false, false, true, false, false, 99, -1) {

		@Override
		public boolean use(Unit u, StateDungeon context) {
			context.clearSelectedTiles();
			context.highlightTiles(u.locX, u.locY, 1, 2, "");
			context.combatMode = true;
			return true;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			context.clearSelectedTiles();
			StateManager.currentState.menus.add(new MenuTrade(new Vector3f(0, 0, -80), context.selectedUnit, u, true));
			context.clearUnit();
			return true;
		}

		@Override
		public int[] calculateAttackingDamage(Unit atk, Unit def, boolean ignoreRange) {
			return null;
		}

		@Override
		public int[] calculateDefendingDamage(Unit atk, Unit def, boolean ignoreRange) {
			return null;
		}

		@Override
		public int calculateHealing(Unit healer, Unit healed) {
			return 0;
		}

		@Override
		public int[] getStats() {
			return null;
		}

		@Override
		public void onCombatEnd(Unit unit, StateDungeon context) {
			// Empty
		}

		@Override
		public void onHit(StateDungeon context) {
			// Empty
		}

		@Override
		public void onRetaliation(StateDungeon context) {
			// Empty
		}

		@Override
		public String[] getInfo() {
			return null;
		}
	},

	ITEM_ABILITY_USE_ITEM_ALLY("", "target.unit", false, false, true, false, false, 99, -1) {

		@Override
		public boolean use(Unit u, StateDungeon context) {
			context.combatMode = true;
			return false;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			context.selectedItemUse.followUp(u, context);
			return false;
		}

		@Override
		public int[] calculateAttackingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int[] calculateDefendingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int calculateHealing(Unit healer, Unit healed) {
			return 0;
		}

		@Override
		public int[] getStats() {
			return null;
		}

		@Override
		public void onCombatEnd(Unit unit, StateDungeon context) {

		}

		@Override
		public void onHit(StateDungeon context) {
			// Empty
		}

		@Override
		public void onRetaliation(StateDungeon context) {
			// Empty
		}

		@Override
		public String[] getInfo() {
			return null;
		}
	},

	ITEM_ABILITY_USE_ITEM_ENEMY("", "target.enemy", false, false, true, false, false, 99, -1) {

		@Override
		public boolean use(Unit u, StateDungeon context) {
			context.combatMode = true;
			return true;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			context.selectedItemUse.followUp(u, context);
			return true;
		}

		@Override
		public int[] calculateAttackingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int[] calculateDefendingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int calculateHealing(Unit healer, Unit healed) {
			return 0;
		}

		@Override
		public int[] getStats() {
			return null;
		}

		@Override
		public void onCombatEnd(Unit unit, StateDungeon context) {

		}

		@Override
		public void onHit(StateDungeon context) {
			// Empty
		}

		@Override
		public void onRetaliation(StateDungeon context) {
			// Empty
		}

		@Override
		public String[] getInfo() {
			return null;
		}
	},

	ITEM_ABILITY_SWAP("Swap", "target.unit", false, false, true, false, true, 1, 0) {

		@Override
		public boolean use(Unit u, StateDungeon context) {
			context.combatMode = true;
			return true;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			int posX = u.locX;
			int posY = u.locY;
			u.locX = context.selectedUnit.locX;
			u.locY = context.selectedUnit.locY;
			context.selectedUnit.locX = posX;
			context.selectedUnit.locY = posY;

			if (context.multiplayerMode && context.phase == 0) {
				context.comThread.eventQueue.add(new String[] { "USE_ABILITY", name(), context.selectedUnit.uuid, u.uuid, "false" });
			}

			context.selectedUnit.setTurn(false);
			context.clearSelectedTiles();
			context.clearUnit();

			return true;
		}

		@Override
		public int[] calculateAttackingDamage(Unit atk, Unit def, boolean ignoreRange) {
			return null;
		}

		@Override
		public int[] calculateDefendingDamage(Unit atk, Unit def, boolean ignoreRange) {
			return null;
		}

		@Override
		public int calculateHealing(Unit healer, Unit healed) {
			return 0;
		}

		@Override
		public int[] getStats() {
			return null;
		}

		@Override
		public void onCombatEnd(Unit unit, StateDungeon context) {
			// Empty
		}

		@Override
		public void onHit(StateDungeon context) {
			// Empty
		}

		@Override
		public void onRetaliation(StateDungeon context) {
			// Empty
		}

		@Override
		public String[] getInfo() {
			return new String[] { toString(), "", "", "Swaps the placements of", "the user and target" };
		}
	},

	ITEM_ABILITY_STEAL("Steal", "target.enemy", false, false, true, false, true, 1, 0) {

		@Override
		public boolean use(Unit u, StateDungeon context) {
			if (u.getItemList().size() < 5) {
				context.combatMode = true;
				return true;
			} else {
				context.messages.add(new Message(new String[] { "You cant carry any more" }));
			}
			return false;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			if (u.getItemList().size() > 0) {
				context.menus.add(new MenuDungeonSteal(new Vector3f(0, 0, -80), u, context.selectedUnit, context));
				context.selectedUnit.setTurn(false);
				context.clearSelectedTiles();
				context.clearUnit();
			} else {
				AudioManager.getSound("invalid.ogg").play(AudioManager.defaultMainSFXGain, false);
				return false;
			}
			return true;
		}

		@Override
		public int[] calculateAttackingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int[] calculateDefendingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int calculateHealing(Unit healer, Unit healed) {
			return 0;
		}

		@Override
		public int[] getStats() {
			return null;
		}

		@Override
		public void onCombatEnd(Unit unit, StateDungeon context) {
			// Empty
		}

		@Override
		public void onHit(StateDungeon context) {
			// Empty
		}

		@Override
		public void onRetaliation(StateDungeon context) {
			// Empty
		}

		@Override
		public String[] getInfo() {
			return new String[] { toString(), "", "", "Offers the user a chance", "to steal an item", "from the target" };
		}
	},

	ITEM_ABILITY_TRICKSTER("Trickster", "target.none", false, false, false, false, true, 1, 1) {

		@Override
		public boolean use(Unit u, StateDungeon context) {
			return false;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			return false;
		}

		@Override
		public int[] calculateAttackingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int[] calculateDefendingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int calculateHealing(Unit healer, Unit healed) {
			return 0;
		}

		@Override
		public int[] getStats() {
			return null;
		}

		@Override
		public void onCombatEnd(Unit unit, StateDungeon context) {
			// Empty
		}

		@Override
		public void onHit(StateDungeon context) {
			// Empty
		}

		@Override
		public void onRetaliation(StateDungeon context) {
			// Empty
		}

		@Override
		public String[] getInfo() {
			return new String[] { toString(), "", "", "Ensures attempts to", "steal will succeed" };
		}
	},

	ITEM_ABILITY_DISARM("Disarm", "target.enemy", true, false, true, false, true, 2, 1) {

		@Override
		public boolean use(Unit u, StateDungeon context) {
			context.combatMode = true;
			return true;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			context.setCombat(context.selectedUnit, u);
			if (context.multiplayerMode) {
				context.comThread.eventQueue.add(new String[] { "USE_ATTACKING_ABILITY", "ITEM_ABILITY_DISARM", context.selectedUnit.weapon.uuid, context.selectedUnit.uuid, u.uuid });
				context.comThread.eventQueue.add(new String[] { "CLEAR_TILE_HIGHLIGHT" });
			}
			return true;
		}

		@Override
		public int[] calculateAttackingDamage(Unit atk, Unit def, boolean ignoreRange) {
			int[] results = new int[2];

			int reduction = StateManager.currentState.tiles[def.locX][def.locY].defense;

			// Returns damage dealt
			if (atk.weapon.damageType.equals(ItemProperty.TYPE_DAMAGE_PHYSICAL)) {
				results[0] = Math.max(0, atk.atk - (def.def + reduction));
			} else if (atk.weapon.damageType.equals(ItemProperty.TYPE_DAMAGE_MAGIC)) {
				results[0] = Math.max(0, atk.mag - (def.res + reduction));
			} else {
				results[0] = Math.max(0, (atk.atk + atk.mag) - (def.def + def.res + reduction));
			}

			// Returns the number of attacks
			if (ignoreRange || atk.weapon.range >= (Math.abs(atk.locX - def.locX) + Math.abs(atk.locY - def.locY))) {
				results[1] = 1;
			} else {
				results[1] = 0;
			}

			return results;
		}

		@Override
		public int[] calculateDefendingDamage(Unit atk, Unit def, boolean ignoreRange) {
			int[] results = new int[2];

			int reduction = StateManager.currentState.tiles[atk.locX][atk.locY].defense;

			// Returns damage dealt
			if (def.weapon.damageType.equals(ItemProperty.TYPE_DAMAGE_PHYSICAL)) {
				results[0] = Math.max(0, def.atk - (atk.def + reduction));
			} else if (def.weapon.damageType.equals(ItemProperty.TYPE_DAMAGE_MAGIC)) {
				results[0] = Math.max(0, def.mag - (atk.res + reduction));
			} else {
				results[0] = Math.max(0, (def.atk + def.mag) - (atk.def + atk.res + reduction));
			}

			// Returns the number of attacks
			if (ignoreRange || def.weapon.range >= (Math.abs(def.locX - atk.locX) + Math.abs(def.locY - atk.locY))) {
				results[1] = Math.max(1, 1 + Math.floorDiv((def.spd - atk.spd) + (int) (def.height - atk.height), 5));
			} else {
				results[1] = 0;
			}

			return results;
		}

		@Override
		public int calculateHealing(Unit healer, Unit healed) {
			return 0;
		}

		@Override
		public int[] getStats() {
			return null;
		}

		@Override
		public void onCombatEnd(Unit unit, StateDungeon context) {
			// Empty
		}

		@Override
		public void onHit(StateDungeon context) {
			Unit u = context.defender;
			if (!u.weapon.type.equals(ItemProperty.TYPE_EMPTY)) {
				int leftover = u.addItem(u.weapon);
				if (leftover == 0) {
					u.weapon = ItemProperty.empty.copy();
					u.lastWeapon = ItemProperty.empty.id;
					context.effects.add(new EffectPoof(new Vector3f(u.x, u.height + 13, u.y + 2)));
					context.defRounds = calculateDefendingDamage(context.attacker, context.defender, false)[1];
				}
			}
		}

		@Override
		public void onRetaliation(StateDungeon context) {
			// Empty
		}

		@Override
		public String[] getInfo() {
			return new String[] { toString(), "", "", "Removes the targets weapon", "when the attack lands" };
		}
	},

	ITEM_ABILITY_FORTIFIED("Fortified", "target.none", false, false, false, true, true, 1, 0) {

		@Override
		public boolean use(Unit u, StateDungeon context) {
			return false;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			return false;
		}

		@Override
		public int[] calculateAttackingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int[] calculateDefendingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int calculateHealing(Unit healer, Unit healed) {
			return 0;
		}

		@Override
		public int[] getStats() {
			return new int[] { 0, 0, 0, 0, 6, 6, -3 };
		}

		@Override
		public void onCombatEnd(Unit unit, StateDungeon context) {
			// Empty
		}

		@Override
		public void onHit(StateDungeon context) {
			// Empty
		}

		@Override
		public void onRetaliation(StateDungeon context) {
			// Empty
		}

		@Override
		public String[] getInfo() {
			return new String[] { toString(), "", "", "Bolsters defenses at the", "cost of movement", "def/res + 6   mov - 3" };
		}
	},

	ITEM_ABILITY_SHELTER("Shelter", "target.none", false, false, false, false, true, 1, 1) {

		@Override
		public boolean use(Unit u, StateDungeon context) {
			return false;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			return false;
		}

		@Override
		public int[] calculateAttackingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int[] calculateDefendingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int calculateHealing(Unit healer, Unit healed) {
			return 0;
		}

		@Override
		public int[] getStats() {
			return null;
		}

		@Override
		public void onCombatEnd(Unit unit, StateDungeon context) {
			for (Unit u : (Inventory.active.contains(unit)) ? Inventory.active : context.enemies) {
				if (u != unit) {
					if (Math.abs(u.locX - unit.locX) + Math.abs(u.locY - unit.locY) <= 1) {
						u.heal(5);
					}
				}
			}
		}

		@Override
		public void onHit(StateDungeon context) {
			// Empty
		}

		@Override
		public void onRetaliation(StateDungeon context) {
			// Empty
		}

		@Override
		public String[] getInfo() {
			return new String[] { toString(), "", "", "Heals adjacent alies for", "5 when combat ends" };
		}
	},

	ITEM_ABILITY_HARDEN("Harden", "target.none", false, false, true, false, true, 1, 1) {

		@Override
		public boolean use(Unit u, StateDungeon context) {
			u.offsetDef += 4;
			u.offsetRes += 4;
			context.effects
					.add(new EffectText("+4 Def_Res", new Vector3f(u.x - ("+4 Def_Res").length() * 1.5f, 20 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y - 8), new Vector4f(ItemProperty.colorList[1], 1)));
			context.effects.add(new EffectShield(new Vector3f(u.x, 10 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y)));
			u.setTurn(false);

			if (context.multiplayerMode && context.phase == 0) {
				context.comThread.eventQueue.add(new String[] { "USE_ABILITY", name(), context.selectedUnit.uuid, u.uuid, "true" });
			}

			context.clearSelectedTiles();
			context.clearUnit();
			return true;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			return false;
		}

		@Override
		public int[] calculateAttackingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int[] calculateDefendingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int calculateHealing(Unit healer, Unit healed) {
			return 0;
		}

		@Override
		public int[] getStats() {
			return null;
		}

		@Override
		public void onCombatEnd(Unit unit, StateDungeon context) {
			// Empty
		}

		@Override
		public void onHit(StateDungeon context) {
			// Empty
		}

		@Override
		public void onRetaliation(StateDungeon context) {
			// Empty
		}

		@Override
		public String[] getInfo() {
			return new String[] { toString(), "", "", "Bolsters defenses for 1 turn" };
		}
	},

	// One attack that confuses target
	ITEM_ABILITY_BASH("Bash", "target.enemy", true, false, true, false, true, 1, 0) {

		@Override
		public boolean use(Unit u, StateDungeon context) {
			context.combatMode = true;
			return true;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			context.setCombat(context.selectedUnit, u);
			if (context.multiplayerMode) {
				context.comThread.eventQueue.add(new String[] { "USE_ATTACKING_ABILITY", "ITEM_ABILITY_BASH", context.selectedUnit.weapon.uuid, context.selectedUnit.uuid, u.uuid });
				context.comThread.eventQueue.add(new String[] { "CLEAR_TILE_HIGHLIGHT" });
			}
			return true;
		}

		@Override
		public int[] calculateAttackingDamage(Unit atk, Unit def, boolean ignoreRange) {
			int[] results = new int[2];

			int reduction = StateManager.currentState.tiles[def.locX][def.locY].defense;

			// Returns damage dealt
			if (atk.weapon.damageType.equals(ItemProperty.TYPE_DAMAGE_PHYSICAL)) {
				results[0] = Math.max(0, atk.atk - (def.def + reduction));
			} else if (atk.weapon.damageType.equals(ItemProperty.TYPE_DAMAGE_MAGIC)) {
				results[0] = Math.max(0, atk.mag - (def.res + reduction));
			} else {
				results[0] = Math.max(0, (atk.atk + atk.mag) - (def.def + def.res + reduction));
			}

			// Returns the number of attacks
			if (ignoreRange || atk.weapon.range >= (Math.abs(atk.locX - def.locX) + Math.abs(atk.locY - def.locY))) {
				results[1] = 1;
			} else {
				results[1] = 0;
			}

			return results;
		}

		@Override
		public int[] calculateDefendingDamage(Unit atk, Unit def, boolean ignoreRange) {
			int[] results = new int[2];

			int reduction = StateManager.currentState.tiles[atk.locX][atk.locY].defense;

			// Returns damage dealt
			if (def.weapon.damageType.equals(ItemProperty.TYPE_DAMAGE_PHYSICAL)) {
				results[0] = Math.max(0, def.atk - (atk.def + reduction));
			} else if (def.weapon.damageType.equals(ItemProperty.TYPE_DAMAGE_MAGIC)) {
				results[0] = Math.max(0, def.mag - (atk.res + reduction));
			} else {
				results[0] = Math.max(0, (def.atk + def.mag) - (atk.def + atk.res + reduction));
			}

			// Returns the number of attacks
			if (ignoreRange || def.weapon.range >= (Math.abs(def.locX - atk.locX) + Math.abs(def.locY - atk.locY))) {
				results[1] = Math.max(1, 1 + Math.floorDiv((def.spd - atk.spd) + (int) (def.height - atk.height), 5));
			} else {
				results[1] = 0;
			}

			return results;
		}

		@Override
		public int calculateHealing(Unit healer, Unit healed) {
			return 0;
		}

		@Override
		public int[] getStats() {
			return null;
		}

		@Override
		public void onCombatEnd(Unit unit, StateDungeon context) {
			if (context.selectedAbility == this) {
				context.defender.applyStatus(Unit.STATUS_DAZE);
			}
		}

		@Override
		public void onHit(StateDungeon context) {
			// Empty
		}

		@Override
		public void onRetaliation(StateDungeon context) {
			// Empty
		}

		@Override
		public String[] getInfo() {
			return new String[] { toString(), "", "", "Inflicts the target with", "daze when the attack lands" };
		}
	},

	ITEM_ABILITY_HEAL_10("S Heal", "target.unit", false, true, true, false, false, 1, 0) {

		@Override
		public boolean use(Unit u, StateDungeon context) {
			context.combatMode = true;
			return true;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			if (u.currentHp < u.hp) {
				u.heal(calculateHealing(context.selectedUnit, u));
				context.selectedUnit.setTurn(false);

				if (context.multiplayerMode && context.phase == 0) {
					context.comThread.eventQueue.add(new String[] { "USE_ABILITY", name(), context.selectedUnit.uuid, u.uuid, "false" });
				}

				context.clearSelectedTiles();
				context.clearUnit();
				return true;
			}
			AudioManager.getSound("invalid.ogg").play(AudioManager.defaultMainSFXGain, false);
			return false;
		}

		@Override
		public int[] calculateAttackingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int[] calculateDefendingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int calculateHealing(Unit healer, Unit healed) {
			return 10;
		}

		@Override
		public int[] getStats() {
			return null;
		}

		@Override
		public void onCombatEnd(Unit unit, StateDungeon context) {
			// Empty
		}

		@Override
		public void onHit(StateDungeon context) {
			// Empty
		}

		@Override
		public void onRetaliation(StateDungeon context) {
			// Empty
		}

		@Override
		public String[] getInfo() {
			return new String[] { toString(), "", "", "Heals the target for 10" };
		}
	},

	ITEM_ABILITY_HEAL_20("M Heal", "target.unit", false, true, true, false, false, 2, 1) {

		@Override
		public boolean use(Unit u, StateDungeon context) {
			context.combatMode = true;
			return true;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			if (u.currentHp < u.hp) {
				u.heal(calculateHealing(context.selectedUnit, u));
				context.selectedUnit.setTurn(false);

				if (context.multiplayerMode && context.phase == 0) {
					context.comThread.eventQueue.add(new String[] { "USE_ABILITY", name(), context.selectedUnit.uuid, u.uuid, "false" });
				}

				context.clearSelectedTiles();
				context.clearUnit();
				return true;
			}
			AudioManager.getSound("invalid.ogg").play(AudioManager.defaultMainSFXGain, false);
			return false;
		}

		@Override
		public int[] calculateAttackingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int[] calculateDefendingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int calculateHealing(Unit healer, Unit healed) {
			return 20;
		}

		@Override
		public int[] getStats() {
			return null;
		}

		@Override
		public void onCombatEnd(Unit unit, StateDungeon context) {
			// Empty
		}

		@Override
		public void onHit(StateDungeon context) {
			// Empty
		}

		@Override
		public void onRetaliation(StateDungeon context) {
			// Empty
		}

		@Override
		public String[] getInfo() {
			return new String[] { toString(), "", "", "Heals the target for 20" };
		}
	},

	ITEM_ABILITY_HEAL_30("L Heal", "target.unit", false, true, true, false, false, 2, 2) {

		@Override
		public boolean use(Unit u, StateDungeon context) {
			context.combatMode = true;
			return true;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			if (u.currentHp < u.hp) {
				u.heal(calculateHealing(context.selectedUnit, u));
				context.selectedUnit.setTurn(false);

				if (context.multiplayerMode && context.phase == 0) {
					context.comThread.eventQueue.add(new String[] { "USE_ABILITY", name(), context.selectedUnit.uuid, u.uuid, "false" });
				}

				context.clearSelectedTiles();
				context.clearUnit();
				return true;
			}
			AudioManager.getSound("invalid.ogg").play(AudioManager.defaultMainSFXGain, false);
			return false;
		}

		@Override
		public int[] calculateAttackingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int[] calculateDefendingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int calculateHealing(Unit healer, Unit healed) {
			return 30;
		}

		@Override
		public int[] getStats() {
			return null;
		}

		@Override
		public void onCombatEnd(Unit unit, StateDungeon context) {
			// Empty
		}

		@Override
		public void onHit(StateDungeon context) {
			// Empty
		}

		@Override
		public void onRetaliation(StateDungeon context) {
			// Empty
		}

		@Override
		public String[] getInfo() {
			return new String[] { toString(), "", "", "Heals the target for 30" };
		}
	},

	ITEM_ABILITY_CURE("Cure", "target.unit", false, false, true, false, false, 2, 0) {

		@Override
		public boolean use(Unit u, StateDungeon context) {
			context.combatMode = true;
			return true;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			if (!u.getStatus().equals("")) {
				u.clearStatus();
				StateManager.currentState.effects.add(new EffectEnergize(new Vector3f(u.x, 10 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y), new Vector4f(0.5f, 1f, 0.5f, 1f)));
				context.selectedUnit.setTurn(false);

				if (context.multiplayerMode && context.phase == 0) {
					context.comThread.eventQueue.add(new String[] { "USE_ABILITY", name(), context.selectedUnit.uuid, u.uuid, "false" });
				}

				context.clearSelectedTiles();
				context.clearUnit();
				return true;
			}

			AudioManager.getSound("invalid.ogg").play(AudioManager.defaultMainSFXGain, false);
			return false;
		}

		@Override
		public int[] calculateAttackingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int[] calculateDefendingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int calculateHealing(Unit healer, Unit healed) {
			return 0;
		}

		@Override
		public int[] getStats() {
			return null;
		}

		@Override
		public void onCombatEnd(Unit unit, StateDungeon context) {
			// Empty
		}

		@Override
		public void onHit(StateDungeon context) {
			// Empty
		}

		@Override
		public void onRetaliation(StateDungeon context) {
			// Empty
		}

		@Override
		public String[] getInfo() {
			return new String[] { toString(), "", "", "Removes statuses from", "the target" };
		}
	},

	ITEM_ABILITY_WARP("Warp", "target.none", false, false, true, false, false, 3, 2) {

		@Override
		public boolean use(Unit u, StateDungeon context) {
			context.combatMode = true;
			context.clearSelectedTiles();
			return true;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			if (context.isTileAvailable(context.cursorPos.x, context.cursorPos.y)) {
				u.setTurn(false);
				context.effects.add(new EffectPoof(new Vector3f(u.x, u.height + 13, u.y + 2)));
				u.placeAt(context.cursorPos.x, context.cursorPos.y);
				u.height = StateManager.currentState.tiles[(int) Math.round(u.x / 16.0)][(int) Math.round(u.y / 16.0)].getPosition().getY();
				context.effects.add(new EffectPoof(new Vector3f(u.x, u.height + 13, u.y + 2)));
				AudioManager.getSound("warp.ogg").play(AudioManager.defaultMainSFXGain, false);

				if (context.multiplayerMode && context.phase == 0) {
					context.comThread.eventQueue.add(new String[] { "USE_ABILITY", name(), context.selectedUnit.uuid, u.uuid, "false" });
				}

				context.clearSelectedTiles();
				context.clearUnit();
				return true;
			}
			AudioManager.getSound("invalid.ogg").play(AudioManager.defaultMainSFXGain, false);
			return false;
		}

		@Override
		public void onHit(StateDungeon context) {

		}

		@Override
		public void onRetaliation(StateDungeon context) {

		}

		@Override
		public int[] calculateAttackingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int[] calculateDefendingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int calculateHealing(Unit healer, Unit healed) {
			return 0;
		}

		@Override
		public int[] getStats() {
			return null;
		}

		@Override
		public void onCombatEnd(Unit unit, StateDungeon context) {

		}

		@Override
		public String[] getInfo() {
			return new String[] { toString(), "", "", "Warps the user to a", "chosen position" };
		}

	},

	ITEM_ABILITY_IMMOLATION("Immolation", "target.none", false, false, true, false, false, 3, 3) {

		@Override
		public boolean use(Unit u, StateDungeon context) {
			if (u.currentHp > 1) {
				if (context.multiplayerMode && context.phase == 0) {
					context.comThread.eventQueue.add(new String[] { "USE_ABILITY", name(), context.selectedUnit.uuid, u.uuid, "true" });
				}
				context.clearSelectedTiles();
				context.clearUnit();
				StateManager.currentState.controlLock = true;
				StateManager.currentState.hideCursor = true;
				context.hideInfo = true;
				AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
				StateManager.currentState.effects.add(new EffectCoupDeGrace(u.dir == 0, u));
				return true;
			}
			AudioManager.getSound("invalid.ogg").play(AudioManager.defaultMainSFXGain, false);
			return false;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			return false;
		}

		@Override
		public void onHit(StateDungeon context) {

		}

		@Override
		public void onRetaliation(StateDungeon context) {

		}

		@Override
		public int[] calculateAttackingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int[] calculateDefendingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int calculateHealing(Unit healer, Unit healed) {
			return 0;
		}

		@Override
		public int[] getStats() {
			return null;
		}

		@Override
		public void onCombatEnd(Unit unit, StateDungeon context) {

		}

		@Override
		public String[] getInfo() {
			return new String[] { toString(), "", "", "Sets the user ablaze to", "deal immense damage to", "nearby enemies" };
		}

	},

	// Adds half of enemy def as damage but with 1/3rd attack
	ITEM_ABILITY_WEAK_POINT("Weak Point", "target.enemy", true, false, true, false, true, 1, 0) {

		@Override
		public boolean use(Unit u, StateDungeon context) {
			context.combatMode = true;
			return true;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			context.setCombat(context.selectedUnit, u);
			if (context.multiplayerMode) {
				context.comThread.eventQueue.add(new String[] { "USE_ATTACKING_ABILITY", "ITEM_ABILITY_WEAK_POINT", context.selectedUnit.weapon.uuid, context.selectedUnit.uuid, u.uuid });
				context.comThread.eventQueue.add(new String[] { "CLEAR_TILE_HIGHLIGHT" });
			}
			return true;
		}

		@Override
		public int[] calculateAttackingDamage(Unit atk, Unit def, boolean ignoreRange) {
			int[] results = new int[2];

			int reduction = StateManager.currentState.tiles[def.locX][def.locY].defense;

			// Returns damage dealt
			if (atk.weapon.damageType.equals(ItemProperty.TYPE_DAMAGE_PHYSICAL)) {
				results[0] = Math.max(0, (atk.atk / 3) - (def.def + reduction)) + (int) (def.def / 2);
			} else if (atk.weapon.damageType.equals(ItemProperty.TYPE_DAMAGE_MAGIC)) {
				results[0] = Math.max(0, (atk.mag / 3) - (def.res + reduction)) + (int) (def.res / 2);
			} else {
				results[0] = Math.max(0, ((atk.atk + atk.mag) / 3) - (def.def + def.res + reduction)) + (int) ((def.def + def.res) / 2);
			}

			// Returns the number of attacks
			if (ignoreRange || atk.weapon.range >= (Math.abs(atk.locX - def.locX) + Math.abs(atk.locY - def.locY))) {
				results[1] = Math.max(1, 1 + Math.floorDiv((atk.spd - def.spd) + (int) (atk.height - def.height), 5));
			} else {
				results[1] = 0;
			}

			return results;
		}

		@Override
		public int[] calculateDefendingDamage(Unit atk, Unit def, boolean ignoreRange) {
			int[] results = new int[2];

			int reduction = StateManager.currentState.tiles[atk.locX][atk.locY].defense;

			// Returns damage dealt
			if (def.weapon.damageType.equals(ItemProperty.TYPE_DAMAGE_PHYSICAL)) {
				results[0] = Math.max(0, def.atk - (atk.def + reduction));
			} else if (def.weapon.damageType.equals(ItemProperty.TYPE_DAMAGE_MAGIC)) {
				results[0] = Math.max(0, def.mag - (atk.res + reduction));
			} else {
				results[0] = Math.max(0, (def.atk + def.mag) - (atk.def + atk.res + reduction));
			}

			// Returns the number of attacks
			if (ignoreRange || def.weapon.range >= (Math.abs(def.locX - atk.locX) + Math.abs(def.locY - atk.locY))) {
				results[1] = Math.max(1, 1 + Math.floorDiv((def.spd - atk.spd) + (int) (def.height - atk.height), 5));
			} else {
				results[1] = 0;
			}

			return results;
		}

		@Override
		public int calculateHealing(Unit healer, Unit healed) {
			return 0;
		}

		@Override
		public int[] getStats() {
			return null;
		}

		@Override
		public void onCombatEnd(Unit unit, StateDungeon context) {
			// Empty
		}

		@Override
		public void onHit(StateDungeon context) {
			// Empty
		}

		@Override
		public void onRetaliation(StateDungeon context) {
			// Empty
		}

		@Override
		public String[] getInfo() {
			return new String[] { toString(), "", "", "Deals extra damage based", "on the targets defenses" };
		}
	},

	ITEM_ABILITY_ATTACK_POISON("Toxic Cut", "target.enemy", true, false, true, false, true, 1, 0) {

		@Override
		public boolean use(Unit u, StateDungeon context) {
			context.combatMode = true;
			return true;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			context.setCombat(context.selectedUnit, u);
			if (context.multiplayerMode) {
				context.comThread.eventQueue.add(new String[] { "USE_ATTACKING_ABILITY", "ITEM_ABILITY_ATTACK_POISON", context.selectedUnit.weapon.uuid, context.selectedUnit.uuid, u.uuid });
				context.comThread.eventQueue.add(new String[] { "CLEAR_TILE_HIGHLIGHT" });
			}
			return true;
		}

		@Override
		public int[] calculateAttackingDamage(Unit atk, Unit def, boolean ignoreRange) {
			int[] results = new int[2];

			int reduction = StateManager.currentState.tiles[def.locX][def.locY].defense;

			// Returns damage dealt
			if (atk.weapon.damageType.equals(ItemProperty.TYPE_DAMAGE_PHYSICAL)) {
				results[0] = Math.max(0, atk.atk - (def.def + reduction));
			} else if (atk.weapon.damageType.equals(ItemProperty.TYPE_DAMAGE_MAGIC)) {
				results[0] = Math.max(0, atk.mag - (def.res + reduction));
			} else {
				results[0] = Math.max(0, (atk.atk + atk.mag) - (def.def + def.res + reduction));
			}

			// Returns the number of attacks
			if (ignoreRange || atk.weapon.range >= (Math.abs(atk.locX - def.locX) + Math.abs(atk.locY - def.locY))) {
				results[1] = 1;
			} else {
				results[1] = 0;
			}

			return results;
		}

		@Override
		public int[] calculateDefendingDamage(Unit atk, Unit def, boolean ignoreRange) {
			int[] results = new int[2];

			int reduction = StateManager.currentState.tiles[atk.locX][atk.locY].defense;

			// Returns damage dealt
			if (def.weapon.damageType.equals(ItemProperty.TYPE_DAMAGE_PHYSICAL)) {
				results[0] = Math.max(0, def.atk - (atk.def + reduction));
			} else if (def.weapon.damageType.equals(ItemProperty.TYPE_DAMAGE_MAGIC)) {
				results[0] = Math.max(0, def.mag - (atk.res + reduction));
			} else {
				results[0] = Math.max(0, (def.atk + def.mag) - (atk.def + atk.res + reduction));
			}

			// Returns the number of attacks
			if (ignoreRange || def.weapon.range >= (Math.abs(def.locX - atk.locX) + Math.abs(def.locY - atk.locY))) {
				results[1] = Math.max(1, 1 + Math.floorDiv((def.spd - atk.spd) + (int) (def.height - atk.height), 5));
			} else {
				results[1] = 0;
			}

			return results;
		}

		@Override
		public int calculateHealing(Unit healer, Unit healed) {
			return 0;
		}

		@Override
		public int[] getStats() {
			return null;
		}

		@Override
		public void onCombatEnd(Unit unit, StateDungeon context) {
			// Empty
		}

		@Override
		public void onHit(StateDungeon context) {
			context.defender.applyStatus(Unit.STATUS_POISON);
		}

		@Override
		public void onRetaliation(StateDungeon context) {
			// Empty
		}

		@Override
		public String[] getInfo() {
			return new String[] { toString(), "", "", "Poisons the target when", "the attack lands" };
		}
	},

	ITEM_ABILITY_ATTACK_SLEEP("Tranq Cut", "target.enemy", true, false, true, false, true, 1, 1) {

		@Override
		public boolean use(Unit u, StateDungeon context) {
			context.combatMode = true;
			return true;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			context.setCombat(context.selectedUnit, u);
			if (context.multiplayerMode) {
				context.comThread.eventQueue.add(new String[] { "USE_ATTACKING_ABILITY", "ITEM_ABILITY_ATTACK_SLEEP", context.selectedUnit.weapon.uuid, context.selectedUnit.uuid, u.uuid });
				context.comThread.eventQueue.add(new String[] { "CLEAR_TILE_HIGHLIGHT" });
			}
			return true;
		}

		@Override
		public int[] calculateAttackingDamage(Unit atk, Unit def, boolean ignoreRange) {
			int[] results = new int[2];

			int reduction = StateManager.currentState.tiles[def.locX][def.locY].defense;

			// Returns damage dealt
			if (atk.weapon.damageType.equals(ItemProperty.TYPE_DAMAGE_PHYSICAL)) {
				results[0] = Math.max(0, atk.atk - (def.def + reduction));
			} else if (atk.weapon.damageType.equals(ItemProperty.TYPE_DAMAGE_MAGIC)) {
				results[0] = Math.max(0, atk.mag - (def.res + reduction));
			} else {
				results[0] = Math.max(0, (atk.atk + atk.mag) - (def.def + def.res + reduction));
			}

			// Returns the number of attacks
			if (ignoreRange || atk.weapon.range >= (Math.abs(atk.locX - def.locX) + Math.abs(atk.locY - def.locY))) {
				results[1] = 1;
			} else {
				results[1] = 0;
			}

			return results;
		}

		@Override
		public int[] calculateDefendingDamage(Unit atk, Unit def, boolean ignoreRange) {
			int[] results = new int[2];

			int reduction = StateManager.currentState.tiles[atk.locX][atk.locY].defense;

			// Returns damage dealt
			if (def.weapon.damageType.equals(ItemProperty.TYPE_DAMAGE_PHYSICAL)) {
				results[0] = Math.max(0, def.atk - (atk.def + reduction));
			} else if (def.weapon.damageType.equals(ItemProperty.TYPE_DAMAGE_MAGIC)) {
				results[0] = Math.max(0, def.mag - (atk.res + reduction));
			} else {
				results[0] = Math.max(0, (def.atk + def.mag) - (atk.def + atk.res + reduction));
			}

			// Returns the number of attacks
			if (ignoreRange || def.weapon.range >= (Math.abs(def.locX - atk.locX) + Math.abs(def.locY - atk.locY))) {
				results[1] = Math.max(1, 1 + Math.floorDiv((def.spd - atk.spd) + (int) (def.height - atk.height), 5));
			} else {
				results[1] = 0;
			}

			return results;
		}

		@Override
		public int calculateHealing(Unit healer, Unit healed) {
			return 0;
		}

		@Override
		public int[] getStats() {
			return null;
		}

		@Override
		public void onCombatEnd(Unit unit, StateDungeon context) {
			if (context.selectedAbility == this) {
				context.defender.applyStatus(Unit.STATUS_SLEEP);
			}
		}

		@Override
		public void onHit(StateDungeon context) {
			// Empty
		}

		@Override
		public void onRetaliation(StateDungeon context) {
			// Empty
		}

		@Override
		public String[] getInfo() {
			return new String[] { toString(), "", "", "Applies sleep to the", "target when hit" };
		}
	},

	ITEM_ABILITY_BRUTE("Brute", "target.none", false, false, false, true, true, 1, 0) {

		@Override
		public boolean use(Unit u, StateDungeon context) {
			return true;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			return true;
		}

		@Override
		public int[] calculateAttackingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int[] calculateDefendingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int calculateHealing(Unit healer, Unit healed) {
			return 10;
		}

		@Override
		public int[] getStats() {
			return new int[] { 0, 10, 0, 0, 0, -10, 0 };
		}

		@Override
		public void onCombatEnd(Unit unit, StateDungeon context) {
			// Empty
		}

		@Override
		public void onHit(StateDungeon context) {
			// Empty
		}

		@Override
		public void onRetaliation(StateDungeon context) {
			// Empty
		}

		@Override
		public String[] getInfo() {
			return new String[] { toString(), "", "", "Increases attack at the", "cost of defense", "atk + 10   def - 10" };
		}
	},

	ITEM_ABILITY_REFLECT("Reflect", "target.none", false, false, false, false, true, 2, 1) {

		@Override
		public boolean use(Unit u, StateDungeon context) {
			return false;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			return false;
		}

		@Override
		public int[] calculateAttackingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int[] calculateDefendingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int calculateHealing(Unit healer, Unit healed) {
			return 0;
		}

		@Override
		public int[] getStats() {
			return null;
		}

		@Override
		public void onCombatEnd(Unit unit, StateDungeon context) {
			// Empty
		}

		@Override
		public void onHit(StateDungeon context) {
			// Empty
		}

		@Override
		public void onRetaliation(StateDungeon context) {
			// Empty
		}

		@Override
		public String[] getInfo() {
			return new String[] { toString(), "", "", "Repels throws objects" };
		}
	},

	ITEM_ABILITY_XPGAIN_10("Growth", "target.none", false, false, false, false, true, 3, 3) {

		@Override
		public boolean use(Unit u, StateDungeon context) {
			return false;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			return false;
		}

		@Override
		public int[] calculateAttackingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int[] calculateDefendingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int calculateHealing(Unit healer, Unit healed) {
			return 0;
		}

		@Override
		public int[] getStats() {
			return null;
		}

		@Override
		public void onCombatEnd(Unit unit, StateDungeon context) {
			// Empty
		}

		@Override
		public void onHit(StateDungeon context) {
			// Empty
		}

		@Override
		public void onRetaliation(StateDungeon context) {
			// Empty
		}

		@Override
		public String[] getInfo() {
			return new String[] { toString(), "", "", "Increases experience gained", "exp x 1.1" };
		}
	},

	ITEM_ABILITY_LOCKSMITH("Locksmith", "target.none", false, false, false, false, true, 1, 1) {

		@Override
		public boolean use(Unit u, StateDungeon context) {
			return false;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			return false;
		}

		@Override
		public int[] calculateAttackingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int[] calculateDefendingDamage(Unit attacker, Unit defender, boolean ignoreRange) {
			return null;
		}

		@Override
		public int calculateHealing(Unit healer, Unit healed) {
			return 0;
		}

		@Override
		public int[] getStats() {
			return null;
		}

		@Override
		public void onCombatEnd(Unit unit, StateDungeon context) {

		}

		@Override
		public void onHit(StateDungeon context) {
			// Empty
		}

		@Override
		public void onRetaliation(StateDungeon context) {
			// Empty
		}

		@Override
		public String[] getInfo() {
			return new String[] { toString(), "", "", "Allows the user to unlock", "doors and chests", "without keys" };
		}
	};

	ItemAbility(String name, String target, boolean showCombat, boolean showHealing, boolean activated, boolean hasStats, boolean canInherit, int size, int rarity) {
		this.name = name;
		this.target = target;
		this.showCombat = showCombat;
		this.showHealing = showHealing;
		this.activated = activated;
		this.hasStats = hasStats;
		this.canInherit = canInherit;
		this.size = size;
		this.rarity = rarity;

		// Calculate gem color
		float b = 0;

		float r = 1;
		for (char c : toString().toCharArray()) {
			r *= c;
		}

		float g = 0;
		g = Math.abs(new Random(toString().length()).nextLong() % (r * 2));

		b += r;
		b += g;

		r %= 255;
		r /= 255;

		g %= 255;
		g /= 255;

		b %= 255;
		b /= 255;

		color = new Vector4f(r > 0.5 ? r : 1 - r, g > 0.5 ? g : 1 - g, b > 0.5 ? b : 1 - b, 1);
	}

	@Override
	public String toString() {
		return name;
	}

	public int getSize() {
		return size;
	}

	public Vector4f getColor() {
		return color;
	}

	public int getRarity() {
		return rarity;
	}

	// These are used for the inital action (use) and what to do after targeting
	// (followUp)
	public abstract boolean use(Unit u, StateDungeon context);

	public abstract boolean followUp(Unit u, StateDungeon context);

	public abstract void onHit(StateDungeon context);

	public abstract void onRetaliation(StateDungeon context);

	// These calculate damage and turn count based on the ability used and
	// perspective
	public abstract int[] calculateAttackingDamage(Unit attacker, Unit defender, boolean ignoreRange);

	public abstract int[] calculateDefendingDamage(Unit attacker, Unit defender, boolean ignoreRange);

	public abstract int calculateHealing(Unit healer, Unit healed);

	// This returns any stat increases an ability may have
	public abstract int[] getStats();

	// This is called after combat ends
	public abstract void onCombatEnd(Unit unit, StateDungeon context);

	public abstract String[] getInfo();

	public static final String TARGET_ENEMY = "target.enemy";
	public static final String TARGET_UNIT = "target.unit";
	public static final String TARGET_NONE = "target.none";

	private String name;
	public String target;
	public boolean showCombat;
	public boolean showHealing;
	public boolean activated;
	public boolean hasStats;
	public boolean canInherit;
	public int size;
	public Vector4f color;
	public int rarity;

	public static ItemAbility getAbilityFromName(String name) {
		for (ItemAbility values : values()) {
			if (values.toString().equals(name)) {
				return values;
			}
		}
		return null;
	}

	public static String[] getNameList(boolean filter) {
		ArrayList<String> names = new ArrayList<>();
		for (int i = 0; i < values().length; i++) {
			if (!filter || values()[i].rarity > -1) {
				names.add(values()[i].name);
			}
		}
		return names.toArray(new String[names.size()]);
	}
}
