package io.itch.deltabreaker.exception;

import javax.swing.JOptionPane;

public class MissingMetaFileException extends Exception {

	private static final long serialVersionUID = 7889367660188299395L;

	public MissingMetaFileException(String message) {
		super(message);
		JOptionPane.showMessageDialog(null, message);
	}

}
