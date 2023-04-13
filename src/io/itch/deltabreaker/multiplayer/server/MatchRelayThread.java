package io.itch.deltabreaker.multiplayer.server;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;

import io.itch.deltabreaker.builder.dungeon.DungeonGenerator;
import io.itch.deltabreaker.multiplayer.GameInputStream;
import io.itch.deltabreaker.multiplayer.GameOutputStream;
import io.itch.deltabreaker.object.Unit;

public class MatchRelayThread implements Runnable {

	private Socket clientOne, clientTwo;
	private GameInputStream inOne, inTwo;
	private GameOutputStream outOne, outTwo;
	public boolean startingPlayer;
	public boolean matchRunning = false;
	
	// Customs
	private int units;
	private int floor;
	private String map;

	public String roomID;
	public String password;
	public String hash;
	public String nameOne, nameTwo;

	public MatchRelayThread(String roomID, Socket clientOne, GameInputStream in, GameOutputStream out) {
		this.roomID = roomID;
		this.clientOne = clientOne;
		inOne = in;
		outOne = out;
		try {
			password = in.readUTF();
			hash = in.readUTF();
			nameOne = in.readUTF();

			units = in.readInt();
			String floor = in.readUTF();
			this.floor = (floor.toLowerCase().equals("random")) ? new Random().nextInt(50) : Integer.parseInt(floor);
			map = in.readUTF();

			out.writeUTF(roomID);
			
			QueueThread.addMatch(this);
			System.out.println("[MatchRelayThread]: Client one connected");
			System.out.println("[MatchRelayThread]: Password for room " + roomID + " was set as " + password);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("[MatchRelayThread]: Error creating room " + roomID);
			disconnectClients();
		}
	}

	public void connect(Socket clientTwo, GameInputStream in, GameOutputStream out) {
		QueueThread.removeMatch(this);
		this.clientTwo = clientTwo;
		inTwo = in;
		outTwo = out;
		try {
			nameTwo = in.readUTF();
			new Thread(this).start();
			System.out.println("[MatchRelayThread]: Client two connected");
		} catch (IOException e) {
			e.printStackTrace();
			QueueThread.addMatch(this);
		}
	}

