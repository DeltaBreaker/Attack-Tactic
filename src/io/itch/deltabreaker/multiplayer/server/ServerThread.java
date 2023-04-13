package io.itch.deltabreaker.multiplayer.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread implements Runnable {

	private int PORT = 36676;
	private ServerSocket server;
	
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
				System.out.println("[MatchRelayThread]: Client connected");
				new Thread(new QueueThread(connection)).start();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("[ServerThread]: Error connecting to client");
			}
		}
	}

}
