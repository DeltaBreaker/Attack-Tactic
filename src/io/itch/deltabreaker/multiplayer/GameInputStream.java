package io.itch.deltabreaker.multiplayer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import io.itch.deltabreaker.ai.AIType;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.ItemProperty;

public class GameInputStream extends DataInputStream {

	public GameInputStream(InputStream in) {
		super(in);
	}

	public ItemProperty readItem() throws IOException {
		ItemProperty item = ItemProperty.get(readUTF());
		
		item.uuid = readUTF();
		
		if (item.type.equals(ItemProperty.TYPE_WEAPON) || item.type.equals(ItemProperty.TYPE_ACCESSORY)) {
			item.abilities = new String[readInt()];
			for(int i = 0; i < item.abilities.length; i++) {
				item.abilities[i] = readUTF();
			}
		}
		
		item.stack = readInt();
		
		return item;
	}
	
	public Unit readUnit() throws IOException {
		Unit u = new Unit(-1, -1, Vector4f.COLOR_BASE, readUTF());
		
		u.name = readUTF();
		u.setHair(readInt(), new Vector4f(readFloat(), readFloat(), readFloat(), 1));
		u.setBody(readInt(), readInt(), new Vector4f(readFloat(), readFloat(), readFloat(), 1));
		u.baseHp = readInt();
		u.baseAtk = readInt();
		u.baseMag = readInt();
		u.baseSpd = readInt();
		u.baseDef = readInt();
		u.baseRes = readInt();
		u.level = readInt();
		u.baseMovement = readInt();

		u.weapon = ItemProperty.get(readUTF());
		u.weapon.uuid = readUTF();
		String[] abilities = new String[readInt()];
		for (int i = 0; i < abilities.length; i++) {
			abilities[i] = readUTF();
		}
		u.weapon.abilities = abilities;

		int length = readInt();
		for (int i = 0; i < length; i++) {
			ItemProperty item = ItemProperty.get(readUTF());
			item.uuid = readUTF();
			String[] itemAbilities = new String[readInt()];
			for (int j = 0; j < itemAbilities.length; j++) {
				itemAbilities[j] = readUTF();
			}
			u.addItem(item);
		}

		u.armor = ItemProperty.get(readUTF());
		u.armor.uuid = readUTF();
		u.accessory = ItemProperty.get(readUTF());
		u.accessory.uuid = readUTF();

		// Set and clean up
		u.currentHp = u.baseHp + u.weapon.hp + u.armor.hp + u.offsetHp + u.accessory.hp;
		u.lastWeapon = u.weapon.id;

		u.AIPattern = AIType.get(readUTF());
		
		return u;
	}
	
}
