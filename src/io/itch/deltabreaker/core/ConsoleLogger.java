package io.itch.deltabreaker.core;

import java.io.PrintStream;

public class ConsoleLogger extends PrintStream {

	private PrintStream console;
	
	public ConsoleLogger(PrintStream out, PrintStream console) {
		super(out, true);
		this.console = console;
	}
	
	@Override
	public void println(String s) {
		super.println(s);
		console.println(s);
	}
	
}
