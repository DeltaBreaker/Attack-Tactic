package io.itch.deltabreaker.object.item;

import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.effect.EffectBuff;
import io.itch.deltabreaker.effect.EffectPoof;
import io.itch.deltabreaker.effect.EffectText;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.state.StateDungeon;
import io.itch.deltabreaker.state.StateManager;
import io.itch.deltabreaker.ui.Message;
import io.itch.deltabreaker.ui.menu.MenuDungeonSteal;

public enum ItemAbility {

	// The standard attack
	ITEM_ABILITY_ATTACK("Attack", "target.enemy", true, false, true, false, false) {

		@Override
		public boolean isUnlocked(ItemProperty item) {
			return true;
		}

		@Override
		public boolean use(Unit u, StateDungeon context) {
			context.combatMode = true;
			return true;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			context.setCombat(context.selectedUnit, u);
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
	},

	ITEM_ABILITY_USE_ITEM_ALLY("", "target.unit", false, false, true, false, false) {

		@Override
		public boolean isUnlocked(ItemProperty item) {
			return false;
		}

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
	},

	ITEM_ABILITY_USE_ITEM_ENEMY("", "target.enemy", false, false, true, false, false) {

		@Override
		public boolean isUnlocked(ItemProperty item) {
			return true;
		}

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
	},

	ITEM_ABILITY_SWAP("Swap", "target.unit", false, false, true, false, true) {

		@Override
		public boolean isUnlocked(ItemProperty item) {
			return true;
		}

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
	},

	ITEM_ABILITY_STEAL("Steal", "target.enemy", false, false, true, false, true) {

		@Override
		public boolean isUnlocked(ItemProperty item) {
			return true;
		}

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
				context.messages.add(new Message(new String[] { "Theres nothing to steal" }));
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
	},

	ITEM_ABILITY_LOCKSMITH("Locksmith", "target.none", false, false, false, false, true) {

		@Override
		public boolean isUnlocked(ItemProperty item) {
			return true;
		}

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
	},

	// The standard attack
	ITEM_ABILITY_DISARM("Disarm", "target.enemy", true, false, true, false, true) {

		@Override
		public boolean isUnlocked(ItemProperty item) {
			return true;
		}

		@Override
		public boolean use(Unit u, StateDungeon context) {
			context.combatMode = true;
			return true;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			context.setCombat(context.selectedUnit, u);
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
	},

	ITEM_ABILITY_FORTIFIED("Fortified", "target.none", false, false, false, true, true) {

		@Override
		public boolean isUnlocked(ItemProperty item) {
			return true;
		}

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
	},

	ITEM_ABILITY_SHELTER("Shelter", "target.none", false, false, false, false, true) {

		@Override
		public boolean isUnlocked(ItemProperty item) {
			return true;
		}

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
	},

	ITEM_ABILITY_HARDEN("Harden", "target.none", false, false, true, false, true) {

		@Override
		public boolean isUnlocked(ItemProperty item) {
			return true;
		}

		@Override
		public boolean use(Unit u, StateDungeon context) {
			u.offsetDef += 4;
			u.offsetRes += 4;
			context.effects
					.add(new EffectText("+4 Def_Res", new Vector3f(u.x - ("+4 Def_Res").length() * 1.5f, 20 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y - 8), new Vector4f(ItemProperty.colorList[1], 1)));
			context.effects.add(new EffectBuff(new Vector3f(u.x, 10 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y)));
			u.setTurn(false);
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
	},

	ITEM_ABILITY_HEAL_10("Heal", "target.unit", false, true, true, false, false) {

		@Override
		public boolean isUnlocked(ItemProperty item) {
			return true;
		}

		@Override
		public boolean use(Unit u, StateDungeon context) {
			context.combatMode = true;
			return true;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			u.heal(calculateHealing(context.selectedUnit, u));
			context.selectedUnit.setTurn(false);
			context.clearSelectedTiles();
			context.clearUnit();
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
	},

	ITEM_ABILITY_WARP("Warp", "target.none", false, false, true, false, false) {

		@Override
		public boolean isUnlocked(ItemProperty item) {
			return true;
		}

		@Override
		public boolean use(Unit u, StateDungeon context) {
			context.combatMode = true;
			return true;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			if (context.isTileAvailable(context.cursorPos.x, context.cursorPos.y)) {
				u.setTurn(false);
				u.placeAt(context.cursorPos.x, context.cursorPos.y);
				u.height = StateManager.currentState.tiles[(int) Math.round(u.x / 16.0)][(int) Math.round(u.y / 16.0)].getPosition().getY();
				context.effects.add(new EffectPoof(new Vector3f(u.x, u.height + 13, u.y + 2)));
				AudioManager.getSound("warp.ogg").play(AudioManager.defaultMainSFXGain, false);
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

	},

	ITEM_ABILITY_WEAK_POINT("Weak Point", "target.enemy", true, false, true, false, true) {

		@Override
		public boolean isUnlocked(ItemProperty item) {
			return true;
		}

		@Override
		public boolean use(Unit u, StateDungeon context) {
			context.combatMode = true;
			return true;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			context.setCombat(context.selectedUnit, u);
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
	},

	ITEM_ABILITY_ATTACK_POISON("Toxic Cut", "target.enemy", true, false, true, false, true) {

		@Override
		public boolean isUnlocked(ItemProperty item) {
			return true;
		}

		@Override
		public boolean use(Unit u, StateDungeon context) {
			context.combatMode = true;
			return true;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			context.setCombat(context.selectedUnit, u);
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
	},

	ITEM_ABILITY_ATTACK_SLEEP("Tranq Cut", "target.enemy", true, false, true, false, true) {

		@Override
		public boolean isUnlocked(ItemProperty item) {
			return true;
		}

		@Override
		public boolean use(Unit u, StateDungeon context) {
			context.combatMode = true;
			return true;
		}

		@Override
		public boolean followUp(Unit u, StateDungeon context) {
			context.setCombat(context.selectedUnit, u);
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
	},

	ITEM_ABILITY_BRUTE("Brute", "target.none", false, false, false, true, true) {

		@Override
		public boolean isUnlocked(ItemProperty item) {
			return true;
		}

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
	},

	ITEM_ABILITY_REFLECT("Reflect", "target.none", false, false, false, false, true) {

		@Override
		public boolean isUnlocked(ItemProperty item) {
			return true;
		}

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
	},

	ITEM_ABILITY_XPGAIN_10("Growth", "target.none", false, false, false, false, true) {

		@Override
		public boolean isUnlocked(ItemProperty item) {
			return true;
		}

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
	};

	ItemAbility(String name, String target, boolean showCombat, boolean showHealing, boolean activated, boolean hasStats, boolean canInherit) {
		this.name = name;
		this.target = target;
		this.showCombat = showCombat;
		this.showHealing = showHealing;
		this.activated = activated;
		this.hasStats = hasStats;
		this.canInherit = canInherit;
	}

	@Override
	public String toString() {
		return name;
	}

	// This is called to check if a certain weapon has met the requirements to use a
	// certain ability
	public abstract boolean isUnlocked(ItemProperty item);

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

	public static ItemAbility getAbilityFromName(String name) {
		for (ItemAbility values : values()) {
			if (values.toString().equals(name)) {
				return values;
			}
		}
		return null;
	}

	public static String[] getNameList() {
		String[] names = new String[values().length];
		for (int i = 0; i < names.length; i++) {
			names[i] = values()[i].name;
		}
		return names;
	}
}
