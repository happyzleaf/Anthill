package it.unipegaso.taranto.parser.route;

import java.io.File;

/**
 * Anthill
 *
 * @author Marco Montanari
 * All rights reserved.
 */
public class RouteResult {
	public final String name;
	public final File file;
	
	private RouteResult(String name, File file) {
		this.name = name;
		this.file = file;
	}
	
	public RouteResult of(String name, File file) {
		return new RouteResult(name, file);
	}
	
	public RouteResult of(String name) {
		return new RouteResult(name, null);
	}
}
