package it.unipegaso.taranto.parser.route;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Anthill
 *
 * @author Marco Montanari
 * All rights reserved.
 */
public class RouteImage implements IRoute<File> {
	private ITesseract tess;
	private Map<String, String> substitutes;
	
	RouteImage() {
		tess = new Tesseract();
		tess.setLanguage("ita");
		substitutes = new HashMap<>();
		substitutes.put("F'", "P");
	}
	
	@Override
	public String parse(File file) throws Exception {
		String c = tess.doOCR(file);
		for (Map.Entry<String, String> entry : substitutes.entrySet()) {
			c = c.replace(entry.getKey(), entry.getValue());
		}
		
		StringBuilder lines = new StringBuilder();
		for (String line : c.split("\n+")) {
			StringBuilder phrases = new StringBuilder();
			for (String phrase : line.split("\\s+")) {
				if (phrase.toUpperCase().equals(phrase) || (phrase.length() > 1 && phrase.substring(0, 1).equals("|"))) {
					phrase = phrase.replace("|", "I");
				} else {
					phrase = phrase.replace("|", "l");
				}
				phrase = phrase.replace("0", "O");
				phrase = phrase.replace("5", "S");
				phrase = phrase.replace("/", "l");
				phrases.append(phrase).append(" ");
			}
			lines.append(phrases.substring(0, phrases.length() - 1)).append("\n");
		}
		lines.substring(0, lines.length() - 1);
		return EnumRoute.STRING.parse(lines.toString());
	}
}
