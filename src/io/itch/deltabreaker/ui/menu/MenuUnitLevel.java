package io.itch.deltabreaker.ui.menu;

import java.awt.Dimension;

import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.state.StateDungeon;
import io.itch.deltabreaker.state.StateManager;
import io.itch.deltabreaker.ui.UIBox;

public class MenuUnitLevel extends Menu {

	private Vector3f cursorPos;
	private Unit unit;
	private int boosts = 3;

	private int baseHp;
	private int baseAtk;
	private int baseMag;
	private int baseSpd;
	private int baseDef;
	private int baseRes;

	private StateDungeon context;
	
	public MenuUnitLevel(Vector3f position, Unit unit, StateDungeon context) {
		super(position, new String[] { "hp - " + unit.hp, "atk - " + unit.atk, "mag - " + unit.mag, "spd - " + unit.spd, "def - " + unit.def, "res - " + unit.res });
		this.unit = unit;
		unit.level++;
		StateManager.currentState.cursor.staticView = true;
		cursorPos = Vector3f.add(StateManager.currentState.cursor.position, 0, 0, 0);
		StateManager.currentState.cursor.warpLocation(Vector3f.add(position, 0, -9, 0));

		baseHp = unit.baseHp;
		baseAtk = unit.baseAtk;
		baseMag = unit.baseMag;
		baseSpd = unit.baseSpd;
		baseDef = unit.baseDef;
		baseRes = unit.baseRes;
		
		this.context = context;
	}

	protected static Dimension getDimensions(String[] text, Unit unit) {
		String longest = unit.name + " lvl " + unit.level;
		int height = 18;
		int times = 0;
		for (String s : text) {
			if (s.length() > longest.length()) {
				longest = s;
			}
			if (times < 5) {
				height += 8;
				times++;
			}
		}
		return new Dimension(longest.length() * 6 + 9, height);
	}

	public void update() {
		width = getDimensions(options, unit).width;
		openTo = getDimensions(options, unit).height;
	}

	public void tick() {
		if(context.xpAlpha == 0) {
			update();
			options = new String[] { "hp - " + unit.hp, "atk - " + unit.atk, "mag - " + unit.mag, "spd - " + unit.spd, "def - " + unit.def, "res - " + unit.res };
			if (selected > options.length - 1) {
				selected = options.length - 1;
			}
			if (options.length == 0) {
				open = false;
			}
			if (selected > options.length - 1) {
				selected = options.length - 1;
			}
			if (open) {
				if (height < openTo) {
					height = Math.min(height + 3, openTo);
				}
				if (height > openTo) {
					height = Math.max(height - 3, openTo);
				}
			} else {
				if (height > 0) {
					height = Math.max(height - 3, 16);
				}
			}
			if (subMenu.size() > 0) {
				subMenu.get(0).tick();
				if (!subMenu.get(0).open && subMenu.get(0).height <= 16) {
					subMenu.remove(0);
				}
			}
			if (selected > options.length - 1) {
				selected = options.length - 1;
			}
			if (selected < 0) {
				selected = 0;
			}
			if (subMenu.size() == 0 && open && StateManager.currentState.status.size() == 0 && StateManager.currentState.itemInfo.size() == 0) {
				Startup.staticView.targetPosition = new Vector3f(position.getX() / 2 - 1 + width / 4, position.getY() / 2 - openTo / 4, Startup.staticView.position.getZ());
				StateManager.currentState.cursor.setLocation(new Vector3f(position.getX() - 10, position.getY() - 17 - 8 * Math.min(4, selected), position.getZ() + 4));
			}
		}
	}

	public void render() {
		if(context.xpAlpha == 0) {
			UIBox.render(position, width, height);
			TextRenderer.render(unit.name + " lvl " + unit.level, Vector3f.add(position, 4, -6, 1), new Vector3f(0, 0, 0), scale, new Vector4f(1, 1, 1, 1), true);
			for (int i = 0; i < Math.min(5, options.length); i++) {
				if (i * 8 + 12 < height) {
					TextRenderer.render(options[i + Math.max(0, selected - 4)], Vector3f.add(position, 4, (-i - Math.max(0, selected - options.length)) * 8 - 14, 1), new Vector3f(0, 0, 0), scale, new Vector4f(1, 1, 1, 1), true);
				}
			}
			if (subMenu.size() > 0) {
				subMenu.get(0).render();
			}
		}
	}

	@Override
	public void close() {
		super.close();
		StateManager.currentState.cursor.staticView = false;
		StateManager.currentState.cursor.warpLocation(Vector3f.add(cursorPos, 0, 0, 0));
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("return")) {
				switch (selected) {

				case 0:
					if (this.unit.baseHp < 99) {
						this.unit.baseHp++;
						boosts--;
						AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
					}
					break;

				case 1:
					if (this.unit.baseAtk < 60) {
						this.unit.baseAtk++;
						boosts--;
						AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
					}
					break;

				case 2:
					if (this.unit.baseMag < 60) {
						this.unit.baseMag++;
						boosts--;
						AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
					}
					break;

				case 3:
					if (this.unit.baseSpd < 60) {
						this.unit.baseSpd++;
						boosts--;
						AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
					}
					break;

				case 4:
					if (this.unit.baseDef < 60) {
						this.unit.baseDef++;
						boosts--;
						AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
					}
					break;

				case 5:
					if (this.unit.baseRes < 60) {
						this.unit.baseRes++;
						boosts--;
						AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
					}
					break;

				}
				if (boosts == 0) {
					close();
				}
			} else {
				switch (selected) {

				case 0:
					if (this.unit.baseHp > baseHp) {
						this.unit.baseHp--;
						boosts++;
						AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
					}
					break;

				case 1:
					if (this.unit.baseAtk > baseAtk) {
						this.unit.baseAtk--;
						boosts++;
						AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
					}
					break;

				case 2:
					if (this.unit.baseMag > baseMag) {
						this.unit.baseMag--;
						boosts++;
						AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
					}
					break;

				case 3:
					if (this.unit.baseSpd > baseSpd) {
						this.unit.baseSpd--;
						boosts++;
						AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
					}
					break;

				case 4:
					if (this.unit.baseDef > baseDef) {
						this.unit.baseDef--;
						boosts++;
						AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
					}
					break;

				case 5:
					if (this.unit.baseRes > baseRes) {
						this.unit.baseRes--;
						boosts++;
						AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
					}
					break;

				}
			}
		} else {
			subMenu.get(0).action(command, unit);
		}
	}

}
