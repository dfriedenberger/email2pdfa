package de.frittenburger.email2pdfa;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.bouncycastle.asn1.x500.style.BCStyle;
import org.junit.Test;

import de.frittenburger.email2pdfa.bo.SignatureInfo;
import de.frittenburger.email2pdfa.impl.SignatureEngineImpl;
import de.frittenburger.email2pdfa.interfaces.SignatureEngine;

public class TestSignatureEngine {

	private static String resources = "src/test/resources/in";
	
	//private static String out = SandboxTestImpl.getTestPath(TestSignatureEngine.class);
	
	@Test
	public void test001() throws Exception {
		
		
	   SignatureEngine engine = new SignatureEngineImpl();
	   MimeMessage mime = readMime(resources + "/SignedEmail.eml");
	   
	   SignatureInfo s = engine.check(mime);
	   assertTrue(s.hasSignature);
	   assertEquals("dirk@friedenberger.net",s.subject.get(BCStyle.E.getId()));
	   assertEquals("C=DE,O=Dirk Friedenberger,OU=www.frittenburger.de,CN=Dirk Friedenberger,E=dirk@friedenberger.net",s.subject.get("string"));

	}

	@Test
	public void test002() throws Exception {
		
		
	   SignatureEngine engine = new SignatureEngineImpl();
	   MimeMessage mime = readMime(resources + "/SimpleHtml.eml");
	   
	   SignatureInfo s = engine.check(mime);
	   assertFalse(s.hasSignature);
	   assertEquals(0,s.subject.size());
	}
	
	@Test(expected=IOException.class)
	public void test003() throws Exception {
		
		
	   SignatureEngine engine = new SignatureEngineImpl();
	   MimeMessage mime = readMime(resources + "/WrongSignedEmail.eml");
	   
	   SignatureInfo s = engine.check(mime);
	   assertTrue(s.hasSignature);
	   assertEquals(0,s.subject.size());

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
