package de.frittenburger.email2pdfa;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMessage;

import org.junit.Test;

import de.frittenburger.email2pdfa.bo.EmailHeader;
import de.frittenburger.email2pdfa.impl.NameServiceImpl;
import de.frittenburger.email2pdfa.interfaces.NameService;

public class TestNameService {

	private static String resources = "src/test/resources/in";
	
	@Test
	public void testGetEmailHeader() throws IOException, MessagingException, GeneralSecurityException, ParseException {
		NameService service = new NameServiceImpl();
		
		MimeMessage message = readMime(resources + "/SimpleHtml.eml");
		EmailHeader header = service.getEmailHeader(message);
		
		assertEquals("sender@example.org", header.senderkey);
	}

	@Test
	public void testParseValidFilename001() throws IOException, MessagingException, GeneralSecurityException, ParseException {
		NameService service = new NameServiceImpl();
		String valid_pattern = "abcABC123+=#.txt";
		String clean = service.parseValidFilename(valid_pattern,new ContentType("text/plain"));
		assertEquals(valid_pattern, clean);
	}

	@Test
	public void testParseValidFilename002() throws IOException, MessagingException, GeneralSecurityException, ParseException {
		NameService service = new NameServiceImpl();
		String clean = service.parseValidFilename("path/path\\filename.txt",new ContentType("text/plain"));
		assertEquals("path_path_filename.txt", clean);
	}
	
	@Test
	public void testParseValidFilename003() throws IOException, MessagingException, GeneralSecurityException, ParseException {
		NameService service = new NameServiceImpl();
		String clean = service.parseValidFilename("allowed=~+[]. notallowed=\"*:%?\\/&<>';",
				new ContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
		assertEquals("allowed=~+[]._notallowed=____________.docx", clean);
	}
	
	
	private MimeMessage readMime(String emlFile) throws IOException, MessagingException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		InputStream is = null;
		MimeMessage mime = null;
		try {
			is = new FileInputStream(emlFile);
			mime = new MimeMessage(session, is);
			return mime;
		} finally {
			if (is != null)
				is.close();
			is = null;
		}
	}
	
	
	
	
}
