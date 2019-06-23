package it.unipegaso.taranto.parser;

import it.unipegaso.taranto.util.Logger;

import java.io.File;
import java.util.Scanner;

/**
 * Anthill
 *
 * @author Marco Montanari
 * All rights reserved.
 */
public class RouteTester {
	private static final Logger LOGGER = Logger.get("RouteTester");
	
	public static void main(String[] args) {
		System.out.print("File name: /anthill/test/");
		String fileName = new Scanner(System.in).nextLine();
		File file = new File(System.getProperty("user.dir"), "/anthill/test/" + fileName);
		LOGGER.info("Trying to parse '%s'...", file);
		
		try {
			LOGGER.info("Parsed '%s'.", RouteFinder.find(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
