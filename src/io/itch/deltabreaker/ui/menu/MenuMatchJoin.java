package io.itch.deltabreaker.ui.menu;

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import io.itch.deltabreaker.core.PerformanceManager;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.multiplayer.client.MatchPreviewThread;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.state.StateMatchLobby;

public class MenuMatchJoin extends Menu {

	public StateMatchLobby context;
	public boolean lock = false;
	public ArrayList<String[]> attempts = new ArrayList<>();

	public MenuMatchJoin(StateMatchLobby context, Vector3f position) {
		super(position, new String[] { "Name", "Room ID", "Password", "Join" });
		this.context = context;
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("back")) {
				if (command.equals("")) {
					if (command.equals("")) {
						switch (selected) {

						case 0:
							PerformanceManager.checkForCrash = false;
							String input = JOptionPane.showInputDialog(null);
							options[0] = (input != null) ? input : "Name";
							PerformanceManager.checkForCrash = true;
							break;

						case 1:
							PerformanceManager.checkForCrash = false;
							String roomID = JOptionPane.showInputDialog(null).toLowerCase();
							options[1] = (roomID != null) ? roomID : "Room ID";
							PerformanceManager.checkForCrash = true;
							break;

						case 2:
							PerformanceManager.checkForCrash = false;
							String password = JOptionPane.showInputDialog(null).toLowerCase();
							options[2] = (password != null) ? password : "Password";
							PerformanceManager.checkForCrash = true;
							break;

						case 3:
							if (!lock) {
								lock = true;
								attempts.add(new String[] { (options[1].equals("Room ID")) ? "" : options[1], (options[2].equals("Password")) ? "" : options[2] });
							}
							if (context.thread == null) {
								context.thread = new MatchPreviewThread(context, "localhost", 36676, options[0], this);
								new Thread(context.thread).start();
							}
							break;

						}
					}
				} else {
					close();
				}
			} else {
				subMenu.get(0).action(command, unit);
			}
		}
	}
	
	@Override
	public void close() {
		super.close();
		if (context.thread != null) {
			context.thread = null;
			try {
				context.thread.in.close();
				context.thread.out.close();
				context.thread.socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
