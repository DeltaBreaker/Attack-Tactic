package io.itch.deltabreaker.ai;

import java.awt.Point;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import io.itch.deltabreaker.core.FileManager;
import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.Item;
import io.itch.deltabreaker.object.item.ItemAbility;
import io.itch.deltabreaker.object.item.ItemProperty;
import io.itch.deltabreaker.object.item.ItemUse;
import io.itch.deltabreaker.state.StateDungeon;

public class AIType {

	private static HashMap<String, AIType> aiList = new HashMap<>();

	private String file;
	private String name;
	private boolean canMove;
	private boolean canHeal;
	private String actionType;
	
	public AIType(String file, String name, boolean canMove, boolean canHeal, String actionType) {
		this.file = file;
		this.name = name;
		this.canMove = canMove;
		this.canHeal = canHeal;
		this.actionType = actionType;
	}

	public String getFile() {
		return file;
	}

	public String getName() {
		return name;
	}

	public static AIType[] values() {
		return aiList.values().toArray(new AIType[aiList.size()]);
	}

	public static AIType get(String name) {
		return aiList.get(name);
	}

	public void run(Unit u, AIHandler parent, StateDungeon context) {
		// Set default option to wait
		parent.currentOption = "wait";

		// Equip best weapon
		equipBestWeapon(u);

		// Prevevents movement if asleep
		if (!u.getStatus().equals(Unit.STATUS_SLEEP)) {
			int fitness = 0;

			// Get attack options
			checkForAttackFitness(u, parent, context, fitness, canMove);

			// Move if able to heal
			if (canHeal) {
				fitness = checkForHealingFitness(u, parent, context, fitness, canMove);
			}
						
			// Get default action if still set to wait
			DefaultActionType.valueOf(actionType).getAction(u, parent, context);
		}

		parent.gettingPath = false;
	}

	// Gets a list of the best combat results which show what unit to attack and
	// what ability to use
	public static int checkForAttackFitness(Unit u, AIHandler parent, StateDungeon context, int fitness, boolean canMove) {
		CombatResult[] results = getBestCombatResults(u, context, false, canMove);
		for (CombatResult r : results) {
			Point[] positions = getBestValidAttackPositions(u, r.unit, context);
			if (positions.length > 0) {
				int attackFitness = (r.damage >= r.unit.currentHp) ? 9999 : r.damage;
				if (attackFitness > fitness) {
					if (canMove) {
						context.highlightTiles(u.locX, u.locY, u.movement, u.weapon.range, "enemy");
						parent.currentPath = context.getPath(positions[0].x, positions[0].y);
					}
					parent.attackTarget = r.unit;
					parent.attackAbility = r.ability;
					parent.currentOption = "attack";

					return attackFitness;
				}
			}
		}
		return fitness;
	}

	private static Point[] getBestValidAttackPositions(Unit attacker, Unit defender, StateDungeon context) {
		TreeMap<Integer, Point> points = new TreeMap<>(Collections.reverseOrder());

		context.clearSelectedTiles();
		context.highlightTiles(attacker.locX, attacker.locY, attacker.movement, attacker.weapon.range, "enemy");

		for (int x = 0; x < context.tiles.length; x++) {
			for (int y = 0; y < context.tiles[x].length; y++) {
				if (context.tiles[x][y].status > 1) {
					if (isTileOpen(context, attacker, x, y)) {
						if (attacker.weapon.range >= (Math.abs(defender.locX - x) + Math.abs(defender.locY - y))) {
							int fitness = context.tiles[x][y].defense + (int) (context.tiles[x][y].getPosition().getY() - context.tiles[defender.locX][defender.locY].getPosition().getY()) / 4;
							if (defender.weapon.range < (Math.abs(defender.locX - x) + Math.abs(defender.locY - y))) {
								int[] damage = ItemAbility.ITEM_ABILITY_ATTACK.calculateDefendingDamage(attacker, defender, true);
								fitness += damage[0] * damage[1];
							}
							points.put(fitness, new Point(x, y));
						}
					}
				}
			}
		}

		context.clearSelectedTiles();

		return points.values().toArray(new Point[points.size()]);
	}

