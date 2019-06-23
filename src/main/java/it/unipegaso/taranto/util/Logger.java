package it.unipegaso.taranto.util;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Anthill
 *
 * @author Marco Montanari
 * All rights reserved.
 *
 * PS: I don't quite remember why I implemented my own Logger instead of using Apache's log4j but it was probably giving me problems and I was running out of time, I guess.
 */
public class Logger {
	private static Map<String, Logger> loggers = new HashMap<>();
	private String prefix;
	private PrintStream info, error;
	
	private Logger(String name, PrintStream info, PrintStream error) {
		this.prefix = "[" + name + "] ";
		this.info = info;
		this.error = error;
	}
	
	public static Logger get(String name) {
		return loggers.computeIfAbsent(name, n -> new Logger(n, System.out, System.err));
	}
	
	public void info(String message, Object... args) {
		info.println(prefix + String.format(message, args));
	}
	
	public void error(String message, Exception e) {
		error.println(prefix + message);
		if (e != null) {
			e.printStackTrace();
		}
	}
}
