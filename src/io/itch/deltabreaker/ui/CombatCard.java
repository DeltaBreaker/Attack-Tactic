package io.itch.deltabreaker.ui;

import java.awt.Dimension;
import java.util.ArrayList;

import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.ItemProperty;
import io.itch.deltabreaker.state.StateDungeon;
import io.itch.deltabreaker.state.StateManager;

public class CombatCard {

	public static void renderLeftToRight(Vector3f position, Unit unit, int[] results) {
		ArrayList<String> text = new ArrayList<>();
		
		text.add(unit.name);
		text.add("hp " + unit.currentHp);
		text.add("atk "  + results[0] + " x " + results[1]);
		text.add(unit.weapon.name);
		
		Dimension size = getDimensions(text.toArray(new String[text.size()]));
		UIBox.render(position, size.width, size.height);
		
		for(int i = 0; i < text.size(); i++) {
			Vector4f color = new Vector4f(1, 1, 1, 1);
			if(i == 0) {
				if(((StateDungeon) StateManager.currentState).enemies.contains(unit)) {
					color = new Vector4f(1, 0.317f, 0.341f, 1);
				} else {
					color = new Vector4f(0, 0.580f, 1, 1);
				}
			}
			if(i == 3) {
				color = new Vector4f(ItemProperty.colorList[unit.weapon.tier], 1);
			}
			TextRenderer.render(text.get(i), Vector3f.add(position, 3, -3 - i * 8, 2), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f), color, true);
		}
	}
	
	public static void renderRightToLeft(Vector3f position, Unit unit, int[] results) {
		ArrayList<String> text = new ArrayList<>();
		
		text.add(unit.name);
		text.add("hp " + unit.currentHp);
		text.add("atk "  + results[0] + " x " + results[1]);
		text.add(unit.weapon.name);
		
		Dimension size = getDimensions(text.toArray(new String[text.size()]));
		UIBox.render(Vector3f.add(position, -size.width, 0, 0), size.width, size.height);
		
		for(int i = 0; i < text.size(); i++) {
			Vector4f color = new Vector4f(1, 1, 1, 1);
			if(i == 0) {
				if(((StateDungeon) StateManager.currentState).enemies.contains(unit)) {
					color = new Vector4f(1, 0.317f, 0.341f, 1);
				} else {
					color = new Vector4f(0, 0.580f, 1, 1);
				}
			}
			if(i == 3) {
				color = new Vector4f(ItemProperty.colorList[unit.weapon.tier], 1);
			}
			TextRenderer.render(text.get(i), Vector3f.add(position, 4 - size.width, -3 - i * 8, 2), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f), color, true);
		}
	}
	
	private static Dimension getDimensions(String[] text) {
		String longest = "";
		int height = 4;
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
	
}
