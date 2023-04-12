package io.itch.deltabreaker.multiplayer;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;

import io.itch.deltabreaker.builder.dungeon.DungeonGenerator;
import io.itch.deltabreaker.multiplayer.client.GameInputStream;
import io.itch.deltabreaker.multiplayer.client.GameOutputStream;
import io.itch.deltabreaker.object.Unit;

public class MatchRelayThread implements Runnable {

	private Socket clientOne, clientTwo;
	private GameInputStream inOne, inTwo;
	private GameOutputStream outOne, outTwo;
	private Random r;
	public boolean startingPlayer;
	private int units = 5;
	private int floor = 9;
	
	public MatchRelayThread(Socket clientOne) {
		this.clientOne = clientOne;
		try {
			inOne = new GameInputStream(clientOne.getInputStream());
			outOne = new GameOutputStream(clientOne.getOutputStream());
			System.out.println("[MatchRelayThread]: Client one connected");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void connect(Socket clientTwo) {
		this.clientTwo = clientTwo;
		try {
			inTwo = new GameInputStream(clientTwo.getInputStream());
			outTwo = new GameOutputStream(clientTwo.getOutputStream());
			System.out.println("[MatchRelayThread]: Client two connected");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		r = new Random();
		startingPlayer = r.nextBoolean();
		long seed = r.nextLong();
		String[] maps = DungeonGenerator.getPalletTags();
		String map = maps[r.nextInt(maps.length)];

		try {
			outOne.writeInt(units);
			outTwo.writeInt(units);
			
			Unit[] unitsOne = new Unit[units];
			for(int i = 0; i < units; i++) {
				unitsOne[i] = inOne.readUnit();
			}
			
			Unit[] unitsTwo = new Unit[units];
			for(int i = 0; i < units; i++) {
				unitsTwo[i] = inTwo.readUnit();
			}
			
			for(int i = 0; i < units; i++) {
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
			
			while (!clientOne.isClosed() && !clientTwo.isClosed()) {
				if(startingPlayer) {
					ServerEvent.valueOf(inOne.readUTF()).run(this, inOne, outOne, inTwo, outTwo);
				} else {
					ServerEvent.valueOf(inTwo.readUTF()).run(this, inTwo, outTwo, inOne, outOne);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("[MatchRelayThread]: Match error occurred. Closing.");
			try {
				clientOne.close();
				clientTwo.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
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
	};

	public abstract void run(MatchRelayThread thread, GameInputStream inOne, GameOutputStream outOne, GameInputStream inTwo, GameOutputStream outTwo) throws Exception;

}