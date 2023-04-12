package io.itch.deltabreaker.exception;

import java.io.IOException;
import java.net.Socket;

import javax.swing.JOptionPane;

public class MatchDesyncException extends Exception {

	private static final long serialVersionUID = -8959375696195792566L;

	public MatchDesyncException(Socket socket) {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		JOptionPane.showMessageDialog(null, "A sync error occurred. You have been disconnected.");
	}
	
}
