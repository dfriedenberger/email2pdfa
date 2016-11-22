package de.frittenburger.mail2pdfa;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;

import javax.mail.MessagingException;

import de.frittenburger.mail2pdfa.bo.EmailServiceAccountData;
import de.frittenburger.mail2pdfa.impl.ContentConverterImpl;
import de.frittenburger.mail2pdfa.impl.EmailServiceImpl;
import de.frittenburger.mail2pdfa.impl.MimeMessageParserImpl;
import de.frittenburger.mail2pdfa.impl.PDFACreatorImpl;
import de.frittenburger.mail2pdfa.interfaces.ContentConverter;
import de.frittenburger.mail2pdfa.interfaces.EmailService;
import de.frittenburger.mail2pdfa.interfaces.MimeMessageParser;
import de.frittenburger.mail2pdfa.interfaces.PDFACreator;

public class Converter {

	public static void main(String[] args) throws Exception {

		// Create SandBox
		/*
		 * sandbox/in/*.eml <= Email - messageId => From_Date_Clock
		 * sandbox/messageid/part_001/ (html + images)
		 * sandbox/messageid/pdfa/part_001.jpg (Screenshot)
		 * sandbox/messageid/pdfa/part_001.txt (Html - Content (gestripped))
		 * sandbox/messageid/pdfa/part_002.txt (Text - Content)
		 * sandbox/messageid/pdfa/filename.ext => (Attachment zum Includieren)
		 * sandbox/messageid/filename.ext => (Attachment)
		 * 
		 * archiv/email/from/messageid_filename.ext => (Attachment)
		 * archiv/email/from/messageid.pdf (Pdf/3A-Document)
		 * 
		 */
		// Download EmailList

		//getMessages("sandbox/in");

		//parseMessages("sandbox/in", "sandbox/messages");

		//convertMessages("sandbox/messages");
		
		createPdfs("sandbox/messages","archiv/email/");
		
		

		// => Header
		// => Multipart/related
		// => Alternative txt/html
		// => Attachments

		// Convert HTML 2 Image

		// Generate Id

		// Create PDF/3A-Document (with HtmlView / TextView and QRCode)

	}

	

	private static void getMessages(String sandbox_in_path) {
		EmailServiceAccountData emailServiceAccountData = new EmailServiceAccountData();

		emailServiceAccountData.provider = "pop3";
		emailServiceAccountData.mailserver = "mailserver";
		emailServiceAccountData.username = "username";
		emailServiceAccountData.password = "password";

		EmailService service = new EmailServiceImpl();
		for(int i = 0;i < 100;i++)
		    service.getMessages(emailServiceAccountData, sandbox_in_path);

	}

	private static void parseMessages(String sandbox_in_path, String sandbox_messages_path)
			throws IOException, MessagingException, GeneralSecurityException, ParseException {
		MimeMessageParser mimeMessageParser = new MimeMessageParserImpl();
		for (File f : new File(sandbox_in_path).listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				return name.endsWith(".eml");
			}
		})) {
			mimeMessageParser.parse(f.getPath(), sandbox_messages_path);
		}
	}

	private static void convertMessages(String sandbox_messages_path) {
		ContentConverter contentConverter = new ContentConverterImpl();

		for (File folder : new File(sandbox_messages_path).listFiles(new FileFilter() {

			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		})) {
			contentConverter.convert(folder.getPath());
		}

	}

	
	private static void createPdfs(String sandbox_messages_path, String archiv_path) throws IOException {
		PDFACreator pdfCreator = new PDFACreatorImpl();
		for (File folder : new File(sandbox_messages_path).listFiles(new FileFilter() {

			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		})) {
			System.out.println("Convert " + folder.getPath());
			pdfCreator.convert(folder.getPath(), archiv_path);
		}
		
	}
}
