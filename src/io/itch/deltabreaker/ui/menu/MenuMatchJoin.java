package io.itch.deltabreaker.ui.menu;

import java.util.ArrayList;

import io.itch.deltabreaker.core.audio.AudioManager;
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

	public void tick() {
		super.tick();
		update();
		width = getDimensions(options).width;
		openTo = getDimensions(options).height;
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("back")) {
				if (command.equals("")) {
					switch (selected) {

					case 0:
						subMenu.add(new MenuTextInput(Vector3f.add(position, width + 5, 0, 0)) {
							@Override
							public void output(String output) {
								parent.options[0] = output;
							}
						}.setParent(this));
						break;

					case 1:
						subMenu.add(new MenuTextInput(Vector3f.add(position, width + 5, 0, 0)) {
							@Override
							public void output(String output) {
								parent.options[1] = output;
							}
						}.setParent(this));
						break;

					case 2:
						subMenu.add(new MenuTextInput(Vector3f.add(position, width + 5, 0, 0)) {
							@Override
							public void output(String output) {
								parent.options[2] = output;
							}
						}.setParent(this));
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
				AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
			}
		} else {
			subMenu.get(0).action(command, unit);
		}
	}

	@Override
	public void close() {
		super.close();
		if (context.thread != null) {
			try {
				context.thread.in.close();
				context.thread.out.close();
				context.thread.socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			context.thread = null;
		}
	}
}
