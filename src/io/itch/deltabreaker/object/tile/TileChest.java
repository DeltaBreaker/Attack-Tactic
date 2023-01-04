package io.itch.deltabreaker.object.tile;

import java.util.ArrayList;
import java.util.Random;

import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.effect.EffectOpenChest;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.ItemProperty;
import io.itch.deltabreaker.state.StateManager;

public class TileChest extends TileCompound {

	public ItemProperty item;
	
	public TileChest(TileProperty property, Vector3f position) {
		super(property, position);
	}
	
	public void action(Unit u, String[] args) {
		if (property.property.equals(Tile.PROPERTY_CHEST)) {
			if(item == null) {
				ItemProperty[] items = ItemProperty.searchForTier(Integer.parseInt(args[0]), ItemProperty.getItemList(), false);
				item = items[new Random().nextInt(items.length)].copy();
				u.addItem(item);
			}
			
			ArrayList<String> tags = new ArrayList<>();
			for (String s : property.tags) {
				if (!s.equals(Tile.TAG_CHEST_CLOSED)) {
					tags.add(s);
				} else {
					tags.add(Tile.TAG_CHEST_OPEN);
				}
			}
			property = getProperty(tags.toArray(new String[tags.size()]))[0];
			StateManager.currentState.effects.add(new EffectOpenChest(
					Vector3f.add(Vector3f.div(position, 2, 2, 2), 0, 8, 0), item));
			updateMatrix();
			AudioManager.getSound("door_open.ogg").play(AudioManager.defaultMainSFXGain, false);
			AudioManager.getSound("loot.ogg").play(AudioManager.defaultMainSFXGain, false);
		}
	}

}
