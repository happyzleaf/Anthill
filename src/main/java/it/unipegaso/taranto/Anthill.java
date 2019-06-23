package it.unipegaso.taranto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.unipegaso.taranto.database.DatabaseService;
import it.unipegaso.taranto.email.EmailService;
import it.unipegaso.taranto.gdrive.GoogleDriveService;
import it.unipegaso.taranto.parser.route.EnumRoute;
import it.unipegaso.taranto.util.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Anthill
 *
 * @author Marco Montanari
 * All rights reserved.
 */
public class Anthill {
	public static final Logger LOGGER = Logger.get("Anthill");
	public static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	public static File anthillDir = new File(System.getProperty("user.dir"), "anthill");
	public static Config config;
	
	public static void main(String[] args) {
		//Config
		try {
			File configFile = new File(anthillDir, "config.json");
			LOGGER.info("Trying to load the config (%s).", configFile.getName());
			if (!configFile.exists()) {
				FileUtils.writeStringToFile(configFile, gson.toJson(new Config()), StandardCharsets.UTF_8);
				LOGGER.info("The config was not found, a new one has been generated, please compile it and reload the program.");
				System.exit(0);
			}
			config = gson.fromJson(new FileReader(configFile), Config.class);
			LOGGER.info("Config loaded.");
		} catch (IOException e) {
			LOGGER.error("There was a problem while loading. The program cannot continue.", e);
			System.exit(1);
		}
		
		LOGGER.info("Would you like to download the emails or to upload the files? [download/upload/exit]");
		Scanner keyboard = new Scanner(System.in);
		do {
			System.out.print("> ");
			switch (keyboard.nextLine().toLowerCase()) {
				case "download":
					download();
					LOGGER.info("Emails downloaded.");
					break;
				case "upload":
					upload();
					LOGGER.info("Files uploaded.");
					break;
				case "exit":
					return;
				default:
					LOGGER.info("Command not found. Please type 'download', 'upload' or 'exit'.");
			}
		} while (true);
	}
	
	public static void download() {
		try {
			//Email
			EmailService email;
			LOGGER.info("Establishing connection to the host...");
			email = new EmailService(config.host, config.user, config.password);
			email.connect();
			LOGGER.info("Connection established.");
			
			//Parsing
			LOGGER.info("Starting to parse the emails...");
			DatabaseService database = new DatabaseService(new File(anthillDir, "database"));
			int parsed = email.parseMessages(database);
			LOGGER.info("Done! Parsed %d files.", parsed);
			
			if (config.clearCache) {
				LOGGER.info("Clearing cache...");
				database.clearCache();
			}
			
			LOGGER.info("Disconnecting...");
			email.disconnect();
			EnumRoute.unloadAll();
			LOGGER.info("Disconnected.");
		} catch (Exception e) {
			LOGGER.error("There was a problem while loading. The program cannot continue.", e);
			System.exit(1);
		}
	}
	
	public static void upload() {
		try {
			LOGGER.info("Connecting to google drive...");
			GoogleDriveService gdrive = new GoogleDriveService(new File(anthillDir, "gdrive"));
			LOGGER.info("Requesting credentials...");
			gdrive.connect();
			LOGGER.info("Connected!");
			gdrive.upload(FileUtils.listFilesAndDirs(new File(anthillDir, "database"), new NotFileFilter(TrueFileFilter.INSTANCE), DirectoryFileFilter.DIRECTORY));
		} catch (Exception e) {
			LOGGER.error("There was a problem, the program cannot continue.", e);
		}
	}
}
