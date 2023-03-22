package io.itch.deltabreaker.object.item;

import java.util.Random;

import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.effect.EffectBuff;
import io.itch.deltabreaker.effect.EffectItemUse;
import io.itch.deltabreaker.effect.EffectProjectile;
import io.itch.deltabreaker.effect.EffectText;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.state.StateDungeon;
import io.itch.deltabreaker.state.StateManager;

public enum ItemUse {

	ITEM_USE_POTION_XS_HP(true) {
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
			u.baseHp = Math.min(99, u.baseHp + 1);
			context.effects.add(new EffectText("+1 HP", new Vector3f(u.x - ("+1 HP").length() * 1.5f, 20 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y - 8), new Vector4f(ItemProperty.colorList[1], 1)));
			context.effects.add(new EffectBuff(new Vector3f(u.x, 10 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y)));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 0;
		}
	},

	ITEM_USE_POTION_XS_ATK(true) {
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
			u.baseAtk = Math.min(60, u.baseAtk + 1);
			context.effects.add(new EffectText("+1 ATK", new Vector3f(u.x - ("+1 ATK").length() * 1.5f, 20 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y - 8), new Vector4f(ItemProperty.colorList[1], 1)));
			context.effects.add(new EffectBuff(new Vector3f(u.x, 10 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y)));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 0;
		}
	},
	
	ITEM_USE_POTION_XS_MAG(true) {
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
			u.baseMag = Math.min(60, u.baseMag + 1);
			context.effects.add(new EffectText("+1 MAG", new Vector3f(u.x - ("+1 MAG").length() * 1.5f, 20 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y - 8), new Vector4f(ItemProperty.colorList[1], 1)));
			context.effects.add(new EffectBuff(new Vector3f(u.x, 10 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y)));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 0;
		}
	},
	
	ITEM_USE_POTION_XS_SPD(true) {
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
			u.baseSpd = Math.min(60, u.baseSpd + 1);
			context.effects.add(new EffectText("+1 SPD", new Vector3f(u.x - ("+1 SPD").length() * 1.5f, 20 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y - 8), new Vector4f(ItemProperty.colorList[1], 1)));
			context.effects.add(new EffectBuff(new Vector3f(u.x, 10 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y)));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 0;
		}
	},
	
	ITEM_USE_POTION_XS_DEF(true) {
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
			u.baseDef = Math.min(60, u.baseDef + 1);
			context.effects.add(new EffectText("+1 DEF", new Vector3f(u.x - ("+1 DEF").length() * 1.5f, 20 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y - 8), new Vector4f(ItemProperty.colorList[1], 1)));
			context.effects.add(new EffectBuff(new Vector3f(u.x, 10 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y)));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 0;
		}
	},
	
	ITEM_USE_POTION_XS_RES(true) {
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
			u.baseRes = Math.min(60, u.baseRes + 1);
			context.effects.add(new EffectText("+1 RES", new Vector3f(u.x - ("+1 RES").length() * 1.5f, 20 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y - 8), new Vector4f(ItemProperty.colorList[1], 1)));
			context.effects.add(new EffectBuff(new Vector3f(u.x, 10 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y)));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 0;
		}
	},
	
	ITEM_USE_POTION_SM_HP(true) {
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
			u.baseHp = Math.min(99, u.baseHp + 2);
			context.effects.add(new EffectText("+2 HP", new Vector3f(u.x - ("+2 HP").length() * 1.5f, 20 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y - 8), new Vector4f(ItemProperty.colorList[1], 1)));
			context.effects.add(new EffectBuff(new Vector3f(u.x, 10 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y)));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 0;
		}
	},

	ITEM_USE_POTION_SM_ATK(true) {
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
			u.baseAtk = Math.min(60, u.baseAtk + 2);
			context.effects.add(new EffectText("+2 ATK", new Vector3f(u.x - ("+2 ATK").length() * 1.5f, 20 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y - 8), new Vector4f(ItemProperty.colorList[1], 1)));
			context.effects.add(new EffectBuff(new Vector3f(u.x, 10 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y)));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 0;
		}
	},
	
	ITEM_USE_POTION_SM_MAG(true) {
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
			u.baseMag = Math.min(60, u.baseMag + 2);
			context.effects.add(new EffectText("+2 MAG", new Vector3f(u.x - ("+2 MAG").length() * 1.5f, 20 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y - 8), new Vector4f(ItemProperty.colorList[1], 1)));
			context.effects.add(new EffectBuff(new Vector3f(u.x, 10 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y)));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 0;
		}
	},
	
	ITEM_USE_POTION_SM_SPD(true) {
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
			u.baseSpd = Math.min(60, u.baseSpd + 2);
			context.effects.add(new EffectText("+2 SPD", new Vector3f(u.x - ("+2 SPD").length() * 1.5f, 20 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y - 8), new Vector4f(ItemProperty.colorList[1], 1)));
			context.effects.add(new EffectBuff(new Vector3f(u.x, 10 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y)));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 0;
		}
	},
	
	ITEM_USE_POTION_SM_DEF(true) {
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
			u.baseDef = Math.min(60, u.baseDef + 2);
			context.effects.add(new EffectText("+2 DEF", new Vector3f(u.x - ("+2 DEF").length() * 1.5f, 20 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y - 8), new Vector4f(ItemProperty.colorList[1], 1)));
			context.effects.add(new EffectBuff(new Vector3f(u.x, 10 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y)));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 0;
		}
	},
	
	ITEM_USE_POTION_SM_RES(true) {
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
			u.baseRes = Math.min(60, u.baseRes + 2);
			context.effects.add(new EffectText("+2 RES", new Vector3f(u.x - ("+2 RES").length() * 1.5f, 20 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y - 8), new Vector4f(ItemProperty.colorList[1], 1)));
			context.effects.add(new EffectBuff(new Vector3f(u.x, 10 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y)));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 0;
		}
	},
	
	ITEM_USE_POTION_MD_HP(true) {
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
			u.baseHp = Math.min(99, u.baseHp + 3);
			context.effects.add(new EffectText("+3 HP", new Vector3f(u.x - ("+3 HP").length() * 1.5f, 20 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y - 8), new Vector4f(ItemProperty.colorList[1], 1)));
			context.effects.add(new EffectBuff(new Vector3f(u.x, 10 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y)));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 0;
		}
	},

	ITEM_USE_POTION_MD_ATK(true) {
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
			u.baseAtk = Math.min(60, u.baseAtk + 3);
			context.effects.add(new EffectText("+3 ATK", new Vector3f(u.x - ("+3 ATK").length() * 1.5f, 20 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y - 8), new Vector4f(ItemProperty.colorList[1], 1)));
			context.effects.add(new EffectBuff(new Vector3f(u.x, 10 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y)));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 0;
		}
	},
	
	ITEM_USE_POTION_MD_MAG(true) {
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
			u.baseMag = Math.min(60, u.baseMag + 3);
			context.effects.add(new EffectText("+3 MAG", new Vector3f(u.x - ("+3 MAG").length() * 1.5f, 20 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y - 8), new Vector4f(ItemProperty.colorList[1], 1)));
			context.effects.add(new EffectBuff(new Vector3f(u.x, 10 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y)));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 0;
		}
	},
	
	ITEM_USE_POTION_MD_SPD(true) {
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
			u.baseSpd = Math.min(60, u.baseSpd + 3);
			context.effects.add(new EffectText("+3 SPD", new Vector3f(u.x - ("+3 SPD").length() * 1.5f, 20 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y - 8), new Vector4f(ItemProperty.colorList[1], 1)));
			context.effects.add(new EffectBuff(new Vector3f(u.x, 10 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y)));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 0;
		}
	},
	
	ITEM_USE_POTION_MD_DEF(true) {
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
			u.baseDef = Math.min(60, u.baseDef + 3);
			context.effects.add(new EffectText("+3 DEF", new Vector3f(u.x - ("+3 DEF").length() * 1.5f, 20 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y - 8), new Vector4f(ItemProperty.colorList[1], 1)));
			context.effects.add(new EffectBuff(new Vector3f(u.x, 10 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y)));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 0;
		}
	},
	
	ITEM_USE_POTION_MD_RES(true) {
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
			u.baseRes = Math.min(60, u.baseRes + 3);
			context.effects.add(new EffectText("+3 RES", new Vector3f(u.x - ("+3 RES").length() * 1.5f, 20 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y - 8), new Vector4f(ItemProperty.colorList[1], 1)));
			context.effects.add(new EffectBuff(new Vector3f(u.x, 10 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y)));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 0;
		}
	},
	
	ITEM_USE_POTION_LG_HP(true) {
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
			u.baseHp = Math.min(99, u.baseHp + 4);
			context.effects.add(new EffectText("+4 HP", new Vector3f(u.x - ("+4 HP").length() * 1.5f, 20 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y - 8), new Vector4f(ItemProperty.colorList[1], 1)));
			context.effects.add(new EffectBuff(new Vector3f(u.x, 10 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y)));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 0;
		}
	},

	ITEM_USE_POTION_LG_ATK(true) {
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
			u.baseAtk = Math.min(60, u.baseAtk + 4);
			context.effects.add(new EffectText("+4 ATK", new Vector3f(u.x - ("+4 ATK").length() * 1.5f, 20 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y - 8), new Vector4f(ItemProperty.colorList[1], 1)));
			context.effects.add(new EffectBuff(new Vector3f(u.x, 10 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y)));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 0;
		}
	},
	
	ITEM_USE_POTION_LG_MAG(true) {
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
			u.baseMag = Math.min(60, u.baseMag + 4);
			context.effects.add(new EffectText("+4 MAG", new Vector3f(u.x - ("+4 MAG").length() * 1.5f, 20 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y - 8), new Vector4f(ItemProperty.colorList[1], 1)));
			context.effects.add(new EffectBuff(new Vector3f(u.x, 10 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y)));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 0;
		}
	},
	
	ITEM_USE_POTION_LG_SPD(true) {
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
			u.baseSpd = Math.min(60, u.baseSpd + 4);
			context.effects.add(new EffectText("+4 SPD", new Vector3f(u.x - ("+4 SPD").length() * 1.5f, 20 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y - 8), new Vector4f(ItemProperty.colorList[1], 1)));
			context.effects.add(new EffectBuff(new Vector3f(u.x, 10 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y)));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 0;
		}
	},
	
	ITEM_USE_POTION_LG_DEF(true) {
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
			u.baseDef = Math.min(60, u.baseDef + 4);
			context.effects.add(new EffectText("+4 DEF", new Vector3f(u.x - ("+4 DEF").length() * 1.5f, 20 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y - 8), new Vector4f(ItemProperty.colorList[1], 1)));
			context.effects.add(new EffectBuff(new Vector3f(u.x, 10 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y)));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 0;
		}
	},
	
	ITEM_USE_POTION_LG_RES(true) {
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
			u.baseRes = Math.min(60, u.baseRes + 4);
			context.effects.add(new EffectText("+4 RES", new Vector3f(u.x - ("+4 RES").length() * 1.5f, 20 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y - 8), new Vector4f(ItemProperty.colorList[1], 1)));
			context.effects.add(new EffectBuff(new Vector3f(u.x, 10 + StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), u.y)));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 0;
		}
	},
	
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

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 0;
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

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 0;
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

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 0;
		}
	},

	ITEM_USE_PEBBLE(false) {
		@Override
		public void use(Unit u, StateDungeon context) {
			context.selectedAbility = ItemAbility.ITEM_ABILITY_USE_ITEM_ENEMY;
			context.selectedItemUse = this;
			context.selectedAbility.use(u, context);

			context.clearSelectedTiles();
			context.highlightTiles(u.locX, u.locY, 0, getRange(u, context), "");
		}

		@Override
		public void followUp(Unit u, StateDungeon context) {
			context.effects.add(new EffectItemUse(u, context.selectedItem, context));
			context.clearSelectedTiles();
			context.selectedUnit.removeItem(context.selectedItem);
		}

		@Override
		public void afterAnimation(Unit u, StateDungeon context) {
			context.effects.add(new EffectProjectile(context.selectedUnit, u, 0, calculateDamage(context.selectedUnit, context)));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			boolean hasReflect = u.weapon.hasAbility(ItemAbility.ITEM_ABILITY_REFLECT) || u.accessory.hasAbility(ItemAbility.ITEM_ABILITY_REFLECT);
			return (hasReflect) ? 0 : 5;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 6;
		}
	},

	ITEM_USE_MAGMA_STONE(false) {
		@Override
		public void use(Unit u, StateDungeon context) {
			context.selectedAbility = ItemAbility.ITEM_ABILITY_USE_ITEM_ENEMY;
			context.selectedItemUse = this;
			context.selectedAbility.use(u, context);

			context.clearSelectedTiles();
			context.highlightTiles(u.locX, u.locY, 0, getRange(u, context), "");
		}

		@Override
		public void followUp(Unit u, StateDungeon context) {
			context.effects.add(new EffectItemUse(u, context.selectedItem, context));
			context.clearSelectedTiles();
			context.selectedUnit.removeItem(context.selectedItem);
		}

		@Override
		public void afterAnimation(Unit u, StateDungeon context) {
			context.effects.add(new EffectProjectile(context.selectedUnit, u, 1, calculateDamage(context.selectedUnit, context)));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			boolean hasReflect = u.weapon.hasAbility(ItemAbility.ITEM_ABILITY_REFLECT) || u.accessory.hasAbility(ItemAbility.ITEM_ABILITY_REFLECT);
			return (hasReflect) ? 0 : 10;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 6;
		}
	},

	ITEM_USE_DART_POISON(false) {
		@Override
		public void use(Unit u, StateDungeon context) {
			context.selectedAbility = ItemAbility.ITEM_ABILITY_USE_ITEM_ENEMY;
			context.selectedItemUse = this;
			context.selectedAbility.use(u, context);

			context.clearSelectedTiles();
			context.highlightTiles(u.locX, u.locY, 0, getRange(u, context), "");
		}

		@Override
		public void followUp(Unit u, StateDungeon context) {
			context.effects.add(new EffectItemUse(u, context.selectedItem, context));
			context.clearSelectedTiles();
			context.selectedUnit.removeItem(context.selectedItem);
		}

		@Override
		public void afterAnimation(Unit u, StateDungeon context) {
			context.effects.add(new EffectProjectile(context.selectedUnit, u, 2, calculateDamage(context.selectedUnit, context)));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			boolean hasReflect = u.weapon.hasAbility(ItemAbility.ITEM_ABILITY_REFLECT) || u.accessory.hasAbility(ItemAbility.ITEM_ABILITY_REFLECT);
			return (hasReflect || !u.getStatus().equals("")) ? 0 : u.currentHp;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 4;
		}
	},

	ITEM_USE_DART_TRANQ(false) {
		@Override
		public void use(Unit u, StateDungeon context) {
			context.selectedAbility = ItemAbility.ITEM_ABILITY_USE_ITEM_ENEMY;
			context.selectedItemUse = this;
			context.selectedAbility.use(u, context);

			context.clearSelectedTiles();
			context.highlightTiles(u.locX, u.locY, 0, getRange(u, context), "");
		}

		@Override
		public void followUp(Unit u, StateDungeon context) {
			context.effects.add(new EffectItemUse(u, context.selectedItem, context));
			context.clearSelectedTiles();
			context.selectedUnit.removeItem(context.selectedItem);
		}

		@Override
		public void afterAnimation(Unit u, StateDungeon context) {
			context.effects.add(new EffectProjectile(context.selectedUnit, u, 3, calculateDamage(context.selectedUnit, context)));
			context.selectedUnit.setTurn(false);
			context.clearUnit();
			context.clearAtkDefUnit();
		}

		@Override
		public int calculateHealing(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			boolean hasReflect = u.weapon.hasAbility(ItemAbility.ITEM_ABILITY_REFLECT) || u.accessory.hasAbility(ItemAbility.ITEM_ABILITY_REFLECT);
			return (hasReflect || !u.getStatus().equals("")) ? 0 : u.atk;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 4;
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

		@Override
		public int calculateDamage(Unit u, StateDungeon context) {
			return 0;
		}

		@Override
		public int getRange(Unit u, StateDungeon context) {
			return 0;
		}
	};

	public boolean canUseFreeRoam;

	public abstract void use(Unit u, StateDungeon context);

	public abstract void followUp(Unit u, StateDungeon context);

	public abstract void afterAnimation(Unit u, StateDungeon context);

	public abstract int calculateHealing(Unit u, StateDungeon context);

	public abstract int calculateDamage(Unit u, StateDungeon context);

	public abstract int getRange(Unit u, StateDungeon context);

	private ItemUse(boolean canUseFreeRoam) {
		this.canUseFreeRoam = canUseFreeRoam;
	}

}
