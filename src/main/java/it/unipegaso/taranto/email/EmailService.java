package it.unipegaso.taranto.email;

import it.unipegaso.taranto.database.DatabaseService;
import it.unipegaso.taranto.parser.RouteFinder;
import it.unipegaso.taranto.util.Logger;
import org.apache.commons.io.FileUtils;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Anthill
 *
 * @author Marco Montanari
 * All rights reserved.
 */
public class EmailService {
	private static final Logger LOGGER = Logger.get("Email");
	
	private String host;
	private String user;
	private String password;
	
	private Store store;
	private Folder folder;
	
	public EmailService(String host, String user, String password) {
		this.host = host;
		this.user = user;
		this.password = password;
		/*Properties properties = new Properties();
		properties.put("mail.pop3.host", host);
		properties.put("mail.pop3.port", "995");
		properties.put("mail.pop3.starttls.enable", "true");
		Session emailSession = Session.getDefaultInstance(properties);
		try {
			store = emailSession.getStore("pop3s");
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		}*/
		Properties props = new Properties();
		props.setProperty("mail.imap.ssl.enable", "true");
		Session session = Session.getInstance(props);
		try {
			store = session.getStore("imap");
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
	public void connect() {
		try {
			store.connect(host, user, password);
			folder = store.getFolder("Bonifici");
			folder.open(Folder.READ_ONLY); //TODO change
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
	public int parseMessages(DatabaseService db) {
		int parsed = 0;
		try {
			Message[] messages = folder.getMessages(folder.getMessageCount() - 20, folder.getMessageCount());
			for (Message m : messages) {
				int p = 0;
				if (m.getContent() instanceof Multipart) {
					Multipart multiPart = (Multipart) m.getContent();
					for (int i = 0; i < multiPart.getCount(); i++) {
						MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(i);
						if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
							int lastIndex = part.getFileName().lastIndexOf("?");
							File attachment = new File(db.temp, lastIndex == -1 ? part.getFileName() : part.getFileName().substring(0, lastIndex));
							LOGGER.info("Downloading '%s'...", attachment.getName());
							part.saveFile(attachment); //Slow
							LOGGER.info("Found '%s'.", attachment.toString());
							if (RouteFinder.findAndUpload(db, attachment, attachment)) {
								p++;
							}
							LOGGER.info("Uploaded.");
						}
					}
					if (p == 0) {
						if (multiPart.getBodyPart(0).getContent() instanceof String) {
							if (parseContent(db, ((MimeMultipart) m.getContent()).getBodyPart(0).getContent().toString(), m.getFrom()[0].toString())) {
								p++;
							}
						}
					}
				} else {
					if (parseContent(db, m.getContent().toString(), m.getFrom()[0].toString())) {
						p++;
					}
				}
				parsed += p;
			}
		} catch (MessagingException | IOException e) {
			e.printStackTrace();
		}
		return parsed;
	}
	
	private boolean parseContent(DatabaseService db, String content, String subject) throws IOException {
		LOGGER.info("No attachments found, parsing the message itself...");
		File file = new File(db.temp, subject + ".txt");
		FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
		boolean r = RouteFinder.findAndUpload(db, content, file);
		LOGGER.info("Uploaded.");
		return r;
	}
	
	public void disconnect() {
		try {
			folder.close(false);
			store.close();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
}
