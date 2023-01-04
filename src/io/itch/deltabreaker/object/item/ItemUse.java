package io.itch.deltabreaker.object.item;

import java.util.Random;

import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.effect.EffectItemUse;
import io.itch.deltabreaker.effect.EffectProjectile;
import io.itch.deltabreaker.effect.EffectText;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.state.StateDungeon;
import io.itch.deltabreaker.state.StateManager;

public enum ItemUse {

	ITEM_USE_HEAL_5(true) {
		@Override
		public void use(Unit u, StateDungeon context) {
			context.effects.add(new EffectItemUse(u, context.selectedItem, context));
			context.clearSelectedTiles();
			context.selectedUnit.removeItem(context.selectedItem);
			context.selectedItemUse = this;
		}

		@Override
		public void followUp(Unit u, StateDungeon context) {

		}

		@Override
		public void afterAnimation(Unit u, StateDungeon context) {
			context.selectedUnit.heal(calculateHealing(u, context));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 5;
		}
	},

	ITEM_USE_HEAL_10(true) {
		@Override
		public void use(Unit u, StateDungeon context) {
			context.effects.add(new EffectItemUse(u, context.selectedItem, context));
			context.clearSelectedTiles();
			context.selectedUnit.removeItem(context.selectedItem);
			context.selectedItemUse = this;
		}

		@Override
		public void followUp(Unit u, StateDungeon context) {

		}

		@Override
		public void afterAnimation(Unit u, StateDungeon context) {
			context.selectedUnit.heal(calculateHealing(u, context));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 10;
		}
	},

	ITEM_USE_HEAL_20(true) {
		@Override
		public void use(Unit u, StateDungeon context) {
			context.effects.add(new EffectItemUse(u, context.selectedItem, context));
			context.clearSelectedTiles();
			context.selectedUnit.removeItem(context.selectedItem);
			context.selectedItemUse = this;
		}

		@Override
		public void followUp(Unit u, StateDungeon context) {

		}

		@Override
		public void afterAnimation(Unit u, StateDungeon context) {
			context.selectedUnit.heal(calculateHealing(u, context));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 20;
		}
	},

	ITEM_USE_PEBBLE(false) {
		@Override
		public void use(Unit u, StateDungeon context) {
			context.selectedAbility = ItemAbility.ITEM_ABILITY_USE_ITEM_ENEMY;
			context.selectedItemUse = this;
			context.selectedAbility.use(u, context);

			context.clearSelectedTiles();
			context.highlightTiles(u.locX, u.locY, 0, 6, "");
		}

		@Override
		public void followUp(Unit u, StateDungeon context) {
			context.effects.add(new EffectItemUse(u, context.selectedItem, context));
			context.clearSelectedTiles();
			context.selectedUnit.removeItem(context.selectedItem);
		}

		@Override
		public void afterAnimation(Unit u, StateDungeon context) {
			context.effects.add(new EffectProjectile(context.selectedUnit, u, 0));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}
	},

	ITEM_USE_MAGMA_STONE(false) {
		@Override
		public void use(Unit u, StateDungeon context) {
			context.selectedAbility = ItemAbility.ITEM_ABILITY_USE_ITEM_ENEMY;
			context.selectedItemUse = this;
			context.selectedAbility.use(u, context);

			context.clearSelectedTiles();
			context.highlightTiles(u.locX, u.locY, 0, 6, "");
		}

		@Override
		public void followUp(Unit u, StateDungeon context) {
			context.effects.add(new EffectItemUse(u, context.selectedItem, context));
			context.clearSelectedTiles();
			context.selectedUnit.removeItem(context.selectedItem);
		}

		@Override
		public void afterAnimation(Unit u, StateDungeon context) {
			context.effects.add(new EffectProjectile(context.selectedUnit, u, 1));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}
	},

	ITEM_USE_DART_POISON(false) {
		@Override
		public void use(Unit u, StateDungeon context) {
			context.selectedAbility = ItemAbility.ITEM_ABILITY_USE_ITEM_ENEMY;
			context.selectedItemUse = this;
			context.selectedAbility.use(u, context);

			context.clearSelectedTiles();
			context.highlightTiles(u.locX, u.locY, 0, 4, "");
		}

		@Override
		public void followUp(Unit u, StateDungeon context) {
			context.effects.add(new EffectItemUse(u, context.selectedItem, context));
			context.clearSelectedTiles();
			context.selectedUnit.removeItem(context.selectedItem);
		}

		@Override
		public void afterAnimation(Unit u, StateDungeon context) {
			context.effects.add(new EffectProjectile(context.selectedUnit, u, 2));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}
	},

	ITEM_USE_DART_TRANQ(false) {
		@Override
		public void use(Unit u, StateDungeon context) {
			context.selectedAbility = ItemAbility.ITEM_ABILITY_USE_ITEM_ENEMY;
			context.selectedItemUse = this;
			context.selectedAbility.use(u, context);

			context.clearSelectedTiles();
			context.highlightTiles(u.locX, u.locY, 0, 4, "");
		}

		@Override
		public void followUp(Unit u, StateDungeon context) {
			context.effects.add(new EffectItemUse(u, context.selectedItem, context));
			context.clearSelectedTiles();
			context.selectedUnit.removeItem(context.selectedItem);
		}

		@Override
		public void afterAnimation(Unit u, StateDungeon context) {
			context.effects.add(new EffectProjectile(context.selectedUnit, u, 3));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}
	},

	ITEM_USE_MONEY(true) {
		@Override
		public void use(Unit u, StateDungeon context) {
			context.effects.add(new EffectItemUse(u, context.selectedItem, context));
			context.clearSelectedTiles();
			context.selectedUnit.removeItem(context.selectedItem);
			context.selectedItemUse = this;
		}

		@Override
		public void followUp(Unit u, StateDungeon context) {

		}

		@Override
		public void afterAnimation(Unit u, StateDungeon context) {
			AudioManager.getSound("loot.ogg").play(AudioManager.defaultSubSFXGain, false);
			int gold = 250 + new Random().nextInt(751);
			Inventory.gold += gold;
			StateManager.currentState.effects.add(
					new EffectText("+" + gold + " g", new Vector3f(u.x - ("+" + gold + " g").length() * 1.5f, 20 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y - 8), new Vector4f(ItemProperty.colorList[1], 1)));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}
	},;

	public boolean canUseFreeRoam;

	public abstract void use(Unit u, StateDungeon context);

	public abstract void followUp(Unit u, StateDungeon context);

	public abstract void afterAnimation(Unit u, StateDungeon context);

	public abstract int calculateHealing(Unit u, StateDungeon context);

	private ItemUse(boolean canUseFreeRoam) {
		this.canUseFreeRoam = canUseFreeRoam;
	}

}
