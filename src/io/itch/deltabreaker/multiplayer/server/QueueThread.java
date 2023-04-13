package io.itch.deltabreaker.multiplayer.server;

import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.itch.deltabreaker.multiplayer.GameInputStream;
import io.itch.deltabreaker.multiplayer.GameOutputStream;

public class QueueThread implements Runnable {

	private static ConcurrentHashMap<String, MatchRelayThread> matches = new ConcurrentHashMap<>();

	private Socket socket;
	private GameInputStream in;
	private GameOutputStream out;

	public QueueThread(Socket socket) {
		this.socket = socket;
	}

	public static void addMatch(MatchRelayThread match) {
		matches.put(match.roomID, match);
	}

	public static void removeMatch(MatchRelayThread match) {
		matches.remove(match.roomID);
	}

	@Override
	public void run() {
		try {
			in = new GameInputStream(socket.getInputStream());
			out = new GameOutputStream(socket.getOutputStream());
			
			boolean create = in.readBoolean();

			if (create) {
				String roomID = UUID.randomUUID().toString().split("-")[0];
				while (matches.contains(roomID)) {
					roomID = UUID.randomUUID().toString().split("-")[0];
				}
				System.out.println("[MatchRelayThread]: Client creating new room with ID " + roomID);
				new MatchRelayThread(roomID, socket, in, out);
			} else {
				System.out.println("[MatchRelayThread]: Client attempting to join");
				boolean connected = false;

				whileLoop:
				while (!connected) {
					String roomID = in.readUTF();
					String password = in.readUTF();
					String hash = in.readUTF();

					try {
						if (!matches.containsKey(roomID)) {
							if (roomID.length() > 0) {
								out.writeBoolean(false);
								out.writeUTF("That room does not exist.");
								continue;
							} else {
								for(MatchRelayThread m : matches.values()) {
									if(m.password.length() == 0 && m.hash.equals(hash)) {
										out.writeBoolean(true);
										matches.get(m.roomID).connect(socket, in, out);
										connected = true;
										break whileLoop;
									}
								}
								out.writeBoolean(false);
								out.writeUTF("There are no rooms available at the moment.");
							}
						}

						if (!matches.get(roomID).password.equals(password)) {
							out.writeBoolean(false);
							out.writeUTF("The password was incorrect.");
							continue;
						}

						if (!matches.get(roomID).hash.equals(hash)) {
							out.writeBoolean(false);
							out.writeUTF("Your game data does not match the host.");
							continue;
						}

						out.writeBoolean(true);
						matches.get(roomID).connect(socket, in, out);
						connected = true;
					} catch (Exception e) {
						e.printStackTrace();
						out.writeBoolean(false);
						out.writeUTF("There was an error connecting to match.");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				in.close();
				out.close();
				socket.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

}
