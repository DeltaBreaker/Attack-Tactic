package io.itch.deltabreaker.effect.battle;

import io.itch.deltabreaker.effect.Effect;
import io.itch.deltabreaker.math.Vector3f;

public abstract class EffectBattle extends Effect {

	public int frame = 0;
	public int frames;
	public int frameTimer = 0;
	public int frameTime;

	public EffectBattle(Vector3f position, Vector3f rotation) {
		super(position, rotation, new Vector3f(0.5f, 0.5f, 0.5f));
	}

	public void tick() {
		if (frameTimer < frameTime) {
			frameTimer++;
		} else {
			frameTimer = 0;
			if (frame < frames - 1) {
				frame++;
				onFrame();
			} else {
				remove = true;
			}
		}
	}

	public void onFrame() {
		
	}
	
	@Override
	public abstract void render();

	public static EffectBattle getEffect(String name, Vector3f position, boolean dir) {
		return BattleAnimation.valueOf(name).getAnimation(position, dir);
	}
	
}

enum BattleAnimation {
	
	ITEM_ANIMATION_SLASH {
		@Override
		public EffectBattle getAnimation(Vector3f position, boolean dir) {
			return new EffectBattleSlash(position, dir);
		}
	},
	
	ITEM_ANIMATION_PIERCE {
		@Override
		public EffectBattle getAnimation(Vector3f position, boolean dir) {
			return new EffectBattlePierce(position, dir);
		}
	},
	
	ITEM_ANIMATION_BASH {
		@Override
		public EffectBattle getAnimation(Vector3f position, boolean dir) {
			return new EffectBattleBash(position, dir);
		}
	},
	
	ITEM_ANIMATION_FIRE_SMALL {
		@Override
		public EffectBattle getAnimation(Vector3f position, boolean dir) {
			return new EffectBattleMeteor(position, dir);
		}
	},
	
	ITEM_ANIMATION_FIRE_MEDIUM {
		@Override
		public EffectBattle getAnimation(Vector3f position, boolean dir) {
			return new EffectBattleNuclear(position, dir);
		}
	},
	
	ITEM_ANIMATION_DARK_SMALL {
		@Override
		public EffectBattle getAnimation(Vector3f position, boolean dir) {
			return new EffectBattleDarkBlast(position, dir);
		}
	},
	
	ITEM_ANIMATION_DARK_MEDIUM {
		@Override
		public EffectBattle getAnimation(Vector3f position, boolean dir) {
			return new EffectBattleImplosion(position, dir);
		}
	};
	
	public abstract EffectBattle getAnimation(Vector3f position, boolean dir);
	
}