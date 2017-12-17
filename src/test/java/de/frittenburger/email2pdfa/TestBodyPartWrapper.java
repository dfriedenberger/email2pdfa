package de.frittenburger.email2pdfa;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.junit.Test;

import de.frittenburger.email2pdfa.impl.BodyPartWrapperImpl;
import de.frittenburger.email2pdfa.interfaces.BodyPartWrapper;

public class TestBodyPartWrapper {

	private static String resources = "src/test/resources/in";

	@Test
	public void test01() throws MessagingException, IOException {
		
		BodyPart bodypart = readBodyPart(resources + "/Bodyparts.eml",0);
			
		BodyPartWrapper wrapper = new BodyPartWrapperImpl(bodypart);
		
		assertEquals("Herbstfeuerfest_2016.html",wrapper.getFilename());
		assertEquals(null,wrapper.getContentId());
		assertEquals("attachment",wrapper.getDisposition());
		assertEquals("text/html",wrapper.getContentType().getBaseType());
		assertEquals("UTF-8",wrapper.getContentType().getParameter("charset"));

	}

	
	@Test
	public void test02() throws MessagingException, IOException {
		
		BodyPart bodypart = readBodyPart(resources + "/Bodyparts.eml",1);
			
		BodyPartWrapper wrapper = new BodyPartWrapperImpl(bodypart);
		
		assertEquals("smime.p7s",wrapper.getFilename());
		assertEquals(null,wrapper.getContentId());
		assertEquals("attachment",wrapper.getDisposition());
		assertEquals("application/pkcs7-signature",wrapper.getContentType().getBaseType());
		assertEquals("smime.p7s",wrapper.getContentType().getParameter("name"));

	}
	
	@Test
	public void test03() throws MessagingException, IOException {
		
		BodyPart bodypart = readBodyPart(resources + "/Bodyparts.eml",2);
			
		BodyPartWrapper wrapper = new BodyPartWrapperImpl(bodypart);
		
		assertEquals(null,wrapper.getFilename());
		assertEquals(null,wrapper.getContentId());
		assertEquals(null,wrapper.getDisposition());
		assertEquals("text/plain",wrapper.getContentType().getBaseType());
		assertEquals("utf-8",wrapper.getContentType().getParameter("charset"));
		assertEquals("flowed",wrapper.getContentType().getParameter("format"));

	}
	
	@Test
	public void test04() throws MessagingException, IOException {
		
		BodyPart bodypart = readBodyPart(resources + "/Bodyparts.eml",3);
			
		BodyPartWrapper wrapper = new BodyPartWrapperImpl(bodypart);
		
		assertEquals("robot1.png",wrapper.getFilename());
		assertEquals(null,wrapper.getContentId());
		assertEquals("attachment",wrapper.getDisposition());
		assertEquals("image/png",wrapper.getContentType().getBaseType());
		assertEquals("robot1.png",wrapper.getContentType().getParameter("name"));

	}
	
	@Test
	public void test05() throws MessagingException, IOException {
		
		BodyPart bodypart = readBodyPart(resources + "/Bodyparts.eml",4);
			
		BodyPartWrapper wrapper = new BodyPartWrapperImpl(bodypart);
		
		assertEquals("Aus_Kibu__Trabczyn_Adelbert_Fri_und_Christine_Kochanke.pdf",wrapper.getFilename());
		assertEquals(null,wrapper.getContentId());
		assertEquals("attachment",wrapper.getDisposition());
		assertEquals("application/pdf",wrapper.getContentType().getBaseType());
		assertEquals("Aus Kibu Trabczyn Adelbert Fri und Christine Kochanke.pdf",wrapper.getContentType().getParameter("name"));

	}
	
	
	@Test
	public void test06() throws MessagingException, IOException {
		
		BodyPart bodypart = readBodyPart(resources + "/Bodyparts.eml",5);
			
		BodyPartWrapper wrapper = new BodyPartWrapperImpl(bodypart);
		
		assertEquals("Nachrichtenteil_als_Anhang.html",wrapper.getFilename());
		assertEquals(null,wrapper.getContentId());
		assertEquals("attachment",wrapper.getDisposition());
		assertEquals("text/html",wrapper.getContentType().getBaseType());
		assertEquals("US-ASCII",wrapper.getContentType().getParameter("charset"));
		assertEquals("Nachrichtenteil als Anhang",wrapper.getContentType().getParameter("name"));

	}
	
	@Test
	public void test07() throws MessagingException, IOException {
		
		BodyPart bodypart = readBodyPart(resources + "/Bodyparts.eml",6);
			
		
		String disposition = bodypart.getDisposition();
		System.out.println(disposition);
		BodyPartWrapper wrapper = new BodyPartWrapperImpl(bodypart);
		
		assertEquals("selfie.jpg",wrapper.getFilename());
		assertEquals("selfie",wrapper.getContentId());
		assertEquals("inline",wrapper.getDisposition());
		assertEquals("image/jpeg",wrapper.getContentType().getBaseType());
		
	}
	
	
	private BodyPart readBodyPart(String emlFile,int ix) throws IOException, MessagingException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		InputStream is = null;
		MimeMessage mime = null;
		try {
			is = new FileInputStream(emlFile);
			mime = new MimeMessage(session, is);
			
			Multipart multipart = (Multipart) mime.getContent();
			return multipart.getBodyPart(ix);		
		} finally {
			if (is != null)
				is.close();
			is = null;
		}
	}
}
