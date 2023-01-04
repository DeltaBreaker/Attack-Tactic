package io.itch.deltabreaker.object.tile;

import java.util.ArrayList;

import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.ItemProperty;

public class TileDoorSeparate extends Tile {

	public TileDoorSeparate(TileProperty property, Vector3f position) {
		super(property, position);
	}

	public void action(Unit u, String[] args) {
		if (property.property.equals(PROPERTY_DOOR_LOCKED)) {
			ArrayList<String> tags = new ArrayList<>();
			for (String s : property.tags) {
				if (!s.equals(Tile.TAG_DOOR_LOCKED)) {
					tags.add(s);
				} else {
					tags.add(Tile.TAG_DOOR_OPEN);
				}
			}
			property = getProperty(tags.toArray(new String[tags.size()]))[0];

			// Removes the key from the unit
			for (int i = 0; i < u.getItemList().size(); i++) {
				if (u.getItemList().get(i).type.equals(ItemProperty.TYPE_KEY_CHEST)) {
					u.removeItem(u.getItemList().get(i));
					break;
				}
			}
			
			AudioManager.getSound("unlock.ogg").play(AudioManager.defaultMainSFXGain, false);
		} else {
			if (containsTag(Tile.TAG_DOOR_CLOSED)) {
				ArrayList<String> tags = new ArrayList<>();
				for (String s : property.tags) {
					if (!s.equals(Tile.TAG_DOOR_CLOSED)) {
						tags.add(s);
					} else {
						tags.add(Tile.TAG_DOOR_OPEN);
					}
				}
				property = getProperty(tags.toArray(new String[tags.size()]))[0];
				AudioManager.getSound("door_open.ogg").play(AudioManager.defaultMainSFXGain, false);
			} else {
				ArrayList<String> tags = new ArrayList<>();
				for (String s : property.tags) {
					if (!s.equals(Tile.TAG_DOOR_OPEN)) {
						tags.add(s);
					} else {
						tags.add(Tile.TAG_DOOR_CLOSED);
					}
				}
				property = getProperty(tags.toArray(new String[tags.size()]))[0];
				AudioManager.getSound("door_close.ogg").play(AudioManager.defaultMainSFXGain, false);
			}
		}
	}

}
