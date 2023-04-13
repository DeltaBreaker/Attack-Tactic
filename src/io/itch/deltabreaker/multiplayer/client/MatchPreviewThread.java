package io.itch.deltabreaker.multiplayer.client;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JOptionPane;

import io.itch.deltabreaker.ai.AIType;
import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.ItemProperty;
import io.itch.deltabreaker.state.StateMatchLobby;

public class MatchPreviewThread implements Runnable {

	private StateMatchLobby state;
	public Socket socket;
	public GameInputStream in;
	public GameOutputStream out;

	public MatchPreviewThread(StateMatchLobby state, String address, int port) {
		this.state = state;
		try {
			socket = new Socket(address, port);
			in = new GameInputStream(socket.getInputStream());
			out = new GameOutputStream(socket.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("[MatchPreviewThread]: There was an error connecting to the server. Cancelling.");
			JOptionPane.showMessageDialog(null, "There was an error connecting to the server. Cancelling.");
		}
	}

	@Override
	public void run() {
		try {
			int units = in.readInt();
			
			ArrayList<float[]> profiles = new ArrayList<>();
			for(float[] profile : Unit.GROWTH_PROFILES.values()) {
				profiles.add(profile);
			}
			for(int i = 0; i < units; i++) {
				Unit u = Unit.randomCombatUnit(-1, -1, new Vector4f(1, 1, 1, 1), 5, 0, profiles.get(new Random().nextInt(profiles.size())), AIType.get("standard_dungeon.json"));
				u.addItem(ItemProperty.get("item.sword.gold"));
				Inventory.active.add(u);
				Inventory.units.add(u);
			}
			
			for(Unit u : Inventory.active) {
				out.writeUnit(u);
			}
			
			Unit[] enemies = new Unit[units];
			for(int i = 0; i < units; i++) {
				enemies[i] = in.readUnit();
			}
						
			boolean startingPlayer = in.readBoolean();
			String map = in.readUTF();
			long seed = in.readLong();
			int floor = in.readInt();
						
			state.readyUp(map, floor, seed, startingPlayer, enemies);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("[MatchPreviewThread]: There was an error starting this match. Cancelling.");
			JOptionPane.showMessageDialog(null, "There was an error starting this match. Cancelling.");
		}
	}

}
