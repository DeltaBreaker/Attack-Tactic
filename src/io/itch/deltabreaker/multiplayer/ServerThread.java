package io.itch.deltabreaker.multiplayer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerThread implements Runnable {

	private int PORT = 36676;

	private ServerSocket server;

	private ArrayList<MatchRelayThread> matches = new ArrayList<>();

	public ServerThread() {
		try {
			server = new ServerSocket(PORT);
			System.out.println("[ServerThread]: Server started and awaiting clients");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("[ServerThread]: Error starting server");
		}
	}

	@Override
	public void run() {
		while (!server.isClosed()) {
			try {
				Socket connection = server.accept();
				if (matches.size() > 0) {
					matches.get(0).connect(connection);
					new Thread(matches.get(0)).start();
					matches.remove(0);
					System.out.println("[ServerThread]: Client connected and placed in active session");
				} else {
					matches.add(new MatchRelayThread(connection));
					System.out.println("[ServerThread]: Client connected and created a new session");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
