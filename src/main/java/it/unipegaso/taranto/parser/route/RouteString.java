package it.unipegaso.taranto.parser.route;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import org.apache.commons.text.WordUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Anthill
 *
 * @author Marco Montanari
 * All rights reserved.
 */
public class RouteString implements IRoute<String> {
	protected List<String> blacklist = Arrays.asList("direttore generale", "dati", "iban", "corso", "laurea", "class", "form", "di", "anno");
	private List<String> keywords = Arrays.asList("causale", "descrizione", "comunicazioni al beneficiario", "intestatario", "conto ordinante", "debitore");
	private CRFClassifier<CoreLabel> classifier;
	
	RouteString() {
		try {
			classifier = CRFClassifier.getClassifier("classifiers/ner-ita-nogpe-noiob_gaz_wikipedia_sloppy.ser.gz");
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public String parse(String string) throws Exception {
		int keyLine = -1;
		String keyFound = null;
		String[] loweredLines = string.toLowerCase().split("\n");
		for (String key : keywords) {
			for (int i = 0; i < loweredLines.length; i++) {
				if (loweredLines[i].contains(key)) {
					keyLine = i;
					keyFound = key;
					break;
				}
			}
			if (keyLine != -1) break;
		}
		if (keyLine == -1) return null;
		String[] lines = format(string).split("\n");
		for (int i = keyLine; i < lines.length; i++) {
			String line = classifier.classifyWithInlineXML(lines[i].replaceAll("(?i)" + keyFound, ""));
			int start = line.indexOf("<PER>") + 5;
			int end = line.indexOf("</PER>");
			if (end > start) {
				return WordUtils.capitalizeFully(line.substring(start, end));
			}
		}
		return null;
	}
	
	private String format(String string) {
		StringBuilder lines = new StringBuilder();
		for (String line : string.split("\n")) {
			StringBuilder phrase = new StringBuilder();
			for (String word : line.split("\\s+")) {
				if (word.isEmpty()) continue;
				char[] chars = word.toCharArray();
				for (int i = 1; i < chars.length; i++) {
					if (Character.isUpperCase(chars[i])) {
						word = WordUtils.capitalizeFully(word);
						break;
					}
				}
				
				phrase.append(word).append(" ");
			}
			if (line.trim().isEmpty()) {
				lines.append("\n");
			} else {
				for (String b : blacklist) {
					int i = phrase.toString().toLowerCase().indexOf(b + " ");
					i = i == -1 ? phrase.toString().toLowerCase().indexOf(" " + b) : i;
					if (i != -1) {
						phrase.delete(i, i + b.length());
					}
				}
				lines.append(phrase.substring(0, phrase.length() - 1)).append("\n");
			}
		}
		lines.substring(0, lines.length() - 1);
		return lines.toString();
	}
}
