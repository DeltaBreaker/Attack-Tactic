package io.itch.deltabreaker.multiplayer.client;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.effect.EffectPoof;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.multiplayer.GameInputStream;
import io.itch.deltabreaker.multiplayer.GameOutputStream;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.Item;
import io.itch.deltabreaker.object.item.ItemAbility;
import io.itch.deltabreaker.object.item.ItemProperty;
import io.itch.deltabreaker.state.StateDungeon;
import io.itch.deltabreaker.state.StateManager;

public class MatchComThread implements Runnable {

	private long WAIT_TIME = 1;

	private Socket socket;
	private GameInputStream in;
	private GameOutputStream out;
	private StateDungeon context;
	public ArrayList<String[]> eventQueue = new ArrayList<>();

	public MatchComThread(Socket socket, GameInputStream in, GameOutputStream out, StateDungeon context) {
		this.socket = socket;
		this.out = out;
		this.in = in;
		this.context = context;
	}

	@Override
	public void run() {
		try {
			while (!socket.isClosed()) {
				if (context.phase == 0 || eventQueue.size() > 0) {
					if (eventQueue.size() == 0) {
						Thread.sleep(WAIT_TIME);
					} else {
						MatchEvent.valueOf(eventQueue.get(0)[0]).send(eventQueue.get(0), context, in, out, this);
						if (eventQueue.size() > 0) {
							eventQueue.remove(0);
						}
					}
				} else {
					MatchEvent.valueOf(in.readUTF()).recieve(context, in, out, this);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			matchEnd();
			JOptionPane.showMessageDialog(null, "There was an error processing this match. Disconnecting.");
		}
	}

	private void matchEnd() {
		try {
			socket.close();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(0);
		}

		// Do something here to send to another state after match or after an error
	}

}

enum MatchEvent {

	CHANGE_PHASE {
		@Override
		public void recieve(StateDungeon context, GameInputStream in, GameOutputStream out, MatchComThread comThread) throws Exception {
			context.changePhase(in.readInt());
		}

		@Override
		public void send(String[] args, StateDungeon context, GameInputStream in, GameOutputStream out, MatchComThread comThread) throws Exception {
			comThread.eventQueue.clear();
			out.writeUTF("CHANGE_PHASE");
			out.writeInt(Integer.parseInt(args[1]));
		}
	},

	MOVE_CURSOR {
		@Override
		public void recieve(StateDungeon context, GameInputStream in, GameOutputStream out, MatchComThread comThread) throws Exception {
			context.cursorPos.setLocation(in.readInt(), in.readInt());
			context.tcamX = in.readDouble();
			context.tcamY = in.readDouble();
			AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
		}

		@Override
		public void send(String[] args, StateDungeon context, GameInputStream in, GameOutputStream out, MatchComThread comThread) throws Exception {
			out.writeUTF("MOVE_CURSOR");
			out.writeInt(context.cursorPos.x);
			out.writeInt(context.cursorPos.y);
			out.writeDouble(context.tcamX);
			out.writeDouble(context.tcamY);
		}
	},

	HIGHLIGHT_UNIT {
		@Override
		public void recieve(StateDungeon context, GameInputStream in, GameOutputStream out, MatchComThread comThread) throws Exception {
			String uuid = in.readUTF();
			Unit u = Inventory.loaded.get(uuid);
			u.select();
		}

		@Override
		public void send(String[] args, StateDungeon context, GameInputStream in, GameOutputStream out, MatchComThread comThread) throws Exception {
			out.writeUTF("HIGHLIGHT_UNIT");
			out.writeUTF(args[1]);
		}
	},

	CLEAR_SEL_UNIT {
		@Override
		public void recieve(StateDungeon context, GameInputStream in, GameOutputStream out, MatchComThread comThread) throws Exception {
			context.clearSelectedTiles();
			context.clearUnit();
		}

		@Override
		public void send(String[] args, StateDungeon context, GameInputStream in, GameOutputStream out, MatchComThread comThread) throws Exception {
			out.writeUTF("CLEAR_SEL_UNIT");
		}
	},

	MOVE_UNIT_PATH {
		@Override
		public void recieve(StateDungeon context, GameInputStream in, GameOutputStream out, MatchComThread comThread) throws Exception {
			Inventory.loaded.get(in.readUTF()).move(context.getPath(in.readInt(), in.readInt()));
		}

		@Override
		public void send(String[] args, StateDungeon context, GameInputStream in, GameOutputStream out, MatchComThread comThread) throws Exception {
			out.writeUTF("MOVE_UNIT_PATH");
			out.writeUTF(args[1]);
			out.writeInt(Integer.parseInt(args[2]));
			out.writeInt(Integer.parseInt(args[3]));
		}
	},

	RESET_UNIT {
		@Override
		public void recieve(StateDungeon context, GameInputStream in, GameOutputStream out, MatchComThread comThread) throws Exception {
			context.selectedUnit.reset();
			context.clearSelectedTiles();
			context.clearUnit();
		}

		@Override
		public void send(String[] args, StateDungeon context, GameInputStream in, GameOutputStream out, MatchComThread comThread) throws Exception {
			out.writeUTF("RESET_UNIT");
		}
	},

	CLEAR_TILE_HIGHLIGHT {
		@Override
		public void recieve(StateDungeon context, GameInputStream in, GameOutputStream out, MatchComThread comThread) throws Exception {
			context.clearSelectedTiles();
		}

		@Override
		public void send(String[] args, StateDungeon context, GameInputStream in, GameOutputStream out, MatchComThread comThread) throws Exception {
			out.writeUTF("CLEAR_TILE_HIGHLIGHT");
		}
	},

	HIGHLIGHT_TILES {
		@Override
		public void recieve(StateDungeon context, GameInputStream in, GameOutputStream out, MatchComThread comThread) throws Exception {
			context.highlightTiles(in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readUTF());
		}

		@Override
		public void send(String[] args, StateDungeon context, GameInputStream in, GameOutputStream out, MatchComThread comThread) throws Exception {
			out.writeUTF("HIGHLIGHT_TILES");
			out.writeInt(Integer.parseInt(args[1]));
			out.writeInt(Integer.parseInt(args[2]));
			out.writeInt(Integer.parseInt(args[3]));
			out.writeInt(Integer.parseInt(args[4]));
			out.writeUTF(args[5]);
		}
	},

	UNIT_WAIT {
		@Override
		public void recieve(StateDungeon context, GameInputStream in, GameOutputStream out, MatchComThread comThread) throws Exception {
			Inventory.loaded.get(in.readUTF()).setTurn(false);
			AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
		}

		@Override
		public void send(String[] args, StateDungeon context, GameInputStream in, GameOutputStream out, MatchComThread comThread) throws Exception {
			out.writeUTF("UNIT_WAIT");
			out.writeUTF(args[1]);
		}
	},

	USE_ATTACKING_ABILITY {
		@Override
		public void recieve(StateDungeon context, GameInputStream in, GameOutputStream out, MatchComThread comThread) throws Exception {
			context.selectedAbility = ItemAbility.valueOf(in.readUTF());
			String weapon = in.readUTF();
			Unit attacker = Inventory.loaded.get(in.readUTF());
			Unit defender = Inventory.loaded.get(in.readUTF());
			if (!attacker.weapon.uuid.equals(weapon)) {
				for (ItemProperty i : attacker.getItemList()) {
					if (i.uuid.equals(weapon)) {
						ItemProperty temp = i;
						attacker.removeItem(i);
						attacker.addItem(attacker.weapon);
						attacker.weapon = temp;
						attacker.lastWeapon = attacker.weapon.id;
						break;
					}
				}
			}
			context.setCombat(attacker, defender);
		}

		@Override
		public void send(String[] args, StateDungeon context, GameInputStream in, GameOutputStream out, MatchComThread comThread) throws Exception {
			out.writeUTF("USE_ATTACKING_ABILITY");
			out.writeUTF(args[1]);
			out.writeUTF(args[2]);
			out.writeUTF(args[3]);
			out.writeUTF(args[4]);
		}
	},

	PICK_UP_ITEM {
		@Override
		public void recieve(StateDungeon context, GameInputStream in, GameOutputStream out, MatchComThread comThread) throws Exception {
			String item = in.readUTF();
			String unit = in.readUTF();
			for (Item i : context.items) {
				if (i.item.uuid.equals(item)) {
					Unit u = Inventory.loaded.get(unit);
					int overflow = u.addItem(i.item);
					if (overflow == 0) {
						StateManager.currentState.effects.add(new EffectPoof(Vector3f.add(i.position, 0, StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), 0)));
						context.items.remove(i);
					}
					u.setTurn(false);
					context.clearSelectedTiles();
					context.clearUnit();
					AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
					AudioManager.getSound("loot.ogg").play(AudioManager.defaultMainSFXGain, false);
					break;
				}
			}
		}

		@Override
		public void send(String[] args, StateDungeon context, GameInputStream in, GameOutputStream out, MatchComThread comThread) throws Exception {
			out.writeUTF("PICK_UP_ITEM");
			out.writeUTF(args[1]);
			out.writeUTF(args[2]);
		}
	},

	DROP_ITEM {
		@Override
		public void recieve(StateDungeon context, GameInputStream in, GameOutputStream out, MatchComThread comThread) throws Exception {
			Unit u = Inventory.loaded.get(in.readUTF());
			String item = in.readUTF();
			String uuid = in.readUTF();
			byte amt = in.readByte();

			System.out.println(u.name);
			System.out.println(item);
			System.out.println("---------------------------");
			
			for (int i = 0; i < u.getItemList().size(); i++) {
				System.out.println(u.getItemList().get(i).uuid);
				if (u.getItemList().get(i).uuid.equals(item)) {
					ItemProperty itemCopy = u.getItemList().get(i);
					itemCopy.uuid = uuid;
					itemCopy.stack = amt;
					context.items.add(new Item(new Vector3f(u.locX * 16, 16, u.locY * 16), u.getItemList().get(i)));
					u.removeItem(itemCopy, amt);
					AudioManager.getSound("footsteps_0.ogg").play(AudioManager.defaultSubSFXGain, false);
					StateManager.currentState.effects.add(new EffectPoof(Vector3f.add(new Vector3f(u.locX * 16, 20, u.locY * 16), 0, StateManager.currentState.tiles[u.locX][u.locY].getPosition().getY(), 0)));
					break;
				}
			}
		}

		@Override
		public void send(String[] args, StateDungeon context, GameInputStream in, GameOutputStream out, MatchComThread comThread) throws Exception {
			out.writeUTF("DROP_ITEM");
			out.writeUTF(args[1]);
			out.writeUTF(args[2]);
			out.writeUTF(args[3]);
			out.writeByte(Byte.parseByte(args[4]));
		}
	};

	public abstract void recieve(StateDungeon context, GameInputStream in, GameOutputStream out, MatchComThread comThread) throws Exception;

	public abstract void send(String[] args, StateDungeon context, GameInputStream in, GameOutputStream out, MatchComThread comThread) throws Exception;

}