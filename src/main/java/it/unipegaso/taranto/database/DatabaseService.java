package it.unipegaso.taranto.database;

import it.unipegaso.taranto.util.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Anthill
 *
 * @author Marco Montanari
 * All rights reserved.
 */
public class DatabaseService {
	private static final Logger LOGGER = Logger.get("Database");
	
	public final File path;
	public final File temp;
	public List<String> names;
	
	public DatabaseService(File path) {
		this.path = path;
		temp = new File(path, "temp");
		temp.mkdirs();
		names = FileUtils.listFilesAndDirs(path, new NotFileFilter(TrueFileFilter.INSTANCE), DirectoryFileFilter.DIRECTORY).stream().map(File::getName).collect(Collectors.toList());
		names.remove("undefined");
	}
	
	public void load(String name, File file) {
		try {
			File namePath;
			if (name == null) {
				namePath = new File(path, "undefined");
			} else {
				//TODO test
				int firstSpace = name.indexOf(" ") + 1;
				File reversedNamePath = new File(path, name.substring(firstSpace) + name.substring(0, firstSpace));
				namePath = reversedNamePath.exists() ? reversedNamePath : new File(path, name);
			}
			namePath.mkdirs();
			File messageFile = new File(namePath, file.getName());
			if (messageFile.exists()) {
				messageFile = new File(namePath, System.currentTimeMillis() + file.getName());
			}
			FileUtils.moveFile(file, messageFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void clearCache() {
		try {
			FileUtils.deleteDirectory(temp);
		} catch (IOException e) {
			LOGGER.error("Cannot clear the cache.", e);
		}
	}
}