	private static Point[] getBestValidDefensePositions(Unit attacker, StateDungeon context, boolean canMove) {
		TreeMap<Integer, Point> points = new TreeMap<>(Collections.reverseOrder());

		for (int x = 0; x < context.tiles.length; x++) {
			for (int y = 0; y < context.tiles[x].length; y++) {
				context.clearSelectedTiles();
				context.highlightTiles(attacker.locX, attacker.locY, (canMove) ? attacker.movement : 2, attacker.weapon.range, "enemy");

				if (context.tiles[x][y].status > 1) {
					if (isTileOpen(context, attacker, x, y)) {
						int fitness = context.tiles[x][y].defense;
						for (Unit u : Inventory.active) {
							context.clearSelectedTiles();
							context.highlightTiles(u.locX, u.locY, u.movement, u.weapon.range, "");

							if (context.tiles[x][y].range < 1) {
								int[] damage = ItemAbility.ITEM_ABILITY_ATTACK.calculateAttackingDamage(u, attacker, true);
								fitness += damage[0] * damage[1];
							}
						}
						points.put(fitness, new Point(x, y));
					}
				}
			}
		}

		context.clearSelectedTiles();

		return points.values().toArray(new Point[points.size()]);
	}

	public static Point[] getBestValidRoamingPositions(Unit unit, StateDungeon context) {
		TreeMap<Float, Point> points = new TreeMap<>(Collections.reverseOrder());

		context.clearSelectedTiles();
		context.highlightTiles(unit.locX, unit.locY, unit.movement, unit.weapon.range, "enemy");

		for (int x = 0; x < context.tiles.length; x++) {
			for (int y = 0; y < context.tiles[x].length; y++) {
				if (context.tiles[x][y].status > 1) {
					if (isTileOpen(context, unit, x, y)) {
						int fitness = unit.movement - context.tiles[x][y].status;
						for (Item i : context.items) {
							if (i.locX == x && i.locY == y) {
								fitness += i.item.tier + 1;
								break;
							}
						}
						points.put(fitness + new Random().nextFloat(), new Point(x, y));
					}
				}
			}
		}

		context.clearSelectedTiles();

		return points.values().toArray(new Point[points.size()]);

	}

	public static Point[] getBestValidPursuingPositions(Unit unit, Unit target, StateDungeon context) {
		TreeMap<Float, Point> points = new TreeMap<>();

		context.clearSelectedTiles();
		context.highlightTiles(unit.locX, unit.locY, unit.movement, unit.weapon.range, "enemy");

		for (int x = 0; x < context.tiles.length; x++) {
			for (int y = 0; y < context.tiles[x].length; y++) {
				if (context.tiles[x][y].status > 1) {
					if (isTileOpen(context, unit, x, y)) {
						int fitness = context.tiles[x][y].status + Math.abs(x - target.locX) + Math.abs(y - target.locY);
						for (Item i : context.items) {
							if (i.locX == x && i.locY == y) {
								fitness += i.item.tier + 1;
								break;
							}
						}
						points.put(fitness + new Random().nextFloat(), new Point(x, y));
					}
				}
			}
		}

		context.clearSelectedTiles();

		return points.values().toArray(new Point[points.size()]);

	}

	public static CombatResult[] getBestCombatResults(Unit attacker, StateDungeon context, boolean ignoreRange, boolean canMove) {
		TreeMap<Integer, CombatResult> results = new TreeMap<>(Collections.reverseOrder());

		context.clearSelectedTiles();
		context.highlightTiles(attacker.locX, attacker.locY, (canMove) ? attacker.movement : 2, attacker.weapon.range, "enemy");

		for (Unit u : Inventory.active) {
			if (context.tiles[u.locX][u.locY].range > 0 || ignoreRange) {
				int[] combatResults = ItemAbility.ITEM_ABILITY_ATTACK.calculateAttackingDamage(attacker, u, true);
				int[] sustainResults = ItemAbility.ITEM_ABILITY_ATTACK.calculateDefendingDamage(attacker, u, true);
				if (combatResults[0] * combatResults[1] > 0) {
					int fitness = combatResults[0] * combatResults[1];
					if (sustainResults[0] * sustainResults[1] >= attacker.currentHp) {
						fitness -= sustainResults[0] * sustainResults[1];
					}
					results.put(fitness, new CombatResult(u, ItemAbility.ITEM_ABILITY_ATTACK, combatResults[0] * combatResults[1], sustainResults[0] * sustainResults[1]));
				}

				for (String i : attacker.weapon.abilities) {
					if (ItemAbility.valueOf(i).activated) {
						combatResults = ItemAbility.valueOf(i).calculateAttackingDamage(attacker, u, true);
						sustainResults = ItemAbility.valueOf(i).calculateDefendingDamage(attacker, u, true);
						if (combatResults != null) {
							if (combatResults[0] * combatResults[1] > 0) {
								int fitness = combatResults[0] * combatResults[1];
								if (sustainResults[0] * sustainResults[1] >= attacker.currentHp) {
									fitness -= sustainResults[0] * sustainResults[1];
								}
								results.put(fitness, new CombatResult(u, ItemAbility.valueOf(i), combatResults[0] * combatResults[1], sustainResults[0] * sustainResults[1]));
							}
						}
					}
				}
			}
		}

		context.clearSelectedTiles();

		return results.values().toArray(new CombatResult[results.size()]);
	}

