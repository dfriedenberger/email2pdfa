package de.frittenburger.mail2pdfa.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import de.frittenburger.mail2pdfa.bo.EmailHeader;
import de.frittenburger.mail2pdfa.bo.MessageContext;
import de.frittenburger.mail2pdfa.interfaces.MimeMessageParser;
import de.frittenburger.mail2pdfa.interfaces.NameService;

public class MimeMessageParserImpl implements MimeMessageParser {

	private NameService nameService = new NameServiceImpl();

	public void parse(String emlFile, String path) throws IOException, MessagingException, GeneralSecurityException, ParseException {

		
		InputStream is = new FileInputStream(emlFile);
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		MimeMessage mime = new MimeMessage(session, is);

		EmailHeader header = nameService.getEmailHeader(mime);


		MessageContext messageContext = new MessageContext();

		messageContext.mesgPath = path + "/" +header.mesgkey;

		new File(messageContext.mesgPath).mkdir();

		ObjectMapper mapper = new ObjectMapper();
		mapper.writerWithDefaultPrettyPrinter().writeValue(new File(messageContext.mesgPath+"/emailheader.json"), header);
		
		
		
		// parseContent
		messageContext.encoding = mime.getEncoding();
		messageContext.contentFilename = null;
		try
		{
			parseContent(messageContext, mime.getContent(), mime.getContentType(), mime.getDisposition());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			//Todo if Unknown Coding, parse 
		}
		is.close();
		
		
		
		

	}




	private void parseContent(MessageContext mesgContext, Object msgContent, String contentType, String disposition)
			throws IOException, MessagingException {

		ContentType ct = new ContentType(contentType);
		System.out.println("disposition " + disposition);
		System.out.println("Part " + msgContent.getClass() + " type=" + ct.getBaseType());

		if (msgContent instanceof Multipart) {
			Multipart multipart = (Multipart) msgContent;
			// System.out.println("MultiPartContentType
			// "+multipart.getContentType());
			
			//Pfad anlegen
			String saveMesgPath = mesgContext.mesgPath;
			mesgContext.mesgPath = nameService.getFolder(mesgContext,ct);
			new File(mesgContext.mesgPath).mkdir();
			
			for (int j = 0; j < multipart.getCount(); j++) {

				BodyPart bodyPart = multipart.getBodyPart(j);

				mesgContext.contentFilename = null;

				// has Filename?
				String filename = bodyPart.getFileName();
				if (filename != null) {
					mesgContext.contentFilename = filename;
				}

				String[] contentID = bodyPart.getHeader("Content-ID");
				if (contentID != null) {
					mesgContext.contentFilename = nameService.parseContentId(contentID[0].trim());
				}
				parseContent(mesgContext, bodyPart.getContent(), bodyPart.getContentType(), bodyPart.getDisposition());
			}
			//Ruecksetzen
			mesgContext.mesgPath = saveMesgPath;
			
			
		} else {

			if (msgContent instanceof String) {
				String str = (String) msgContent;
				save(mesgContext, new ByteArrayInputStream( str.getBytes()), ct);

			} else if (msgContent instanceof InputStream) {
				// BASE64DecoderStream
				// QPDecoderStream
				// SharedByteArrayInputStream
				InputStream is = (InputStream) msgContent;
				save(mesgContext, is, ct);
			} else if (msgContent instanceof MimeMessage) {
				MimeMessage mimeMessage = (MimeMessage) msgContent;
				save(mesgContext, mimeMessage.getRawInputStream(), new ContentType("message/rfc822"));
			} else {
				throw new RuntimeException("Not implemented " + msgContent);
			}
		}

	}

	private void save(MessageContext mesgContext, InputStream in, ContentType contentType) throws IOException {

		if(mesgContext.contentFilename == null)
			mesgContext.contentFilename = nameService.getFilename(mesgContext, contentType);
			
	
		System.out.println("fileName " + mesgContext.contentFilename);

		FileOutputStream out = new FileOutputStream(mesgContext.mesgPath+"/"+mesgContext.contentFilename);
		byte[] buffer = new byte[1024];
		int len = in.read(buffer);
		while (len != -1) {
		    out.write(buffer, 0, len);
		    len = in.read(buffer);
		}
		
		out.close();
		
		
		
	}

}
