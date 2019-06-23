package it.unipegaso.taranto.gdrive;

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import it.unipegaso.taranto.Anthill;
import it.unipegaso.taranto.util.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * Anthill
 *
 * @author Marco Montanari
 * All rights reserved.
 */
public class GoogleDriveService {
	private static final Logger LOGGER = Logger.get("GDrive");
	
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final Set<String> SCOPES = DriveScopes.all();
	
	private java.io.File dir;
	private GoogleClientSecrets clientSecrets;
	private Drive service;
	
	public GoogleDriveService(java.io.File gdriveDir) {
		try {
			this.dir = gdriveDir;
			clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new FileReader(new java.io.File(dir, "client_id.json")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void connect() throws GeneralSecurityException, IOException {
		NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				httpTransport,
				JSON_FACTORY,
				clientSecrets,
				SCOPES)
				.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(dir, "credentials")))
				.setAccessType("offline")
				.build();
		service = new Drive.Builder(httpTransport,
				JSON_FACTORY,
				new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user"))
				.setApplicationName("AnthillClient")
				.build();
	}
	
	public void upload(Collection<java.io.File> directories) throws IOException {
		//Create folders if they don't exist.
		Map<String, Collection<java.io.File>> uploadMap = new HashMap<>();
		FileList queries = service.files().list()
				.setQ("'" + Anthill.config.gdriveFolder + "' in parents and trashed = false")
				.setSpaces("drive")
				.setFields("nextPageToken, files(id, name, parents)")
				.execute();
		for (java.io.File nameDir : directories) {
			String id = null;
			for (File folderName : queries.getFiles()) {
				if (folderName.getName().equalsIgnoreCase(nameDir.getName())) {
					id = folderName.getId();
					break;
				}
			}
			if (id == null) {
				LOGGER.info("Creating dir '%s'...", nameDir.getName());
				File fileMetadata = new File();
				fileMetadata.setParents(Collections.singletonList(Anthill.config.gdriveFolder));
				fileMetadata.setName(nameDir.getName());
				fileMetadata.setMimeType("application/vnd.google-apps.folder");
				
				File file = service.files()
						.create(fileMetadata)
						.setFields("id")
						.execute();
				id = file.getId();
			}
			LOGGER.info("Dir created with the id '%s'.", id);
			uploadMap.put(id, FileUtils.listFiles(nameDir, TrueFileFilter.INSTANCE, null));
		}
		
		for (Map.Entry<String, Collection<java.io.File>> entry : uploadMap.entrySet()) {
			for (java.io.File file : entry.getValue()) {
				LOGGER.info("Uploading '%s' in '/%s/'...", file.getName(), entry.getKey());
				File fileMetadata = new File();
				fileMetadata.setName(file.getName());
				fileMetadata.setParents(Collections.singletonList(entry.getKey()));
				FileContent mediaContent = new FileContent(null, file);
				service.files().create(fileMetadata, mediaContent)
						.setFields("id, parents")
						.execute();
			}
		}
		
		/*List<String> existingIDs = service.files().list().setFields().setFields("nextPageToken, files(id, name)").execute();
		
		Map<String, List<java.io.File>> folderIDs = new HashMap<>();
		for (java.io.File nameDir : directories) {
		
		}
		FileUtils.listFiles(, TrueFileFilter.INSTANCE, null);*/
	}
	
	public void test() throws IOException {
		// Print the names and IDs for up to 10 files.
		FileList result = service.files().list()
				.setPageSize(10)
				.setFields("nextPageToken, files(id, name)")
				.execute();
		List<File> files = result.getFiles();
		if (files == null || files.isEmpty()) {
			System.out.println("No files found.");
		} else {
			System.out.println("Files:");
			for (File file : files) {
				System.out.printf("%s (%s)\n", file.getName(), file.getId());
			}
		}
	}
}
