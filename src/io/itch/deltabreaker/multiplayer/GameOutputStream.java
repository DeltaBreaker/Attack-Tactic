package io.itch.deltabreaker.multiplayer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.ItemProperty;

public class GameOutputStream extends DataOutputStream {

	public GameOutputStream(OutputStream out) {
		super(out);
	}

	public void writeItem(ItemProperty item) throws IOException {
		writeUTF(item.id);
		writeUTF(item.uuid);
		
		if (item.type.equals(ItemProperty.TYPE_WEAPON) || item.type.equals(ItemProperty.TYPE_ACCESSORY)) {
			writeInt(item.abilities.length);
			for(int i = 0; i < item.abilities.length; i++) {
				writeUTF(item.abilities[i]);
			}
		}
		
		writeInt(item.stack);
	}
	
	public void writeUnit(Unit u) throws IOException {
		writeUTF(u.uuid);
		writeUTF(u.name);
		writeInt(u.hair);
		writeFloat(u.hairColor.getX());
		writeFloat(u.hairColor.getY());
		writeFloat(u.hairColor.getZ());
		writeInt(u.race);
		writeInt(u.body);
		writeFloat(u.bodyColor.getX());
		writeFloat(u.bodyColor.getY());
		writeFloat(u.bodyColor.getZ());

		writeInt(u.baseHp);
		writeInt(u.baseAtk);
		writeInt(u.baseMag);
		writeInt(u.baseSpd);
		writeInt(u.baseDef);
		writeInt(u.baseRes);
		writeInt(u.level);
		writeInt(u.baseMovement);

		writeUTF(u.weapon.id);
		writeUTF(u.weapon.uuid);
		writeInt(u.weapon.abilities.length);
		for (String s : u.weapon.abilities) {
			writeUTF(s);
		}

		writeInt(u.getItemList().size());
		for (ItemProperty i : u.getItemList()) {
			writeUTF(i.id);
			writeUTF(i.uuid);
			writeInt(i.abilities.length);
			for (String s : i.abilities) {
				writeUTF(s);
			}
		}

		// Output weapon ability unlock stats when added

		writeUTF(u.armor.id);
		writeUTF(u.armor.uuid);
		writeUTF(u.accessory.id);
		writeUTF(u.accessory.uuid);

		writeUTF(u.AIPattern.getFile());
	}
	
}
