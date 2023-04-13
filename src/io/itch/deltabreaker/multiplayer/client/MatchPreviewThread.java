package io.itch.deltabreaker.multiplayer.client;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JOptionPane;

import io.itch.deltabreaker.ai.AIType;
import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.core.ResourceManager;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.multiplayer.GameInputStream;
import io.itch.deltabreaker.multiplayer.GameOutputStream;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.ItemProperty;
import io.itch.deltabreaker.state.StateMatchLobby;
import io.itch.deltabreaker.ui.menu.MenuMatchJoin;

public class MatchPreviewThread implements Runnable {

	private StateMatchLobby state;
	public Socket socket;
	public GameInputStream in;
	public GameOutputStream out;

	public String name, map, password, address, floor, roomID;
	public int units, port;
	public boolean create;

	public MenuMatchJoin menu;

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
				out.writeUTF(password);
				out.writeUTF("" + ResourceManager.currentHash);
				out.writeUTF(name);
				out.writeInt(units);
				out.writeUTF(floor);
				out.writeUTF(map);
			} else {
				boolean approved = false;
				while (!approved) {
					if (menu.attempts.size() > 0) {
						System.out.println("Attempting");
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
			}

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

			ArrayList<float[]> profiles = new ArrayList<>();
			for (float[] profile : Unit.GROWTH_PROFILES.values()) {
				profiles.add(profile);
			}
			for (int i = 0; i < units; i++) {
				Unit u = Unit.randomCombatUnit(-1, -1, new Vector4f(1, 1, 1, 1), 5, 0, profiles.get(new Random().nextInt(profiles.size())), AIType.get("standard_dungeon.json"));
				u.addItem(ItemProperty.get("item.sword.gold"));
				Inventory.active.add(u);
				Inventory.units.add(u);
			}

			for (Unit u : Inventory.active) {
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
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("[MatchPreviewThread]: There was an error starting this match. Cancelling.");
			JOptionPane.showMessageDialog(null, "There was an error starting this match. Cancelling.");
		}
	}

}
