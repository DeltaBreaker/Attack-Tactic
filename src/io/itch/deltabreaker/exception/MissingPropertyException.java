package io.itch.deltabreaker.exception;

import javax.swing.JOptionPane;

public class MissingPropertyException extends Exception {

	private static final long serialVersionUID = 8498739426247785626L;

	public MissingPropertyException(String message) {
		super(message);
		JOptionPane.showMessageDialog(null, message);
	}
	
}
