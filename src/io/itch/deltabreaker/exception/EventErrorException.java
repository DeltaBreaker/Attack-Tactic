package io.itch.deltabreaker.exception;

import javax.swing.JOptionPane;

public class EventErrorException extends Exception {

	private static final long serialVersionUID = -6501875107743155852L;

	public EventErrorException(int line, String command) {
		super("Error processing event at line " + line + ": " + command);
		JOptionPane.showMessageDialog(null, "Error processing event at line " + line + ": " + command);
	}
	
}