	public static boolean isTileOpen(StateDungeon context, Unit disregard, int x, int y) {
		for (Unit u : Inventory.active) {
			if (u != disregard) {
				if (u.locX == x && u.locY == y) {
					return false;
				}
			}
		}
		for (Unit u : context.enemies) {
			if (u != disregard) {
				if (u.locX == x && u.locY == y) {
					return false;
				}
			}
		}
		return !context.tiles[x][y].isSolid();
	}

	public Point getDefaultRoamLocation(Unit u, StateDungeon context) {
		Unit target = Inventory.active.get(0);

		boolean direction = new Random().nextBoolean();
		int amt = (new Random().nextBoolean()) ? 1 : -1;
		Point location = new Point((direction) ? u.locX : u.locX + amt, (!direction) ? u.locY : u.locY + amt);

		// Prevents loop from being infinate
		int tries = 16;

		int distance = Math.abs(u.locX - target.locX) + Math.abs(u.locY - target.locY);

		// Random walking
		if (distance > 3) {
			while (tries > 0 && (!context.isTileWalkable(location.x, location.y) || (location.x == target.locX && location.y == target.locY))) {
				direction = new Random().nextBoolean();
				amt = (new Random().nextBoolean()) ? 1 : -1;
				location.setLocation((direction) ? u.locX : u.locX + amt, (!direction) ? u.locY : u.locY + amt);
				tries--;
			}
		} else {
			// Move closer if in range
			int x = (direction) ? u.locX : u.locX + amt;
			int y = (!direction) ? u.locY : u.locY + amt;
			int postDistance = Math.abs(x - target.locX) + Math.abs(y - target.locY);

			while (tries > 0 && (!context.isTileWalkable(location.x, location.y) || (location.x == target.locX && location.y == target.locY) || postDistance > distance)) {
				direction = new Random().nextBoolean();
				amt = (new Random().nextBoolean()) ? 1 : -1;

				x = (direction) ? u.locX : u.locX + amt;
				y = (!direction) ? u.locY : u.locY + amt;
				postDistance = Math.abs(x - target.locX) + Math.abs(y - target.locY);

				location.setLocation(x, y);
				tries--;
			}
		}

		return (tries > 0) ? location : new Point(u.locX, u.locY);
	}

	// This returns whether a unit should use an ability or not (non-combat)
	public static boolean shouldUseAbility(Unit unit, StateDungeon context, ItemAbility ability) {
		switch (ability) {

		case ITEM_ABILITY_HARDEN:
			return true;

		default:
			return false;

		}
	}

	public static String[] getNameList() {
		ArrayList<String> aiNames = new ArrayList<>();
		for (AIType ai : aiList.values()) {
			aiNames.add(ai.name);
		}
		return aiNames.toArray(new String[aiNames.size()]);
	}

	public static int checkForHealingFitness(Unit u, AIHandler parent, StateDungeon context, int fitness, boolean canMove) {
		// If you can heal more damage than you deal you will heal instead. If would-be
		// combat is fatal, healing is the highest priority
		for (ItemProperty i : u.getItemList()) {
			if (i.use != null && ItemUse.valueOf(i.use) != null) {
				int healing = Math.min(u.hp - u.currentHp, ItemUse.valueOf(i.use).calculateHealing(u, context));
				if (healing > 0 && healing > fitness) {
					Point[] positions = getBestValidDefensePositions(u, context, canMove);
					if (positions.length > 0) {
						context.highlightTiles(u.locX, u.locY, u.movement, u.weapon.range, "enemy");
						parent.currentPath = context.getPath(positions[0].x, positions[0].y);
						parent.itemUse = i;
						parent.attackTarget = u;
						parent.currentOption = "item";

						fitness = healing;

						int[] sustain = parent.attackAbility.calculateDefendingDamage(u, parent.attackTarget, true);
						if (parent.currentOption.equals("attack") && sustain[0] * sustain[1] >= u.currentHp) {
							fitness = 9999;
						}
						return fitness;
					}
				}
			}
		}
		return fitness;
	}

