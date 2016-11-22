package de.frittenburger.mail2pdfa;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMessage;

import org.junit.Test;

import de.frittenburger.mail2pdfa.impl.MimeMessageParserImpl;
import de.frittenburger.mail2pdfa.interfaces.MimeMessageParser;

public class TestReadMimeMessage {

	String mailname = null;
	int a = 0;
	
	@Test
	public void test() throws IOException, MessagingException, GeneralSecurityException, ParseException {
		
		
		MimeMessageParser mimeMessageParser = new MimeMessageParserImpl();
		for(File f : new File("src/test/resources/mail").listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				return name.endsWith(".eml");
			}}))
		{
			mimeMessageParser.parse(f.getPath(),"temp/messages");				
		}
		
	}

}
