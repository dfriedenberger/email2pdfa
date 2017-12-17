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
	public void testErrorEmailHeaderInvalidDate() throws IOException, MessagingException, GeneralSecurityException, ParseException {
		NameService service = new NameServiceImpl();
		
		MimeMessage message = readMime(resources + "/ErrorHeaderInvalidDate.eml");
		try
		{
			service.getEmailHeader(message);
			fail("has invalid date");
		} 
		catch(MessagingException e)
		{
			assertEquals("Invalid Date-Header",e.getMessage());
		}
	}
	
	@Test
	public void testErrorEmailHeaderInvalidSubject() throws IOException, MessagingException, GeneralSecurityException, ParseException {
		NameService service = new NameServiceImpl();
		
		MimeMessage message = readMime(resources + "/ErrorHeaderInvalidSubject.eml");
		try
		{
			service.getEmailHeader(message);
			fail("has invalid from");
		} 
		catch(MessagingException e)
		{
			assertEquals("Invalid From-Header",e.getMessage());
		}
	}
	

	@Test
	public void testErrorEmptyAdress() throws IOException, MessagingException, GeneralSecurityException, ParseException {
		NameServiceImpl service = new NameServiceImpl();
		
		
		
		try
		{
			service.parseFromAddress("");
			service.parseFromName("");
			fail("has invalid from");
		} 
		catch(MessagingException e)
		{
			assertEquals("Empty Address",e.getMessage());
		}
	
	
	}
	
	@Test
	public void testSpecialAddress() throws IOException, MessagingException, GeneralSecurityException, ParseException {
		NameServiceImpl service = new NameServiceImpl();
		
		assertEquals("support@psw.net",service.parseFromAddress("=?UTF-8?Q?=20PSW=20GROUP=20GmbH=20&=20Co.=20KG?= <support@psw.net>"));
		assertEquals(" PSW GROUP GmbH & Co. KG",service.parseFromName("=?UTF-8?Q?=20PSW=20GROUP=20GmbH=20&=20Co.=20KG?= <support@psw.net>"));

	
	}
	
	@Test
	public void testSpecialDate() throws IOException, MessagingException, GeneralSecurityException, ParseException {
		NameServiceImpl service = new NameServiceImpl();
		
		assertEquals("20160110_1419",service.parseDate("Sun, 10 Jan 2016 14:19:04 +0100"));
		assertEquals("20160228_1317",service.parseDate("Sun Feb 28 13:17:24 GMT+01:00 2016"));
	
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