	public static void equipBestWeapon(Unit u) {
		int attack = 0;
		ItemProperty weapon = u.weapon;

		switch (u.weapon.damageType) {

		case ItemProperty.TYPE_DAMAGE_PHYSICAL:
			attack = u.weapon.atk + u.baseAtk;
			break;

		case ItemProperty.TYPE_DAMAGE_MAGIC:
			attack = u.weapon.mag + u.baseMag;
			break;

		default:
			attack = (u.weapon.atk + u.weapon.mag + u.baseAtk + u.baseMag) / 2;
			break;

		}

		for (ItemProperty i : u.getItemList()) {
			if (i.type.equals(ItemProperty.TYPE_WEAPON)) {
				switch (i.damageType) {

				case ItemProperty.TYPE_DAMAGE_PHYSICAL:
					if (attack < i.atk + u.baseAtk) {
						attack = i.atk + u.baseAtk;
						weapon = i;
					}
					break;

				case ItemProperty.TYPE_DAMAGE_MAGIC:
					if (attack < i.mag + u.baseMag) {
						attack = i.mag + u.baseMag;
						weapon = i;
					}
					break;

				default:
					if (attack < (i.atk + u.baseAtk + i.mag + u.baseMag) / 2) {
						attack = (i.atk + u.baseAtk + i.mag + u.baseMag) / 2;
						weapon = i;
					}
					break;

				}
			}
		}

		if (weapon != u.weapon) {
			u.lastWeapon = weapon.id;
			u.removeItem(weapon);
			u.addItem(u.weapon);
			u.weapon = weapon;

			u.updateStats();
		}
	}

	public static void loadAITypes(String folder) {
		File dir = new File(folder);
		if (dir.exists()) {
			for (File f : FileManager.getFiles(folder)) {
				try {
					JSONObject jo = (JSONObject) new JSONParser().parse(new FileReader(f));
					
					String name = (String) jo.get("name");
					boolean canMove = (boolean) jo.get("can_move");
					boolean canHeal = (boolean) jo.get("can_heal");
					String actionType = (String) jo.get("default_action");
					
					aiList.put(f.getName(), new AIType(f.getName(), name, canMove, canHeal, actionType));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.out.println("[AIType]: AI types loaded with size " + aiList.size());
		}
	}

	public static AIType getDefault() {
		for(AIType ai : aiList.values()) {
			return ai;
		}
		return null;
	}

}

class CombatResult {

	public Unit unit;
	public ItemAbility ability;
	public int damage;
	public int sustain;

	public CombatResult(Unit unit, ItemAbility ability, int damage, int sustain) {
		this.unit = unit;
		this.ability = ability;
		this.damage = damage;
		this.sustain = sustain;
	}

}

enum DefaultActionType {

	ROAM {
		@Override
		public void getAction(Unit u, AIHandler parent, StateDungeon context) {
			// Get a roaming position if no combat options are available
			if (parent.currentOption.equals("wait")) {
				Point[] positions = AIType.getBestValidRoamingPositions(u, context);
				context.highlightTiles(u.locX, u.locY, u.movement, u.weapon.range, "enemy");
				parent.currentPath = context.getPath(positions[0].x, positions[0].y);

				for (String s : u.weapon.abilities) {
					if (AIType.shouldUseAbility(u, context, ItemAbility.valueOf(s))) {
						parent.currentOption = "ability";
						parent.attackAbility = ItemAbility.valueOf(s);
					}
				}
			}
		}
	},

	PURSUE {
		@Override
		public void getAction(Unit u, AIHandler parent, StateDungeon context) {
			// Get a roaming position if no combat options are available
			if (parent.currentOption.equals("wait")) {
				CombatResult[] results = AIType.getBestCombatResults(u, context, true, true);
				for (CombatResult r : results) {
					Point[] positions = AIType.getBestValidPursuingPositions(u, r.unit, context);
					if (positions.length > 0) {
						context.highlightTiles(u.locX, u.locY, u.movement, u.weapon.range, "enemy");
						parent.currentPath = context.getPath(positions[0].x, positions[0].y);
						break;
					}
				}

				for (String s : u.weapon.abilities) {
					if (AIType.shouldUseAbility(u, context, ItemAbility.valueOf(s))) {
						parent.currentOption = "ability";
						parent.attackAbility = ItemAbility.valueOf(s);
					}
				}
			}
		}
	},

	IDLE {
		@Override
		public void getAction(Unit u, AIHandler parent, StateDungeon context) {
			if (parent.currentOption.equals("wait")) {
				for (String s : u.weapon.abilities) {
					if (AIType.shouldUseAbility(u, context, ItemAbility.valueOf(s))) {
						parent.currentOption = "ability";
						parent.attackAbility = ItemAbility.valueOf(s);
					}
				}
			}
		}
	};

	public abstract void getAction(Unit u, AIHandler parent, StateDungeon context);

}