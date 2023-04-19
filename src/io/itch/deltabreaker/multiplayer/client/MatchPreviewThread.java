package io.itch.deltabreaker.multiplayer.client;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JOptionPane;

import io.itch.deltabreaker.builder.dungeon.DungeonGenerator;
import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.core.ResourceManager;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.multiplayer.GameInputStream;
import io.itch.deltabreaker.multiplayer.GameOutputStream;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.ItemProperty;
import io.itch.deltabreaker.state.StateManager;
import io.itch.deltabreaker.state.StateMatchLobby;
import io.itch.deltabreaker.ui.RoomInfo;
import io.itch.deltabreaker.ui.menu.Menu;
import io.itch.deltabreaker.ui.menu.MenuMatch;
import io.itch.deltabreaker.ui.menu.MenuMatchJoin;
import io.itch.deltabreaker.ui.menu.MenuUnitSelect;

public class MatchPreviewThread implements Runnable {

	private StateMatchLobby state;
	public Socket socket;
	public GameInputStream in;
	public GameOutputStream out;

	public String name = "", opponentName = "", map, password, address, floor, roomID;
	public int units, port;
	public boolean create;

	public MenuMatchJoin menu;

	public boolean matchReady = false;
	public boolean ready = false;
	public boolean opponentReady = false;
	public boolean cancelled = false;
	public boolean waiting = false;
	public boolean finishedSelecting = false;

	public MatchPreviewThread(StateMatchLobby state, String address, int port, String name, String map, String floor, int units, String password) {
		this.state = state;
		this.address = address;
		this.port = port;
		this.name = name;
		this.map = map;
		this.floor = floor;
		this.units = units;
		this.password = password;
		create = true;
	}

	public MatchPreviewThread(StateMatchLobby state, String address, int port, String name, MenuMatchJoin menu) {
		this.state = state;
		this.address = address;
		this.port = port;
		this.name = name;
		this.menu = menu;
		create = false;
	}

	@Override
	public void run() {
		try {
			socket = new Socket(address, port);
			in = new GameInputStream(socket.getInputStream());
			out = new GameOutputStream(socket.getOutputStream());

			out.writeBoolean(create);

			if (create) {
				create();
			} else {
				join();
			}

			readyUp();

			setUpMatch();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("[MatchPreviewThread]: There was an error starting this match. Cancelling.");
			JOptionPane.showMessageDialog(null, "There was an error starting this match. Cancelling.");
		}
	}

	public void create() throws IOException {
		out.writeUTF(password);
		out.writeUTF("" + ResourceManager.currentHash);
		out.writeUTF(name);
		out.writeInt(units);
		out.writeUTF(floor);
		out.writeUTF(map);

		state.details = new RoomInfo(new Vector3f(0, 0, -80), new String[] { name, "room id - " + in.readUTF(), (password.length() > 0) ? "password - " + password : "password - none", "units - " + units,
				"floor - " + ((floor.equals("random")) ? floor : (Integer.parseInt(floor) + 1)), (map.equals("Random") || map.equals("Vote")) ? map : "map - " + DungeonGenerator.getPatternNameFromFile(map) });
		StateManager.currentState.hideCursor = false;
		state.menus.add(new Menu(new Vector3f(0, 20, -80), new String[] { "Ready" }) {
			@Override
			public void action(String command, Unit unit) {
				if (subMenu.size() == 0) {
					if (!command.equals("back")) {
						if (command.equals("")) {
							close();
							StateManager.currentState.hideCursor = true;
							ready = true;
							AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
						}
					} else {
						close();
						cancelled = true;
						AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
					}
				} else {
					subMenu.get(0).action(command, unit);
				}
			}
		}.setCameraMove(false));
	}

	public void join() throws IOException {
		boolean approved = false;
		while (!approved) {
			if (menu.attempts.size() > 0) {
				String[] attempt = menu.attempts.get(0);
				menu.attempts.remove(0);

				out.writeUTF(attempt[0]);
				out.writeUTF(attempt[1]);
				out.writeUTF(ResourceManager.currentHash);

				approved = in.readBoolean();
				if (!approved) {
					System.out.println(in.readUTF());
				}

				menu.lock = false;
			} else {
				try {
					Thread.sleep(250L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		menu.closeAll();
		out.writeUTF(name);
		roomID = in.readUTF();
		password = in.readUTF();
		units = in.readInt();
		floor = in.readUTF();
		map = in.readUTF();

		state.details = new RoomInfo(new Vector3f(0, 0, -80), new String[] { name, "room id - " + roomID, (password.length() > 0) ? "password - " + password : "password - none", "units - " + units,
				"floor - " + ((floor.equals("random")) ? floor : (Integer.parseInt(floor) + 1)), (map.equals("Random") || map.equals("Vote")) ? map : "map - " + DungeonGenerator.getPatternNameFromFile(map) });
		StateManager.currentState.hideCursor = false;
		state.menus.add(new Menu(new Vector3f(0, 20, -80), new String[] { "Ready" }) {
			@Override
			public void action(String command, Unit unit) {
				if (subMenu.size() == 0) {
					if (!command.equals("back")) {
						if (command.equals("")) {
							close();
							StateManager.currentState.hideCursor = true;
							ready = true;
							AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
						}
					} else {
						close();
						cancelled = true;
						AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
					}
				} else {
					subMenu.get(0).action(command, unit);
				}
			}
		}.setCameraMove(false));
	}

	public void readyUp() throws IOException {
		waiting = true;
		boolean ready = this.ready;
		while ((!opponentReady || !ready) && !cancelled) {
			ready = this.ready;
			out.writeBoolean(ready);
			opponentName = in.readUTF();
			opponentReady = in.readBoolean();
		}
		if (cancelled) {
			in.close();
			out.close();
			socket.close();
			state.details = null;
			state.menus.add(new MenuMatch(state, new Vector3f(0, 0, -80)));
		}
		waiting = false;
	}

	public void setUpMatch() throws IOException {
		if (in.readBoolean()) {
			// This is where a menu needs to be displayed to get user input
			if (new Random().nextBoolean()) {
				out.writeUTF("scorched_crater.json");
			} else {
				out.writeUTF("seabed_cove.json");
			}
		}

		String opponent = in.readUTF();

		int units = in.readInt();
		state.details = null;
		StateManager.currentState.hideCursor = false;
		MenuUnitSelect menu = new MenuUnitSelect(new Vector3f(0, 0, -80), units, Inventory.units, true, false) {
			@Override
			public void returnedUnits(ArrayList<Unit> units) {
				Inventory.active = units;
				finishedSelecting = true;

				synchronized (opponent) {
					opponent.notifyAll();
				}
			}
		};
		state.menus.add(menu);

		synchronized (opponent) {
			try {
				opponent.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		for (Unit u : Inventory.active) {
			// Remove items from units inventories and place them into
			ArrayList<ItemProperty> items = u.getItemList();
			for (int i = 0; i < items.size(); i++) {
				Inventory.addItem(items.get(i));
				u.removeItem(items.get(i));
				i--;
			}
			out.writeUnit(u);
		}

		Unit[] enemies = new Unit[units];
		for (int i = 0; i < units; i++) {
			enemies[i] = in.readUnit();
		}

		boolean startingPlayer = in.readBoolean();
		String map = in.readUTF();
		long seed = in.readLong();
		int floor = in.readInt();

		state.readyUp(map, floor, seed, startingPlayer, enemies, name, opponent);
	}

}