	public void disconnectClients() {
		try {
			inOne.close();
			outOne.close();
			clientOne.close();
			inTwo.close();
			outTwo.close();
			clientTwo.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void closeRoom() {
		disconnectClients();
		QueueThread.removeMatch(this);
	}

	@Override
	public void run() {
		startingPlayer = new Random().nextBoolean();
		long seed = new Random().nextLong();
		
		try {
			if (map.toLowerCase().equals("random")) {
				String[] maps = DungeonGenerator.getPalletTags();
				map = maps[new Random().nextInt(maps.length)];
			}
			if (map.toLowerCase().equals("vote")) {
				outOne.writeBoolean(true);
				outTwo.writeBoolean(true);
				
				String mapOne = inOne.readUTF();
				String mapTwo = inTwo.readUTF();
				
				map = (new Random().nextBoolean()) ? mapOne : mapTwo;
			} else {
				outOne.writeBoolean(false);
				outTwo.writeBoolean(false);
			}
			
			outOne.writeUTF(nameTwo);
			outTwo.writeUTF(nameOne);
			
			outOne.writeInt(units);
			outTwo.writeInt(units);
			
			Unit[] unitsOne = new Unit[units];
			for (int i = 0; i < units; i++) {
				unitsOne[i] = inOne.readUnit();
			}
			
			Unit[] unitsTwo = new Unit[units];
			for (int i = 0; i < units; i++) {
				unitsTwo[i] = inTwo.readUnit();
			}

			for (int i = 0; i < units; i++) {
				outOne.writeUnit(unitsTwo[i]);
				outTwo.writeUnit(unitsOne[i]);
			}
			
			outOne.writeBoolean(startingPlayer);
			outTwo.writeBoolean(!startingPlayer);

			outOne.writeUTF(map);
			outTwo.writeUTF(map);

			outOne.writeLong(seed);
			outTwo.writeLong(seed);

			outOne.writeInt(floor);
			outTwo.writeInt(floor);

			matchRunning = true;
			while (!clientOne.isClosed() && !clientTwo.isClosed() && matchRunning) {
				if (startingPlayer) {
					ServerEvent.valueOf(inOne.readUTF()).run(this, inOne, outOne, inTwo, outTwo);
				} else {
					ServerEvent.valueOf(inTwo.readUTF()).run(this, inTwo, outTwo, inOne, outOne);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("[MatchRelayThread]: Match error occurred. Closing.");
			closeRoom();
		}
	}

}

enum ServerEvent {

	CHANGE_PHASE {
		@Override
		public void run(MatchRelayThread thread, GameInputStream inOne, GameOutputStream outOne, GameInputStream inTwo, GameOutputStream outTwo) throws Exception {
			int phase = inOne.readInt();

			outTwo.writeUTF("CHANGE_PHASE");
			outTwo.writeInt(phase);

			thread.startingPlayer = !thread.startingPlayer;
		}
	},

	MOVE_CURSOR {
		@Override
		public void run(MatchRelayThread thread, GameInputStream inOne, GameOutputStream outOne, GameInputStream inTwo, GameOutputStream outTwo) throws Exception {
			int locX = inOne.readInt();
			int locY = inOne.readInt();
			double tcamX = inOne.readDouble();
			double tcamY = inOne.readDouble();

			outTwo.writeUTF("MOVE_CURSOR");
			outTwo.writeInt(locX);
			outTwo.writeInt(locY);
			outTwo.writeDouble(tcamX);
			outTwo.writeDouble(tcamY);
		}
	},

	HIGHLIGHT_UNIT {
		@Override
		public void run(MatchRelayThread thread, GameInputStream inOne, GameOutputStream outOne, GameInputStream inTwo, GameOutputStream outTwo) throws Exception {
			String uuid = inOne.readUTF();

			outTwo.writeUTF("HIGHLIGHT_UNIT");
			outTwo.writeUTF(uuid);
		}
	},

	CLEAR_SEL_UNIT {
		@Override
		public void run(MatchRelayThread thread, GameInputStream inOne, GameOutputStream outOne, GameInputStream inTwo, GameOutputStream outTwo) throws Exception {
			outTwo.writeUTF("CLEAR_SEL_UNIT");
		}
	},

	MOVE_UNIT_PATH {
		@Override
		public void run(MatchRelayThread thread, GameInputStream inOne, GameOutputStream outOne, GameInputStream inTwo, GameOutputStream outTwo) throws Exception {
			String uuid = inOne.readUTF();
			int x = inOne.readInt();
			int y = inOne.readInt();

			outTwo.writeUTF("MOVE_UNIT_PATH");
			outTwo.writeUTF(uuid);
			outTwo.writeInt(x);
			outTwo.writeInt(y);
		}
	},

	RESET_UNIT {
		@Override
		public void run(MatchRelayThread thread, GameInputStream inOne, GameOutputStream outOne, GameInputStream inTwo, GameOutputStream outTwo) throws Exception {
			outTwo.writeUTF("RESET_UNIT");
		}
	},

	CLEAR_TILE_HIGHLIGHT {
		@Override
		public void run(MatchRelayThread thread, GameInputStream inOne, GameOutputStream outOne, GameInputStream inTwo, GameOutputStream outTwo) throws Exception {
			outTwo.writeUTF("CLEAR_TILE_HIGHLIGHT");
		}
	},

	HIGHLIGHT_TILES {
		@Override
		public void run(MatchRelayThread thread, GameInputStream inOne, GameOutputStream outOne, GameInputStream inTwo, GameOutputStream outTwo) throws Exception {
			int x = inOne.readInt();
			int y = inOne.readInt();
			int movement = inOne.readInt();
			int range = inOne.readInt();
			String type = inOne.readUTF();

			outTwo.writeUTF("HIGHLIGHT_TILES");
			outTwo.writeInt(x);
			outTwo.writeInt(y);
			outTwo.writeInt(movement);
			outTwo.writeInt(range);
			outTwo.writeUTF(type);
		}
	},

	UNIT_WAIT {
		@Override
		public void run(MatchRelayThread thread, GameInputStream inOne, GameOutputStream outOne, GameInputStream inTwo, GameOutputStream outTwo) throws Exception {
			String uuid = inOne.readUTF();

			outTwo.writeUTF("UNIT_WAIT");
			outTwo.writeUTF(uuid);
		}
	},

	USE_ATTACKING_ABILITY {
		@Override
		public void run(MatchRelayThread thread, GameInputStream inOne, GameOutputStream outOne, GameInputStream inTwo, GameOutputStream outTwo) throws Exception {
			String ability = inOne.readUTF();
			String weapon = inOne.readUTF();
			String uuidOne = inOne.readUTF();
			String uuidTwo = inOne.readUTF();

			outTwo.writeUTF("USE_ATTACKING_ABILITY");
			outTwo.writeUTF(ability);
			outTwo.writeUTF(weapon);
			outTwo.writeUTF(uuidOne);
			outTwo.writeUTF(uuidTwo);
		}
	},

	PICK_UP_ITEM {
		@Override
		public void run(MatchRelayThread thread, GameInputStream inOne, GameOutputStream outOne, GameInputStream inTwo, GameOutputStream outTwo) throws Exception {
			String item = inOne.readUTF();
			String unit = inOne.readUTF();
			outTwo.writeUTF("PICK_UP_ITEM");
			outTwo.writeUTF(item);
			outTwo.writeUTF(unit);
		}
	};

	public abstract void run(MatchRelayThread thread, GameInputStream inOne, GameOutputStream outOne, GameInputStream inTwo, GameOutputStream outTwo) throws Exception;

}