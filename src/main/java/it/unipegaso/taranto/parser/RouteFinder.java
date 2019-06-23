package it.unipegaso.taranto.parser;

import it.unipegaso.taranto.database.DatabaseService;
import it.unipegaso.taranto.parser.route.EnumRoute;
import it.unipegaso.taranto.util.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Anthill
 *
 * @author Marco Montanari
 * All rights reserved.
 */
public class RouteFinder {
	private static Logger LOGGER = Logger.get("RouteFinder");
	
	public static String find(Object obj) throws Exception {
		String string;
		
		if (obj instanceof File) {
			File file = (File) obj;
			if (isImage(file)) return EnumRoute.IMAGE.parse(obj);
			if (isPDF(file)) return EnumRoute.PDF.parse(obj);
			string = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
		} else {
			string = obj.toString();
		}
		
		if (isHTML(string)) return EnumRoute.HTML.parse(string);
		return EnumRoute.STRING.parse(string);
	}
	
	public static boolean findAndUpload(DatabaseService db, Object obj, File file) {
		try {
			LOGGER.info("Trying to parse '%s'...", file.toString());
			/*RouteResult r = find(obj);
			if (r.file != null) {
				file = r.file;
			}*/
			String name = find(obj);
			LOGGER.info("Found '%s'.", name);
			LOGGER.info("Moving to the db...");
			db.load(name, file);
			return !name.equals("undefined");
		} catch (Exception e) {
			return false;
		}
	}
	
	//Credits to dbennett455 (https://github.com/dbennett455/DetectHtml)
	private static final Pattern htmlPattern = Pattern.compile("(\\<\\w+((\\s+\\w+(\\s*\\=\\s*(?:\".*?\"|'.*?'|[^'\"\\>\\s]+))?)+\\s*|\\s*)\\>.*\\</\\w+\\>)|(\\<\\w+((\\s+\\w+(\\s*\\=\\s*(?:\".*?\"|'.*?'|[^'\"\\>\\s]+))?)+\\s*|\\s*)/\\>)|(&[a-zA-Z][a-zA-Z0-9]+;)", Pattern.DOTALL);
	
	private static boolean isHTML(String string) {
		return htmlPattern.matcher(string).find();
	}
	
	private static boolean isImage(File file) {
		return new MimetypesFileTypeMap().getContentType(file).split("/")[0].equals("image");
	}
	
	//Taken somewhere, can't remember.
	private static boolean isPDF(File file) {
		if (FilenameUtils.isExtension(file.getName(), "pdf")) return true;
		try {
			byte[] data = FileUtils.readFileToByteArray(file);
			if (data != null && data.length > 4 &&
					data[0] == 0x25 && // %
					data[1] == 0x50 && // P
					data[2] == 0x44 && // D
					data[3] == 0x46 && // F
					data[4] == 0x2D) { // -
				
				if (data[5] == 0x31 && data[6] == 0x2E && data[7] == 0x33 &&
						data[data.length - 7] == 0x25 && // %
						data[data.length - 6] == 0x25 && // %
						data[data.length - 5] == 0x45 && // E
						data[data.length - 4] == 0x4F && // O
						data[data.length - 3] == 0x46 && // F
						data[data.length - 2] == 0x20 && // SPACE
						data[data.length - 1] == 0x0A) { // EOL
					return true;
				}
				
				// EOL
				return data[5] == 0x31 && data[6] == 0x2E && data[7] == 0x34 &&
						data[data.length - 6] == 0x25 && // %
						data[data.length - 5] == 0x25 && // %
						data[data.length - 4] == 0x45 && // E
						data[data.length - 3] == 0x4F && // O
						data[data.length - 2] == 0x46 && // F
						data[data.length - 1] == 0x0A;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}
}
