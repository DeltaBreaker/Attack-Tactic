package io.itch.deltabreaker.ui;

import java.awt.Dimension;
import java.util.ArrayList;

import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.ItemAbility;
import io.itch.deltabreaker.state.StateDungeon;
import io.itch.deltabreaker.state.StateManager;

public class HealingCard {

	public static void render(Vector3f position, Unit u1, Unit u2, ItemAbility attack) {
		ArrayList<String> text = new ArrayList<>();
		text.add(u1.name);
		text.add("hp " + u2.currentHp + "@" + Math.min(u2.hp, u2.currentHp + attack.calculateHealing(u1, u2)));
		
		Dimension size = getDimensions(text.toArray(new String[text.size()]));
		UIBox.render(Vector3f.add(position, -size.width / 2, 0, 0), size.width, size.height);
		
		for(int i = 0; i < text.size(); i++) {
			Vector4f color = new Vector4f(1, 1, 1, 1);
			if(i == 0) {
				if(((StateDungeon) StateManager.currentState).enemies.contains(u1)) {
					color = new Vector4f(1, 0.317f, 0.341f, 1);
				} else {
					color = new Vector4f(0, 0.580f, 1, 1);
				}
			}
			TextRenderer.render(text.get(i), Vector3f.add(position, 3 - size.width / 2, -3 - i * 8, 2), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f), color, true);
		}
	}
	
	private static Dimension getDimensions(String[] text) {
		String longest = "";
		int height = 6;
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
