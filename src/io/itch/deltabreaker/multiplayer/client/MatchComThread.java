package io.itch.deltabreaker.multiplayer.client;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.state.StateDungeon;

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
		}

		@Override
		public void send(String[] args, StateDungeon context, GameInputStream in, GameOutputStream out, MatchComThread comThread) throws Exception {
			out.writeUTF("UNIT_WAIT");
			out.writeUTF(args[1]);
		}
	};

	public abstract void recieve(StateDungeon context, GameInputStream in, GameOutputStream out, MatchComThread comThread) throws Exception;

	public abstract void send(String[] args, StateDungeon context, GameInputStream in, GameOutputStream out, MatchComThread comThread) throws Exception;

}